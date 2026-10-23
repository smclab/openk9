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

"""The tenant provider must reach the graph configuration.

The nodes read `model_type` from the configuration at each use, so a request
whose LLM provider changed picks the new structured output method without a
restart. That only works if `get_agentic_rag` puts the provider there.
"""

from unittest.mock import MagicMock, patch

from app.rag import chain as chain_module

QUERY = "Quali sono i massimali della polizza?"


def _graph_configuration(model_type):
    rag_graph = MagicMock()
    rag_graph.return_value.stream.return_value = iter([])

    with patch.object(chain_module, "RagGraph", rag_graph):
        list(
            chain_module.get_agentic_rag(
                "CHAT_RAG",
                None,
                None,
                None,
                None,
                None,
                None,
                {},
                None,
                None,
                None,
                QUERY,
                None,
                None,
                "tenant-1",
                False,
                None,
                "1",
                1,
                {},
                {"model_type": model_type},
                {},
                "http://localhost:9200",
                "localhost:50052",
                "localhost:50051",
            )
        )

    return rag_graph.call_args.args[1]


def test_the_graph_configuration_carries_the_llm_provider():
    assert _graph_configuration("ollama")["model_type"] == "ollama"
    assert _graph_configuration("openai")["model_type"] == "openai"
