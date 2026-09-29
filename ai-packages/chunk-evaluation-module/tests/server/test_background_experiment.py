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


from datetime import datetime
from unittest.mock import MagicMock

import pytest

import server


@pytest.fixture
def experiments(monkeypatch):
    client = MagicMock()
    client.datasets.list.return_value = [
        {"id": "d1", "name": "dataset-1", "example_count": 5},
        {"id": "d2", "name": "dataset-2", "example_count": 5},
    ]
    make_experiment = MagicMock()
    monkeypatch.setattr(server, "client", client)
    monkeypatch.setattr(server, "make_experiment", make_experiment)
    monkeypatch.setattr(server, "last_experiment", None)
    return client, make_experiment


def test_background_experiment_runs_the_latest_incomplete_dataset(experiments):
    client, make_experiment = experiments
    client.experiments.list.side_effect = lambda dataset_id: (
        [] if dataset_id == "d2" else [{"successful_run_count": 5}]
    )
    task, evaluators = object(), [object()]

    server.background_experiment(task, evaluators)

    make_experiment.assert_called_once_with(task, evaluators, dataset="dataset-2")
    assert server.last_experiment is None


def test_background_experiment_records_the_run_when_all_are_complete(
    experiments,
):
    client, make_experiment = experiments
    client.experiments.list.return_value = [{"successful_run_count": 5}]

    server.background_experiment(object(), [])

    make_experiment.assert_not_called()
    assert isinstance(server.last_experiment, datetime)
