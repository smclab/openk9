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
Verifier opzionale (Blocco 1) — gate finale sull'output dell'orchestratore.

Implementato come agent LLM che valuta ``OrchestratorOutput`` e ritorna ``VerificationOk`` oppure
``VerificationFailed(reason, unsupported_claims)``. L'integrazione (design §3, §6): un
``@orchestrator.output_validator`` che, se ``VerificationFailed``, alza ``ModelRetry`` con le
istruzioni di riparazione (cercare o togliere ogni affermazione non supportata) forzando un altro
giro dell'orchestratore. Attivo solo con flag ``enable_verifier``.

Il giudizio e' solo di *grounding*, sugli stessi documenti consegnati all'orchestratore
(``deps.delivered_ids``), interi. La storia della conversazione arriva al Verifier per dare senso a
una domanda di follow-up, e perche' ripetere quello che un turno precedente ha gia' detto non e' un
errore di grounding.
"""

from pydantic_ai import Agent

from app.rag.agentic.contracts import (
    OrchestratorOutput,
    VerificationFailed,
    VerificationOk,
    VerifierOutput,
)
from app.rag.agentic.deps import AgenticDeps
from app.utils.logger import get_logger

logger = get_logger(__name__)

VERIFIER_SYSTEM_PROMPT = """You are a Verifier for a document RAG system. You are given a user
question, a proposed answer, the ids the answer claims to cite, and the full content of the
documents the answer was written from.

Your only job is grounding: check that every substantive claim of the answer is supported by the
documents.

- A claim is supported when any of the documents states it, or when it follows directly from them
  (counting the items of a list, adding up figures, combining two statements).
- When the documents disagree, an answer that reports the different versions is supported: that is
  the correct way to handle a conflict, not an error.
- Judge only against the documents. Do not use your own knowledge: do not reject a date because it
  looks in the future, or a fact because you believe it is different.
- Do not reject an answer for being incomplete. Missing details are not a grounding error.
- Cited ids must be among the provided documents.
- The conversation so far, when given, is the context of the question: a follow-up question may
  only make sense with it. A claim that repeats what an earlier answer of the conversation says is
  supported.

Return VerificationOk when every claim is supported.

Otherwise return VerificationFailed with a short `reason` and, in `unsupported_claims`, every claim
that is not supported, quoted as written, each with an action:
- "search" when the documents simply do not mention it (another search may find it);
- "remove" when the documents contradict it, or it cannot come from documents at all."""


def build_verifier_agent(model) -> Agent:
    return Agent(
        model,
        name="verifier",
        output_type=[VerificationOk, VerificationFailed],
        deps_type=AgenticDeps,
        system_prompt=VERIFIER_SYSTEM_PROMPT,
        # un giudizio, non una generazione: stessa risposta, stesso verdetto
        model_settings={"temperature": 0},
    )


def _render_conversation(messages) -> str:
    """Domande e risposte dei turni precedenti, in testo. La risposta dell'orchestratore e' il
    campo ``message`` del suo output strutturato (una tool call); quella di una chat salvata da
    agentic-rag-module e' testo."""
    lines = []
    for message in messages or []:
        for part in message.parts:
            if part.part_kind == "user-prompt" and isinstance(part.content, str):
                lines.append(f"User: {part.content}")
            elif part.part_kind == "text":
                lines.append(f"Assistant: {part.content}")
            elif part.part_kind == "tool-call":
                answer = part.args_as_dict().get("message")
                if answer:
                    lines.append(f"Assistant: {answer}")
    return "\n".join(lines)


def _render_documents(deps: AgenticDeps) -> str:
    """I documenti consegnati all'orchestratore, interi: il Verifier deve giudicare sullo stesso
    contesto da cui la risposta e' stata scritta, non su un estratto."""
    lines = []
    for doc_id in deps.delivered_ids:
        rec = deps.discovered.get(doc_id, {})
        lines.append(f"[{doc_id}] {rec.get('title') or ''}\n{rec.get('content') or ''}")
    return "\n\n".join(lines) if lines else "(no documents available)"


async def verify(
    *,
    output: OrchestratorOutput,
    deps: AgenticDeps,
    usage=None,
    model=None,
) -> VerifierOutput:
    """Esegue il Verifier sull'output dell'orchestratore. ``usage`` per il conteggio cumulativo."""
    agent = build_verifier_agent(model or deps.orchestrator_model)
    conversation = _render_conversation(deps.message_history)
    prompt = (
        (f"Conversation so far:\n{conversation}\n\n" if conversation else "")
        + f"User question:\n{deps.question}\n\n"
        f"Proposed answer:\n{output.message}\n\n"
        f"Cited document ids: {output.used_document_ids}\n\n"
        f"Documents:\n{_render_documents(deps)}"
    )
    logger.debug(f"[verifier] verifica risposta per q={deps.question!r}")
    result = await agent.run(prompt, deps=deps, usage=usage)
    verdict = result.output
    failed = isinstance(verdict, VerificationFailed)
    # The verdict at INFO, the text of the claims only at DEBUG.
    logger.info(
        f"[verifier] verdetto={'FAILED' if failed else 'OK'}"
        + (f" unsupported_claims={len(verdict.unsupported_claims)}" if failed else "")
    )
    if failed:
        logger.debug(
            f"[verifier] reason={verdict.reason!r} claims="
            + str([(c.action, c.claim) for c in verdict.unsupported_claims])
        )
    return verdict
