import os
import threading

import requests
from dotenv import load_dotenv
from fastapi import FastAPI
from pydantic import BaseModel

from app.utils.converter import conversion
from app.utils.exceptions import handle_exception

load_dotenv()

DATASOURCE_HOST = os.getenv("DATASOURCE_HOST", default="http://localhost:8001")


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
    try:
        binaries = [
            b for b in payload["resources"].get("binaries", []) if "resourceId" in b
        ]
        tenant = payload["tenantId"]
    except Exception as e:
        handle_exception(e)

    print("Starting process")
    if len(binaries) > 1:
        for bin in binaries:
            try:
                result = conversion(bin, tenant)
                markdown = result.document.export_to_markdown()
                bin["markdown"] = markdown
            except Exception as e:
                handle_exception(e)

            print("Process ended")
            response = {"binaries": binaries}

    elif len(binaries) == 1:
        try:
            result = conversion(bin, tenant)
            markdown = result.document.export_to_markdown()
        except Exception as e:
            handle_exception(e)

        print("Process ended")
        response = {"document": markdown}

    else:
        response = {}

    response = requests.post(
        f"{DATASOURCE_HOST}/api/datasource/pipeline/callback/{token}", json=response
    )
    print("Status:", response.status_code)
