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

FMHelper = FileManagerHelper(os.getenv("FM_HOST"))


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
    return {"status": "ok", "message": f"Proces started"}

def operation(payload,configs,token):
    s_host=os.getenv("S_HOST")

    resource_ids = [
        b.get("resourceId")
        for b in payload["resources"].get("binaries", [])
        if "resourceId" in b
    ]
    tenant = payload["tenantId"]

    resources = [
        FMHelper.getBase64(tenant, resource_id) for resource_id in resource_ids
    ]
    bites = [BytesIO(base64.b64decode(resource)) for resource in resources]

    print("Starting process")

    source = DocumentStream(name="doc.docx", stream=bites[0])

    converter = DocumentConverter()
    result = converter.convert(source)
    markdown = result.document.export_to_markdown()
    print("Process ended")
    res = {"markdown": markdown}
    response = requests.post(
        f"{s_host}/api/datasource/pipeline/callback/{token}", json=res
    )  # body json
    print("Status:", response.status_code)
