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
"""The evaluate route annotates the turn spans of a period on Phoenix, with the judges the
request asks for, on the model of the tenant."""

from fastapi.testclient import TestClient

import app.server as server


def test_evaluate_runs_the_judges_asked_for(monkeypatch):
    captured = {}

    async def fake_evaluations(rag_configuration, llm_configuration, project, base_url, *args):
        captured.update(
            llm_configuration=llm_configuration, project=project, base_url=base_url, args=args
        )
        return {"span-1"}

    monkeypatch.setattr(server, "evaluations", fake_evaluations)
    monkeypatch.setattr(
        server,
        "get_configurations",
        lambda **_: {"rag_configuration": {}, "llm_configuration": {"model": "m"}},
    )
    monkeypatch.setattr(server, "ARIZE_PHOENIX_ENDPOINT", "http://phoenix:6006/v1/traces")

    response = TestClient(server.app).post(
        "/api/rag/evaluate",
        json={"limit": 10, "evaluateResponse": True},
        headers={"x-tenant-id": "tenant-1"},
    )

    assert response.status_code == 200
    assert response.json() == {"message": "Spans evaluated: {'span-1'}", "status": "success"}
    assert captured["llm_configuration"] == {"model": "m"}
    assert captured["base_url"] == "http://phoenix:6006"
    limit, _start, _end, router, retriever, response_judge = captured["args"]
    assert (limit, router, retriever, response_judge) == (10, False, False, True)


def test_evaluate_without_a_tenant_is_refused():
    response = TestClient(server.app).post("/api/rag/evaluate", json={})

    assert response.status_code == 400
