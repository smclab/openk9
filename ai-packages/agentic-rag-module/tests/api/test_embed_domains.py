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


"""Domain documents are embedded and indexed behind the admin credentials.

Each chunk carries the domain of the document it comes from, so the domain
retriever can classify a query by its nearest chunks.
"""

import logging
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

ADMIN = ("admin", "s3cret")
HEADERS = {"x-tenant-id": "tenant-1"}
INDEX = "domain-documents-index"
HYBRID_SEARCH_PIPELINE = {
    "description": "Post processor for hybrid search",
    "phase_results_processors": [
        {
            "normalization-processor": {
                "normalization": {"technique": "min_max"},
                "combination": {
                    "technique": "arithmetic_mean",
                    "parameters": {"weights": [0.5, 0.5]},
                },
            }
        }
    ],
}
EMBEDDING_MODEL_CONFIGURATION = {"vector_size": 4, "model_name": "embedder"}


def _chunk(text):
    return {"chunkText": text, "vector": [0.1, 0.2, 0.3, 0.4]}


@pytest.fixture
def embedding(monkeypatch):
    monkeypatch.setattr(server, "OPENK9_SECURITY_ADMIN_PASSWORD", "s3cret")
    documents_embedding = MagicMock(
        # Two chunks per document, to prove every chunk gets the domain.
        side_effect=lambda **kwargs: [
            _chunk(kwargs["document"]["text"]),
            _chunk(kwargs["document"]["text"]),
        ]
    )
    save_domains_documents = MagicMock(return_value="Successfully indexed 4 documents")
    get_embedding_model_configuration = MagicMock(
        return_value=EMBEDDING_MODEL_CONFIGURATION
    )
    monkeypatch.setattr(
        server, "get_embedding_model_configuration", get_embedding_model_configuration
    )
    monkeypatch.setattr(server, "documents_embedding", documents_embedding)
    monkeypatch.setattr(server, "save_domains_documents", save_domains_documents)
    return (
        documents_embedding,
        save_domains_documents,
        get_embedding_model_configuration,
    )


def _opensearch_mock(index_exists):
    client = MagicMock()
    client.indices.exists.return_value = index_exists
    client.bulk.return_value = {"errors": False, "items": []}
    return client


def test_embed_domains_tags_every_chunk_with_its_domain(embedding):
    documents_embedding, save, get_configuration = embedding

    response = TestClient(server.app).post(
        "/api/rag/embed-domains",
        json=[
            {"text": "polizza auto", "domain": "assicurazioni"},
            {"text": "ricetta della carbonara"},
        ],
        headers=HEADERS,
        auth=ADMIN,
    )

    assert response.status_code == 200
    assert response.json() == "Successfully indexed 4 documents"
    get_configuration.assert_called_once_with(
        grpc_host=server.GRPC_DATASOURCE_HOST, tenant_id="tenant-1"
    )
    assert [c.kwargs["document"] for c in documents_embedding.call_args_list] == [
        {"text": "polizza auto"},
        {"text": "ricetta della carbonara"},
    ]
    host, chunks, vector_size = save.call_args.args
    assert host == server.OPENSEARCH_HOST
    assert vector_size == 4
    assert [(c["chunkText"], c["domain"]) for c in chunks] == [
        ("polizza auto", "assicurazioni"),
        ("polizza auto", "assicurazioni"),
        ("ricetta della carbonara", "unknown"),
        ("ricetta della carbonara", "unknown"),
    ]


@pytest.mark.parametrize(
    "auth",
    [("admin", "wrong"), ("root", "s3cret"), None],
    ids=["wrong_password", "wrong_username", "no_credentials"],
)
def test_embed_domains_refuses_anyone_but_the_admin(auth, embedding):
    documents_embedding, save, _ = embedding

    response = TestClient(server.app).post(
        "/api/rag/embed-domains",
        json=[{"text": "testo", "domain": "d"}],
        headers=HEADERS,
        auth=auth,
    )

    assert response.status_code == 401
    assert response.headers["www-authenticate"] == "Basic"
    documents_embedding.assert_not_called()
    save.assert_not_called()


