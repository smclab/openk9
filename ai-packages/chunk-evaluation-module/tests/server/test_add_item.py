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
def buffer(monkeypatch):
    client = MagicMock()
    client.datasets.list.return_value = [
        {"name": "dataset-base-0", "example_count": server.N_MAX_DOCUMENTS},
        {"name": "dataset-base-1", "example_count": 3},
    ]
    manage_daily_dataset = MagicMock()
    scheduler = MagicMock()
    monkeypatch.setattr(server, "client", client)
    monkeypatch.setattr(server, "manage_daily_dataset", manage_daily_dataset)
    monkeypatch.setattr(server, "scheduler", scheduler)
    monkeypatch.setattr(server, "input_data", [])
    monkeypatch.setattr(server, "output_data", [])
    monkeypatch.setattr(server, "metadata", [])
    monkeypatch.setattr(server, "last_ingestion", None)
    return manage_daily_dataset, scheduler


def test_add_item_flushes_into_the_first_dataset_not_yet_full(buffer):
    manage_daily_dataset, scheduler = buffer

    server.add_item("dataset-base", [{"text": "t"}], [{"expected": []}])

    # dataset-base-0 already holds N_MAX_DOCUMENTS examples.
    manage_daily_dataset.assert_called_once_with(
        dataset_name="dataset-base-1",
        input_item=[{"text": "t"}],
        output_item=[{"expected": []}],
        metadata=[{}],
    )
    assert server.input_data == []
    assert server.last_ingestion is not None
    scheduler.add_job.assert_not_called()


def test_add_item_buffers_and_schedules_a_flush_right_after_an_ingestion(
    buffer, monkeypatch
):
    manage_daily_dataset, scheduler = buffer
    monkeypatch.setattr(server, "last_ingestion", datetime.now())

    server.add_item("dataset-base", [{"text": "t"}], [{"expected": []}])

    manage_daily_dataset.assert_not_called()
    assert server.input_data == [{"text": "t"}]
    scheduler.add_job.assert_called_once()
    assert scheduler.add_job.call_args.kwargs["id"] == "debounce_flush_job"
    assert scheduler.add_job.call_args.kwargs["kwargs"] == {
        "dataset_base_name": "dataset-base",
        "input_item": [],
        "output_item": [],
    }
