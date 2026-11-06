#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

"""
Orchestratore (Agent PydanticAI): sostituisce la catena fissa ``rag_router -> retriever ->
llm_response`` di agentic-rag-module.

- delega = tool ``discover`` che fa ``await run_discovery(..., usage=ctx.usage)`` (agent
  delegation, conteggio cumulativo delle richieste);
- tetto alle richieste del turno = ``UsageLimits`` da ``max_iterations`` (RAGConfiguration);
- tetto alle deleghe del turno = :data:`MAX_DISCOVERIES_PER_TURN`;
- ``parallel_discovery``: anche il tool ``discover_many``, che delega piu' sotto-domande in
  parallelo, al massimo ``max_parallel_subtasks``;
- ``bypass_rag``: l'orchestratore nasce **senza** il tool ``discover``, quindi il retrieval e'
  irraggiungibile per costruzione. Vedi :func:`build_orchestrator`;
- ``enable_verifier``: un ``output_validator`` fa giudicare la risposta al Verifier e, se la
  boccia, rimanda all'orchestratore le affermazioni da cercare o da togliere (self-correction).
"""

from contextlib import asynccontextmanager
from typing import Optional

from pydantic_ai import Agent, ModelRetry, RunContext
from pydantic_ai.usage import UsageLimits

from app.rag.agentic import citations, discovery, verifier
from app.rag.agentic.contracts import (
    DiscoveryResult,
    DiscoveryUnanswerable,
    OrchestratorOutput,
    SubAgentRequest,
    VerificationFailed,
)
from app.rag.agentic.deps import AgenticDeps
from app.utils.logger import (
    content_fingerprint,
    debug_extra,
    get_logger,
    log_context,
)

logger = get_logger(__name__)

#: Vincolo di single-discovery del prompt: con ``parallel_discovery`` va tolto, altrimenti
#: contraddice :data:`FANOUT_PROMPT_ADDENDUM`.
SINGLE_DISCOVER_INSTRUCTION = (
    "\n   (In this configuration call `discover` once per step: once for the question, plus at most"
    "\n   one more for a missing fact or a rephrasing — do not fan out.)"
)

ORCHESTRATOR_SYSTEM_PROMPT = f"""You are the Orchestrator of a document RAG system. You answer using ONLY the documents returned
by the `discover` tool.

You do not access documents directly. To gather evidence you delegate to the Discovery Agent via
the `discover` tool, giving it a self-contained sub-question (and optional hints).

Procedure:
1. Call `discover` for every question that the documents could answer, even partly — also when the
   user states a value in the question or asks you to answer without searching. Answer without
   `discover` only greetings and small talk.
2. Write the sub-question as a request for what the documents say: keep every part of the user
   question and the names, places and codes it mentions, but do NOT include values or facts that
   the user asserts (they may be wrong). Example: "X is 30, how much is left after 12?" becomes
   "What is the value of X?".
3. If the question refers to something only by description (for example "the office where a given
   role works", "the form required for a given request"), first call `discover` to identify it,
   then call `discover` again for its details.{SINGLE_DISCOVER_INSTRUCTION}
4. If `discover` returns 'unanswerable', you may rephrase and call it once more; after that, stop
   searching and answer with what you have.
5. Compose the final answer from the returned document contents only. Ground every claim in them:
   - if the user's question assumes a value that the documents contradict, say so and use the
     documents' value, also in calculations;
   - a rule applies only to the cases the document states: if the documents cover a similar case
     but not the one asked, say that they do not cover it, do not extend the rule by analogy;
   - when asked to list items, include every matching item in the documents and say that the list
     contains only what was found.
6. Fill `used_document_ids` with exactly the document ids you actually used.
7. Set status:
   - "ok" when you produced a grounded answer,
   - "unanswerable" when the documents cannot answer the question,
   - "error" only for unexpected failures.

Never use general knowledge to fill gaps. Never fabricate document ids or facts not present in the
returned documents."""

