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
