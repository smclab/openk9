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


import json
from unittest.mock import AsyncMock, MagicMock

from fastapi.testclient import TestClient

import api


def experiment(successful, missing):
    return {
        "example_count": 2,
        "repetitions": 1,
        "successful_run_count": successful,
        "failed_run_count": 0,
        "missing_run_count": missing,
    }


def events(body):
    return [
        json.loads(line.removeprefix("data: "))
        for line in body.splitlines()
        if line.startswith("data: ") and line != "data: "
    ]


def test_stream_process_reports_progress_until_the_experiment_is_done(
    monkeypatch,
):
    async_client = MagicMock()
    async_client.experiments.get = AsyncMock(
        side_effect=[experiment(1, 1), experiment(2, 0)]
    )
    monkeypatch.setattr(api, "async_client", async_client)
    monkeypatch.setattr(api, "POLLING_FREQUENCY", 0)

    response = TestClient(api.app).get("/evaluate/stream/exp-1")

    assert response.status_code == 200
    assert events(response.text) == [
        {"event": "progress", "data": {"progress": 50.0, "completed": 1, "total": 2}},
        {"event": "progress", "data": {"progress": 100.0, "completed": 2, "total": 2}},
        {"event": "done", "data": {"status": "completed"}},
    ]
    async_client.experiments.get.assert_awaited_with(experiment_id="exp-1")
