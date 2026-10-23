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
provider writes the ``message`` field of the structured output.
"""

import json
import time

from app.rag.agentic import orchestrator, streaming
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.models_factory import build_model
from app.utils.error_events import error_event
from app.utils.logger import content_fingerprint, debug_extra, get_logger, log_context

logger = get_logger(__name__)

GUARDRAIL_VIOLATION_MESSAGE = "Guardrail violation"
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


async def run_turn(*, rag_type: str, search_text: str, deps: AgenticDeps):
    """
    Answer ``search_text`` and yield the SSE events of the answer as JSON strings.

    :param rag_type: The RAG type of the request.
    :param search_text: The question of the user.
    :param deps: The deps of the request.
    """
    async with orchestrator.stream_orchestrator(
        question=search_text, deps=deps
    ) as orchestrator_stream:

        async def post_stream():
            _, refs, _ = await orchestrator_stream.finalize()
            return None, False, refs

        async for event in streaming.stream_answer(
            deltas=streaming.coalesce(orchestrator_stream.deltas()),
            rag_type=rag_type,
            post_stream=post_stream,
        ):
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

    Every request leaves a ``[request] start`` record and a ``[request] end`` one
    with its outcome (COMPLETED, BLOCKED_OUTPUT, ERROR) and its duration.
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

        async for event in run_turn(rag_type=rag_type, search_text=search_text, deps=deps):
            if json.loads(event)["type"] == "ERROR":
                outcome = "ERROR"
            yield event

    except Exception as e:  # noqa: BLE001
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
