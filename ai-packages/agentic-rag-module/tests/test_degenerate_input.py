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

"""Robustness tests for degenerate chat input.

Degenerate input (empty, whitespace-only, emoji-only, zero-width, full-width,
mixed-script, ...) is chunked by the embedding service into *zero* chunks, so
``documents_embedding`` returns an empty list. Indexing that empty list
(``embedded_query[0]`` in the retrievers, ``response.content[0]`` in the LLM
response node) used to raise ``IndexError: list index out of range`` and surface
as an ``ERROR`` event on the ``/api/rag/chat`` SSE stream. These tests pin the
guards that let the pipeline proceed instead of crashing.
"""

from unittest.mock import MagicMock, patch

import pytest

from app.rag.agentic_rag import GraphState, RagGraph
from app.rag.retrievers import (
    domain_documents_retriever,
    guardrail_documents_retriever,
    uploaded_documents_retriever,
)
from app.rag.retrievers.domain_documents_retriever import (
    OpenSearchDomainDocumentsRetriever,
)
from app.rag.retrievers.guardrail_documents_retriever import (
    OpenSearchGuardrailDocumentsRetriever,
)
from app.rag.retrievers.uploaded_documents_retriever import (
    OpenSearchUploadedDocumentsRetriever,
)

# The 12 inputs reported as crashing in issue #2238: after normalization they
# carry no textual token, so the embedding service returns zero chunks.
DEGENERATE_INPUTS = [
    ("empty", ""),
    ("spaces", "     "),
    ("newlines", "\n\n\n"),
    ("tabs", "\t\t\t"),
    ("non_breaking_space", "   "),
    ("emoji", "🙂🙂🙂🙂"),
    ("emoji_zwj", "👨‍👩‍👧‍👦"),
    ("zero_width", "​​​"),
    ("emoji_long", "😀" * 800),
    ("full_width", "Ｉｇｎｏｒｅ　ｅｖｅｒｙｔｈｉｎｇ"),
    ("mixed_script", "日本語 العربية русский ελληνικά עברית ไทย 한국어"),
    ("replacement", "���"),
]


def _build_guardrail(search_text):
    return OpenSearchGuardrailDocumentsRetriever(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="localhost:50053",
        embedding_model_configuration={},
        uploaded_documents_index="guardrails-documents-index",
        retrieve_type="HYBRID",
        search_text=search_text,
    )


def _build_domain(search_text):
    return OpenSearchDomainDocumentsRetriever(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="localhost:50053",
        embedding_model_configuration={},
        uploaded_documents_index="domain-documents-index",
        retrieve_type="HYBRID",
        search_text=search_text,
    )


def _build_uploaded(search_text):
    return OpenSearchUploadedDocumentsRetriever(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="localhost:50053",
        embedding_model_configuration={},
        uploaded_documents_index="test-uploaded-documents-index",
        retrieve_type="HYBRID",
        user_id="user-1",
        chat_id="chat-1",
        search_text=search_text,
    )


RETRIEVERS = [
    (guardrail_documents_retriever, _build_guardrail),
    (domain_documents_retriever, _build_domain),
    (uploaded_documents_retriever, _build_uploaded),
]


@pytest.mark.parametrize(
    "module, factory",
    RETRIEVERS,
    ids=[module.__name__.rsplit(".", 1)[-1] for module, _ in RETRIEVERS],
)
def test_retriever_returns_empty_when_embedding_is_empty(module, factory):
    """An empty embedding must short-circuit to no documents, never IndexError."""
    retriever = factory("")
    client = MagicMock()

    with patch.object(module, "OpenSearch", return_value=client), patch.object(
        module, "documents_embedding", return_value=[]
    ):
        documents = retriever._get_relevant_documents("", run_manager=MagicMock())

    assert documents == []
    # The guard fires before any OpenSearch query is issued.
    client.search.assert_not_called()


@pytest.mark.parametrize(
    "label, search_text", DEGENERATE_INPUTS, ids=[label for label, _ in DEGENERATE_INPUTS]
)
def test_guardrail_retriever_survives_degenerate_input(label, search_text):
    """No degenerate input may crash the entry (guardrail) retriever."""
    retriever = _build_guardrail(search_text)

    with patch.object(
        guardrail_documents_retriever, "OpenSearch", return_value=MagicMock()
    ), patch.object(
        guardrail_documents_retriever, "documents_embedding", return_value=[]
    ):
        documents = retriever._get_relevant_documents(
            search_text, run_manager=MagicMock()
        )

    assert documents == []


def test_llm_response_node_handles_empty_llm_content():
    """An empty ``content`` list from the LLM must not raise IndexError."""
    rag_graph = RagGraph.__new__(RagGraph)
    rag_graph.configuration = {}
    rag_graph.llm = MagicMock()
    rag_graph._format_conversation_history = MagicMock(return_value="")

    empty_content_response = MagicMock()
    empty_content_response.content = []
    fake_chain = MagicMock()
    fake_chain.invoke.return_value = empty_content_response

    with patch("app.rag.agentic_rag.ChatPromptTemplate") as mock_chat_prompt:
        mock_chat_prompt.from_template.return_value.__or__.return_value = fake_chain
        state = rag_graph.llm_response_node(
            GraphState(current_query="🙂", use_rag=False)
        )

    assert state.response == ""