def test_embed_domains_without_tenant_is_refused(embedding):
    documents_embedding, _, _ = embedding

    response = TestClient(server.app).post(
        "/api/rag/embed-domains", json=[{"text": "testo"}], auth=ADMIN
    )

    assert response.status_code == 400
    assert response.json()["detail"] == "Missing x_tenant_id header."
    documents_embedding.assert_not_called()


def test_save_domains_creates_the_index_with_the_vector_size(monkeypatch):
    open_search_client = _opensearch_mock(index_exists=False)
    get_opensearch_client = MagicMock(return_value=open_search_client)
    monkeypatch.setattr(server, "get_opensearch_client", get_opensearch_client)

    server.save_domains_documents("http://opensearch", [_chunk("a")], 4)

    create = open_search_client.indices.create.call_args.kwargs
    assert create["index"] == INDEX
    assert create["body"]["settings"] == {"index": {"knn": True}}
    assert create["body"]["mappings"]["properties"] == {
        "timestamp": {"type": "date"},
        "chunkText": {"type": "text"},
        "domain": {"type": "keyword"},
        "vector": {"type": "knn_vector", "dimension": 4},
    }
    get_opensearch_client.assert_called_once_with("http://opensearch")
    open_search_client.indices.exists.assert_called_once_with(index=INDEX)
    open_search_client.transport.perform_request.assert_called_once_with(
        "PUT",
        f"/_search/pipeline/{server.SEARCH_PIPELINE}",
        body=HYBRID_SEARCH_PIPELINE,
    )
    open_search_client.indices.put_settings.assert_called_once_with(
        index=INDEX,
        body={"index": {"search": {"default_pipeline": server.SEARCH_PIPELINE}}},
    )


def test_save_domains_reuses_an_existing_index(monkeypatch):
    open_search_client = _opensearch_mock(index_exists=True)
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    server.save_domains_documents("http://opensearch", [_chunk("a")], 4)

    open_search_client.indices.create.assert_not_called()
    open_search_client.transport.perform_request.assert_not_called()


def test_save_domains_bulk_indexes_every_document(monkeypatch):
    open_search_client = _opensearch_mock(index_exists=True)
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    result = server.save_domains_documents(
        "http://opensearch", [_chunk("a"), _chunk("b")], 4
    )

    assert result == "Successfully indexed 2 documents"
    open_search_client.bulk.assert_called_once_with(
        body=[
            {"index": {"_index": INDEX}},
            _chunk("a"),
            {"index": {"_index": INDEX}},
            _chunk("b"),
        ]
    )


def test_save_domains_reports_a_failed_bulk(monkeypatch):
    open_search_client = _opensearch_mock(index_exists=True)
    open_search_client.bulk.side_effect = RuntimeError("cluster down")
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    result = server.save_domains_documents("http://opensearch", [_chunk("a")], 4)

    assert result == "Bulk indexing failed: cluster down"


def test_save_domains_logs_the_documents_that_failed_to_index(monkeypatch, caplog):
    open_search_client = _opensearch_mock(index_exists=True)
    open_search_client.bulk.return_value = {
        "errors": True,
        "items": [
            {"index": {"status": 201}},
            {"index": {"error": {"type": "mapper_parsing_exception"}}},
        ],
    }
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    with caplog.at_level(logging.ERROR, logger="app"):
        server.save_domains_documents(
            "http://opensearch", [_chunk("a"), _chunk("b")], 3
        )

    errors = [r.getMessage() for r in caplog.records if r.levelno == logging.ERROR]
    assert errors == [
        "Some documents failed to index:",
        "Failed to index document: {'type': 'mapper_parsing_exception'}",
    ]