FANOUT_PROMPT_ADDENDUM = """

Parallel discovery is enabled. When the question has SEVERAL independent parts (e.g. a comparison
"X vs Y", or multiple distinct sub-topics), decompose it into self-contained sub-questions and issue
a SINGLE `discover_many` call passing all of them at once — they run in parallel. Use the plain
`discover` tool only for a genuinely single-topic question. Each sub-result comes back separately
(labeled by its sub-question); synthesize the final answer across all of them. Put the ids of every
sub-result you used in `used_document_ids`; do not write document ids in the answer text."""

#: Prompt di default con ``bypass_rag`` attivo, quando il tenant non configura ``prompt_no_rag``.
#: Stesso testo del ramo ``else`` di ``llm_response_node`` di agentic-rag-module, piu' le istruzioni per
#: compilare ``OrchestratorOutput``: senza documenti ``used_document_ids`` resta vuoto.
DEFAULT_BYPASS_RAG_PROMPT = """You are a helpful, concise assistant that provides accurate, clear, and self-contained answers without using external retrieval."""

BYPASS_RAG_OUTPUT_ADDENDUM = """

Answer from your own knowledge: you have no document tools in this configuration.
Set `status` to "ok" and leave `used_document_ids` empty — there are no documents to cite."""


#: Tetto di ``max_parallel_subtasks``: oltre questo numero di Discovery in parallelo il tenant viene
#: riportato al limite (ogni Discovery costa le sue ricerche piu' una selezione).
MAX_PARALLEL_SUBTASKS_LIMIT = 4

#: Sotto-domande delegate al Discovery per turno, ``discover`` e ``discover_many`` insieme: senza
#: ``max_iterations`` non c'e' ``UsageLimits``, e ogni Discovery costa fino a
#: ``discovery.MAX_RETRIEVER_CALLS`` ricerche piu' una selezione. Sei coprono un fan-out pieno
#: (:data:`MAX_PARALLEL_SUBTASKS_LIMIT`) piu' due deleghe di riparazione.
MAX_DISCOVERIES_PER_TURN = 6

#: Risposta del tool di delega a budget esaurito: l'orchestratore chiude con cio' che ha.
DISCOVERY_LIMIT_REACHED = (
    "Discovery limit reached for this turn: no more searches. Compose the final answer from the "
    "documents already returned, and say what could not be found."
)


def verifier_enabled(rag_configuration: dict) -> bool:
    """Whether the Verifier judges the answer: only with retrieval, since without documents
    it would reject every answer."""
    return bool(rag_configuration.get("enable_verifier")) and not rag_configuration.get(
        "bypass_rag"
    )


def resolve_max_parallel_subtasks(rag_configuration: dict) -> int:
    """``max_parallel_subtasks`` del jsonConfig (default 3) limitato a ``1..4``. Un valore non
    numerico usa il default: e' configurazione scritta a mano, non deve far fallire il turno."""
    configured = rag_configuration.get("max_parallel_subtasks")
    if configured is None:
        return 3
    try:
        configured = int(configured)
    except (TypeError, ValueError):
        logger.warning(f"[orchestrator] max_parallel_subtasks={configured!r} non numerico: uso 3")
        return 3
    resolved = min(max(configured, 1), MAX_PARALLEL_SUBTASKS_LIMIT)
    if resolved != configured:
        logger.warning(
            f"[orchestrator] max_parallel_subtasks={configured} fuori da "
            f"1..{MAX_PARALLEL_SUBTASKS_LIMIT}: uso {resolved}"
        )
    return resolved


