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


from unittest.mock import patch

from app.rag.retrievers.retriever import OpenSearchRetriever


def _hit(
    content_id,
    number,
    chunk_text,
    previous_chunks=None,
    next_chunks=None,
    extra_source=None,
):
    """An OpenSearch hit for a chunk: previous and next are missing from the
    source when the chunk has no neighbours on that side."""
    source = {
        "contentId": content_id,
        "number": number,
        "chunkText": chunk_text,
        "domain": "a domain",
        "documentTypes": ["web"],
    }
    if previous_chunks is not None:
        source["previous"] = previous_chunks
    if next_chunks is not None:
        source["next"] = next_chunks
    if extra_source:
        source.update(extra_source)

    return {"_score": 1.0, "_source": source}


def _retrieve(hits, chunk_window, metadata=None, retrieve_type="HYBRID"):
    """Run the retriever against the given OpenSearch hits and return the
    documents it produces."""
    retriever = OpenSearchRetriever(
        search_text="ciao",
        chunk_window=chunk_window,
        range_values=[],
        tenant_id="a-tenant",
        metadata=metadata,
        context_window=100000,
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


def test_chunk_without_neighbours_is_returned_when_window_is_enabled():
    # a single-chunk page has no previous/next in the source: the merge must
    # still return the document instead of failing and emptying the retrieval
    documents = _retrieve([_hit("doc-1", 0, "content")], chunk_window=2)

    assert len(documents) == 1
    assert documents[0].page_content == "content"


def test_document_without_title_and_url_is_returned_when_window_is_enabled():
    # title and url only come from the tenant metadata mapping: a document
    # without them must survive the merge
    documents = _retrieve(
        [_hit("doc-1", 0, "content", next_chunks=[{"chunkText": "next-1"}])],
        chunk_window=2,
        metadata={"other-document-type": {"title": "title"}},
    )

    assert len(documents) == 1
    assert documents[0].page_content == "contentnext-1"
    assert "title" not in documents[0].metadata


def test_merged_document_keeps_domain_and_mapped_metadata():
    documents = _retrieve(
        [_hit("doc-1", 0, "content", extra_source={"web": {"webTitle": "a title"}})],
        chunk_window=2,
        metadata={"web": {"title": "webTitle"}},
    )

    assert len(documents) == 1
    assert documents[0].metadata["document_id"] == "doc-1"
    assert documents[0].metadata["score"] == 1.0
    assert documents[0].metadata["domain"] == "a domain"
    assert documents[0].metadata["title"] == "a title"


def test_merged_document_drops_the_per_chunk_metadata():
    documents = _retrieve([_hit("doc-1", 0, "content")], chunk_window=2)

    assert "chunk_idx" not in documents[0].metadata
    assert "prev" not in documents[0].metadata
    assert "next" not in documents[0].metadata


def test_textual_retrieve_is_not_merged_even_when_the_window_is_enabled():
    # a textual retrieve carries no per-chunk metadata: the window must be
    # ignored instead of failing on the missing keys
    documents = _retrieve(
        [_hit("doc-1", 0, "content", extra_source={"rawContent": "raw content"})],
        chunk_window=2,
        retrieve_type="TEXT",
    )

    assert len(documents) == 1
    assert documents[0].page_content == "raw content"


def test_chunks_are_not_merged_when_the_window_is_disabled():
    # regression: with chunk_window=0 each chunk stays a document of its own,
    # carrying its per-chunk metadata
    documents = _retrieve(
        [
            _hit("doc-1", 0, "content-0", next_chunks=[{"chunkText": "content-1"}]),
            _hit("doc-1", 1, "content-1", previous_chunks=[{"chunkText": "content-0"}]),
        ],
        chunk_window=0,
    )

    assert [document.page_content for document in documents] == [
        "content-0",
        "content-1",
    ]
    assert documents[0].metadata["chunk_idx"] == 0
    assert documents[0].metadata["next"] == ["content-1"]
    assert documents[0].metadata["prev"] == []
