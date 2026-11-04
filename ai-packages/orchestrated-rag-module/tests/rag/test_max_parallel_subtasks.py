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

"""Tetto di ``max_parallel_subtasks`` (jsonConfig): al massimo 4 Discovery in parallelo.

Il valore governa sia il fan-out di ``discover_many`` sia il budget di richieste
(``build_usage_limits``): i due devono leggere lo **stesso** valore normalizzato, altrimenti il cap
anti-runaway verrebbe dimensionato su un fan-out che non avviene.
"""

import logging

import pytest

from app.rag.agentic.discovery import MAX_RETRIEVER_CALLS
from app.rag.agentic.orchestrator import (
    MAX_PARALLEL_SUBTASKS_LIMIT,
    build_usage_limits,
    resolve_max_parallel_subtasks,
)


def test_limit_is_four():
    assert MAX_PARALLEL_SUBTASKS_LIMIT == 4


@pytest.mark.parametrize(
    "configured, expected",
    [
        (None, 3),  # chiave assente: default invariato
        (2, 2),
        (4, 4),
        (4.0, 4),  # dal jsonConfig i numeri arrivano float (MessageToDict)
        (10, 4),  # oltre il tetto: ridotto a 4
        (-1, 1),  # sotto il minimo: almeno un Discovery
        (0, 1),  # zero e' sotto il minimo, non "assente"
    ],
)
def test_configured_value_is_clamped(configured, expected):
    cfg = {} if configured is None else {"max_parallel_subtasks": configured}

    assert resolve_max_parallel_subtasks(cfg) == expected


@pytest.mark.parametrize("configured, expected", [(0, 1), (10, 4), ("due", 3), ([2], 3)])
def test_a_value_that_is_not_used_as_written_is_logged(configured, expected, caplog):
    """Un valore fuori intervallo o non numerico, scritto a mano nel jsonConfig, non ferma il turno:
    si usa il limite o il default, e il warning dice quale."""
    with caplog.at_level(logging.WARNING, logger="app"):
        resolved = resolve_max_parallel_subtasks({"max_parallel_subtasks": configured})

    assert resolved == expected
    assert [r.levelno for r in caplog.records] == [logging.WARNING]
    assert f"uso {expected}" in caplog.records[0].getMessage()


def test_an_absent_value_takes_the_default_silently(caplog):
    """La mappatura della RAG configuration passa ``None`` quando il tenant non ha la chiave."""
    with caplog.at_level(logging.WARNING, logger="app"):
        resolved = resolve_max_parallel_subtasks({"max_parallel_subtasks": None})

    assert resolved == 3
    assert caplog.records == []


def test_usage_budget_uses_the_clamped_value():
    """Con fan-out attivo il budget e' ``(max_iterations+1) * (per_discovery*k + 2) + 2``: con k
    oltre il tetto deve risultare uguale a quello con k = 4."""
    base = {"max_iterations": 1, "parallel_discovery": True}

    over = build_usage_limits({**base, "max_parallel_subtasks": 10})
    at_limit = build_usage_limits({**base, "max_parallel_subtasks": 4})

    per_discovery = MAX_RETRIEVER_CALLS + 1
    assert over.request_limit == at_limit.request_limit == 2 * (per_discovery * 4 + 2) + 2
