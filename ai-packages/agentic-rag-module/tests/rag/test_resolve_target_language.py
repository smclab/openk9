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

from app.rag.agentic_rag import RagGraph


def _graph_with_llm_returning(content):
    """RagGraph stub whose utility_llm replies with the given content payload."""
    graph = RagGraph.__new__(RagGraph)
    llm = MagicMock()
    llm.return_value = SimpleNamespace(content=content)
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
