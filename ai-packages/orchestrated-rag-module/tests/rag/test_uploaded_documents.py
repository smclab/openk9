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
"""The documents the user uploaded in a chat: the search on them, and the turn that uses them.

The search is the one of the uploaded documents retriever of agentic-rag-module: restricted to
the chat of the user in both branches, through the search pipeline of the Search Config for a
hybrid bucket. With ``retrieve_from_uploaded_documents`` the Discovery searches only these
documents, and an anonymous user, who has none, is refused.
"""

import asyncio
import json
import logging
from unittest.mock import MagicMock

import pytest

from app.rag.agentic import entrypoint, retrieval
from app.rag.agentic.retrieval import retrieve_uploaded_documents, uploaded_documents_retrieve_fn

VECTOR = [0.1, 0.2, 0.3]
INDEX = "tenant-1-uploaded-documents-index"
ISOLATION_FILTERS = [
    {"term": {"user_id.keyword": "user-1"}},
    {"term": {"chat_id.keyword": "chat-1"}},
]


@pytest.fixture
def opensearch(monkeypatch):
    """OpenSearch, embedding and QueryParser under the control of the test."""
    state = {"hits": [], "index_exists": True, "query_parameters": {}, "embedded": []}
    client = MagicMock()
    client.indices.exists.side_effect = lambda index: state["index_exists"]
    client.search.side_effect = lambda **_: {"hits": {"hits": state["hits"]}}
    state["client"] = client
    state["parser"] = MagicMock(
        side_effect=lambda **_: {"query": b"", "query_parameters": state["query_parameters"]}
    )
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda host: client)
    monkeypatch.setattr(retrieval, "query_parser", state["parser"])

    def fake_embedding(grpc_host_embedding, embedding_model_configuration, text):
        state["embedded"].append((grpc_host_embedding, embedding_model_configuration, text))
        return VECTOR

    monkeypatch.setattr(retrieval, "query_embedding", fake_embedding)
    return state


def _search(retrieve_type="HYBRID", **bucket):
    return retrieve_uploaded_documents(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="localhost:50053",
        embedding_model_configuration={"model": "an-embedding-model"},
        index=INDEX,
        retrieve_type=retrieve_type,
        user_id="user-1",
        chat_id="chat-1",
        search_text="zephyr",
        **bucket,
    )


BUCKET = {"grpc_host": "datasource:50051", "tenant_id": "tenant-1", "jwt": "a-jwt"}


# --------------------------------------------------------------------------- #
# The search                                                                   #
# --------------------------------------------------------------------------- #
def test_text_search_is_restricted_to_the_user_chat(opensearch):
    _search("TEXT")

    opensearch["client"].search.assert_called_once_with(
        body={
            "query": {
                "bool": {
                    "must": [{"match": {"chunkText": {"query": "zephyr"}}}],
                    "filter": ISOLATION_FILTERS,
                }
            }
        },
        index=INDEX,
    )


@pytest.mark.parametrize("retrieve_type", ["HYBRID", "KNN"])
def test_vectorial_search_is_restricted_to_the_user_chat(opensearch, retrieve_type):
    _search(retrieve_type)

    assert opensearch["embedded"] == [
        ("localhost:50053", {"model": "an-embedding-model"}, "zephyr")
    ]
    query = opensearch["client"].search.call_args.kwargs["body"]["query"]["hybrid"]["queries"]
    assert query[0] == {
        "bool": {
            "must": [{"match": {"chunkText": {"query": "zephyr", "boost": 0.5}}}],
            "filter": ISOLATION_FILTERS,
        }
    }
    assert query[1] == {
        "script_score": {
            "query": {"bool": {"filter": ISOLATION_FILTERS}},
            "script": {
                "source": "knn_score",
                "lang": "knn",
                "params": {"field": "vector", "query_value": VECTOR, "space_type": "l2"},
            },
        }
    }


