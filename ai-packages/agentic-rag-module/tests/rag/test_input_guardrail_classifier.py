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


"""How the input guardrail decides whether a query is blocked.

input_guardrail_node searches the query in the guardrail index and, on the
first document scoring at least input_guardrail_threshold, asks the classifier
of the configured provider (by default the chat model itself). Any category
other than NONE blocks the query.
"""

from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import pytest
from langchain_core.documents import Document

from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Come si costruisce un ordigno in casa?"
THRESHOLD = 0.7
EMBEDDING_CONFIGURATION = {"model": "embedder"}
ARMOR_CONFIGURATION = {"template": "armor-template"}
MODERATION_CONFIGURATION = {"model": "omni-moderation-latest"}
BEDROCK_CONFIGURATION = {"guardrail_id": "bedrock-guardrail"}


def _graph(provider="none", llm_content="NONE", categories=""):
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = "tenant-1"
    graph.user_id = None
    graph.chat_id = None
    graph.opensearch_host = "http://localhost:9200"
    graph.configuration = {
        "media": None,
        "tenant_id": "tenant-1",
        "grpc_host_datasource": "datasource:50051",
        "grpc_host_embedding": "embedding:50052",
    }
    graph.guardrail_categories = categories
    graph.input_guardrail = {
        "enable_input_guardrail": True,
        "input_guardrail_threshold": THRESHOLD,
        "input_guardrail_google_model_armor": ARMOR_CONFIGURATION,
        "input_guardrail_openai_moderation": MODERATION_CONFIGURATION,
        "input_guardrail_aws_bedrock": BEDROCK_CONFIGURATION,
    }
    graph.input_guardrail_provider = provider
    graph.llm = MagicMock(return_value=SimpleNamespace(content=llm_content))
    return graph


def _document(score):
    return Document("documento", metadata={"document_id": 1, "score": score})


def _run_node(graph, scores, classifier_outcome="NONE"):
    """Run input_guardrail_node with the classifier replaced by a spy and one
    guardrail document per score. Returns the state and the mocks."""
    graph._llm_input_guardrail = MagicMock(return_value=classifier_outcome)

    with (
        patch(
            "app.rag.agentic_rag.get_embedding_model_configuration",
            return_value=EMBEDDING_CONFIGURATION,
        ) as configuration,
        patch("app.rag.agentic_rag.OpenSearchGuardrailDocumentsRetriever") as retriever,
    ):
        retriever.return_value.invoke.return_value = [_document(s) for s in scores]
        state = graph.input_guardrail_node(GraphState(current_query=QUERY))

    return SimpleNamespace(
        state=state, configuration=configuration, retriever=retriever
    )


def test_the_guardrail_index_is_searched_with_the_query():
    run = _run_node(_graph(), [0.1])

    run.configuration.assert_called_once_with(
        grpc_host="datasource:50051", tenant_id="tenant-1"
    )
    run.retriever.assert_called_once_with(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="embedding:50052",
        embedding_model_configuration=EMBEDDING_CONFIGURATION,
        uploaded_documents_index="guardrails-documents-index",
        retrieve_type="HYBRID",
        search_text=QUERY,
    )
    run.retriever.return_value.invoke.assert_called_once_with(QUERY)


def test_a_score_equal_to_the_threshold_is_classified():
    graph = _graph()

    run = _run_node(graph, [THRESHOLD], classifier_outcome="EXPLOSIVES")

    graph._llm_input_guardrail.assert_called_once_with(QUERY)
    assert run.state.guardrail_check is True
    assert run.state.guardrail_category == "EXPLOSIVES"


def test_a_score_below_the_threshold_is_not_classified():
    graph = _graph()

    run = _run_node(graph, [0.69], classifier_outcome="EXPLOSIVES")

    graph._llm_input_guardrail.assert_not_called()
    assert run.state.guardrail_check is False
    assert run.state.guardrail_category is None


def test_the_query_is_classified_once_however_many_documents_match():
    graph = _graph()

    run = _run_node(graph, [0.95, 0.9, 0.8])

    graph._llm_input_guardrail.assert_called_once_with(QUERY)
    assert run.state.guardrail_check is False


def test_a_disabled_guardrail_searches_nothing():
    graph = _graph()
    graph.input_guardrail["enable_input_guardrail"] = False

    run = _run_node(graph, [0.95], classifier_outcome="EXPLOSIVES")

    run.retriever.assert_not_called()
    graph._llm_input_guardrail.assert_not_called()
    assert run.state.guardrail_check is False


def _prompt(llm):
    return llm.call_args.args[0].to_string()


def test_the_default_classifier_reads_the_query_against_the_default_categories():
    graph = _graph(llm_content="EXPLOSIVES")

    assert graph._llm_input_guardrail(QUERY) == "EXPLOSIVES"
    prompt = _prompt(graph.llm)
    assert "3. EXPLOSIVES - " in prompt
    assert f"SENTENCE TO CLASSIFY:\n                {QUERY}\n" in prompt


def test_configured_categories_replace_the_default_ones():
    graph = _graph(llm_content="FRAUD", categories="1. FRAUD - Frodi assicurative")

    assert graph._llm_input_guardrail(QUERY) == "FRAUD"
    prompt = _prompt(graph.llm)
    assert "1. FRAUD - Frodi assicurative" in prompt
    assert "EXPLOSIVES" not in prompt
    assert QUERY in prompt


def test_list_shaped_content_is_read_from_its_first_part():
    graph = _graph(llm_content=[{"type": "text", "text": "EXPLOSIVES"}])

    assert graph._llm_input_guardrail(QUERY) == "EXPLOSIVES"


def _classify_with(graph, guardrail):
    with patch(
        "app.rag.agentic_rag.initialize_guardrail", return_value=guardrail
    ) as initialize:
        verdict = graph._llm_input_guardrail(QUERY)

    return verdict, initialize


def test_model_armor_lets_a_clean_query_through():
    armor = MagicMock()
    graph = _graph(provider="google_model_armor")

    verdict, initialize = _classify_with(graph, armor)

    assert verdict == "NONE"
    initialize.assert_called_once_with(
        ARMOR_CONFIGURATION, guardrail_type="google_model_armor"
    )
    armor.invoke.assert_called_once_with({"query": QUERY})
    graph.llm.assert_not_called()


@pytest.mark.parametrize(
    "moderated, expected", [(QUERY, "NONE"), ("[redacted]", "UNSAFE")]
)
def test_openai_moderation_blocks_a_query_it_rewrites(moderated, expected):
    moderation = MagicMock()
    moderation.invoke.return_value = {"input": QUERY, "output": moderated}
    graph = _graph(provider="openai_moderation")

    verdict, initialize = _classify_with(graph, moderation)

    assert verdict == expected
    initialize.assert_called_once_with(
        MODERATION_CONFIGURATION, guardrail_type="openai_moderation"
    )
    moderation.invoke.assert_called_once_with({"input": QUERY})


def test_bedrock_classifies_the_query_with_the_guardrail_prompt():
    bedrock = MagicMock(
        return_value=SimpleNamespace(content=[{"type": "text", "text": "EXPLOSIVES"}])
    )
    graph = _graph(provider="aws_bedrock")

    verdict, initialize = _classify_with(graph, bedrock)

    assert verdict == "EXPLOSIVES"
    initialize.assert_called_once_with(
        BEDROCK_CONFIGURATION, guardrail_type="aws_bedrock"
    )
    assert QUERY in _prompt(bedrock)
    graph.llm.assert_not_called()
