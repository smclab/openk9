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

"""The chunk header of a datasource chunk reaches the model in front of its
text, once per document of the context, also when the chunk window merges the
previous and next chunks; a chunk without it is read as it is."""

from unittest.mock import patch

from app.rag.retrievers.retriever import OpenSearchRetriever

HEADER = "title: Manuale Alfa\n"


def _hit(content_id, number, chunk_text, chunk_header=None, **windows):
    source = {
        "contentId": content_id,
        "number": number,
        "chunkText": chunk_text,
        "documentTypes": [],
        **windows,
    }
    if chunk_header is not None:
        source["chunkHeader"] = chunk_header

    return {"_score": 1.0, "_source": source}


def _retrieve(hits, chunk_window=0, retrieve_type="HYBRID", context_window=100000):
    retriever = OpenSearchRetriever(
        search_text="ciao",
        chunk_window=chunk_window,
        range_values=[],
        tenant_id="a-tenant",
        context_window=context_window,
        retrieve_type=retrieve_type,
        opensearch_host="http://localhost:9200",
        grpc_host="localhost:50051",
    )

    with (
        patch("app.rag.retrievers.retriever.query_parser") as mock_query_parser,
        patch("app.rag.retrievers.retriever.get_opensearch_client") as mock_get_client,
    ):
        mock_query_parser.return_value = {
            "query": "{}",
            "index_name": ["an-index"],
            "query_parameters": {},
        }
        mock_get_client.return_value.search.return_value = {"hits": {"hits": hits}}

        return retriever.invoke("ciao")


def test_the_header_goes_in_front_of_the_chunk_text():
    documents = _retrieve(
        [
            _hit("doc-1", 1, "uguale", HEADER),
            _hit("doc-2", 1, "uguale", "title: Manuale Beta\n"),
        ]
    )

    assert [document.page_content for document in documents] == [
        "title: Manuale Alfa\nuguale",
        "title: Manuale Beta\nuguale",
    ]
    assert "chunk_header" not in documents[0].metadata


def test_the_window_takes_the_header_once_before_previous_and_next():
    documents = _retrieve(
        [
            _hit(
                "doc-1",
                2,
                "content",
                HEADER,
                previous=[{"chunkText": "prev-1"}],
                next=[{"chunkText": "next-1"}],
            ),
            _hit("doc-1", 4, "other", HEADER, previous=[{"chunkText": "next-1"}]),
        ],
        chunk_window=1,
    )

    assert [document.page_content for document in documents] == [
        "title: Manuale Alfa\nprev-1contentnext-1other"
    ]
    assert "chunk_header" not in documents[0].metadata


def test_a_chunk_without_the_header_is_read_as_it_is():
    documents = _retrieve(
        [
            _hit("doc-1", 1, "content"),
            _hit("doc-2", 1, "content", ""),
            _hit("doc-3", 1, "content", None, next=[{"chunkText": "next-1"}]),
        ],
        chunk_window=1,
    )

    assert sorted(document.page_content for document in documents) == [
        "content",
        "content",
        "contentnext-1",
    ]


def test_a_chunk_window_off_keeps_the_per_chunk_metadata():
    documents = _retrieve([_hit("doc-1", 3, "content", HEADER)])

    assert documents[0].metadata["chunk_idx"] == 3
    assert documents[0].page_content == HEADER + "content"


def test_the_header_counts_in_the_context_budget():
    # context window 10 -> budget 8.5 tokens: 14 characters of header and 14 of
    # text are 8 tokens, the second hit no longer fits
    documents = _retrieve(
        [
            _hit("doc-1", 1, "a" * 14, "h" * 14),
            _hit("doc-2", 1, "b" * 7),
        ],
        context_window=10,
    )

    assert [document.metadata["document_id"] for document in documents] == ["doc-1"]
