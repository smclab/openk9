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


"""The evaluate endpoint runs the span evaluations on the tenant configuration."""

from datetime import datetime
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"x-tenant-id": "tenant-1"}


@pytest.fixture
def evaluations(monkeypatch):
    evaluations = MagicMock(return_value=4)
    monkeypatch.setattr(server, "evaluations", evaluations)
    monkeypatch.setattr(
        server,
        "get_configurations",
        MagicMock(
            return_value={
                "rag_configuration": {"prompt": "rag"},
                "llm_configuration": {"model": "llm"},
            }
        ),
    )
    monkeypatch.setattr(
        server, "ARIZE_PHOENIX_ENDPOINT", "http://phoenix:6006/v1/traces"
    )
    monkeypatch.setattr(server, "ARIZE_PHOENIX_PROJECT_NAME", "openk9")
    return evaluations


def test_evaluate_reports_the_number_of_evaluated_spans(evaluations):
    response = TestClient(server.app).post(
        "/api/rag/evaluate", json={}, headers=HEADERS
    )

    assert response.status_code == 200
    assert response.json() == {"message": "Spans evaluated: 4", "status": "success"}


def test_evaluate_forwards_the_request_to_the_evaluations(evaluations):
    TestClient(server.app).post(
        "/api/rag/evaluate",
        json={
            "limit": 10,
            "start_time": "2026-01-01T00:00:00",
            "end_time": "2026-02-01T00:00:00",
            "evaluateRagRouter": True,
            "evaluateRetriever": False,
            "evaluateResponse": True,
        },
        headers=HEADERS,
    )

    evaluations.assert_called_once_with(
        {"prompt": "rag"},
        {"model": "llm"},
        "openk9",
        "http://phoenix:6006",
        10,
        datetime(2026, 1, 1),
        datetime(2026, 2, 1),
        True,
        False,
        True,
    )


def test_evaluate_defaults(evaluations):
    TestClient(server.app).post("/api/rag/evaluate", json={}, headers=HEADERS)

    args = evaluations.call_args.args
    assert args[4:] == (100, None, None, False, False, False)


def test_evaluate_without_tenant_is_refused(evaluations):
    response = TestClient(server.app).post("/api/rag/evaluate", json={})

    assert response.status_code == 400
    evaluations.assert_not_called()
