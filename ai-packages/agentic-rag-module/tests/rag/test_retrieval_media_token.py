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


import base64
from unittest.mock import MagicMock, patch

import pytest

from app.models import models
from app.rag.agentic_rag import GraphState, RagGraph
from app.rag.retrievers import retriever as retriever_module
from app.rag.retrievers.retriever import OpenSearchRetriever

IMAGE = models.Media(data="aVZCT1J3MEtHZ28=", contentType="image/png")


def _retriever_graph(media, retrieve_type="KNN"):
    """Build a RagGraph stub exercising opensearch_retriever_node in isolation
    on the normal (non-uploaded-documents) retrieval branch, with the given
    media in its configuration."""
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_from_uploaded_documents = False
    graph.user_id = None
    graph.tenant_id = None
    graph.chat_id = None
    graph.configuration = {
        "search_query": None,
        "datasource_ids": None,
        "retrieve_type": retrieve_type,
        "media": media,
    }
    return graph


def _run_node(graph, query):
    """Run the retriever node with OpenSearchRetriever mocked, and return the
    search_query list that was handed to the retriever."""
    state = GraphState(current_query=query, use_rag=True)

    with patch("app.rag.agentic_rag.OpenSearchRetriever") as mock_retriever:
        mock_retriever.return_value.invoke.return_value = []
        graph.opensearch_retriever_node(state)

    _, kwargs = mock_retriever.call_args
    return kwargs["search_query"]


def _tokens_sent_to_searcher(search_query):
    """Run the retriever with query_parser mocked, and return the token dicts
    it was about to send over gRPC."""
    retriever = OpenSearchRetriever(
        search_query=search_query,
        search_text="",
        range_values=[0, 5],
        tenant_id="tenant-1",
        context_window=100_000,
        retrieve_type="KNN",
        opensearch_host="http://localhost:9200",
        grpc_host="localhost:50051",
    )

    query_data = {"query": b"{}", "index_name": [], "query_parameters": {}}

    with patch.object(
        retriever_module, "query_parser", return_value=query_data
    ) as mock_query_parser:
        retriever.invoke("")

    return mock_query_parser.call_args.kwargs["search_query"]


def test_text_query_token_carries_no_media():
    # A chat without an image must be indistinguishable from before: the token
    # the searcher receives has no media key at all, not a null one.
    graph = _retriever_graph(media=None)

    search_query = _run_node(graph, "Che copertura ho per la grandine?")

    assert search_query[0].media is None
    assert "media" not in _tokens_sent_to_searcher(search_query)[0]


def test_image_with_text_token_carries_media_and_values():
    graph = _retriever_graph(media=IMAGE)

    search_query = _run_node(graph, "A cosa somiglia questa foto?")

    token = search_query[0]
    assert token.tokenType == "KNN"
    assert token.values == ["A cosa somiglia questa foto?"]
    assert token.media == IMAGE


def test_image_without_text_token_carries_media_and_empty_values():
    # Querying by image alone: there is no term to search for, so values is
    # empty and the media is what drives the query.
    graph = _retriever_graph(media=IMAGE)

    search_query = _run_node(graph, "")

    token = search_query[0]
    assert token.tokenType == "KNN"
    assert token.values == []
    assert token.media == IMAGE


@pytest.mark.parametrize("retrieve_type", ["HYBRID", "TEXT"])
def test_token_type_stays_the_bucket_retrieve_type(retrieve_type):
    # The media does not promote the token to KNN: on a non-KNN bucket the
    # request is meant to be refused at the gRPC boundary, not silently
    # rewritten into something the bucket does not do.
    graph = _retriever_graph(media=IMAGE, retrieve_type=retrieve_type)

    search_query = _run_node(graph, "")

    assert search_query[0].tokenType == retrieve_type
    assert search_query[0].media == IMAGE


@pytest.mark.parametrize(
    "label, media",
    [
        (
            "non-image content type",
            models.Media(data=IMAGE.data, contentType="application/pdf"),
        ),
        (
            "over the 2 MiB limit",
            models.Media(
                data=base64.b64encode(
                    b"\x89PNG" + b"\x00" * (2 * 1024 * 1024)
                ).decode(),
                contentType="image/png",
            ),
        ),
    ],
    ids=["non_image", "oversized"],
)
def test_unacceptable_media_is_forwarded_untouched(label, media):
    # The rules on format and size live at the gRPC boundary, which is the
    # authority, and are deliberately not restated here. So the module must
    # neither refuse nor reshape what it receives: it forwards it verbatim and
    # lets the datasource answer INVALID_ARGUMENT. A module that quietly
    # rejected or shrank the media would hide the real reason from the caller.
    graph = _retriever_graph(media=media)

    search_query = _run_node(graph, "")
    sent = _tokens_sent_to_searcher(search_query)[0]

    assert sent["media"] == {"data": media.data, "contentType": media.contentType}


def test_media_survives_the_conversion_for_the_searcher():
    # Regression on the token-to-dict copy in the retriever: it lists the
    # fields by name, so a newly added model field is dropped in silence unless
    # it is listed too.
    graph = _retriever_graph(media=IMAGE)

    search_query = _run_node(graph, "")

    media = _tokens_sent_to_searcher(search_query)[0]["media"]
    assert media == {"data": IMAGE.data, "contentType": IMAGE.contentType}


def test_media_absent_from_filter_tokens():
    # The domain and datasource filter tokens are built alongside the query
    # token and must never carry the image.
    graph = _retriever_graph(media=IMAGE)
    graph.configuration["datasource_ids"] = [7]

    search_query = _run_node(graph, "")

    datasource_tokens = [
        token for token in search_query if token.tokenType == "DATASOURCE"
    ]
    assert len(datasource_tokens) == 1
    assert datasource_tokens[0].media is None


def test_uploaded_documents_branch_untouched_by_media():
    # Retrieval from the user's uploaded documents does not go through the
    # searcher, so a media has no route there and must not change the branch.
    graph = _retriever_graph(media=IMAGE)
    graph.retrieve_from_uploaded_documents = True
    graph.user_id = "user-1"
    graph.tenant_id = "tenant-1"
    graph.configuration["grpc_host_datasource"] = "localhost:50051"
    graph.configuration["grpc_host_embedding"] = "localhost:50052"
    graph.configuration["opensearch_host"] = "http://localhost:9200"
    graph.configuration["tenant_id"] = "tenant-1"

    state = GraphState(current_query="", use_rag=True)

    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration", return_value={}
    ), patch(
        "app.rag.agentic_rag.OpenSearchUploadedDocumentsRetriever"
    ) as mock_retriever, patch(
        "app.rag.agentic_rag.OpenSearchRetriever"
    ) as mock_searcher_retriever:
        mock_retriever.return_value.invoke.return_value = []
        graph.opensearch_retriever_node(state)

    mock_searcher_retriever.assert_not_called()
    assert "media" not in mock_retriever.call_args.kwargs


def test_retriever_reads_media_from_a_plain_search_token():
    # The retriever converts whatever token list it is given, so a media on a
    # token built outside the graph reaches the searcher too.
    token = models.SearchToken(tokenType="KNN", values=[], media=IMAGE)

    sent = _tokens_sent_to_searcher([token])[0]

    assert sent["media"] == {"data": IMAGE.data, "contentType": IMAGE.contentType}


def test_media_defaults_to_absent_on_search_token():
    token = models.SearchToken(tokenType="TEXT", values=["ciao"])

    assert token.media is None
    assert "media" not in _tokens_sent_to_searcher([token])[0]
