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
Entrypoint of the orchestrated pipeline: from a RAG request to its SSE events.

- :func:`run_turn` is the core, with the ``deps`` injected, so that it runs
  offline with a test model and a stub retrieval;
- :func:`get_agentic_rag` is the production wrapper, with the signature of
  ``chain.get_agentic_rag``: it builds the model and the deps of the tenant and
  delegates to :func:`run_turn`.

The orchestrator answers in real streaming: the CHUNK events leave while the
provider writes the ``message`` field of the structured output. With the
Verifier enabled the answer is buffered instead: it is verified, possibly
repaired, and only then streamed in pieces, since a streamed run cannot retry
and the frontends have no event to take back text already shown.
"""

import asyncio
import json
import time

from fastapi import HTTPException

from app.external_services.grpc.grpc_client import get_embedding_model_configuration
from app.rag.agentic import citations, guardrails, intent, orchestrator, retrieval, streaming
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.models_factory import build_model
from app.rag.agentic.persistence import (
    FrontendConversationStore,
    OpenSearchConversationStore,
)
from app.utils.authentication import unauthorized_response
from app.utils.error_events import error_event
from app.utils.logger import content_fingerprint, debug_extra, get_logger, log_context

logger = get_logger(__name__)

GUARDRAIL_VIOLATION_MESSAGE = "Guardrail violation"
SCOPE_GATE_REDIRECT_MESSAGE = "Posso aiutarti solo su temi di questo dominio."
UNEXPECTED_ERROR_MESSAGE = "Unexpected error"
RATE_LIMIT_MESSAGE = "Rate limit exceeded. Try again later."
# The provider refuses the generation with its own filter (Azure content
# policy, #2253): a block, reported as one.
CONTENT_POLICY_MARKERS = (
    "content_filter",
    "contentpolicyviolationerror",
    "responsibleaipolicyviolation",
    "content management policy",
)


async def generate_title(model, question: str, answer: str) -> str:
    """A short title for the first exchange of a chat."""
    from pydantic_ai import Agent

    agent = Agent(
        model,
        output_type=str,
        system_prompt=(
            "Generate a short conversation title (a few words, no quotes) for the exchange "
            "the user provides. Reply with the title only."
        ),
    )
    result = await agent.run(f"User: {question}\nAI: {answer}")
    return (result.output or "").strip()


async def resolve_conversation_title(
    model, *, enabled, chat_sequence_number: int, user_id, chat_id, question: str, answer: str
):
    """
    Return ``(title, emit_title)`` for a turn.

    The first turn of a chat of a logged user gets a generated title, sent as
    the TITLE event. With the generation disabled the title falls back to the
    question, or the chat list would show the chat without one.
    """
    if enabled and chat_sequence_number == 1 and user_id and chat_id:
        return await generate_title(model, question, answer), True
    if chat_sequence_number == 1 and user_id:
        return question.strip('"'), False
    return None, False


def delivered_context(deps: AgenticDeps) -> str:
    """The text of the documents delivered to the orchestrator in this turn, for the scope gate."""
    return "\n\n".join(
        deps.discovered[document_id].get("content", "")
        for document_id in deps.delivered_ids
        if document_id in deps.discovered
    )


def output_guardrail_options(guardrails_configuration, *, search_text: str, deps) -> dict:
    """The options of :func:`streaming.stream_answer` for the output guardrail of the tenant:
    none when it is disabled."""
    output_config = (guardrails_configuration or {}).get("output_guardrail") or {}
    if not output_config.get("enable_output_guardrail"):
        return {}

    async def output_check(answer: str, stage: str) -> str:
        return await guardrails.output_guardrail(
            answer=answer,
            guardrails_configuration=guardrails_configuration,
            model=deps.orchestrator_model,
            deps=deps,
            stage=stage,
        )

    async def scope_check(answer_prefix: str) -> str:
        # read when the prefix arrives, after the tool round has delivered the documents
        return await guardrails.scope_gate(
            query=search_text,
            context_text=delivered_context(deps),
            answer_prefix=answer_prefix,
            output_guardrail=output_config,
            model=deps.orchestrator_model,
            deps=deps,
        )

    return {
        "output_guardrail_type": output_config.get("output_guardrail_type", 1),
        "chunk_interval": output_config.get("output_guardrail_chunk_interval") or 10,
        "output_check": output_check,
        "scope_check": scope_check,
        "scope_gate_prefix_chars": output_config.get("scope_gate_prefix_chars", 250),
        "scope_gate_redirect_message": output_config.get(
            "scope_gate_redirect_message", SCOPE_GATE_REDIRECT_MESSAGE
        ),
    }


async def detect_intent(*, rag_type: str, search_text: str, deps: AgenticDeps) -> None:
    """Fill the domains of ``deps``: the ones of the tenant, which the Discovery may choose
    from, and the ones detected for the question, its domain filter. Without intent detection
    both stay empty, and the Discovery searches everything."""
    context = log_context(tenant_id=deps.tenant_id, user_id=deps.user_id, chat_id=deps.chat_id)
    if not intent.intent_detection_enabled(rag_type, deps.rag_configuration):
        logger.info(f"[intent_detection] skipped rag_type={rag_type} {context}")
        return
    try:
        deps.allowed_domains, deps.detected_domains = await intent.detect_domains(
            query=search_text, deps=deps
        )
    except Exception as e:  # noqa: BLE001
        # a failed detection costs the filter, not the answer: the Discovery searches everything
        logger.warning(f"[intent_detection] failed, no domain filter: {e!r} {context}")


async def run_turn(
    *,
    rag_type: str,
    search_text: str,
    deps: AgenticDeps,
    store,
    chat_id=None,
    user_id=None,
    chat_sequence_number: int = 1,
    retrieve_from_uploaded_documents=None,
    enable_conversation_title=False,
    guardrails_configuration=None,
):
    """
    Answer ``search_text`` and yield the SSE events of the answer as JSON strings.

    The input guardrail comes first: a blocked query gets only the GUARDRAIL and
    END events, and the turn is not saved. The history of the chat comes from
    ``store`` from the second turn on, and the turn is saved there with its
    title once the answer has been delivered. An answer that the output
    guardrail blocks ends with a CANCEL event and is not saved either.

    After the input guardrail the intent detection gives the domains of the
    question, which the Discovery searches in.

    :param rag_type: The RAG type of the request.
    :param search_text: The question of the user.
    :param deps: The deps of the request.
    :param store: The conversation store: OpenSearch for a logged user, the
        history sent by the client for an anonymous one.
    :param guardrails_configuration: The guardrails of the tenant.
    """
    verdict = await guardrails.input_guardrail(
        query=search_text,
        guardrails_configuration=guardrails_configuration,
        model=deps.orchestrator_model,
        deps=deps,
    )
    if verdict not in (None, guardrails.SAFE):
        yield json.dumps({"chunk": GUARDRAIL_VIOLATION_MESSAGE, "type": "GUARDRAIL"})
        yield json.dumps({"chunk": "", "type": "END"})
        return

    message_history = (
        store.load_message_history(chat_id) if chat_sequence_number > 1 else []
    )
    guard = output_guardrail_options(guardrails_configuration, search_text=search_text, deps=deps)

    async def finish_turn(output, refs, result):
        """Title and save once the answer is complete: after the CHUNK events."""
        title, emit_title = await resolve_conversation_title(
            deps.orchestrator_model,
            enabled=enable_conversation_title,
            chat_sequence_number=chat_sequence_number,
            user_id=user_id,
            chat_id=chat_id,
            question=search_text,
            answer=output.message,
        )
        store.save_turn(
            chat_id=chat_id,
            chat_sequence_number=chat_sequence_number,
            original_query=search_text,
            current_query=search_text,
            response=output.message,
            conversation_title=title,
            retrieve_from_uploaded_documents=retrieve_from_uploaded_documents,
            used_document_refs=refs,
            message_history=result.all_messages(),
        )
        return title, emit_title, refs

    # One queue for the events produced outside this generator: the phases the
    # tools publish and, on the streaming path, the whole answer. A single queue
    # keeps the order by construction: the phases of the tools always come
    # before the first CHUNK, because the tool round ends before the model
    # starts writing. "planning" is the only phase that is true before the run
    # starts; the tools publish "discovering" and "retrieving" when they really
    # run, so a turn that does not search does not announce a search.
    events: asyncio.Queue = asyncio.Queue()
    deps.status_sink = lambda phase: events.put_nowait(streaming.status_event(phase))
    yield streaming.status_event("planning")
    await detect_intent(rag_type=rag_type, search_text=search_text, deps=deps)

    if orchestrator.verifier_enabled(deps.rag_configuration):
        running = asyncio.ensure_future(
            orchestrator.run_orchestrator(
                question=search_text, deps=deps, message_history=message_history
            )
        )
        async for event in streaming.relay_until(events, running):
            yield event
        output, refs, result = await running

        async def post_stream():
            return await finish_turn(output, refs, result)

        async for event in streaming.stream_answer(
            deltas=streaming.deltas_from_message(output.message),
            rag_type=rag_type,
            post_stream=post_stream,
            sources=lambda: citations.sources(deps),
            **guard,
        ):
            yield event
        return

    # ``run_stream`` runs the whole tool round inside ``__aenter__``: entering it
    # here would block this generator until the search is over, and the phases
    # would come out all together, after the fact. The whole stream, context
    # manager included, runs in a task, and this generator relays the queue.
    async def produce():
        async with orchestrator.stream_orchestrator(
            question=search_text, deps=deps, message_history=message_history
        ) as orchestrator_stream:

            async def post_stream():
                return await finish_turn(*await orchestrator_stream.finalize())

            async for event in streaming.stream_answer(
                deltas=streaming.coalesce(orchestrator_stream.deltas()),
                rag_type=rag_type,
                post_stream=post_stream,
                sources=lambda: citations.sources(deps),
                **guard,
            ):
                events.put_nowait(event)

    async for event in streaming.relay_until(events, asyncio.ensure_future(produce())):
        yield event


async def get_agentic_rag(
    rag_type,
    search_query,
    datasource_ids,
    after_key,
    suggest_keyword,
    suggestion_category_id,
    jwt,
    extra,
    sort,
    sort_after_key,
    language,
    search_text,
    chat_id,
    user_id,
    tenant_id,
    retrieve_from_uploaded_documents,
    chat_history,
    timestamp,
    chat_sequence_number,
    rag_configuration,
    llm_configuration,
    guardrails_configuration,
    opensearch_host,
    grpc_host_embedding,
    grpc_host_datasource,
    media=None,
):
    """
    Stream the answer to a RAG request, with the signature of ``chain.get_agentic_rag``.

    With ``retrieve_from_uploaded_documents`` the Discovery searches only the
    documents the user uploaded in the chat; an anonymous user gets a 401 ERROR.

    Every request leaves a ``[request] start`` record and a ``[request] end`` one
    with its outcome (COMPLETED, BLOCKED_INPUT, BLOCKED_OUTPUT, OFF_SCOPE, ERROR)
    and its duration.
    """
    started_at = time.monotonic()
    outcome = "COMPLETED"
    context = log_context(tenant_id=tenant_id, user_id=user_id, chat_id=chat_id)
    logger.info(
        f"[request] start rag_type={rag_type} "
        f"{content_fingerprint('query', search_text)} {context}"
        + debug_extra(logger, query=search_text)
    )
    try:
        model = build_model(llm_configuration)
        deps = AgenticDeps(
            rag_configuration=rag_configuration,
            llm_configuration=llm_configuration,
            opensearch_host=opensearch_host,
            grpc_host_datasource=grpc_host_datasource,
            grpc_host_embedding=grpc_host_embedding,
            tenant_id=tenant_id,
            orchestrator_model=model,
            discovery_model=model,
            user_id=user_id,
            chat_id=chat_id,
            datasource_ids=datasource_ids or [],
            request_search_args={
                "jwt": jwt,
                "extra": extra,
                "sort": sort,
                "sort_after_key": sort_after_key,
                "language": language,
            },
            # of the tokens of the search UI only the filters: the Discovery
            # writes the text it searches
            filter_tokens=[token for token in search_query or [] if token.filter],
        )
        if retrieve_from_uploaded_documents:
            # the uploaded documents belong to a user and a chat: an anonymous
            # user has none to search
            if not user_id:
                unauthorized_response()
            deps.retrieve_fn = retrieval.uploaded_documents_retrieve_fn(
                user_id=user_id,
                chat_id=chat_id,
                grpc_host_embedding=grpc_host_embedding,
                embedding_model_configuration=get_embedding_model_configuration(
                    grpc_host=grpc_host_datasource, tenant_id=tenant_id
                ),
            )

        # A logged user keeps the chat on OpenSearch; for an anonymous one the
        # client sends the history with every request.
        store = (
            OpenSearchConversationStore(opensearch_host, tenant_id, user_id)
            if (user_id and chat_id)
            else FrontendConversationStore(chat_history)
        )

        async for event in run_turn(
            rag_type=rag_type,
            search_text=search_text,
            deps=deps,
            store=store,
            chat_id=chat_id,
            user_id=user_id,
            chat_sequence_number=chat_sequence_number,
            retrieve_from_uploaded_documents=retrieve_from_uploaded_documents,
            enable_conversation_title=rag_configuration.get("enable_conversation_title"),
            guardrails_configuration=guardrails_configuration,
        ):
            parsed = json.loads(event)
            if parsed["type"] == "ERROR":
                outcome = "ERROR"
            elif parsed["type"] == "GUARDRAIL":
                outcome = "BLOCKED_INPUT"
            elif parsed["type"] == "CANCEL":
                blocked = parsed["chunk"] == streaming.OUTPUT_GUARDRAIL_CANCEL_MESSAGE
                outcome = "BLOCKED_OUTPUT" if blocked else "OFF_SCOPE"
            yield event

    except Exception as e:  # noqa: BLE001
        if isinstance(e, HTTPException) and e.status_code == 401:
            # a refused request, not a fault of the module
            logger.warning(f"[request] refused: {e.detail} {context}")
        else:
            logger.error(f"Unexpected error: {e} {context}")
        outcome = "ERROR"
        detail = str(e).lower()
        if any(marker in detail for marker in CONTENT_POLICY_MARKERS):
            outcome = "BLOCKED_OUTPUT"
            logger.warning(f"[output_guardrail] BLOCKED by provider content policy {context}")
            yield json.dumps({"chunk": GUARDRAIL_VIOLATION_MESSAGE, "type": "GUARDRAIL"})
            yield json.dumps({"chunk": "", "type": "END"})
            return
        if "rate_limit" in detail or "429" in detail:
            yield json.dumps({"chunk": RATE_LIMIT_MESSAGE, "type": "ERROR"})
            return
        # the detail of the exception stays in the logs
        yield json.dumps(error_event(e, UNEXPECTED_ERROR_MESSAGE))
    finally:
        logger.info(
            f"[request] end outcome={outcome} "
            f"duration_ms={int((time.monotonic() - started_at) * 1000)} {context}"
        )
