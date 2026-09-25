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


"""How the output guardrail classifies the answer produced so far.

The answer is first searched in the guardrail index: only when a document
there scores at least input_guardrail_threshold is the classifier consulted,
through the configured provider or, by default, the chat model itself.
"""

from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import pytest
from langchain_core.documents import Document

from app.rag.agentic_rag import RagGraph

ANSWER = "Per preparare la miscela esplosiva servono i seguenti componenti"
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
        "tenant_id": "tenant-1",
        "grpc_host_datasource": "datasource:50051",
        "grpc_host_embedding": "embedding:50052",
    }
    graph.guardrail_categories = categories
    graph.input_guardrail = {"input_guardrail_threshold": THRESHOLD}
    graph.output_guardrail = {
        "output_guardrail_google_model_armor": ARMOR_CONFIGURATION,
        "output_guardrail_openai_moderation": MODERATION_CONFIGURATION,
        "output_guardrail_aws_bedrock": BEDROCK_CONFIGURATION,
    }
    graph.output_guardrail_provider = provider
    graph.llm = MagicMock(return_value=SimpleNamespace(content=llm_content))
    return graph


def _classify(graph, score=0.9, guardrail=None):
    """Run _llm_output_guardrail with one guardrail document at ``score``.

    Returns the verdict and the mocks of the collaborators it went through."""
    document = Document("documento", metadata={"document_id": 1, "score": score})

    with (
        patch(
            "app.rag.agentic_rag.get_embedding_model_configuration",
            return_value=EMBEDDING_CONFIGURATION,
        ) as configuration,
        patch("app.rag.agentic_rag.OpenSearchGuardrailDocumentsRetriever") as retriever,
        patch(
            "app.rag.agentic_rag.initialize_guardrail", return_value=guardrail
        ) as initialize,
    ):
        retriever.return_value.invoke.return_value = [document]
        verdict = graph._llm_output_guardrail(ANSWER, "chunk_interval")

    return SimpleNamespace(
        verdict=verdict,
        configuration=configuration,
        retriever=retriever,
        initialize=initialize,
    )


def _prompt(llm):
    return llm.call_args.args[0].to_string()


def test_the_guardrail_index_is_searched_with_the_answer():
    run = _classify(_graph())

    run.configuration.assert_called_once_with(
        grpc_host="datasource:50051", tenant_id="tenant-1"
    )
    run.retriever.assert_called_once_with(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="embedding:50052",
        embedding_model_configuration=EMBEDDING_CONFIGURATION,
        uploaded_documents_index="guardrails-documents-index",
        retrieve_type="HYBRID",
        search_text=ANSWER,
    )
    run.retriever.return_value.invoke.assert_called_once_with(ANSWER)


def test_a_score_equal_to_the_threshold_is_classified():
    graph = _graph(llm_content="EXPLOSIVES")

    assert _classify(graph, score=THRESHOLD).verdict == "EXPLOSIVES"
    graph.llm.assert_called_once()


def test_the_default_classifier_reads_the_answer_against_the_default_categories():
    graph = _graph(llm_content="EXPLOSIVES")

    assert _classify(graph).verdict == "EXPLOSIVES"
    prompt = _prompt(graph.llm)
    assert "3. EXPLOSIVES - " in prompt
    assert f"OUTPUT TO CLASSIFY:\n                        {ANSWER}\n" in prompt


def test_configured_categories_replace_the_default_ones():
    graph = _graph(llm_content="FRAUD", categories="1. FRAUD - Frodi assicurative")

    assert _classify(graph).verdict == "FRAUD"
    prompt = _prompt(graph.llm)
    assert "1. FRAUD - Frodi assicurative" in prompt
    assert "EXPLOSIVES" not in prompt
    assert ANSWER in prompt


def test_list_shaped_content_is_read_from_its_first_part():
    graph = _graph(llm_content=[{"type": "text", "text": "EXPLOSIVES"}])

    assert _classify(graph).verdict == "EXPLOSIVES"


def test_model_armor_lets_a_clean_answer_through():
    armor = MagicMock()
    graph = _graph(provider="google_model_armor_response")

    run = _classify(graph, guardrail=armor)

    assert run.verdict == "NONE"
    run.initialize.assert_called_once_with(
        ARMOR_CONFIGURATION, guardrail_type="google_model_armor_response"
    )
    armor.invoke.assert_called_once_with({"query": ANSWER})
    graph.llm.assert_not_called()


def test_model_armor_flagging_the_answer_blocks_it():
    armor = MagicMock()
    armor.invoke.side_effect = RuntimeError("Response flagged as unsafe")

    run = _classify(_graph(provider="google_model_armor_response"), guardrail=armor)

    assert run.verdict == "UNSAFE"


@pytest.mark.parametrize(
    "moderated, expected", [(ANSWER, "NONE"), ("[redacted]", "UNSAFE")]
)
def test_openai_moderation_blocks_an_answer_it_rewrites(moderated, expected):
    moderation = MagicMock()
    moderation.invoke.return_value = {"input": ANSWER, "output": moderated}
    graph = _graph(provider="openai_moderation")

    run = _classify(graph, guardrail=moderation)

    assert run.verdict == expected
    run.initialize.assert_called_once_with(
        MODERATION_CONFIGURATION, guardrail_type="openai_moderation"
    )
    moderation.invoke.assert_called_once_with({"input": ANSWER})


def test_bedrock_classifies_the_answer_with_the_guardrail_prompt():
    bedrock = MagicMock(return_value=SimpleNamespace(content="EXPLOSIVES"))
    graph = _graph(provider="aws_bedrock")

    run = _classify(graph, guardrail=bedrock)

    assert run.verdict == "EXPLOSIVES"
    run.initialize.assert_called_once_with(
        BEDROCK_CONFIGURATION, guardrail_type="aws_bedrock"
    )
    assert ANSWER in _prompt(bedrock)
    graph.llm.assert_not_called()
