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

from app.rag.agentic_rag import GraphState, RagGraph

CURRENT_QUERY = "Un'altra cosa: come vi si contatta?"
PREVIOUS_QUERY = "Che cos'è la garanzia infortuni del conducente?"
PREVIOUS_RESPONSE = "È una copertura assicurativa..."
CONVERSATION_CONTEXT = f"User: {PREVIOUS_QUERY}\nAssistant: {PREVIOUS_RESPONSE}"


def _graph(configuration):
    """Build a RagGraph stub that records the prompt reaching the analyze
    chain, so the prompt actually sent to the LLM can be asserted on."""
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = "CHAT_RAG"
    graph.chat_sequence_number = 2
    graph.reformulate = False
    graph.configuration = configuration
    graph.utility_llm = MagicMock()
    graph.sent_prompts = []

    def _analyze(prompt_value):
        graph.sent_prompts.append(prompt_value.to_string())
        return SimpleNamespace(response=SimpleNamespace(value="NEW_QUESTION"))

    graph.utility_llm.with_structured_output.return_value = _analyze
    graph._rewrite_query = MagicMock(return_value="REWRITTEN QUERY")
    return graph


def _state():
    return GraphState(
        current_query=CURRENT_QUERY,
        messages=[
            HumanMessage(content=PREVIOUS_QUERY),
            AIMessage(content=PREVIOUS_RESPONSE),
        ],
    )


def _analyze_prompt(configuration):
    graph = _graph(configuration)
    graph.analyze_and_rewrite_query_node(_state())
    return graph.sent_prompts[0]


def test_default_prompt_classifies_on_self_containedness():
    prompt = _analyze_prompt({})

    # The classifier must decide on referential self-containedness, not on
    # topical similarity: the latter made every same-domain sub-topic look
    # like a FOLLOW_UP.
    assert "DECISIVE TEST - self-containedness, not topical similarity" in prompt
    assert "Sharing the same broad domain is NOT enough for FOLLOW_UP" in prompt


def test_default_prompt_neutralizes_leading_connectors():
    prompt = _analyze_prompt({})

    # "Ok, e…" / "un'altra cosa" / "by the way" must not tip the decision.
    assert "Ignore leading connectors/fillers" in prompt


def test_default_prompt_drops_the_over_broad_followup_definition():
    prompt = _analyze_prompt({})

    # The old wording counted any "extension or application" of a discussed
    # concept as a FOLLOW_UP, which swallowed the topic switches.
    assert "extensions, or applications of concepts already discussed" not in prompt


def test_default_prompt_carries_conversation_and_current_query():
    # Sibling test modules in this suite replace langchain_core.messages with
    # fakes, so the isinstance checks inside _get_conversation_context cannot be
    # relied on once the whole suite runs. Stub the extracted context: what this
    # test is about is the template wiring, that both placeholders reach the
    # prompt filled in.
    graph = _graph({})
    graph._get_conversation_context = lambda _messages: CONVERSATION_CONTEXT

    graph.analyze_and_rewrite_query_node(_state())
    prompt = graph.sent_prompts[0]

    assert CONVERSATION_CONTEXT in prompt
    assert CURRENT_QUERY in prompt


def test_tenant_prompt_still_overrides_the_default():
    tenant_prompt = "Classifica secondo le regole del tenant."

    prompt = _analyze_prompt({"analyze_query_prompt_template": tenant_prompt})

    # The per-tenant prompt (jsonConfig `analyze_query_prompt`) keeps winning:
    # promoting the hardened default must not shadow it.
    assert tenant_prompt in prompt
    assert "DECISIVE TEST" not in prompt
    assert CURRENT_QUERY in prompt
