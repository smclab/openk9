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

"""The Vertex credentials of the embedding model are written as an ADC file,
and GOOGLE_APPLICATION_CREDENTIALS points the Google clients at it."""

import json
import os

import pytest

from app import server as server_module

CREDENTIALS = {
    "type": "authorized_user",
    "client_id": "client-id",
    "quota_project_id": "project-id",
}


@pytest.fixture(autouse=True)
def _restore_environment(monkeypatch):
    # records the original value, so what the function writes is undone
    monkeypatch.setenv("GOOGLE_APPLICATION_CREDENTIALS", "")


def test_credentials_are_written_and_exported(tmp_path):
    server_module.save_google_application_credentials(CREDENTIALS, f"{tmp_path}/")

    credential_file = tmp_path / "application_default_credentials.json"
    assert json.loads(credential_file.read_text(encoding="utf-8")) == CREDENTIALS
    assert os.environ["GOOGLE_APPLICATION_CREDENTIALS"] == str(credential_file)


def test_an_unwritable_directory_is_reported(tmp_path):
    with pytest.raises(OSError, match="Failed to write credentials file"):
        server_module.save_google_application_credentials(
            CREDENTIALS, f"{tmp_path}/missing/"
        )

    assert os.environ["GOOGLE_APPLICATION_CREDENTIALS"] == ""
