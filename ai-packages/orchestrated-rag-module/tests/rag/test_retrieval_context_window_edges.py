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

"""``chunk_window`` nel retrieval: i chunk ai bordi del documento non hanno vicini.

Il primo chunk non ha ``previous``, l'ultimo non ha ``next``: devono diventare liste vuote, come
in ``OpenSearchRetriever`` di ``agentic-rag-module`` (Issue #2298), altrimenti
``get_context_window_merged`` fallisce su ``None``.

``query_parser`` (gRPC) e il client OpenSearch sono stubbati: nessun servizio esterno.
"""

from unittest.mock import MagicMock

from app.rag.agentic import retrieval


def _hit(number, text, previous=None, following=None, doc="doc"):
    source = {"contentId": doc, "number": number, "chunkText": text, "rawContent": text}
    if previous is not None:
        source["previous"] = [{"chunkText": t} for t in previous]
    if following is not None:
        source["next"] = [{"chunkText": t} for t in following]
    return {"_score": 1.0, "_source": source}


def _retrieve(monkeypatch, hits, chunk_window, retrieve_type="HYBRID"):
    monkeypatch.setattr(
        retrieval,
        "query_parser",
        lambda **_: {"query": "{}", "index_name": ["idx"], "query_parameters": {}},
    )
    client = MagicMock()
    client.search.return_value = {"hits": {"hits": hits, "total": {"value": len(hits)}}}
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda *_, **__: client)

    return retrieval.retrieve_documents(
        query="q",
        search_text="q",
        retrieve_type=retrieve_type,
        tenant_id="t1",
        opensearch_host="http://os",
        grpc_host="ds:9000",
        context_window=4096,
        range_values=[],
        chunk_window=chunk_window,
    )


def test_first_chunk_without_previous_is_merged_with_its_next(monkeypatch):
    # il primo chunk del documento: OpenSearch non restituisce `previous`
    documents = _retrieve(monkeypatch, [_hit(0, "A", following=["B"])], chunk_window=1)

    assert [doc["content"] for doc in documents] == ["AB"]


def test_last_chunk_without_next_is_merged_with_its_previous(monkeypatch):
    documents = _retrieve(monkeypatch, [_hit(3, "D", previous=["C"])], chunk_window=1)

    assert [doc["content"] for doc in documents] == ["CD"]


def test_single_chunk_document_gets_only_its_own_neighbours(monkeypatch):
    # il documento con un solo chunk non deve ricevere il "next" dell'ultimo chunk di un altro
    hits = [
        _hit(0, "A", following=["B"], doc="uno"),
        _hit(5, "X", previous=["W"], following=["Y"], doc="due"),
        _hit(1, "B", previous=["A"], following=["Z"], doc="uno"),
    ]

    documents = _retrieve(monkeypatch, hits, chunk_window=1)

    by_id = {doc["document_id"]: doc["content"] for doc in documents}
    assert by_id["due"] == "WXY"


def test_keyword_retrieval_ignores_chunk_window(monkeypatch):
    # TEXT non ha chunk_idx/prev/next: il merge vale solo per i tipi vettoriali (come nel modulo)
    documents = _retrieve(monkeypatch, [_hit(0, "A")], chunk_window=1, retrieve_type="TEXT")

    assert [doc["content"] for doc in documents] == ["A"]


def test_merged_context_stays_within_the_context_budget(monkeypatch):
    # tre documenti da un chunk di 35 caratteri (10 token): sotto il tetto prima del merge, ma
    # con i vicini ogni documento triplica. Il tetto e' 85% di context_window.
    chunk, neighbour = "a" * 35, "b" * 35
    hits = [
        _hit(1, chunk, previous=[neighbour], following=[neighbour], doc=f"d{i}")
        for i in range(3)
    ]
    monkeypatch.setattr(
        retrieval,
        "query_parser",
        lambda **_: {"query": "{}", "index_name": ["idx"], "query_parameters": {}},
    )
    client = MagicMock()
    client.search.return_value = {"hits": {"hits": hits, "total": {"value": 3}}}
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda *_, **__: client)

    documents = retrieval.retrieve_documents(
        query="q",
        search_text="q",
        retrieve_type="HYBRID",
        tenant_id="t1",
        opensearch_host="http://os",
        grpc_host="ds:9000",
        context_window=50,  # tetto: 42.5 token
        range_values=[],
        chunk_window=1,
    )

    tokens = sum(len(doc["content"]) for doc in documents) / retrieval.TOKEN_SIZE
    assert tokens <= 50 * retrieval.MAX_CONTEXT_WINDOW_PERCENTAGE
    assert [doc["document_id"] for doc in documents] == ["d0"]