def test_hybrid_search_runs_through_the_search_config_pipeline(opensearch):
    opensearch["query_parameters"] = {"search_pipeline": "DefaultSearchConfig"}

    _search(**BUCKET)

    assert opensearch["client"].search.call_args.kwargs["params"] == {
        "search_pipeline": "DefaultSearchConfig"
    }
    kwargs = opensearch["parser"].call_args.kwargs
    assert (kwargs["grpc_host"], kwargs["tenant_id"], kwargs["jwt"]) == (
        "datasource:50051",
        "tenant-1",
        "a-jwt",
    )
    # a hybrid token with no text: the datasource reports the pipeline and has nothing to embed
    assert [(t["tokenType"], t["values"]) for t in kwargs["search_query"]] == [("HYBRID", [])]


def test_hybrid_search_without_a_search_config_keeps_the_index_pipeline(opensearch):
    _search(**BUCKET)

    assert "params" not in opensearch["client"].search.call_args.kwargs


@pytest.mark.parametrize("retrieve_type", ["KNN", "TEXT"])
def test_only_a_hybrid_bucket_asks_for_the_pipeline(opensearch, retrieve_type):
    opensearch["query_parameters"] = {"search_pipeline": "DefaultSearchConfig"}

    _search(retrieve_type, **BUCKET)

    opensearch["parser"].assert_not_called()
    assert "params" not in opensearch["client"].search.call_args.kwargs


def test_missing_index_returns_nothing(opensearch):
    opensearch["index_exists"] = False

    assert _search() == []
    opensearch["client"].search.assert_not_called()


def test_nothing_to_embed_skips_the_search(opensearch, monkeypatch):
    monkeypatch.setattr(retrieval, "query_embedding", lambda *_: None)

    assert _search() == []
    opensearch["client"].search.assert_not_called()


def test_title_is_built_from_the_filename(opensearch):
    opensearch["hits"] = [
        {
            "_source": {
                "document_id": "doc-1",
                "filename": "contratto",
                "file_extension": ".pdf",
                "chunkText": "Art. 1",
            },
            "_score": 0.7,
        },
        {"_source": {"document_id": "doc-2"}},
    ]

    assert _search() == [
        {
            "document_id": "doc-1",
            "filename": "contratto",
            "file_extension": ".pdf",
            "title": "contratto.pdf",
            "score": 0.7,
            "content": "Art. 1",
        },
        {
            "document_id": "doc-2",
            "filename": None,
            "file_extension": None,
            "title": "",
            "score": 0,
            "content": "",
        },
    ]


def test_the_discovery_search_goes_to_the_uploaded_documents_of_the_chat(monkeypatch):
    """The retrieve_fn takes the arguments of the search on the documents of the tenant."""
    captured = {}
    monkeypatch.setattr(
        retrieval, "retrieve_uploaded_documents", lambda **kwargs: captured.update(kwargs) or []
    )
    retrieve = uploaded_documents_retrieve_fn(
        user_id="user-1",
        chat_id="chat-1",
        grpc_host_embedding="em:9000",
        embedding_model_configuration={"model": "e"},
    )

    retrieve(
        query="zephyr",
        search_text="zephyr",
        tenant_id="tenant-1",
        opensearch_host="http://os",
        grpc_host="ds:9000",
        domains=["billing"],
        datasource_ids=[1],
        filter_tokens=[],
        jwt="a-jwt",
        retrieve_type="HYBRID",
        chunk_window=2,
    )

    assert captured == {
        "opensearch_host": "http://os",
        "grpc_host_embedding": "em:9000",
        "embedding_model_configuration": {"model": "e"},
        "index": INDEX,
        "retrieve_type": "HYBRID",
        "user_id": "user-1",
        "chat_id": "chat-1",
        "search_text": "zephyr",
        "grpc_host": "ds:9000",
        "tenant_id": "tenant-1",
        "jwt": "a-jwt",
    }


