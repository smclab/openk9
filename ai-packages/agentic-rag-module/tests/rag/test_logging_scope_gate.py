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


"""Tests for what the scope gate writes to the log.

An off-scope verdict cancels the answer and sends the redirect message in its
place, which from the outside looks the same as several other blocks: the
record has to say it was the gate, and on how long a prefix it decided.
"""

import logging
from types import SimpleNamespace
from unittest.mock import MagicMock

from app.rag.agentic_rag import RagGraph

QUERY = "Scrivimi una funzione Python che ordina una lista"
PREFIX = "Certo, ecco una funzione che ordina una lista in Python: def sort"


def _scope_gate_graph(verdict):
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = "litwick"
    graph.user_id = None
    graph.chat_id = "def456"
    graph.scope_gate_domain_description = "Assistenza sui corsi di formazione."
    graph.scope_gate_prefix_chars = 250
    graph.llm = MagicMock(return_value=SimpleNamespace(content=verdict))
    return graph


def _messages(caplog, level):
    return [record.getMessage() for record in caplog.records if record.levelno == level]


def test_off_scope_verdict_is_reported_as_a_warning(caplog):
    graph = _scope_gate_graph("OFF_SCOPE")

    with caplog.at_level(logging.INFO):
        verdict = graph._llm_scope_gate(QUERY, "Contesto di dominio.", PREFIX)

    assert verdict == "OFF_SCOPE"
    off_scope = _messages(caplog, logging.WARNING)[0]
    assert "[scope_gate] OFF_SCOPE" in off_scope
    assert f"prefix_chars={len(PREFIX)}" in off_scope
    assert "scope_gate_prefix_chars=250" in off_scope
    assert "tenant_id=litwick" in off_scope
    assert "chat_id=def456" in off_scope


def test_in_scope_verdict_is_reported_at_info(caplog):
    graph = _scope_gate_graph("VALID")

    with caplog.at_level(logging.INFO):
        verdict = graph._llm_scope_gate(QUERY, "Contesto di dominio.", PREFIX)

    assert verdict == "VALID"
    assert _messages(caplog, logging.WARNING) == []
    assert "[scope_gate] VALID" in _messages(caplog, logging.INFO)[0]


def test_neither_the_query_nor_the_prefix_appear_at_info(caplog):
    graph = _scope_gate_graph("OFF_SCOPE")

    with caplog.at_level(logging.INFO):
        graph._llm_scope_gate(QUERY, "Contesto di dominio.", PREFIX)

    off_scope = _messages(caplog, logging.WARNING)[0]
    assert QUERY not in off_scope
    assert PREFIX not in off_scope


def test_query_and_prefix_ride_on_the_same_record_at_debug(caplog):
    graph = _scope_gate_graph("OFF_SCOPE")

    with caplog.at_level(logging.DEBUG, logger="app"):
        graph._llm_scope_gate(QUERY, "Contesto di dominio.", PREFIX)

    off_scope = [m for m in _messages(caplog, logging.WARNING) if "OFF_SCOPE" in m]
    assert len(off_scope) == 1
    assert QUERY in off_scope[0]
    assert PREFIX in off_scope[0]
