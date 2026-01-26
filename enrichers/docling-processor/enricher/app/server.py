import os
import threading

import requests
from dotenv import load_dotenv
from fastapi import FastAPI
from pydantic import BaseModel

from app.utils.converter import conversion
from app.utils.exceptions import handle_exception
from app.utils.logger import logger

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
    response = {}
    try:
        binaries = [
            b for b in payload["resources"].get("binaries", []) if "resourceId" in b
        ]
        tenant = payload["tenantId"]
    except Exception as e:
        handle_exception(e)

    error_strategy = configs.get("error_strategy", "fail_fast")
    logger.info(f"Error strategy: {error_strategy}")

    logger.info("Starting process")
    if len(binaries) > 1:
        logger.info("Multiple binary")
        for bin in binaries:
            try:
                result = conversion(bin, tenant)
                markdown = result.document.export_to_markdown()
                bin["markdown"] = markdown
            except Exception as e:
                error = handle_exception(e)
                logger.error(error)
                if error_strategy == "fail-soft":
                    # fail-soft: isola l’errore
                    bin["error"] = str(error)
                    continue

                elif error_strategy == "fail-fast":
                    # invalida tutto e interrompe
                    response = {"error": "conversion failed"}
                    break

                else:
                    # invalida tutto e interrompe
                    response = {"error": "conversion failed"}
                    break
        response = {"binaries": binaries}
        logger.info("Process ended")

    elif len(binaries) == 1:
        logger.info("Single binary")
        try:
            result = conversion(binaries[0], tenant)
            markdown = result.document.export_to_markdown()
            response = {"document": {"markdown": markdown}}
        except Exception as e:
            error = handle_exception(e)
            logger.error(error)

        logger.info("Process ended")

    request_response = requests.post(
        f"{DATASOURCE_HOST}/api/datasource/pipeline/callback/{token}", json=response
    )
    logger.info(f"Status: {request_response.status_code}")
