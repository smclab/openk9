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

"""Test del Range Start/End della RAGConfiguration nella query di retrieval.

``get_rag_configuration`` espone i due valori come ``range_values``: ``retrieve_top_k_args``
li leggeva come ``range``, quindi il Range impostato in admin non arrivava mai al query parser
e OpenSearch applicava il proprio default. Questi test presidiano il passaggio.

``query_parser`` (gRPC) e il client OpenSearch sono stubbati: nessun servizio esterno.
"""



import pytest

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


# --------------------------------------------------------------------------- #
# RAGConfiguration -> parametri di retrieval                                    #
# --------------------------------------------------------------------------- #
def test_range_values_reach_the_retrieval_args():
    args = _deps({"range_values": [2, 5]}).retrieve_top_k_args()

    assert args["range_values"] == [2, 5], "Range Start/End devono arrivare al retrieval"


def test_missing_range_values_degrade_to_empty_list():
    # la chiave c'e' sempre, ma il protobuf restituisce lista vuota quando il campo e' assente:
    # il query parser itera range_values, None lo farebbe esplodere.
    assert _deps({"range_values": []}).retrieve_top_k_args()["range_values"] == []
    assert _deps({}).retrieve_top_k_args()["range_values"] == []


# --------------------------------------------------------------------------- #
# Parametri di retrieval -> query parser                                        #
# --------------------------------------------------------------------------- #
class _StopHere(Exception):
    """Interrompe retrieve_documents subito dopo la costruzione della query."""


def test_range_values_are_forwarded_to_the_query_parser(monkeypatch):
    captured = {}

    def fake_query_parser(**kwargs):
        captured.update(kwargs)
        raise _StopHere

    monkeypatch.setattr(retrieval, "query_parser", fake_query_parser)

    with pytest.raises(_StopHere):
        retrieval.retrieve_documents(
            query="chi e' il RUP?",
            search_text="chi e' il RUP?",
            retrieve_type="HYBRID",
            tenant_id="t1",
            opensearch_host="http://os",
            grpc_host="ds:9000",
            context_window=4096,
            range_values=[2, 5],
        )

    assert captured["range_values"] == [2, 5]
