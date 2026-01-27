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

import base64
from io import BytesIO

import requests


class FileManagerHelper:
    def __init__(self, host):
        self.host = host

    def delete(self, tenant, resource):
        response = requests.post(
            f"{self.host}/api/file-manager/v1/delete/{resource}/{tenant}"
        )
        pass

    def get_base64(self, tenant, resource):
        response = requests.get(
            f"{self.host}/api/file-manager/v1/download/base64/{resource}/{tenant}"
        )
        return response.text

    def get_bytes(self, tenant, resource):
        response = requests.get(
            f"{self.host}/api/file-manager/v1/download/byte/{resource}/{tenant}"
        )
        decoded_bytes = base64.b64decode(response.text)
        bites_io = BytesIO(decoded_bytes)
        return bites_io

    def download(self, tenant, resource):
        response = requests.get(
            f"{self.host}/api/file-manager/v1/download/{resource}/{tenant}"
        )
        pass

    def upload(self, tenant, sourceId, fileId):
        response = requests.get(
            f"{self.host}/api/file-manager/v1/upload/{sourceId}/{fileId}/{tenant}"
        )
        pass
