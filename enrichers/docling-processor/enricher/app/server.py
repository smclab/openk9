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

import os
from concurrent.futures import ThreadPoolExecutor

import requests
from fastapi import FastAPI
from pydantic import BaseModel

from app.config import settings
from app.utils.converter import conversion, fetch_to_file
from app.utils.exceptions import handle_exception
from app.utils.logger import logger

DATASOURCE_HOST = settings.datasource_host
# Conversions are CPU-bound and a single one peaks around 3 GiB (docling loads
# the OCR, layout and table models, and each worker keeps its own), so the pool
# is deliberately serial by default: raising it multiplies the resident memory
# and makes every single conversion slower.
MAX_CONCURRENT_CONVERSIONS = settings.max_concurrent_conversions
CALLBACK_TIMEOUT_SECONDS = settings.callback_timeout_seconds
# Fetches are I/O-bound and run as soon as a task arrives, ahead of the
# conversion queue: the pre-signed URLs expire within minutes, while a task may
# wait much longer for its turn to convert.
MAX_CONCURRENT_FETCHES = settings.max_concurrent_fetches

EXECUTOR = ThreadPoolExecutor(
    max_workers=MAX_CONCURRENT_CONVERSIONS, thread_name_prefix="conversion"
)
FETCH_EXECUTOR = ThreadPoolExecutor(
    max_workers=MAX_CONCURRENT_FETCHES, thread_name_prefix="fetch"
)


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
    FETCH_EXECUTOR.submit(
        enqueue, payload=payload, configs=enrich_item_config, token=token
    )
    return {"status": "ok", "message": "Proces started"}


@app.get(
    "/health",
    summary="Check the health status of the Docling Processor",
    description="Returns the current health status of the Docling Processor.",
    tags=["Health Checks"],
)
def health_check():
    return {"status": "UP"}


def enqueue(payload, configs, token):
    """
    Fetches the payload binaries while their pre-signed URLs are fresh, then
    queues the conversion.

    A binary that cannot be fetched here is left to the conversion, which tries
    its URL again and reports the failure through the callback.
    """
    staged = {}
    try:
        for index, binary in enumerate(_fetchable_binaries(payload)):
            try:
                staged[index] = fetch_to_file(binary["url"])
            except Exception as e:
                # the URL is a credential: only the status or the error type
                reason = str(e) if isinstance(e, requests.HTTPError) else type(e).__name__
                logger.warning(f"Early fetch of binary {index} failed: {reason}")
    except Exception as e:
        # a malformed payload: operation() reports it through the callback
        logger.warning(f"Early fetch skipped: {handle_exception(e)}")
    finally:
        EXECUTOR.submit(
            operation, payload=payload, configs=configs, token=token, staged=staged
        )


def operation(payload, configs, token, staged=None):
    """
    Converts the payload binaries and always answers the enrich callback.

    The callback is sent from a `finally` block: the enrich item that started
    the task waits for it, so a task that dies without answering leaves the
    ingestion pipeline hanging until its request timeout expires. The files
    the binaries were fetched into are removed there too.
    """
    staged = staged or {}
    response = {}
    try:
        response = _convert_binaries(payload, configs, staged)
    except Exception as e:
        error = handle_exception(e)
        logger.error(error)
        response = {"error": error}
    finally:
        _send_callback(token, response)
        _remove_staged(staged)


def _fetchable_binaries(payload):
    return [b for b in payload["resources"].get("binaries", []) if "url" in b]


def _remove_staged(staged):
    for path in staged.values():
        try:
            os.remove(path)
        except OSError as e:
            logger.warning(f"Cannot remove a fetched binary: {e}")


def _convert_binaries(payload, configs, staged):
    binaries = _fetchable_binaries(payload)

    error_strategy = configs.get("error_strategy", "fail-fast")
    logger.info(f"Error strategy: {error_strategy}")

    logger.info("Starting process")
    if len(binaries) > 1:
        logger.info("Multiple binary")
        for index, bin in enumerate(binaries):
            try:
                result = conversion(bin, configs, local_path=staged.get(index))
                markdown = result.document.export_to_markdown()
                bin["markdown"] = markdown
            except Exception as e:
                error = handle_exception(e)
                logger.error(error)
                if error_strategy == "fail-soft":
                    # fail-soft: isolate the error
                    bin["error"] = str(error)
                    continue

                # fail-fast, and any unknown strategy: invalidate and stop
                logger.info("Process ended")
                return {"error": "conversion failed"}

        logger.info("Process ended")
        return {"binaries": binaries}

    if len(binaries) == 1:
        logger.info("Single binary")
        try:
            result = conversion(binaries[0], configs, local_path=staged.get(0))
            markdown = result.document.export_to_markdown()
            response = {"document": {"markdown": markdown}}
        except Exception as e:
            error = handle_exception(e)
            logger.error(error)
            response = {"error": "conversion failed"}

        logger.info("Process ended")
        return response

    # A document with no binaries is not an error: there is nothing to merge
    # back into it.
    return {}


def _send_callback(token, response):
    try:
        request_response = requests.post(
            f"{DATASOURCE_HOST}/api/datasource/pipeline/callback/{token}",
            json=response,
            timeout=CALLBACK_TIMEOUT_SECONDS,
        )
        logger.info(f"Status: {request_response.status_code}")
    except Exception as e:
        logger.error(f"Callback failed: {handle_exception(e)}")