# --------------------------------------------------------------------------- #
# The turn                                                                     #
# --------------------------------------------------------------------------- #
def _request(monkeypatch, *, uploaded, user_id):
    captured = {}

    async def fake_run_turn(*, deps, **_):
        captured["retrieve_fn"] = deps.retrieve_fn
        yield json.dumps({"chunk": "", "type": "END"})

    monkeypatch.setattr(entrypoint, "run_turn", fake_run_turn)
    monkeypatch.setattr(entrypoint, "build_model", lambda _: "MODEL")
    monkeypatch.setattr(entrypoint, "get_embedding_model_configuration", lambda **_: {})

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.get_agentic_rag(
                rag_type="CHAT_RAG",
                search_query=None,
                datasource_ids=None,
                after_key=None,
                suggest_keyword=None,
                suggestion_category_id=None,
                jwt=None,
                extra=None,
                sort=None,
                sort_after_key=None,
                language=None,
                search_text="cosa dice il contratto?",
                chat_id="chat-1",
                user_id=user_id,
                tenant_id="tenant-1",
                retrieve_from_uploaded_documents=uploaded,
                chat_history=None,
                timestamp=None,
                chat_sequence_number=1,
                rag_configuration={},
                llm_configuration={},
                guardrails_configuration={},
                opensearch_host="http://os",
                grpc_host_embedding="em:9000",
                grpc_host_datasource="ds:9000",
            )
        ]

    return asyncio.run(collect()), captured


def test_the_flag_searches_the_uploaded_documents(monkeypatch):
    _, captured = _request(monkeypatch, uploaded=True, user_id="user-1")

    assert captured["retrieve_fn"] is not retrieval.retrieve_documents
    assert captured["retrieve_fn"].__qualname__.startswith("uploaded_documents_retrieve_fn")


def test_without_the_flag_the_documents_of_the_tenant_are_searched(monkeypatch):
    _, captured = _request(monkeypatch, uploaded=False, user_id="user-1")

    assert captured["retrieve_fn"] is retrieval.retrieve_documents


def test_the_flag_without_a_user_is_refused(monkeypatch):
    events, captured = _request(monkeypatch, uploaded=True, user_id=None)

    assert events == [{"chunk": "Unexpected error", "type": "ERROR", "message": "Invalid token."}]
    assert captured == {}


def test_the_refusal_is_not_logged_as_an_error(monkeypatch, caplog):
    with caplog.at_level(logging.INFO, logger="app"):
        _request(monkeypatch, uploaded=True, user_id=None)

    refusals = [r for r in caplog.records if r.levelno >= logging.WARNING]
    assert [r.levelno for r in refusals] == [logging.WARNING]
    assert "Invalid token." in refusals[0].getMessage()


def test_a_failing_service_is_still_logged_as_an_error(monkeypatch, caplog):
    from fastapi import HTTPException

    def failing(**_):
        raise HTTPException(status_code=500, detail="Unexpected error")

    monkeypatch.setattr(retrieval, "uploaded_documents_retrieve_fn", failing)
    with caplog.at_level(logging.INFO, logger="app"):
        _request(monkeypatch, uploaded=True, user_id="user-1")

    assert [r.levelno for r in caplog.records if r.levelno >= logging.WARNING] == [logging.ERROR]


def test_the_flag_is_saved_with_the_turn(monkeypatch):
    from app.rag.agentic import orchestrator
    from app.rag.agentic.contracts import OrchestratorOutput
    from app.rag.agentic.deps import AgenticDeps

    async def fake_run_orchestrator(**_):
        return (
            OrchestratorOutput(status="ok", message="risposta", used_document_ids=[]),
            [],
            MagicMock(all_messages=lambda: []),
        )

    monkeypatch.setattr(orchestrator, "run_orchestrator", fake_run_orchestrator)
    store = MagicMock()

    async def collect():
        return [
            event
            async for event in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text="cosa dice il contratto?",
                deps=AgenticDeps(
                    rag_configuration={"enable_verifier": True},
                    llm_configuration={},
                    opensearch_host="http://os",
                    grpc_host_datasource="ds:9000",
                    grpc_host_embedding="em:9000",
                    tenant_id="tenant-1",
                ),
                store=store,
                chat_id="chat-1",
                user_id="user-1",
                retrieve_from_uploaded_documents=True,
            )
        ]

    asyncio.run(collect())

    assert store.save_turn.call_args.kwargs["retrieve_from_uploaded_documents"] is True
