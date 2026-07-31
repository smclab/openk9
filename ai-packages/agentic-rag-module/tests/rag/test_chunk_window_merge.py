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


from app.rag.chunk_window import get_context_window_merged


def _chunk(
    document_id, chunk_idx, content, prev_texts=None, next_texts=None, metadata=None
):
    """A retrieved chunk as the retriever hands it to the merge: prev and next
    are None when the chunk has no neighbours on that side."""
    return {
        "document_id": document_id,
        "chunk_idx": chunk_idx,
        "prev": prev_texts,
        "next": next_texts,
        "metadata": metadata if metadata is not None else {"document_id": document_id},
        "content": content,
    }


def test_first_chunk_of_page_enlarges_forward_only():
    # prev=None (first chunk of a multi-chunk page): context grows forward only
    chunks = [_chunk("doc-1", 0, "content", next_texts=["next-1", "next-2"])]

    documents = get_context_window_merged(chunks, window_size=2)

    assert len(documents) == 1
    assert documents[0]["content"] == "contentnext-1next-2"


def test_last_chunk_of_page_enlarges_backward_only():
    # next=None (last chunk of a page): context grows backward only
    chunks = [_chunk("doc-1", 5, "content", prev_texts=["prev-1", "prev-2"])]

    documents = get_context_window_merged(chunks, window_size=2)

    assert len(documents) == 1
    assert documents[0]["content"] == "prev-1prev-2content"


def test_single_chunk_page_uses_only_content():
    # prev=None and next=None (single-chunk page): nothing to enlarge
    chunks = [_chunk("doc-1", 0, "content")]

    documents = get_context_window_merged(chunks, window_size=2)

    assert len(documents) == 1
    assert documents[0]["content"] == "content"


def test_chunk_without_neighbour_keys_uses_only_content():
    # a chunk carrying no prev/next keys at all is treated as having no neighbours
    chunks = [
        {
            "document_id": "doc-1",
            "chunk_idx": 0,
            "metadata": {"document_id": "doc-1"},
            "content": "content",
        }
    ]

    documents = get_context_window_merged(chunks, window_size=2)

    assert len(documents) == 1
    assert documents[0]["content"] == "content"


def test_single_chunk_document_does_not_borrow_context_from_another_document():
    # the end context of a single-chunk group must come from that group, not
    # from the last chunk seen in the whole result set
    chunks = [
        _chunk("doc-1", 0, "first-doc-content"),
        _chunk("doc-2", 0, "second-doc-content", next_texts=["leaked"]),
    ]

    documents = get_context_window_merged(chunks, window_size=2)

    by_document = {
        document["metadata"]["document_id"]: document for document in documents
    }
    assert by_document["doc-1"]["content"] == "first-doc-content"
    assert by_document["doc-2"]["content"] == "second-doc-contentleaked"


def test_adjacent_chunks_of_same_page_are_merged_without_duplication():
    # consecutive chunks: no gap to fill between them, each text appears once
    chunks = [
        _chunk(
            "doc-1",
            1,
            "content-1",
            prev_texts=["chunk-0"],
            next_texts=["content-2", "chunk-3"],
        ),
        _chunk(
            "doc-1",
            2,
            "content-2",
            prev_texts=["chunk-0", "content-1"],
            next_texts=["chunk-3"],
        ),
    ]

    documents = get_context_window_merged(chunks, window_size=2)

    assert len(documents) == 1
    assert documents[0]["content"] == "chunk-0content-1content-2chunk-3"


def test_distant_chunks_of_same_page_fill_the_gap_from_both_sides():
    # window_size < gap <= 2 * window_size: the hole is filled with the next of
    # the earlier chunk and the prev of the later one
    chunks = [
        _chunk("doc-1", 0, "content-0", next_texts=["chunk-1", "chunk-2"]),
        _chunk("doc-1", 4, "content-4", prev_texts=["chunk-2", "chunk-3"]),
    ]

    documents = get_context_window_merged(chunks, window_size=2)

    assert len(documents) == 1
    assert documents[0]["content"] == "content-0chunk-1chunk-2chunk-3content-4"


def test_chunks_farther_than_the_window_become_separate_documents():
    # gap > 2 * window_size: the context is closed and a new one is started
    chunks = [
        _chunk("doc-1", 0, "content-0", next_texts=["chunk-1"]),
        _chunk("doc-1", 10, "content-10", prev_texts=["chunk-9"]),
    ]

    documents = get_context_window_merged(chunks, window_size=2)

    assert len(documents) == 2
    assert documents[0]["content"] == "content-0chunk-1"
    assert documents[1]["content"] == "chunk-9content-10"


def test_document_metadata_is_carried_over_to_the_merged_document():
    metadata = {
        "document_id": "doc-1",
        "score": 1.5,
        "title": "a title",
        "url": "http://example.org",
        "domain": "a domain",
    }
    chunks = [_chunk("doc-1", 0, "content", metadata=metadata)]

    documents = get_context_window_merged(chunks, window_size=2)

    assert documents[0]["metadata"] == metadata