def build_usage_limits(rag_configuration: dict) -> Optional[UsageLimits]:
    """``max_iterations`` (jsonConfig) -> ``UsageLimits``, un tetto alle richieste LLM del turno.

    Il tetto deve coprire, per ogni tentativo (1 + retry di self-correction), la delega al
    Discovery (ricerche e selezione, che condividono ``ctx.usage`` con l'orchestratore), le due
    richieste dell'orchestratore (delegare e comporre) e, se attivo, il Verifier. Ogni tentativo
    puo' delegare di nuovo: la riparazione dopo un rifiuto del Verifier cerca le affermazioni non
    supportate. Troppo stretto farebbe scattare ``UsageLimitExceeded`` gia' sul percorso felice;
    resta comunque un tetto anti-runaway. Con ``parallel_discovery`` un solo giro puo' delegare
    fino a ``max_parallel_subtasks`` Discovery, quindi il tetto cresce con il fan-out.
    """
    max_iterations = rag_configuration.get("max_iterations")
    if not max_iterations:
        return None
    attempts = int(max_iterations) + 1  # tentativo iniziale + retry
    per_discovery = discovery.MAX_RETRIEVER_CALLS + 1  # ricerche + 1 selezione id
    if rag_configuration.get("parallel_discovery"):
        per_discovery *= resolve_max_parallel_subtasks(rag_configuration)
    per_orchestrator = 2  # 1 richiesta per delegare + 1 per comporre
    per_verifier = 1 if verifier_enabled(rag_configuration) else 0
    request_limit = attempts * (per_discovery + per_orchestrator + per_verifier) + 2  # + margine
    return UsageLimits(request_limit=request_limit)


def _serialize_discovery(outcome, deps: AgenticDeps) -> dict:
    """Esito Discovery → dict per l'orchestratore (id + contenuto, niente score; design §4). Ogni
    documento porta il suo numero di fonte, assegnato qui, alla consegna: con ``discover_many`` gli
    esiti si serializzano nell'ordine delle sotto-domande, quindi anche i numeri."""
    if isinstance(outcome, DiscoveryUnanswerable):
        return {"status": "unanswerable", "reason": outcome.reason, "documents": []}
    assert isinstance(outcome, DiscoveryResult)
    return {
        "status": "ok",
        "documents": [
            {
                "source": citations.source_number(deps, d.document_id),
                "document_id": d.document_id,
                "content": d.content,
                "title": d.title,
                "domain": d.domain,
            }
            for d in outcome.documents
        ],
    }


def _build_system_prompt(
    *,
    bypass_rag: bool,
    prompt_no_rag: Optional[str],
    parallel_discovery: bool = False,
    retrieve_citations: bool = False,
) -> str:
    """System prompt dell'orchestratore. Con ``bypass_rag`` non ci sono tool, quindi il prompt di
    default (che istruisce a chiamare ``discover``) non va usato."""
    if bypass_rag:
        return (prompt_no_rag or DEFAULT_BYPASS_RAG_PROMPT) + BYPASS_RAG_OUTPUT_ADDENDUM
    prompt = ORCHESTRATOR_SYSTEM_PROMPT
    if parallel_discovery:
        prompt = prompt.replace(SINGLE_DISCOVER_INSTRUCTION, "") + FANOUT_PROMPT_ADDENDUM
    return prompt + citations.instructions(retrieve_citations)


def repair_instructions(verdict: VerificationFailed) -> str:
    """Rifiuto del Verifier → messaggio di riprova per l'orchestratore: cosa fare di ogni
    affermazione non supportata, invece del solo motivo (con il quale riscriveva la stessa
    risposta)."""
    lines = [f"The verifier rejected your answer: {verdict.reason}"]
    if verdict.unsupported_claims:
        lines.append("Unsupported claims and how to repair them:")
        for item in verdict.unsupported_claims:
            if item.action == "search":
                lines.append(
                    f'- "{item.claim}": call `discover` with a sub-question targeted at this '
                    "claim; keep it only if the new documents support it, otherwise remove it."
                )
            else:
                lines.append(f'- "{item.claim}": remove it from the answer.')
    lines.append(
        "Do not repeat an unsupported claim without new supporting documents. "
        "Then return the corrected answer."
    )
    return "\n".join(lines)


#: Temperatura 0: la scelta di cosa delegare deve essere stabile fra richieste uguali.
ORCHESTRATOR_SETTINGS = {"temperature": 0}

#: Con il fan-out il parallelismo passa solo da ``discover_many``, dove vale il tetto
#: ``max_parallel_subtasks``: piu' ``discover`` nella stessa risposta del modello lo aggirerebbero.
FANOUT_ORCHESTRATOR_SETTINGS = {**ORCHESTRATOR_SETTINGS, "parallel_tool_calls": False}

