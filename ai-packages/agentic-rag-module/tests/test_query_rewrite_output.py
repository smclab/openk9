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


from langchain_core.runnables import RunnableLambda

from app.rag.agentic_rag import RagGraph

QUERY = "dimmi di più"
PREVIOUS_QUERY = "Che cos'è la garanzia Infortuni del Conducente?"
PREVIOUS_RESPONSE = "È una copertura assicurativa per il conducente."


def _graph(chain_output):
    """Build a RagGraph stub exercising the real _rewrite_query: only the
    rewriting chain is replaced, by a runnable returning a fixed string."""
    graph = RagGraph.__new__(RagGraph)
    graph.configuration = {}
    graph.utility_llm = RunnableLambda(lambda _prompt_value: chain_output)
    return graph


def test_rewrite_returns_the_chain_output_verbatim():
    graph = _graph("Puoi fornirmi maggiori dettagli?")

    rewritten = graph._rewrite_query(QUERY, PREVIOUS_QUERY, PREVIOUS_RESPONSE)

    # A rewrite sharing no content word with the previous query used to get the
    # previous query prepended, which put the earlier subject back into the
    # retrieval query even when the turn was a topic switch. The chain output is
    # now what reaches retrieval, untouched.
    assert rewritten == "Puoi fornirmi maggiori dettagli?"
    assert PREVIOUS_QUERY not in rewritten


def test_rewrite_keeping_the_subject_is_returned_untouched():
    resolved = "Che cos'è la garanzia Infortuni del Conducente nel dettaglio?"
    graph = _graph(resolved)

    rewritten = graph._rewrite_query(QUERY, PREVIOUS_QUERY, PREVIOUS_RESPONSE)

    assert rewritten == resolved
