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


from unittest.mock import patch

from app.rag.chain import get_agentic_rag


def _graph_configuration(retrieve_citations):
    """Run get_agentic_rag far enough to capture the RagGraph configuration."""
    with patch("app.rag.chain.RagGraph") as rag_graph:
        rag_graph.return_value.stream.return_value = []
        list(
            get_agentic_rag(
                rag_type="CHAT_RAG",
                search_query=[],
                datasource_ids=[],
                after_key=None,
                suggest_keyword=None,
                suggestion_category_id=None,
                jwt=None,
                extra={},
                sort=[],
                sort_after_key=None,
                language=None,
                search_text="Domanda?",
                chat_id=None,
                user_id=None,
                tenant_id="tenant",
                retrieve_from_uploaded_documents=False,
                chat_history=[],
                timestamp=None,
                chat_sequence_number=1,
                rag_configuration={},
                llm_configuration={"retrieve_citations": retrieve_citations},
                guardrails_configuration={},
                opensearch_host="http://localhost:9200",
                grpc_host_embedding=None,
                grpc_host_datasource=None,
            )
        )
    return rag_graph.call_args.args[1]


def test_flag_reaches_the_graph_configuration():
    # Without this the whole feature stays dead: the flag used to stop at the
    # LLM configuration and never reached RagGraph.
    assert _graph_configuration(True)["retrieve_citations"] is True


def test_flag_defaults_to_the_configured_value_when_off():
    assert _graph_configuration(False)["retrieve_citations"] is False
