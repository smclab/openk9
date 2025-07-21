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
import threading
from extraction.extractor import MinioExtractor
from extraction.excel_extractor import ExcelMinioExtractor

app = FastAPI()

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://openk9-ingestion:8080/api/ingestion/v1/ingestion/"

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
    prefix: Optional[str] = None
    additionalMetadata: Optional[dict] = {}
    columns: Optional[list] = []


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