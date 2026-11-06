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
Discovery Agent (sub-agent invocato come tool dall'orchestratore) + post-processing deterministico.

Due livelli (design §4.1):

1. **LLM** (``pydantic_ai.Agent``): chiama il tool ``retriever`` e poi emette SOLO gli id
   (``DiscoverySelection``) oppure dichiara ``DiscoveryUnanswerable``. Niente contenuto → niente
   allucinazione di testo.
2. **Post-processing (non-LLM)**: valida ogni ``document_id`` (deve stare tra i candidati del
   retriever), assembla il contenuto raggruppando i chunk, costruisce ``DiscoveryResult``.

Il retrieval usa ``deps.retrieve_fn`` (iniettabile) → validabile offline con uno stub.
:func:`run_parallel_discovery` esegue piu' Discovery in parallelo per ``discover_many``.
"""

import asyncio
from dataclasses import dataclass, field
from typing import Optional

from pydantic_ai import Agent, RunContext

from app.rag.agentic import retrieval
from app.rag.agentic.contracts import (
    DiscoveredDocument,
    DiscoveryResult,
    DiscoverySelection,
    DiscoveryUnanswerable,
    DocumentRef,
    RetrieveRequest,
    SubAgentRequest,
)
from app.rag.agentic.deps import AgenticDeps
from app.utils.logger import get_logger, log_context

logger = get_logger(__name__)

DISCOVERY_SYSTEM_PROMPT = """You are a Discovery Agent for a document RAG system.
Your job: find which documents can answer the sub-question you are given.

Procedure:
1. Call the `retriever` tool with a focused query (and domains, only if clearly relevant).
2. Look at the returned candidates: each has a `document_id` and a content preview.
3. Return the `document_id` values of the candidates that contain information needed to answer
   the sub-question (DiscoverySelection). For a question that asks for a list, select every
   candidate that matches. Leave out candidates that are only similar: same kind of document,
   different subject. Do NOT invent ids: choose strictly among the candidates.
4. If none of the candidates can answer, return DiscoveryUnanswerable with a short reason.

Never write the final answer text: your output is only the selection of ids (or unanswerable)."""


#: Ricerche per Discovery. Senza tetto un Discovery che non trova la risposta continua a
#: riformulare: ogni risultato resta nella sua conversazione, fino a sforare il contesto del modello.
MAX_RETRIEVER_CALLS = 3

SEARCH_LIMIT_REACHED = (
    f"Search limit reached ({MAX_RETRIEVER_CALLS} searches). Do not search again: select among "
    "the candidates already returned, or return DiscoveryUnanswerable."
)


@dataclass
class _CandidateStore:
    """Cattura i candidati restituiti dal tool retriever, per il post-processing deterministico."""

    by_id: dict = field(default_factory=dict)
    searches: int = 0

    def add_many(self, records: list[dict]) -> None:
        for rec in records:
            merge_record(self.by_id, rec)


def merge_record(by_id: dict, rec: dict) -> None:
    """Aggiunge ``rec`` a ``by_id``. Se lo stesso id torna piu' volte, concatena il contenuto
    (chunk gia' merge-ati a monte), ma non lo stesso chunk ritrovato da un'altra ricerca."""
    doc_id = rec.get("document_id")
    if doc_id is None:
        return
    if doc_id in by_id:
        existing = by_id[doc_id]
        content = rec.get("content", "")
        if content and content not in existing.get("content", ""):
            existing["content"] = f"{existing.get('content', '')}\n{content}".strip()
    else:
        by_id[doc_id] = dict(rec)


def resolve_filter_domains(
    deps: AgenticDeps, llm_domains: Optional[list[str]]
) -> Optional[list[str]]:
    """Domini da usare come filtro di retrieval.

    L'intent detection ha la **precedenza** sulla scelta dell'LLM, che resta un ripiego per quando
    la detection non produce nulla. Due motivi:

    1. i domini rilevati vengono da similarita' su documenti di dominio reali, quelli scelti
       dall'LLM da un'inferenza sul *nome* del dominio nel prompt: il primo e' segnale ancorato,
       il secondo una supposizione;
    2. qui diventa un filtro **rigido**, che puo' nascondere documenti — un dominio plausibile ma
       sbagliato costa recall in silenzio, quindi vince il segnale ancorato.

    E' anche la semantica dell'originale, dove il filtro veniva da ``state.domain`` e l'LLM non
    aveva voce in capitolo. ``None`` = nessuno scoping (si cerca su tutto).
    """
    return deps.detected_domains or llm_domains or None


def _build_discovery_agent(model, store: _CandidateStore, deps: AgenticDeps) -> Agent:
    """Crea il Discovery Agent con il tool retriever che popola ``store``."""
    domains_xml = (
        "<domains>\n"
        + "\n".join(f"  <domain>{d}</domain>" for d in deps.allowed_domains)
        + "\n</domains>"
        if deps.allowed_domains
        else "<domains/>"
    )
    agent = Agent(
        model,
        output_type=[DiscoverySelection, DiscoveryUnanswerable],
        deps_type=AgenticDeps,
        system_prompt=f"{DISCOVERY_SYSTEM_PROMPT}\n\nAllowed domains:\n{domains_xml}",
        # selezione di id: step di utilita', a temperatura 0 come nel modulo (utility_llm)
        model_settings={"temperature": 0},
    )

    @agent.tool
    def retriever(ctx: RunContext[AgenticDeps], request: RetrieveRequest) -> list[dict] | str:
        """Recupera documenti candidati da OpenSearch. Ritorna [{document_id, content_preview}]."""
        if store.searches >= MAX_RETRIEVER_CALLS:
            logger.debug(f"[discovery] tool=retriever tetto di {MAX_RETRIEVER_CALLS} ricerche raggiunto")
            return SEARCH_LIMIT_REACHED
        store.searches += 1

        # validazione domini a runtime: scarta quelli inventati (design §4.2)
        domains = None
        if request.domains:
            valid = [d for d in request.domains if d in ctx.deps.allowed_domains]
            domains = valid or None

        ctx.deps.emit_status("retrieving")
        filter_domains = resolve_filter_domains(ctx.deps, domains)
        # NB: top_k/range/chunk_window vengono da RAGConfiguration, non dall'LLM
        records = ctx.deps.retrieve_fn(
            query=request.question,
            search_text=request.question,
            tenant_id=ctx.deps.tenant_id,
            opensearch_host=ctx.deps.opensearch_host,
            grpc_host=ctx.deps.grpc_host_datasource,
            domains=filter_domains,
            datasource_ids=ctx.deps.datasource_ids,
            filter_tokens=ctx.deps.filter_tokens,
            media=ctx.deps.media,
            **ctx.deps.request_search_args,
            **ctx.deps.retrieve_top_k_args(),
        )
        logger.debug(
            f"[discovery] tool=retriever q={request.question!r} "
            f"domains={request.domains} -> {len(records)} candidati"
        )
        context = log_context(
            tenant_id=ctx.deps.tenant_id, user_id=ctx.deps.user_id, chat_id=ctx.deps.chat_id
        )
        logger.info(
            f"[retriever] retrieved {len(records)} docs domains={filter_domains} "
            f"media={'yes' if ctx.deps.media else 'no'} {context}"
        )
        store.add_many(records)

        # espone all'LLM SOLO id + anteprima (niente score: confonde il modello, design §4)
        return [
            {
                "document_id": rec.get("document_id"),
                "content_preview": (rec.get("content") or "")[:800],
                "title": rec.get("title"),
                "domain": rec.get("domain"),
            }
            for rec in records
        ]

    return agent


def _post_process(selection: DiscoverySelection, store: _CandidateStore) -> DiscoveryResult:
    """Valida gli id scelti (devono essere tra i candidati) e assembla ``DiscoveryResult``."""
    documents: list[DiscoveredDocument] = []
    for doc_id in selection.document_ids:
        candidate = store.by_id.get(doc_id)
        if candidate is None:
            # id inventato / non tra i risultati → scartato (design §4.1)
            continue
        documents.append(
            DiscoveredDocument(
                document_id=doc_id,
                content=candidate.get("content", ""),
                title=candidate.get("title"),
                domain=candidate.get("domain"),
            )
        )
    return DiscoveryResult(documents=documents)


def _within_turn_budget(documents: list, deps: AgenticDeps) -> list:
    """Documenti che stanno ancora nel budget di contesto del turno.

    Il tetto del retrieval vale per una ricerca; l'orchestratore riceve i documenti di tutte le
    deleghe del turno (fino a ``MAX_DISCOVERIES_PER_TURN``), quindi la somma va limitata a parte,
    alla stessa quota del ``context_window`` del modello. Senza ``context_window`` non c'e' tetto.
    """
    context_window = deps.llm_configuration.get("context_window") or 0
    if not context_window:
        return documents
    budget = context_window * retrieval.MAX_CONTEXT_WINDOW_PERCENTAGE
    kept = []
    for document in documents:
        tokens = len(document.content) / retrieval.TOKEN_SIZE
        if deps.delivered_tokens + tokens > budget:
            logger.info(
                f"[discovery] budget di contesto del turno ({int(budget)} token): "
                f"{len(documents) - len(kept)} documenti non consegnati"
            )
            break
        deps.delivered_tokens += tokens
        kept.append(document)
    return kept


async def run_discovery(
    *,
    request: SubAgentRequest,
    deps: AgenticDeps,
    usage=None,
    model=None,
):
    """
    Esegue un Discovery Agent e ritorna ``DiscoveryResult`` oppure ``DiscoveryUnanswerable``.

    ``usage`` viene inoltrato a ``agent.run`` per il conteggio cumulativo (agent delegation).
    ``model`` override per i test; altrimenti ``deps.discovery_model``.
    """
    store = _CandidateStore()
    agent = _build_discovery_agent(model or deps.discovery_model, store, deps)

    prompt = request.question
    if request.hints:
        prompt = f"{request.question}\n\nHints from orchestrator: {request.hints}"

    logger.debug(f"[discovery] run q={request.question!r}")
    result = await agent.run(prompt, deps=deps, usage=usage)
    output = result.output

    # accumula i candidati ricchi (score/title/url/domain) per risolvere used_document_ids →
    # DocumentRef nell'evento SSE DOCUMENT (mantiene la parità dell'evento con oggi). Un documento
    # gia' consegnato da un'altra delega del turno unisce i chunk, non li sostituisce: il Verifier
    # giudica su questo testo, e l'orchestratore li ha letti tutti.
    for rec in store.by_id.values():
        merge_record(deps.discovered, rec)

    if isinstance(output, DiscoveryUnanswerable):
        logger.debug(f"[discovery] esito=UNANSWERABLE reason={output.reason!r}")
        return output
    selected = _post_process(output, store)
    if not selected.documents:
        # nessun id valido (o nessuno scelto): per l'orchestratore e' "non rispondibile", non un
        # successo vuoto da cui comporre una risposta senza fonti
        logger.debug(f"[discovery] esito=UNANSWERABLE nessun id valido tra {output.document_ids}")
        return DiscoveryUnanswerable(reason="no relevant document among the search results")
    selected.documents = _within_turn_budget(selected.documents, deps)
    if not selected.documents:
        logger.debug("[discovery] esito=UNANSWERABLE budget di contesto del turno esaurito")
        return DiscoveryUnanswerable(reason="context budget of this turn exhausted")
    for doc in selected.documents:
        if doc.document_id not in deps.delivered_ids:
            deps.delivered_ids.append(doc.document_id)
    logger.debug(
        f"[discovery] esito=SELECTION scelti={len(output.document_ids)} "
        f"validati={len(selected.documents)} docs="
        + " | ".join(
            f"id={doc.document_id} title={doc.title!r} domain={doc.domain}"
            for doc in selected.documents
        )
    )
    return selected


async def run_parallel_discovery(
    *,
    requests: list[SubAgentRequest],
    deps: AgenticDeps,
    usage=None,
    model=None,
    max_parallel: int = 3,
) -> list:
    """Esegue piu' Discovery in **parallelo** e ritorna gli esiti separati, uno per richiesta e
    nello stesso ordine (``DiscoveryResult`` oppure ``DiscoveryUnanswerable``).

    - la concorrenza e' limitata da ``max_parallel``;
    - ``usage`` e' condiviso: le richieste si sommano a quelle dell'orchestratore;
    - un Discovery che fallisce diventa ``DiscoveryUnanswerable`` e non ferma gli altri.

    Lo stato del turno in ``deps`` (``discovered``, ``delivered_ids``, budget di contesto) si
    aggiorna senza ``await`` fra lettura e scrittura, quindi i Discovery concorrenti non si
    pestano i piedi.
    """
    if not requests:
        return []
    semaphore = asyncio.Semaphore(max(1, int(max_parallel)))

    async def _one(request: SubAgentRequest):
        async with semaphore:
            return await run_discovery(request=request, deps=deps, usage=usage, model=model)

    settled = await asyncio.gather(*[_one(request) for request in requests], return_exceptions=True)
    outcomes = []
    for res in settled:
        if isinstance(res, Exception):
            logger.warning(f"[discovery] subtask failed: {res!r}")
            outcomes.append(DiscoveryUnanswerable(reason=f"subtask failed: {res}"))
        else:
            outcomes.append(res)
    return outcomes


def resolve_document_refs(deps: AgenticDeps, used_document_ids: list[str]) -> list[DocumentRef]:
    """Risolve gli id citati dall'orchestratore in ``DocumentRef`` (per l'evento SSE DOCUMENT)."""
    refs: list[DocumentRef] = []
    seen: set[str] = set()
    for doc_id in used_document_ids:
        if doc_id in seen:
            continue
        seen.add(doc_id)
        rec = deps.discovered.get(doc_id, {})
        refs.append(
            DocumentRef(
                document_id=doc_id,
                title=rec.get("title"),
                url=rec.get("url"),
                score=rec.get("score"),
                domain=rec.get("domain"),
            )
        )
    return refs
