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

"""Uploaded documents are indexed in the tenant's uploaded-documents index.

The first upload of a tenant creates the index, with a knn vector of the
embedding size, and the hybrid search pipeline it defaults to; later uploads
only add documents. Indexing failures are logged, never raised, so a failed
upload does not break the request that triggered it.
"""

from unittest.mock import MagicMock

import pytest

from app.utils import chat_history

INDEX = "tenant-1-uploaded-documents-index"
DOCUMENTS = [
    {"chunkText": "un gatto", "vector": [0.1, 0.2, 0.3]},
    {"chunkText": "un topo", "vector": [0.4, 0.5, 0.6]},
]


@pytest.fixture
def client(monkeypatch):
    client = MagicMock()
    client.bulk.return_value = {"errors": False, "items": []}
    monkeypatch.setattr(chat_history, "get_opensearch_client", lambda host: client)
    return client


def _save(documents=DOCUMENTS):
    chat_history.save_uploaded_documents(
        "http://localhost:9200", "tenant-1", documents, vector_size=3
    )


def test_first_upload_creates_the_index_and_its_pipeline(client):
    client.indices.exists.return_value = False

    _save()

    client.indices.create.assert_called_once()
    create = client.indices.create.call_args.kwargs
    assert create["index"] == INDEX
    assert create["body"]["settings"] == {"index": {"knn": True}}
    assert create["body"]["mappings"]["properties"]["vector"] == {
        "type": "knn_vector",
        "dimension": 3,
    }

    method, path = client.transport.perform_request.call_args.args
    assert (method, path) == (
        "PUT",
        f"/_search/pipeline/{chat_history.SEARCH_PIPELINE}",
    )
    client.indices.put_settings.assert_called_once_with(
        index=INDEX,
        body={"index": {"search": {"default_pipeline": chat_history.SEARCH_PIPELINE}}},
    )


def test_later_upload_only_adds_documents(client):
    client.indices.exists.return_value = True

    _save()

    client.indices.create.assert_not_called()
    client.transport.perform_request.assert_not_called()
    client.indices.put_settings.assert_not_called()
    client.bulk.assert_called_once()


def test_documents_are_bulk_indexed_in_the_tenant_index(client):
    client.indices.exists.return_value = True

    _save()

    assert client.bulk.call_args.kwargs["body"] == [
        {"index": {"_index": INDEX}},
        DOCUMENTS[0],
        {"index": {"_index": INDEX}},
        DOCUMENTS[1],
    ]


def test_no_documents_means_no_bulk(client):
    client.indices.exists.return_value = True

    _save(documents=[])

    client.bulk.assert_not_called()


def test_partial_indexing_errors_are_not_raised(client):
    client.indices.exists.return_value = True
    client.bulk.return_value = {
        "errors": True,
        "items": [{"index": {"error": {"type": "mapper_parsing_exception"}}}],
    }

    _save()


def test_bulk_failure_is_not_raised(client):
    client.indices.exists.return_value = True
    client.bulk.side_effect = ConnectionError("opensearch down")

    _save()
