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


def test_db_list_returns_the_dataset_names(monkeypatch):
    client = MagicMock()
    client.datasets.list.return_value = [{"name": "dataset-a"}, {"name": "dataset-b"}]
    monkeypatch.setattr(api, "client", client)

    response = TestClient(api.app).post("/db_list")

    assert response.status_code == 200
    assert response.json() == ["dataset-a", "dataset-b"]


def test_get_metrics_lists_the_available_metrics():
    response = TestClient(api.app).post("/get_metrics")

    assert response.status_code == 200
    assert response.json() == {
        "available_metrics": [
            "semantic_choerence",
            "redundancy_bloat",
            "layout_fidelity",
        ]
    }
