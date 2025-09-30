import requests

class FileManagerHelper():
    def __init__(self,host):
        self.host=host

    def getBase64(self,tenant,resource):
        response = requests.get(f"{self.host}/api/file-manager/v1/download/base64/{resource}/{tenant}")
        return response.text
