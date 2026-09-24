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

"""An uploaded document is sent to the embedding module as one request.

The chunking is fixed for uploaded documents, character chunks of 2000, and
the embedding model is mapped from the searcher configuration onto the gRPC
message. The embedded chunks come back unchanged, ready to be indexed.
"""

from unittest.mock import patch

from app.utils import embedding

CONFIGURATION = {
    "tenant_id": "mew",
    "api_url": "http://embedding.local",
    "api_key": "secret",
    "model_type": "openai",
    "model": "text-embedding-3-small",
    "vector_size": 1536,
    "json_config": {"dimensions": 1536},
}
DOCUMENT = {"text": "un gatto e un topo", "filename": "gatti", "file_extension": ".md"}


def _documents_embedding(returned=None):
    with patch.object(
        embedding, "generate_documents_embeddings", return_value=returned
    ) as generate:
        result = embedding.documents_embedding(
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration=CONFIGURATION,
            document=DOCUMENT,
        )
    return result, generate.call_args.args


def test_chunking_is_fixed_for_uploaded_documents():
    _, (_, _, chunk, _, _) = _documents_embedding()

    assert chunk["type"] == 1
    assert dict(chunk["jsonConfig"]) == {"size": 2000}


def test_embedding_model_is_mapped_from_the_configuration():
    _, (_, _, _, embedding_model, _) = _documents_embedding()

    assert embedding_model == {
        "apiKey": "secret",
        "providerModel": {"provider": "openai", "model": "text-embedding-3-small"},
        "jsonConfig": {"dimensions": 1536},
        "apiUrl": "http://embedding.local",
        "multimodal": False,
    }


def test_host_and_document_are_forwarded():
    _, (host, _, _, _, document) = _documents_embedding()

    assert host == "localhost:50053"
    assert document is DOCUMENT


def test_embedded_chunks_are_returned_unchanged():
    chunks = [{"chunkText": "un gatto", "vector": [0.1, 0.2]}]

    result, _ = _documents_embedding(returned=chunks)

    assert result is chunks
