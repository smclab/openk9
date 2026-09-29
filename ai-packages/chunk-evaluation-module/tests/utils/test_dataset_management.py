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

import utils.helpers as helpers

DATASETS = [
    {"name": "dataset-01-09-2026-0", "example_count": 150},
    {"name": "dataset-01-09-2026-1", "example_count": 20},
    {"name": "Other-Dataset", "example_count": 80},
]


def test_get_dataset_index_returns_all_datasets_without_filters():
    assert helpers.get_dataset_index(DATASETS) == DATASETS


def test_get_dataset_index_filters_by_name_ignoring_case():
    assert helpers.get_dataset_index(DATASETS, name_contains="other") == [
        DATASETS[2]
    ]


def test_get_dataset_index_filters_by_example_count_range():
    assert helpers.get_dataset_index(
        DATASETS, name_contains="dataset-01-09-2026", min_examples=150
    ) == [DATASETS[0]]
    assert helpers.get_dataset_index(DATASETS, min_examples=50, max_examples=100) == [
        DATASETS[2]
    ]


def test_manage_daily_dataset_adds_examples_to_an_existing_dataset(monkeypatch):
    client = MagicMock()
    monkeypatch.setattr(helpers, "client", client)

    result = helpers.manage_daily_dataset(
        dataset_name="dataset-x", input_item=["in"], output_item=["out"], metadata=[{}]
    )

    client.datasets.add_examples_to_dataset.assert_called_once_with(
        dataset="dataset-x",
        inputs=["in"],
        outputs=["out"],
        metadata=[{}],
        timeout=120,
    )
    client.datasets.create_dataset.assert_not_called()
    assert result is client.datasets.add_examples_to_dataset.return_value


def test_manage_daily_dataset_creates_a_missing_dataset(monkeypatch):
    client = MagicMock()
    client.datasets._get_dataset_id_by_name.side_effect = ValueError("not found")
    monkeypatch.setattr(helpers, "client", client)

    result = helpers.manage_daily_dataset(
        dataset_name="dataset-x", input_item=["in"], output_item=["out"], metadata=[{}]
    )

    client.datasets.create_dataset.assert_called_once_with(
        name="dataset-x", inputs=["in"], outputs=["out"], metadata=[{}]
    )
    client.datasets.add_examples_to_dataset.assert_not_called()
    assert result is client.datasets.create_dataset.return_value
