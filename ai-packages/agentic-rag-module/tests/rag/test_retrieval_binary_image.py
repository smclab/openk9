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

"""The retriever tells the response node which chunks carry an image the
generation model can read: an image binary with a URL at its source. Every
other chunk stays text, and the DOCUMENT events do not change."""

import json
from unittest.mock import MagicMock, patch

from app.rag.agentic_rag import RagGraph
from app.rag.retrievers import retriever as retriever_module
from app.rag.retrievers.retriever import OpenSearchRetriever

PHOTO_URL = "https://photos.example.com/retro.jpg"


def _retrieve(sources, chunk_window=0):
    client = MagicMock()
    client.search.return_value = {
        "hits": {"hits": [{"_source": source, "_score": 1.0} for source in sources]}
    }
    query_data = {"query": b"{}", "index_name": ["idx"], "query_parameters": {}}
    retriever = OpenSearchRetriever(
        search_text="retro",
        range_values=[0, 5],
        tenant_id="tenant-1",
        context_window=100_000,
        retrieve_type="HYBRID",
        chunk_window=chunk_window,
        opensearch_host="http://localhost:9200",
        grpc_host="localhost:50051",
    )

    with patch.object(
        retriever_module, "get_opensearch_client", return_value=client
    ), patch.object(retriever_module, "query_parser", return_value=query_data):
        return retriever.invoke("retro")


def _image_chunk(**binary):
    return {
        "contentId": "doc-1",
        "chunkText": "Foto del retro",
        "number": 1,
        "binary": {"id": "b-1", "name": "retro.jpg", **binary},
    }


def test_image_chunk_with_source_url_carries_it():
    [document] = _retrieve(
        [_image_chunk(contentType="image/jpeg", sourceUrl=PHOTO_URL)]
    )

    assert document.metadata["image_source_url"] == PHOTO_URL
    assert document.metadata["image_name"] == "retro.jpg"
    assert document.page_content == "Foto del retro"


def test_image_chunk_without_source_url_stays_text():
    [document] = _retrieve([_image_chunk(contentType="image/jpeg")])

    assert "image_source_url" not in document.metadata


def test_non_image_binary_stays_text():
    # a PDF page is a binary too, but the model only gets image files
    [document] = _retrieve(
        [_image_chunk(contentType="application/pdf", sourceUrl=PHOTO_URL)]
    )

    assert "image_source_url" not in document.metadata


def test_text_chunk_stays_text():
    [document] = _retrieve([{"contentId": "doc-1", "chunkText": "Testo", "number": 1}])

    assert "image_source_url" not in document.metadata


def test_chunk_window_keeps_the_image_url():
    [document] = _retrieve(
        [_image_chunk(contentType="image/png", sourceUrl=PHOTO_URL)],
        chunk_window=1,
    )

    assert document.metadata["image_source_url"] == PHOTO_URL


def test_document_events_do_not_carry_the_image_url():
    documents = _retrieve([_image_chunk(contentType="image/jpeg", sourceUrl=PHOTO_URL)])
    graph = RagGraph.__new__(RagGraph)

    events = [json.loads(event) for event in graph._stream_documents(documents)]

    assert events == [
        {"chunk": {"citations": [], "score": 1.0, "domain": None}, "type": "DOCUMENT"}
    ]