#: Nota per l'orchestratore quando ``discover_many`` scarta delle sotto-domande oltre
#: ``max_parallel_subtasks`` e il turno ha ancora deleghe: il limite e' sui Discovery contemporanei,
#: quindi le scartate vanno cercate con la chiamata successiva. Chiedere di dirle "non coperte"
#: faceva rispondere al modello che i documenti non le contengono.
DROPPED_SUBTASKS_NOTE = (
    "These sub-questions were NOT searched yet: only a limited number of searches run at once. "
    "Search them with another call before composing the answer. Do not answer them from your own "
    "knowledge."
)

#: Nota quando le sotto-domande scartate non possono piu' essere cercate: il tetto di deleghe del
#: turno e' esaurito. La risposta deve distinguere "non cercato" da "non trovato nei documenti".
SUBTASKS_OVER_BUDGET_NOTE = (
    "These sub-questions were NOT searched: the search limit of this turn is reached. In the "
    "answer, say that these parts were not searched; do not say that the documents lack them, "
    "and do not answer them from your own knowledge."
)


def build_orchestrator(
    model,
    *,
    enable_verifier: bool = False,
    verifier_model=None,
    retries: int = 1,
    parallel_discovery: bool = False,
    max_parallel_subtasks: int = 3,
    bypass_rag: bool = False,
    prompt_no_rag: Optional[str] = None,
    retrieve_citations: bool = False,
) -> Agent:
    """Crea l'Agent orchestratore con il tool di delega ``discover``.

    Con ``enable_verifier`` registra un ``output_validator`` che esegue il Verifier e, su
    ``VerificationFailed``, alza ``ModelRetry`` con le istruzioni di riparazione: un nuovo giro
    dell'orchestratore. ``retries`` limita i tentativi, oltre a ``UsageLimits``.

    Con ``parallel_discovery`` registra anche ``discover_many``, che delega fino a
    ``max_parallel_subtasks`` sotto-domande in parallelo, e aggiunge al prompt le istruzioni di
    decomposizione.

    Con ``bypass_rag`` NESSUN tool di delega viene registrato: il retrieval e' irraggiungibile per
    costruzione (piu' forte di agentic-rag-module, dove ``rag_router_node`` forzava
    ``use_rag=False`` ma la catena restava capace di recuperare). Il system prompt diventa
    ``prompt_no_rag`` del tenant, o :data:`DEFAULT_BYPASS_RAG_PROMPT`, perche' quello di default
    istruirebbe il modello a chiamare un tool inesistente. Il Verifier viene disattivato: giudica
    il grounding sui documenti consegnati, che senza retrieval non ci sono, quindi boccerebbe
    sempre, con retry fino al tetto.
    """
    agent = Agent(
        model,
        output_type=OrchestratorOutput,
        deps_type=AgenticDeps,
        system_prompt=_build_system_prompt(
            bypass_rag=bypass_rag,
            prompt_no_rag=prompt_no_rag,
            parallel_discovery=parallel_discovery,
            retrieve_citations=retrieve_citations,
        ),
        retries=retries,
        model_settings=(
            FANOUT_ORCHESTRATOR_SETTINGS if parallel_discovery else ORCHESTRATOR_SETTINGS
        ),
    )

    if bypass_rag:
        logger.debug("[orchestrator] bypass_rag=True -> nessun tool di delega registrato")
        return agent

    @agent.tool
    async def discover(ctx: RunContext[AgenticDeps], request: SubAgentRequest):
        """Delega una sotto-domanda al Discovery Agent (agent delegation)."""
        if ctx.deps.discoveries_run >= MAX_DISCOVERIES_PER_TURN:
            logger.info(f"[orchestrator] discover: budget di {MAX_DISCOVERIES_PER_TURN} esaurito")
            return DISCOVERY_LIMIT_REACHED
        ctx.deps.discoveries_run += 1
        ctx.deps.emit_status("discovering")
        _log_delegation(ctx.deps, request)
        outcome = await discovery.run_discovery(
            request=request,
            deps=ctx.deps,
            usage=ctx.usage,  # conteggio cumulativo condiviso
        )
        return _serialize_discovery(outcome, ctx.deps)

    if parallel_discovery:

        @agent.tool
        async def discover_many(ctx: RunContext[AgenticDeps], requests: list[SubAgentRequest]):
            """Delega piu' sotto-domande indipendenti in PARALLELO (fan-out). Ritorna gli esiti
            separati, uno per sotto-domanda."""
            budget = MAX_DISCOVERIES_PER_TURN - ctx.deps.discoveries_run
            if budget <= 0:
                logger.info(
                    f"[orchestrator] discover_many: budget di {MAX_DISCOVERIES_PER_TURN} esaurito"
                )
                return DISCOVERY_LIMIT_REACHED
            capped = requests[: min(max_parallel_subtasks, budget)]
            dropped = [request.question for request in requests[len(capped) :]]
            ctx.deps.discoveries_run += len(capped)
            ctx.deps.emit_status("discovering")
            for request in capped:
                _log_delegation(ctx.deps, request)
            if dropped:
                logger.info(
                    f"[orchestrator] discover_many: {len(dropped)} sotto-domande scartate "
                    f"(max_parallel_subtasks={max_parallel_subtasks}, budget={budget})"
                )
            outcomes = await discovery.run_parallel_discovery(
                requests=capped,
                deps=ctx.deps,
                usage=ctx.usage,  # conteggio cumulativo condiviso
                max_parallel=max_parallel_subtasks,
            )
            subtasks = [
                {**_serialize_discovery(outcome, ctx.deps), "subtask": request.question}
                for request, outcome in zip(capped, outcomes)
            ]
            if not dropped:
                return {"subtasks": subtasks}
            budget_left = budget - len(capped) > 0
            return {
                "subtasks": subtasks,
                "dropped_subtasks": dropped,
                "note": DROPPED_SUBTASKS_NOTE if budget_left else SUBTASKS_OVER_BUDGET_NOTE,
            }

    if enable_verifier:

        @agent.output_validator
        async def _verify(ctx: RunContext[AgenticDeps], output: OrchestratorOutput):
            return await _verified(ctx, output, verifier_model)

    return agent


