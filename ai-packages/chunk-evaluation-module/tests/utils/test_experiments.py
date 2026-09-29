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
from unittest.mock import MagicMock, call

import pytest

import utils.helpers as helpers


class FixedDatetime(datetime):
    @classmethod
    def today(cls):
        return cls(2026, 9, 1, 10, 30)


@pytest.fixture
def client(monkeypatch):
    client = MagicMock()
    client.experiments.list.return_value = [{"id": "old-1"}, {"id": "old-2"}]
    client.projects.list.return_value = []
    monkeypatch.setattr(helpers, "client", client)
    monkeypatch.setattr(helpers, "datetime", FixedDatetime)
    return client


def test_make_experiment_runs_on_the_named_dataset(client, monkeypatch):
    run_experiment = MagicMock()
    monkeypatch.setattr(helpers, "run_experiment", run_experiment)
    task, evaluators = object(), [object()]

    result = helpers.make_experiment(task, evaluators, dataset="dataset-x")

    dataset = client.datasets.get_dataset.return_value
    client.datasets.get_dataset.assert_called_once_with(dataset="dataset-x")
    # Previous experiments on the dataset are replaced by the new one.
    client.experiments.delete.assert_has_calls(
        [call(experiment_id="old-1"), call(experiment_id="old-2")]
    )
    run_experiment.assert_called_once_with(
        experiment_name="experiment-01-09-2026-10-30",
        dataset=dataset,
        task=task,
        evaluators=evaluators,
        experiment_metadata={"version": "1.0"},
    )
    assert result is run_experiment.return_value


def test_make_experiment_defaults_to_the_daily_dataset(client, monkeypatch):
    monkeypatch.setattr(helpers, "run_experiment", MagicMock())

    helpers.make_experiment(object(), [], experiment_name="custom")

    client.datasets.get_dataset.assert_called_once_with(dataset="dataset-01-09-2026")
    assert helpers.run_experiment.call_args.kwargs["experiment_name"] == "custom"


def test_create_experiment_creates_without_running(client):
    experiment, dataset = helpers.create_experiment(
        dataset="dataset-x", experiment_name="Test"
    )

    assert dataset is client.datasets.get_dataset.return_value
    client.experiments.create.assert_called_once_with(
        dataset_id=dataset.id,
        experiment_name="Test",
        experiment_metadata={"version": "1.0"},
    )
    assert experiment is client.experiments.create.return_value
    client.experiments.resume_experiment.assert_not_called()


def test_execute_experiment_resumes_the_created_experiment(client):
    task, evaluators = object(), [object()]

    helpers.execute_experiment({"id": "exp-1"}, task, evaluators)

    client.experiments.resume_experiment.assert_called_once_with(
        experiment_id="exp-1",
        task=task,
        evaluators=evaluators,
        print_summary=False,
    )
