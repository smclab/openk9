"""
Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

from fastapi import FastAPI, Request, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional
import logging
import os
import requests
import json
import threading
from extraction.extractor import MinioExtractor
from extraction.excel_extractor import ExcelMinioExtractor

app = FastAPI()

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://ingestion:8080/api/ingestion/v1/ingestion/"

log_level = os.environ.get("INPUT_LOG_LEVEL")
if log_level is None:
    log_level = "INFO"

logger = logging.getLogger(__name__)


class MinioRequest(BaseModel):
    host: str
    port: str
    accessKey: str
    secretKey: str
    bucketName: str
    datasourceId: int
    scheduleId: str
    timestamp: int
    tenantId: str
    datasourcePayloadKey: Optional[str] = None
    prefix: Optional[str] = None
    columns: Optional[list] = []
    additionalMetadata: Optional[dict] = {}
    doTryExtractHeader: Optional[bool] = True


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    exc_str = f'{exc}'.replace('\n', ' ').replace('   ', ' ')
    logging.error(f"{request}: {exc_str}")
    content = {'status_code': 10422, 'message': exc_str, 'data': None}
    return JSONResponse(content=content, status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)


@app.post("/execute")
def execute(request: MinioRequest):
    request = request.dict()

    datasource_id = request['datasourceId']
    schedule_id = request['scheduleId']
    timestamp = request["timestamp"]
    tenant_id = request["tenantId"]

    host = request["host"]
    port = request["port"]
    access_key = request["accessKey"]
    secret_key = request["secretKey"]
    bucket_name = request["bucketName"]
    prefix = request["prefix"]
    additional_metadata = request["additionalMetadata"]

    extractor = MinioExtractor(host, port, access_key, secret_key, bucket_name, prefix, additional_metadata,
                               datasource_id, timestamp, schedule_id, tenant_id, ingestion_url)

    thread = threading.Thread(target=extractor.extract_data)
    thread.start()

    return "Extraction of files started"


@app.post("/extract-excel")
def execute(request: MinioRequest):
    request = request.dict()

    datasource_id = request['datasourceId']
    schedule_id = request['scheduleId']
    timestamp = request["timestamp"]
    tenant_id = request["tenantId"]

    host = request["host"]
    port = request["port"]
    access_key = request["accessKey"]
    secret_key = request["secretKey"]
    bucket_name = request["bucketName"]
    prefix = request["prefix"]
    datasource_payload_key = request["datasourcePayloadKey"]
    columns = request["columns"]
    do_try_extract_header = request["doTryExtractHeader"]

    extractor = ExcelMinioExtractor(host, port, access_key, secret_key, bucket_name, columns, prefix, datasource_payload_key,
                                datasource_id, timestamp, schedule_id, tenant_id, ingestion_url, do_try_extract_header)

    thread = threading.Thread(target=extractor.extract_data)
    thread.start()

    return "Extraction of files started"


class HealthCheck(BaseModel):
    """Response model to validate and return when performing a health check."""

    status: str = "UP"


@app.get(
    "/health",
    tags=["healthcheck"],
    summary="Perform a Health Check",
    response_description="Return HTTP Status Code 200 (OK)",
    status_code=status.HTTP_200_OK,
    response_model=HealthCheck,
)
def get_health() -> HealthCheck:
    """
    ## Perform a Health Check
    Endpoint to perform a healthcheck on. This endpoint can primarily be used Docker
    to ensure a robust container orchestration and management is in place. Other
    services which rely on proper functioning of the API service will not deploy if this
    endpoint returns any other HTTP status code except 200 (OK).
    Returns:
        HealthCheck: Returns a JSON response with the health status
    """

    try:
        fastapi_response = requests.get("http://localhost:5000/docs")
        if fastapi_response.status_code == 200:
            return HealthCheck(status="UP")
        else:
            return HealthCheck(status="DOWN")
    except requests.RequestException as e:
        logger.error(str(e) + " during request for health check")
        raise e


@app.get("/sample",
        tags=["sample"],
        summary="Get a sample of result",
        response_description="Return json sample result", )
def get_sample():
    f = open('data/sample.json')

    # returns JSON object as
    # a dictionary
    data = json.load(f)

    f.close()

    return data


@app.get("/form",
        tags=["form"],
        summary="Get form structure of request",
        response_description="Return json form structure", )
def get_sitemap_form():
    f = open('data/form.json')

    # returns JSON object as
    # a dictionary
    data = json.load(f)

    f.close()

    return data
