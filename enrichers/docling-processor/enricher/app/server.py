import asyncio
import base64
import os
import threading
import time
from io import BytesIO
from typing import Dict, List

import requests
from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream
from dotenv import dotenv_values, load_dotenv
from fastapi import Body, FastAPI
from pydantic import BaseModel

from app.utils.fm_helper import FileManagerHelper

load_dotenv()

FILE_MANAGER_HOST = os.getenv("FILE_MANAGER_HOST", default="http://localhost:8000")
DATASOURCE_HOST = os.getenv("DATASOURCE_HOST", default="http://localhost:8001")
FMHelper = FileManagerHelper(FILE_MANAGER_HOST)


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
    binaries = [
        b for b in payload["resources"].get("binaries", []) if "resourceId" in b
    ]
    tenant = payload["tenantId"]

    print("Starting process")
    for bin in binaries:
        try:
            resource_id = bin.get("resourceId")
            resource = FMHelper.get_base64(tenant, resource_id)
            bites = BytesIO(base64.b64decode(resource))
            source = DocumentStream(name="doc.docx", stream=bites)
            converter = DocumentConverter()
            result = converter.convert(source)
            markdown = result.document.export_to_markdown()
            bin["markdown"] = markdown
        except (base64.binascii.Error, ValueError) as e:
            print(f"base64 error: {str(e)}")
        except AttributeError as e:
            print(f"export error: {str(e)}")
        except Exception as e:
            print(f"generic error: {str(e)}")

    print("Process ended")
    response = {"binaries": binaries}
    response = requests.post(
        f"{DATASOURCE_HOST}/api/datasource/pipeline/callback/{token}", json=response
    )
    print("Status:", response.status_code)
