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
