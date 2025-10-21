import asyncio
import base64
import random
from io import BytesIO

import requests
from fastapi import FastAPI
from pydantic import BaseModel


class MarkdownR(BaseModel):
    markdown: str


app = FastAPI()


@app.get("/payload/")
def get_random_string():
    input = {
        "payload": {
            "tenantId": "mrossi",
            "resources": {"binaries": [{"resourceId": "doc_2"}]},
        },
        "enrichItemConfig": {"configs": "Config passata"},
        "replyTo": "fake-token",
    }
    response = requests.post("http://127.0.0.1:8002/start-task/", json=input)
    print("Status:", response.status_code)
    print("Response:", response.json())


@app.post("/api/datasource/pipeline/callback/{token}")
def cose(markdown_response: MarkdownR):
    print("Result: \n", markdown_response.markdown[:200])
