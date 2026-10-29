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
from app.rag.agentic import (
    citations,
    discovery,
    evaluation,
    guardrails,
    intent,
    orchestrator,
    retrieval,
    streaming,
)
from app.rag.agentic.contracts import OrchestratorOutput
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.models_factory import build_model
from app.rag.agentic.persistence import (
    FrontendConversationStore,
    OpenSearchConversationStore,
)
from app.rag.evaluations import record_turn
from app.utils.authentication import unauthorized_response
from app.utils.error_events import error_event
from app.utils.query_validation import is_blank_query
from app.utils.logger import content_fingerprint, debug_extra, get_logger, log_context

logger = get_logger(__name__)

GUARDRAIL_VIOLATION_MESSAGE = "Guardrail violation"
# The image never reaches the model, which only sees the text of the documents
# found: any visual detail it wrote would be invented. And a document found by
# visual similarity can be an image, whose text is a caption, a file name or
# nothing at all.
MEDIA_ONLY_QUERY_INSTRUCTION = (
    "The user submitted an image instead of a written question. "
    "The documents in the context are the closest matches to that "
    "image, and some may themselves be images whose text is a "
    "caption, a file name or nothing at all. Summarise what the "
    "matches contain and what connects them, using only the text "
    "present in the context: the image itself is not part of it, so "
    "do not describe visual details. If the matches carry no usable "
    "text, say so plainly instead of guessing."
)
SCOPE_GATE_REDIRECT_MESSAGE = "Posso aiutarti solo su temi di questo dominio."
UNEXPECTED_ERROR_MESSAGE = "Unexpected error"
RATE_LIMIT_MESSAGE = "Rate limit exceeded. Try again later."
# The user cannot fix the configuration: the detail goes to the log.
MODEL_WITHOUT_TOOLS_MESSAGE = "The configured language model cannot search the documents."
# The provider refuses the generation with its own filter (Azure content
# policy, #2253): a block, reported as one.
CONTENT_POLICY_MARKERS = (
    "content_filter",
    "contentpolicyviolationerror",
    "responsibleaipolicyviolation",
    "content management policy",
)


async def generate_title(model, question: str, answer: str, usage=None) -> str:
    """A short title for the first exchange of a chat."""
    from pydantic_ai import Agent

    agent = Agent(
        model,
        name="title",
        output_type=str,
        system_prompt=(
            "Generate a short conversation title (a few words, no quotes) for the exchange "
            "the user provides. Reply with the title only."
        ),
    )
    result = await agent.run(f"User: {question}\nAI: {answer}", usage=usage)
    return (result.output or "").strip()


async def resolve_conversation_title(
    model,
    *,
    enabled,
    chat_sequence_number: int,
    user_id,
    chat_id,
    question: str,
    answer: str,
    usage=None,
):
    """
    Return ``(title, emit_title)`` for a turn.

    The first turn of a chat of a logged user gets a generated title, sent as
    the TITLE event. With the generation disabled the title falls back to the
    question, or the chat list would show the chat without one.
    """
    if enabled and chat_sequence_number == 1 and user_id and chat_id:
        return await generate_title(model, question, answer, usage), True
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