async def _verified(ctx: RunContext[AgenticDeps], output: OrchestratorOutput, verifier_model):
    """Gate finale: Verifier → ModelRetry con le istruzioni di riparazione (self-correction).
    A riprove esaurite restituisce l'ultima risposta: l'esito resta nel log."""
    # non verificare gli esiti non-ok (unanswerable/error passano)
    if output.status != "ok":
        return output
    # la risposta e' gia' scritta: da qui in poi l'utente aspetta il giudizio
    ctx.deps.emit_status("evaluating")
    verdict = await verifier.verify(
        output=output, deps=ctx.deps, usage=ctx.usage, model=verifier_model
    )
    if not isinstance(verdict, VerificationFailed):
        return output
    if ctx.retry >= ctx.max_retries:
        logger.warning(
            f"[orchestrator] verifier: riprove esaurite ({ctx.max_retries}), "
            f"risposta non verificata restituita: {verdict.reason!r}"
        )
        return output
    raise ModelRetry(repair_instructions(verdict))


def _log_delegation(deps: AgenticDeps, request: SubAgentRequest) -> None:
    """Una delega al Discovery: cosa l'orchestratore ha chiesto decide cosa viene cercato, ma e'
    testo derivato dalla domanda, quindi sopra DEBUG resta un'impronta."""
    context = log_context(tenant_id=deps.tenant_id, user_id=deps.user_id, chat_id=deps.chat_id)
    logger.info(
        f"[orchestrator] discover {content_fingerprint('sub_question', request.question)} "
        f"{content_fingerprint('hints', request.hints)} "
        f"discoveries={deps.discoveries_run}/{MAX_DISCOVERIES_PER_TURN} {context}"
        + debug_extra(logger, sub_question=request.question, hints=request.hints)
    )


