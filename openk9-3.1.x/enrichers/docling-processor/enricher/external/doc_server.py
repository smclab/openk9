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

import asyncio
import base64
import random
from io import BytesIO
from typing import Any, Dict, List, Literal, Union

import requests
from fastapi import FastAPI
from pydantic import BaseModel


class Response(BaseModel):
    resources: Dict[str, Any]


class ResponseMulti(BaseModel):
    binaries: List[Dict[str, Any]]


class ResponseSingle(BaseModel):
    document: Dict[Literal["markdown"], Any]


ResponseModel = Union[ResponseMulti, ResponseSingle]


app = FastAPI()


@app.get("/payload/")
def get_random_string():
    input = {
        "payload": {
            "tenantId": "mrossi",
            "resources": {
                "binaries": [
                    # {"resourceId": "doc_error", "metadata_vari": "metadato_error"},
                    # {"resourceId": "doc_2", "metadata_vari": "metadato_2"},
                    {"resourceId": "doc_1", "metadata_vari": "metadato_1"},
                ]
            },
        },
        "enrichItemConfig": {
            "configs": "Config passata",
            "error_strategy": "fail-soft",
        },
        "replyTo": "fake-token",
    }
    response = requests.post("http://127.0.0.1:8002/start-task/", json=input)
    print("Status:", response.status_code)
    print("Response:", response.json())


@app.post("/api/datasource/pipeline/callback/{token}")
def cose(response: ResponseModel):
    print("Response: \n", response)
