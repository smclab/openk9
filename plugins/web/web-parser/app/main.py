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

from fastapi import FastAPI
from typing import Optional
from pydantic import BaseModel
import logging
import os
import requests
import json

app = FastAPI()

ingestion_url = os.environ.get("INGESTION_URL")
delete_url = os.environ.get("DELETE_URL")

logger = logging.getLogger("uvicorn.access")


class SitemapRequest(BaseModel):
    sitemapUrls: list
    bodyTag: str
    titleTag: Optional[str] = "title::text"
    datasourceId: int
    timestamp: int
    replaceRule: Optional[list] = ["", ""]
    allowedDomains: list
    maxLength: Optional[int] = None


class GenericRequest(BaseModel):
    startUrls: list
    allowedDomains: list
    allowedPaths: list
    excludedPaths: list
    bodyTag: str
    titleTag: Optional[str] = "title::text"
    pageCount: Optional[int] = 0
    depth: Optional[int] = 0
    datasourceId: int
    timestamp: int
    follow: Optional[bool] = True
    maxLength: Optional[int] = 0


def post_message(url, payload, timeout):
    try:
        r = requests.post(url, data=payload, timeout=timeout)
        if r.status_code == 200:
            return r.json()
        else:
            r.raise_for_status()
    except requests.RequestException as e:
        logger.error(str(e) + " during request at url: " + str(url))
        raise e


@app.post("/execute-generic")
def execute_generic(request: GenericRequest):

    request = request.dict()

    start_urls = request['startUrls']
    allowed_domains = request["allowedDomains"]
    allowed_paths = request["allowedPaths"]
    excluded_paths = request["excludedPaths"]
    body_tag = request["bodyTag"]
    title_tag = request["titleTag"]
    datasource_id = request['datasourceId']
    timestamp = request["timestamp"]
    page_count = request["pageCount"]
    depth = request["depth"]
    follow = request["follow"]

    max_length = request["maxLength"]

    payload = {
        "project": "crawler",
        "spider": "CustomGenericSpider",
        "start_urls": json.dumps(start_urls),
        "allowed_domains": json.dumps(allowed_domains),
        "allowed_paths": json.dumps(allowed_paths),
        "excluded_paths": json.dumps(excluded_paths),
        "body_tag": body_tag,
        "title_tag": title_tag,
        "setting": ["CLOSESPIDER_PAGECOUNT=%s" % page_count, "DEPTH_LIMIT=%s" % depth],
        "datasource_id": datasource_id,
        "ingestion_url": ingestion_url,
        "delete_url": delete_url,
        "timestamp": timestamp,
        "follow": follow,
        "max_length": max_length
    }

    if timestamp == 0:
        response = post_message("http://localhost:6800/schedule.json", payload, 10)
    else:
        response = {
            "status": "error",
            "message": "timestamp greater than zero"
        }

    if response["status"] == 'ok':
        return "Crawling process started with job " + str(response["jobid"])
    else:
        return response


@app.post("/execute-sitemap")
def execute(request: SitemapRequest):

    request = request.dict()

    sitemap_urls = request['sitemapUrls']
    body_tag = request["bodyTag"]
    title_tag = request["titleTag"]
    datasource_id = request['datasourceId']
    timestamp = request["timestamp"]
    allowed_domains = request["allowedDomains"]
    max_length = request["maxLength"]
    replace_rule = request["replaceRule"]

    payload = {
        "project": "crawler",
        "spider": "CustomSitemapSpider",
        "sitemap_urls": json.dumps(sitemap_urls),
        "allowed_domains": json.dumps(allowed_domains),
        "body_tag": body_tag,
        "title_tag": title_tag,
        "replace_rule": json.dumps(replace_rule),
        "datasource_id": datasource_id,
        "ingestion_url": ingestion_url,
        "delete_url": delete_url,
        "timestamp": timestamp,
        "max_length": max_length
    }

    response = post_message("http://localhost:6800/schedule.json", payload, 10)

    if response["status"] == 'ok':
        return "Crawling process started with job " + str(response["jobid"])
    else:
        return response


@app.post("/cancel-job/{job_id}")
def cancel_job(job_id: str):

    payload = {
        "project": "crawler",
        "job": str(job_id)
    }

    response = post_message("http://localhost:6800/cancel.json", payload, 10)

    if response["status"] == 'ok':
        return "Cancelled job with id " + str(job_id)
    else:
        return response


