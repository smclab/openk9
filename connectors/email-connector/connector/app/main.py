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

import threading
import json
import logging
import requests
from fastapi import FastAPI, status
from pydantic import BaseModel
from typing import Optional
from imap.imap_extraction import AsyncEmailExtraction

app = FastAPI()

logger = logging.getLogger("uvicorn.access")

class ImapRequest(BaseModel):
    mailServer: str
    port: str
    useSsl: Optional[bool] = True
    username: str
    password: str
    timestamp: int
    datasourceId: int
    scheduleId: str
    folder: str
    tenantId: str
    indexAcl: Optional[bool] = False
    getAttachments: Optional[bool] = False
    additionalMetadata: Optional[dict] = {}


@app.post("/execute")
def get_data(request: ImapRequest):

    request = request.dict()

    mail_server = request['mailServer']
    port = request['port']
    use_ssl = request['useSsl']
    username = request["username"]
    password = request["password"]
    datasource_id = request["datasourceId"]
    timestamp = request["timestamp"]
    folder = request["folder"]
    schedule_id = request["scheduleId"]
    tenant_id = request["tenantId"]
    index_acl = request["indexAcl"]
    get_attachments = request["getAttachments"]
    additional_metadata = request["additionalMetadata"]

    email_extraction_task = AsyncEmailExtraction(mail_server, port, use_ssl, username, password, timestamp, datasource_id,
                                                 folder, schedule_id, tenant_id, index_acl, get_attachments,
                                                 additional_metadata)

    thread = threading.Thread(target=email_extraction_task.extract)
    thread.start()

    return "extraction started"


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
        tags=["sitemap-form"],
        summary="Get form structure of Sitemap request",
        response_description="Return json form structure", )
def get_sitemap_form():
    f = open('data/sitemap-form.json')

    # returns JSON object as
    # a dictionary
    data = json.load(f)

    f.close()

    return data
