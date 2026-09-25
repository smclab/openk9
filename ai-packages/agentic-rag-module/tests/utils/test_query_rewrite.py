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


from types import SimpleNamespace
from unittest.mock import MagicMock

from langchain_core.messages import AIMessage, HumanMessage
from langchain_core.runnables import RunnableLambda

from app.rag.agentic_rag import GraphState, RagGraph
from app.utils.query_rewrite import escape_curly_braces


def test_escape_curly_braces_doubles_braces():
    assert escape_curly_braces('a {"k": "v"} b') == 'a {{"k": "v"}} b'
    assert escape_curly_braces("no braces") == "no braces"


def _graph(configuration):
    """Build a RagGraph stub whose utility LLM records the prompt it receives,
    so the template built by the real code is what gets rendered."""
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = "CHAT_RAG"
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    graph.chat_sequence_number = 2
    graph.reformulate = False
    graph.configuration = configuration
    graph.sent_prompts = []
    return graph


def test_rewrite_template_renders_with_tenant_prompt_containing_braces():
    # The tenant prompt is concatenated into the template string: its literal
    # braces must reach the model as written, not be read as placeholders,
    # while the boilerplate placeholders stay live.
    tenant_prompt = 'Rispondi in JSON come {"query": "..."}.'
    graph = _graph({"rephrase_prompt_template": tenant_prompt})

    def _rewrite(prompt_value):
        graph.sent_prompts.append(prompt_value.to_string())
        return "rewritten"

    graph.utility_llm = RunnableLambda(_rewrite)

    rewritten = graph._rewrite_query("q", "pq", "pr with {braces}")

    assert rewritten == "rewritten"
    (prompt,) = graph.sent_prompts
    assert prompt.startswith(tenant_prompt)
    assert '"q"' in prompt
    assert '"pq"' in prompt
    assert "pr with {braces}" in prompt


def test_analyze_template_renders_with_tenant_prompt_containing_braces():
    tenant_prompt = "Classifica usando le chiavi {follow_up} e {new}."
    graph = _graph({"analyze_query_prompt_template": tenant_prompt})

    def _analyze(prompt_value):
        graph.sent_prompts.append(prompt_value.to_string())
        return SimpleNamespace(response=SimpleNamespace(value="NEW_QUESTION"))

    graph.utility_llm = MagicMock()
    graph.utility_llm.with_structured_output.return_value = _analyze
    state = GraphState(
        current_query="q",
        messages=[
            HumanMessage(content="ctx with {braces}"),
            AIMessage(content="answer"),
        ],
    )

    graph.analyze_and_rewrite_query_node(state)

    (prompt,) = graph.sent_prompts
    assert prompt.startswith(tenant_prompt)
    assert "ctx with {braces}" in prompt
    assert prompt.rstrip().endswith("q")