async def media_only_turn(
    *, rag_type: str, deps: AgenticDeps, message_history, finish_turn, guard
):
    """
    A turn with an image and no text, as in agentic-rag-module: the guardrail,
    the intent detection and the orchestrator read the written question, and
    there is none. The image goes straight to a single search, and the model
    sums up the text of what it found with :data:`MEDIA_ONLY_QUERY_INSTRUCTION`.
    The answer cites every document found.
    """
    from pydantic_ai import Agent

    context = log_context(tenant_id=deps.tenant_id, user_id=deps.user_id, chat_id=deps.chat_id)
    logger.info(f"[media_only] guardrail, intent detection and orchestrator skipped {context}")
    yield streaming.status_event("retrieving")
    records = deps.retrieve_fn(
        query="",
        search_text="",
        tenant_id=deps.tenant_id,
        opensearch_host=deps.opensearch_host,
        grpc_host=deps.grpc_host_datasource,
        datasource_ids=deps.datasource_ids,
        filter_tokens=deps.filter_tokens,
        media=deps.media,
        **deps.request_search_args,
        **deps.retrieve_top_k_args(),
    )
    for record in records:
        discovery.merge_record(deps.discovered, record)
    document_ids = list(deps.discovered)
    deps.delivered_ids.extend(document_ids)
    documents = "\n\n".join(deps.discovered[i].get("content", "") for i in document_ids)
    # instructions, not a system prompt: sent at every turn by the agent that runs it, while those
    # of the past turns are not sent again, so neither lost after the first turn nor left to the
    # orchestrator of the next one
    agent = Agent(
        deps.orchestrator_model,
        name="media_only_answer",
        output_type=str,
        instructions=MEDIA_ONLY_QUERY_INSTRUCTION,
    )
    result = await agent.run(
        f"Context:\n{documents}", message_history=message_history, usage=deps.usage
    )
    output = OrchestratorOutput(status="ok", message=result.output, used_document_ids=document_ids)

    async def post_stream():
        refs = discovery.resolve_document_refs(deps, document_ids)
        return await finish_turn(output, refs, result)

    async for event in streaming.stream_answer(
        deltas=streaming.deltas_from_message(output.message),
        rag_type=rag_type,
        post_stream=post_stream,
        **guard,
    ):
        yield event


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
    question, which the Discovery searches in. A question made of an image
    alone skips both, and the orchestrator too: see :func:`media_only_turn`.

    With ``enable_real_time_evaluation`` the turn runs as the evaluation graph
    of agentic-rag-module: without input guardrail and intent detection, and
    with the judges of :mod:`evaluation` on the complete answer, before it is
    streamed. Every turn leaves its span for the offline evaluation.

    :param rag_type: The RAG type of the request.
    :param search_text: The question of the user.
    :param deps: The deps of the request.
    :param store: The conversation store: OpenSearch for a logged user, the
        history sent by the client for an anonymous one.
    :param guardrails_configuration: The guardrails of the tenant.
    """
    started_ns = time.time_ns()
    real_time_evaluation = bool(deps.rag_configuration.get("enable_real_time_evaluation"))
    media_only = bool(deps.media) and is_blank_query(search_text)
    if not media_only and not real_time_evaluation:
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

    async def finish_turn(output, refs, result, evaluation_fields=None):
        """Title and save once the answer is complete: after the CHUNK events."""
        title, emit_title = await resolve_conversation_title(
            deps.orchestrator_model,
            enabled=enable_conversation_title,
            chat_sequence_number=chat_sequence_number,
            user_id=user_id,
            chat_id=chat_id,
            question=search_text,
            answer=output.message,
            usage=deps.usage,
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
            evaluation_fields=evaluation_fields,
        )
        record_turn(
            started_ns=started_ns,
            question=search_text,
            response=output.message,
            deps=deps,
            used_rag=deps.discoveries_run > 0,
        )
        return title, emit_title, refs

    if media_only:
        async for event in media_only_turn(
            rag_type=rag_type,
            deps=deps,
            message_history=message_history,
            finish_turn=finish_turn,
            guard=guard,
        ):
            yield event
        return

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
    if not real_time_evaluation:
        await detect_intent(rag_type=rag_type, search_text=search_text, deps=deps)

    if orchestrator.verifier_enabled(deps.rag_configuration) or real_time_evaluation:
        running = asyncio.ensure_future(
            orchestrator.run_orchestrator(
                question=search_text, deps=deps, message_history=message_history
            )
        )
        async for event in streaming.relay_until(events, running):
            yield event
        output, refs, result = await running

        evaluation_fields = None
        if real_time_evaluation:
            # the Verifier, when on, has already announced this phase
            if "evaluating" not in deps.emitted_phases:
                yield streaming.status_event("evaluating")
            evaluation_fields = await evaluation.run_evaluations(
                question=search_text,
                output=output,
                deps=deps,
                used_rag=deps.discoveries_run > 0,
            )

        async def post_stream():
            return await finish_turn(output, refs, result, evaluation_fields)

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


def render_token_usage(usage, model_name) -> str:
    """The tokens a request spent, as agentic-rag-module records them. A provider that reports
    no usage leaves it empty, and the record says so rather than claiming a free request."""
    if usage is None or not usage.has_values():
        return "tokens=unavailable"
    spent = f"{usage.input_tokens}/{usage.output_tokens}"
    return (
        f"tokens_in={usage.input_tokens} tokens_out={usage.output_tokens} "
        f"by_model[{model_name}={spent}]"
    )


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
    with its outcome (COMPLETED, BLOCKED_INPUT, BLOCKED_OUTPUT, OFF_SCOPE, ERROR),
    its duration and the tokens it spent on the model of the tenant.
    """
    started_at = time.monotonic()
    outcome = "COMPLETED"
    deps = None
    context = log_context(tenant_id=tenant_id, user_id=user_id, chat_id=chat_id)
    logger.info(
        f"[request] start rag_type={rag_type} "
        f"{content_fingerprint('query', search_text)} {context}"
        + debug_extra(logger, query=search_text)
    )
    try:
        model = build_model(llm_configuration)
        # The documents are found only through tools, and PydanticAI drops the
        # tools of a model that cannot call them: the orchestrator would answer
        # without documents. With bypass_rag it has no tools, so any model answers.
        if not model.profile.get("supports_tools", True) and not rag_configuration.get(
            "bypass_rag"
        ):
            logger.warning(
                f"[request] model_type={llm_configuration.get('model_type')} cannot call "
                "tools: configure an OpenAI-compatible endpoint with tool calling, "
                f"or enable bypass_rag {context}"
            )
            outcome = "ERROR"
            yield json.dumps({"chunk": MODEL_WITHOUT_TOOLS_MESSAGE, "type": "ERROR"})
            return
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
            media=media,
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
        tokens = render_token_usage(deps and deps.usage, llm_configuration.get("model"))
        logger.info(
            f"[request] end outcome={outcome} "
            f"duration_ms={int((time.monotonic() - started_at) * 1000)} {context} {tokens}"
        )
