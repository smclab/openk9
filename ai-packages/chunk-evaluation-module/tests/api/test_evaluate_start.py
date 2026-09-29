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


from unittest.mock import MagicMock

from fastapi.testclient import TestClient

import api


class InlineThread:
    """Runs the target on start(), so the test can assert on it right away."""

    def __init__(self, target, args, daemon):
        self.target, self.args = target, args

    def start(self):
        self.target(*self.args)


def test_evaluate_start_returns_the_id_and_runs_the_experiment(monkeypatch):
    experiment = {"id": "exp-1"}
    create_experiment = MagicMock(return_value=(experiment, MagicMock()))
    execute_experiment = MagicMock()
    monkeypatch.setattr(api, "create_experiment", create_experiment)
    monkeypatch.setattr(api, "execute_experiment", execute_experiment)
    monkeypatch.setattr(api.threading, "Thread", InlineThread)

    response = TestClient(api.app).post(
        "/evaluate/start",
        json={"db_name": "dataset-x", "metrics": ["redundancy_bloat"]},
    )

    assert response.status_code == 200
    assert response.json() == {"experiment_id": "exp-1"}
    create_experiment.assert_called_once_with(
        experiment_name="Test", dataset="dataset-x"
    )
    execute_experiment.assert_called_once_with(
        experiment, api.task, [api.redundancy_bloat]
    )
