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


"""The language the answer must be written in.

It is resolved by a node of its own, before the query is rewritten. Its model
call is tagged nostream, so the JSON it returns never reaches the messages
stream that is forwarded to the client.
"""

from types import SimpleNamespace
from unittest.mock import MagicMock

from langchain_core.language_models.fake_chat_models import FakeListChatModel
from langgraph.constants import TAG_NOSTREAM
from langgraph.graph import END, StateGraph

from app.rag.agentic_rag import GraphState, RagGraph


def _graph_with_llm_returning(content):
    """RagGraph stub whose utility_llm replies with the given content payload."""
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    llm = MagicMock()
    llm.return_value = SimpleNamespace(content=content)
    # The tagged model is the one the chain calls.
    llm.with_config.return_value = llm
    graph.utility_llm = llm
    return graph


def test_returns_language_from_string_content():
    graph = _graph_with_llm_returning('{"language": "French"}')

    assert graph._resolve_target_language("Bonjour, parlez-moi de DGS") == "French"


def test_returns_language_from_list_content():
    # Some chat models return content as a list of {"text": ...} blocks.
    graph = _graph_with_llm_returning([{"text": '{"language": "German"}'}])

    assert graph._resolve_target_language("Erzaehl mir von DGS") == "German"


def test_explicit_request_language_is_propagated():
    # The explicit-request precedence ("answer in English" inside an Italian
    # query) is decided by the utility LLM; the resolver must faithfully return
    # whatever language it reports.
    graph = _graph_with_llm_returning('{"language": "English"}')

    assert graph._resolve_target_language("Parlami di DGS, answer in English") == "English"


def test_falls_back_to_italian_on_unparseable_output():
    graph = _graph_with_llm_returning("this is not a json object")

    assert graph._resolve_target_language("qualcosa") == "Italian"


def test_falls_back_to_italian_when_language_is_blank():
    graph = _graph_with_llm_returning('{"language": "   "}')

    assert graph._resolve_target_language("qualcosa") == "Italian"


def test_falls_back_to_italian_when_utility_llm_unavailable():
    # No utility_llm attribute at all -> resolution must degrade gracefully.
    graph = RagGraph.__new__(RagGraph)

    assert graph._resolve_target_language("qualcosa") == "Italian"


def test_model_call_is_tagged_nostream():
    graph = _graph_with_llm_returning('{"language": "English"}')

    graph._resolve_target_language("Tell me about DGS")

    graph.utility_llm.with_config.assert_called_once_with(tags=[TAG_NOSTREAM])


def test_node_writes_the_language_of_the_query_into_the_state():
    graph = _graph_with_llm_returning('{"language": "Spanish"}')

    state = graph.resolve_language_node(GraphState(current_query="Hablame de DGS"))

    assert state.target_lang == "Spanish"
    prompt = graph.utility_llm.call_args.args[0].to_string()
    assert "Hablame de DGS" in prompt


def test_node_falls_back_to_italian_when_resolution_fails():
    graph = _graph_with_llm_returning("this is not a json object")

    state = graph.resolve_language_node(GraphState(current_query="qualcosa"))

    assert state.target_lang == "Italian"


def test_node_output_never_reaches_the_messages_stream():
    # The filter on the node name in stream() is not what keeps it out: run
    # with a model that streams for real, nothing of it is emitted.
    graph = _graph_with_llm_returning(None)
    graph.utility_llm = FakeListChatModel(responses=['{"language": "English"}'])
    workflow = StateGraph(GraphState)
    workflow.add_node("resolve_language", graph.resolve_language_node)
    workflow.set_entry_point("resolve_language")
    workflow.add_edge("resolve_language", END)
    compiled = workflow.compile()

    messages = list(
        compiled.stream({"current_query": "Tell me about DGS"}, stream_mode="messages")
    )

    assert messages == []
    assert compiled.invoke({"current_query": "Tell me"})["target_lang"] == "English"
