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

from unittest.mock import patch

import pytest
from fastapi.testclient import TestClient

import app.server as server

VALID_INPUT = {
    "payload": {"resources": {"binaries": []}, "tenantId": "t"},
    "enrichItemConfig": {},
    "replyTo": "tok",
}


def test_valid_input_starts_the_task():
    with patch.object(server, "EXECUTOR") as executor:
        response = TestClient(server.app).post("/start-task/", json=VALID_INPUT)

    assert response.status_code == 200
    assert response.json()["status"] == "ok"
    executor.submit.assert_called_once()


@pytest.mark.parametrize(
    "body",
    [
        {k: v for k, v in VALID_INPUT.items() if k != "replyTo"},
        {k: v for k, v in VALID_INPUT.items() if k != "payload"},
        {**VALID_INPUT, "payload": "not a dict"},
        {**VALID_INPUT, "enrichItemConfig": ["not", "a", "dict"]},
    ],
    ids=["no-reply-to", "no-payload", "payload-not-a-dict", "config-not-a-dict"],
)
def test_invalid_input_is_rejected_without_starting_the_task(body):
    with patch.object(server, "EXECUTOR") as executor:
        response = TestClient(server.app).post("/start-task/", json=body)

    assert response.status_code == 422
    executor.submit.assert_not_called()
