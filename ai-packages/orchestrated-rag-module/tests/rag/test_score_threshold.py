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

"""Test di ``score_threshold`` del jsonConfig RAG nel retrieval.

Port di ``OpenSearchRetriever`` di ``agentic-rag-module``: la soglia e' **relativa** al miglior
score del result set (``top_score * score_threshold``), non un valore assoluto, perche' gli score
OpenSearch non hanno una scala fissa (BM25, KNN e hybrid normalizzato sono diversi).

``query_parser`` (gRPC) e il client OpenSearch sono stubbati: nessun servizio esterno.
"""

from unittest.mock import MagicMock


from app.rag.agentic import retrieval
from app.rag.agentic.deps import AgenticDeps


def _deps(rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={"context_window": 4096},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def _hit(document_id, score):
    return {
        "_score": score,
        "_source": {"contentId": document_id, "chunkText": f"testo {document_id}"},
    }


def _retrieve(monkeypatch, hits, **kwargs):
    monkeypatch.setattr(
        retrieval,
        "query_parser",
        lambda **_: {"query": "{}", "index_name": ["idx"], "query_parameters": {}},
    )
    client = MagicMock()
    client.search.return_value = {"hits": {"hits": hits, "total": {"value": len(hits)}}}
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda *_, **__: client)

    documents = retrieval.retrieve_documents(
        query="chi e' il RUP?",
        search_text="chi e' il RUP?",
        retrieve_type="HYBRID",
        tenant_id="t1",
        opensearch_host="http://os",
        grpc_host="ds:9000",
        context_window=4096,
        range_values=[],
        **kwargs,
    )
    return [doc["document_id"] for doc in documents]


def test_score_threshold_reaches_the_retrieval_args():
    assert _deps({"score_threshold": 0.5}).retrieve_top_k_args()["score_threshold"] == 0.5


def test_hits_below_the_relative_cutoff_are_dropped(monkeypatch):
    # top 10 * 0.3 = cutoff 3: resta fuori solo lo score 2.9
    hits = [_hit("a", 10), _hit("b", 5), _hit("c", 2.9)]

    assert _retrieve(monkeypatch, hits, score_threshold=0.3) == ["a", "b"]


def test_cutoff_uses_the_best_score_not_the_first_hit(monkeypatch):
    # un sort custom puo' mettere in testa un hit non migliore: il top score va preso su tutti
    hits = [_hit("a", 2), _hit("b", 10), _hit("c", 4)]

    assert _retrieve(monkeypatch, hits, score_threshold=0.3) == ["b", "c"]


def test_no_threshold_keeps_every_hit(monkeypatch):
    hits = [_hit("a", 10), _hit("b", 0.1)]

    assert _retrieve(monkeypatch, hits, score_threshold=None) == ["a", "b"]
    assert _retrieve(monkeypatch, hits, score_threshold=0) == ["a", "b"]