class OrchestratorStream:
    """Handle ceduto da :func:`stream_orchestrator`.

    ``deltas()`` cede i frammenti *nuovi* del campo ``message`` man mano che il provider li manda
    (partial validation di PydanticAI sul JSON incompleto della tool-call di output). A stream
    esaurito ``output``, ``refs`` e ``result`` sono popolati, come li restituirebbe
    :func:`run_orchestrator`.
    """

    def __init__(self, streamed_result, deps: AgenticDeps):
        self._streamed = streamed_result
        self._deps = deps
        self.output = None
        self.refs: list = []
        self.result = streamed_result

    async def deltas(self):
        """Frammenti incrementali di ``message``. Il diff è necessario perché la partial validation
        cede l'oggetto *intero* ricostruito a ogni giro, non il pezzo nuovo.

        ``debounce_by=None`` (nessun raggruppamento) è una scelta misurata, non il default della
        libreria: col ritmo reale del provider (~4 char ogni 19 ms) un debounce a 20 ms dimezza le
        validazioni parziali ma ritarda il primo testo di ~19 ms, per risparmiare ~10 ms di CPU
        (~0,15 ms per validazione). Su una risposta lunga l'overhead totale resta di poche decine
        di ms distribuiti su secondi: la latenza del primo token vale più del risparmio.
        """
        emitted = 0
        async for partial in self._streamed.stream_output(debounce_by=None):
            message = getattr(partial, "message", None) or ""
            if len(message) > emitted:
                yield message[emitted:]
                emitted = len(message)

    async def finalize(self):
        """Da chiamare a delta esauriti: risolve output finale e ``DocumentRef``."""
        self.output = await self._streamed.get_output()
        self.refs = citations.cited_refs(self._deps, self.output)
        return self.output, self.refs, self.result


def _orchestrator_for(deps: AgenticDeps, model=None, *, enable_verifier=False) -> Agent:
    """L'orchestratore configurato dalla RAGConfiguration e dalla configurazione LLM del tenant."""
    cfg = deps.rag_configuration
    return build_orchestrator(
        model or deps.orchestrator_model,
        enable_verifier=enable_verifier,
        retries=int(cfg.get("max_iterations")) if cfg.get("max_iterations") else 1,
        parallel_discovery=bool(cfg.get("parallel_discovery")),
        max_parallel_subtasks=resolve_max_parallel_subtasks(cfg),
        bypass_rag=bool(cfg.get("bypass_rag")),
        prompt_no_rag=cfg.get("prompt_no_rag"),
        retrieve_citations=bool(deps.llm_configuration.get("retrieve_citations")),
    )


@asynccontextmanager
async def stream_orchestrator(
    *,
    question: str,
    deps: AgenticDeps,
    message_history=None,
    model=None,
):
    """Variante **streaming** di :func:`run_orchestrator`: cede un :class:`OrchestratorStream`.

    Presuppone il Verifier **disattivato**: ``run_stream()`` non supporta i retry degli
    ``output_validator``, e ritrattare token gia' inviati richiederebbe un evento SSE che i
    frontend non gestiscono. Con il Verifier attivo l'entrypoint usa :func:`run_orchestrator`.
    """
    async with _orchestrator_for(deps, model).run_stream(
        question,
        deps=deps,
        message_history=message_history,
        usage_limits=build_usage_limits(deps.rag_configuration),
    ) as streamed:
        yield OrchestratorStream(streamed, deps)


async def run_orchestrator(
    *,
    question: str,
    deps: AgenticDeps,
    message_history=None,
    model=None,
):
    """
    Esegue l'orchestratore su una domanda utente.

    Ritorna ``(OrchestratorOutput, list[DocumentRef], result)``: il secondo elemento risolve
    ``used_document_ids`` sui candidati Discovery accumulati (per l'evento SSE DOCUMENT).
    ``message_history`` = history PydanticAI (ripresa conversazione). ``model`` override per i test.
    """
    deps.question = question
    deps.message_history = list(message_history or [])
    enable_verifier = verifier_enabled(deps.rag_configuration)
    result = await _orchestrator_for(deps, model, enable_verifier=enable_verifier).run(
        question,
        deps=deps,
        message_history=message_history,
        usage_limits=build_usage_limits(deps.rag_configuration),
    )
    output = result.output
    refs = citations.cited_refs(deps, output)
    logger.debug(
        f"[orchestrator] done status={output.status} "
        f"used_ids={output.used_document_ids} refs={len(refs)}"
    )
    return output, refs, result
