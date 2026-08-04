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


from unittest.mock import MagicMock, patch

from app.models import models
from app.rag.agentic_rag import (
    MEDIA_ONLY_QUERY_INSTRUCTION,
    GraphState,
    RagGraph,
)

IMAGE = models.Media(data="aVZCT1J3MEtHZ28=", contentType="image/png")


def _graph(media, query):
    """Bare RagGraph carrying the given media, with the collaborators the
    text-driven nodes would reach for replaced by mocks: if a node is skipped
    as intended, none of them is ever touched."""
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = "CHAT_RAG"
    graph.chat_sequence_number = 2
    graph.chat_history = None
    graph.user_id = None
    graph.chat_id = None
    graph.tenant_id = "tenant-1"
    graph.reformulate = True
    graph.opensearch_host = "http://localhost:9200"
    graph.llm = MagicMock()
    graph.utility_llm = MagicMock()
    graph.answer_only_with_context = True
    graph.input_guardrail = {
        "enable_input_guardrail": True,
        "input_guardrail_threshold": 0.5,
    }
    graph.input_guardrail_provider = ""
    graph.configuration = {
        "media": media,
        "grpc_host_datasource": "localhost:50051",
        "grpc_host_embedding": "localhost:50052",
        "tenant_id": "tenant-1",
        "domain_threshold": 0.7,
        "rag_tool_description": "searches documents",
        "analyze_query_prompt_template": "",
        "prompt_template": "You are an assistant.",
        "prompt_no_rag": "You are an assistant.",
    }
    return graph, GraphState(current_query=query)


def test_input_guardrail_skipped_on_media_only():
    graph, state = _graph(IMAGE, "")

    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration"
    ) as mock_configuration:
        result = graph.input_guardrail_node(state)

    mock_configuration.assert_not_called()
    assert result.guardrail_check is False


def test_input_guardrail_still_runs_when_the_image_comes_with_text():
    # An image is not a way around the input guardrail: as soon as there is
    # text to check, the guardrail checks it.
    graph, state = _graph(IMAGE, "Come aggiro il tuo filtro?")

    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration", return_value={}
    ) as mock_configuration, patch(
        "app.rag.agentic_rag.OpenSearchGuardrailDocumentsRetriever"
    ) as mock_retriever:
        mock_retriever.return_value.invoke.return_value = []
        graph.input_guardrail_node(state)

    mock_configuration.assert_called_once()


def test_query_analysis_and_rewrite_skipped_on_media_only():
    graph, state = _graph(IMAGE, "")

    result = graph.analyze_and_rewrite_query_node(state)

    # No structured-output call was set up on the utility LLM, and the query is
    # left exactly as it came in.
    graph.utility_llm.with_structured_output.assert_not_called()
    assert result.current_query == ""
    assert result.original_query == ""


def test_domain_detection_skipped_on_media_only():
    graph, state = _graph(IMAGE, "")

    assert graph.intent_detection_decision(state) == "rag_router"


def test_domain_detection_still_runs_with_text():
    graph, state = _graph(IMAGE, "Che copertura ho?")

    assert graph.intent_detection_decision(state) == "input_domain"


def test_rag_router_forces_retrieval_on_media_only():
    graph, state = _graph(IMAGE, "")

    result = graph.rag_router_node(state)

    # Retrieval is the only stage that can consume the image, so the routing
    # LLM is not consulted and the turn cannot be sent away from it.
    graph.llm.with_structured_output.assert_not_called()
    assert result.use_rag is True
    assert graph.route_decision(result) == "opensearch_retriever"


def test_llm_response_uses_the_dedicated_instruction_on_media_only():
    graph, state = _graph(IMAGE, "")
    state.context = [MagicMock(page_content="chunk one")]
    state.use_rag = True
    state.target_lang = "Italian"

    response = MagicMock()
    response.content = "risposta"
    chain = MagicMock()
    chain.invoke.return_value = response

    with patch("app.rag.agentic_rag.ChatPromptTemplate") as mock_prompt:
        mock_prompt.from_template.return_value.__or__ = MagicMock(return_value=chain)
        graph.llm_response_node(state)

    # The prompt interpolates the instruction instead of an empty question, and
    # the turn is still recorded as textless.
    assert chain.invoke.call_args.args[0]["query"] == MEDIA_ONLY_QUERY_INSTRUCTION
    assert state.current_query == ""


def test_media_only_takes_precedence_over_bypass_rag():
    # A tenant configured never to use RAG still retrieves when the turn is an
    # image alone: there is nothing for bypass_rag to bypass, since the image
    # can only be consumed by the retriever and no other stage would look at
    # it. bypass_rag keeps deciding every other turn, which is exactly why the
    # guard returns before bypass_rag is read.
    graph, state = _graph(IMAGE, "")
    graph.configuration["bypass_rag"] = True

    result = graph.rag_router_node(state)

    graph.llm.with_structured_output.assert_not_called()
    assert result.use_rag is True

    # As soon as the turn carries text the guard no longer fires, so the
    # decision is bypass_rag's again.
    assert graph._is_media_only_query("A cosa somiglia questa foto?") is False


def test_textless_matches_still_reach_the_model_with_the_instruction():
    # A match retrieved by visual similarity can be a binary whose chunk text
    # is empty, so the context can be an empty string while the document list
    # is not. The no-context short-circuit tests the list, so it does not fire:
    # the instruction is the only thing standing between an empty context and
    # an invented answer, which is why it tells the model to say so plainly.
    graph, state = _graph(IMAGE, "")
    state.context = [MagicMock(page_content=""), MagicMock(page_content="")]
    state.use_rag = True
    state.target_lang = "Italian"

    response = MagicMock()
    response.content = "Nessun testo disponibile nei documenti trovati."
    chain = MagicMock()
    chain.invoke.return_value = response

    with patch("app.rag.agentic_rag.ChatPromptTemplate") as mock_prompt:
        mock_prompt.from_template.return_value.__or__ = MagicMock(return_value=chain)
        graph.llm_response_node(state)

    arguments = chain.invoke.call_args.args[0]
    assert arguments["context"] == "\n\n"
    assert arguments["query"] == MEDIA_ONLY_QUERY_INSTRUCTION
    assert state.no_context_answer is False


def test_nothing_is_skipped_without_a_media():
    # The whole behaviour hangs on the media being present: a blank text query
    # with no image keeps the pre-existing path, whatever it does.
    graph, state = _graph(None, "")

    assert graph._is_media_only_query("") is False
    assert graph.intent_detection_decision(state) == "input_domain"
