import os
from io import BytesIO
from typing import Dict, List

from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream
from dotenv import dotenv_values, load_dotenv
from fastapi import Body, FastAPI

from app.utils.fm_helper import FileManagerHelper

load_dotenv()

# accessing and printing value

import asyncio
import base64
import threading
import time

import requests
from pydantic import BaseModel

FMHelper = FileManagerHelper(os.getenv("FM_HOST", default="http://localhost:8001"))


class Input(BaseModel):
    payload: dict
    enrichItemConfig: dict
    replyTo: str


app = FastAPI()


@app.post(
    "/start-task/",
    summary="Start a document processing task",
    description=(
        """Receives a payload from Openk9, starts the processing in background, 
        and sends the results to the callback endpoint specified in `replyTo`."""
    ),
    tags=["Task Execution"],
    response_description="Returns the process start status",
)
async def start_task(input: Input):
    payload = input.payload
    enrich_item_config = input.enrichItemConfig
    token = input.replyTo
    thread = threading.Thread(
        target=operation,
        kwargs={"payload": payload, "configs": enrich_item_config, "token": token},
    )
    thread.start()
    return {"status": "ok", "message": "Proces started"}


@app.get(
    "/health",
    summary="Check the health status of the Docling Processor",
    description="Returns the current health status of the Docling Processor.",
    tags=["Health Checks"],
)
def health_check():
    return {"status": "UP"}


@app.get(
    "/form",
    summary="Return configuration form definition for the connector",
    description=(
        "Returns a JSON schema describing the configuration fields required "
        "to set up the connector in the Openk9 UI."
    ),
    tags=["Connector Configuration"],
    response_description="Form structure with field definitions",
)
def form():
    response = {"fields": []}
    return response


def operation(payload, configs, token):
    s_host = os.getenv("S_HOST", default="http://localhost:8000")

    binaries = [
        b for b in payload["resources"].get("binaries", []) if "resourceId" in b
    ]
    tenant = payload["tenantId"]

    print(binaries)
    print("Starting process")
    for bin in binaries:
        resource_id = bin.get("resourceId")
        resource = FMHelper.get_base64(tenant, resource_id)
        # bites = BytesIO(base64.b64decode(resource))
        # source = DocumentStream(name="doc.docx", stream=bites)
        # converter = DocumentConverter()
        # result = converter.convert(source)
        # markdown = result.document.export_to_markdown()
        bin["markdown"] = "test"  # markdown

    print("Process ended")
    print(binaries)
    response = {"resources": {binaries}}
    response = requests.post(
        f"{s_host}/api/datasource/pipeline/callback/{token}", json=response
    )  # body json
    print("Status:", response.status_code)
