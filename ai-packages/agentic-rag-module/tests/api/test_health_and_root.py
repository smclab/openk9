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


"""The health check answers UP and the root redirects to the API docs."""

from fastapi.testclient import TestClient

import app.server as server


def test_health_is_up():
    response = TestClient(server.app).get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "UP"}


def test_root_redirects_to_the_docs():
    response = TestClient(server.app).get("/", follow_redirects=False)

    assert response.status_code == 307
    assert response.headers["location"] == "/docs"
