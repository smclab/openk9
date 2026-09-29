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

import pytest
from fastapi.testclient import TestClient

import api


@pytest.fixture
def make_experiment(monkeypatch):
    make_experiment = MagicMock()
    monkeypatch.setattr(api, "make_experiment", make_experiment)
    return make_experiment


def test_evaluate_runs_the_selected_metrics_on_the_dataset(make_experiment):
    response = TestClient(api.app).post(
        "/evaluate",
        json={"db_name": "dataset-x", "metrics": ["layout_fidelity", "unknown"]},
    )

    assert response.status_code == 200
    assert response.json() == {"status": "ok", "message": "Process ended"}
    # Unsupported metric names are skipped.
    make_experiment.assert_called_once_with(
        api.task, [api.layout_fidelity], dataset="dataset-x"
    )


def test_evaluate_uses_every_metric_and_the_daily_dataset_by_default(
    make_experiment,
):
    response = TestClient(api.app).post(
        "/evaluate", json={"db_name": None, "metrics": None}
    )

    assert response.status_code == 200
    make_experiment.assert_called_once_with(api.task, list(api.METRICS.values()))
