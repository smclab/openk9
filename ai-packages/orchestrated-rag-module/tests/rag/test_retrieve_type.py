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

"""Test del retrieve_type (KNN/HYBRID/TEXT) nei parametri di retrieval.

Il valore arriva dal Bucket via ``GetLLMConfigurations``, non dalla RAGConfiguration:
``retrieve_top_k_args`` lo cercava in ``rag_configuration``, dove non esiste, e ricadeva
sempre su HYBRID — ignorando un bucket configurato TEXT o KNN.
"""



import pytest

from app.rag.agentic.deps import AgenticDeps


def _deps(llm_configuration):
    return AgenticDeps(
        rag_configuration={"range_values": [0, 5]},
        llm_configuration=llm_configuration,
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


@pytest.mark.parametrize("retrieve_type", ["KNN", "HYBRID", "TEXT"])
def test_retrieve_type_comes_from_the_llm_configuration(retrieve_type):
    args = _deps({"retrieve_type": retrieve_type}).retrieve_top_k_args()

    assert args["retrieve_type"] == retrieve_type


def test_missing_retrieve_type_falls_back_to_hybrid():
    # il campo protobuf assente arriva come stringa vuota: il fallback deve comunque scattare,
    # altrimenti la query partirebbe con un tokenType vuoto.
    assert _deps({"retrieve_type": ""}).retrieve_top_k_args()["retrieve_type"] == "HYBRID"
    assert _deps({}).retrieve_top_k_args()["retrieve_type"] == "HYBRID"


def test_retrieve_type_is_not_read_from_the_rag_configuration():
    deps = _deps({"retrieve_type": "TEXT"})
    deps.rag_configuration["retrieve_type"] = "KNN"

    assert deps.retrieve_top_k_args()["retrieve_type"] == "TEXT"
