from abc import ABC

from fastapi import FastAPI, Request, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse, FileResponse
from pydantic import BaseModel, ValidationInfo, field_validator, model_validator
from typing import Optional, Self
import json
import logging
import os
import requests
import os

log_level = os.environ.get("INPUT_LOG_LEVEL")
if log_level is None:
    log_level = "INFO"

logger = logging.getLogger(__name__)
logger.setLevel(log_level)
formatter = logging.Formatter("%(asctime)s,%(msecs)d \t %(levelname)s \t| %(name)s | %(message)s")
console_handler = logging.StreamHandler()
console_handler.setFormatter(formatter)
logger.addHandler(console_handler)


logger = logging.getLogger(__name__)

def post_message(url, payload, timeout=30):
    '''Pass the body as json instead of data'''
    try:
        r = requests.post(url, data=payload, timeout=timeout)
        if r.status_code == 200:
            return r.json()
        else:
            r.raise_for_status()
    except requests.RequestException as e:
        logger.error(str(e) + " during request at url: " + str(url))
        raise e


app = FastAPI()

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://ingestion:8080/api/ingestion/v1/ingestion/"


class BaseRequest(ABC, BaseModel):
    bodyTag: Optional[str] = "body"
    titleTag: Optional[str] = "title::text"
    excludedBodyTags: Optional[list] = []
    allowedDomains: Optional[list] = []
    excludedPaths: Optional[list] = []
    allowedPaths: Optional[list] = []
    maxLength: Optional[int] = -1
    documentFileExtensions: Optional[list] = []
    customMetadata: Optional[dict] = {}
    pageCount: int = 0
    additionalMetadata: Optional[dict] = {}
    doExtractDocs: bool = False
    certVerification: bool = True
    maxSizeBytes: int
    datasourceId: int
    scheduleId: str
    timestamp: int
    tenantId: str = ""
    doUseDefaultMimetypeMap: Optional[bool] = True
    mimetypeMap: Optional[dict] = {}

    @model_validator(mode='after')
    def log_failed_validation(self) -> Self:
        try:
            assert self.doUseDefaultMimetypeMap or self.mimetypeMap, \
                f"If `doUseDefaultMimetypeMap` is set to False `mimetypeMap` must be provided"
            return self
        except AssertionError as e:
            logger.error(e)
            raise


class SitemapRequest(BaseRequest):
    sitemapUrls: list
    replaceRule: Optional[list] = ["", ""]
    linksToFollow: Optional[list[str]] = []


class CrawlRequest(BaseRequest):
    startUrls: list
    depth: Optional[int] = 0
    follow: Optional[bool] = True


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    exc_str = f'{exc}'.replace('\n', ' ').replace('   ', ' ')
    logger.error(f"{request}: {exc_str}")
    content = {'status_code': 10422, 'message': exc_str, 'data': None}
    return JSONResponse(content=content, status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)


def get_base_request_payload(request):
    body_tag = request["bodyTag"]
    excluded_body_tags = request["excludedBodyTags"]
    title_tag = request["titleTag"]
    allowed_domains = request["allowedDomains"]
    max_length = request["maxLength"]
    tenant_id = request["tenantId"]
    links_to_follow = request["linksToFollow"]
    excluded_paths = request["excludedPaths"]
    allowed_paths = request["allowedPaths"]
    max_length = request["maxLength"]
    document_file_extensions = request["documentFileExtensions"]
    custom_metadata = request["customMetadata"]
    page_count = request["pageCount"]
    additional_metadata = request["additionalMetadata"]
    do_extract_docs = request["doExtractDocs"]
    cert_verification = request["certVerification"]
    max_size_bytes = request["maxSizeBytes"]
    datasource_id = request['datasourceId']
    schedule_id = request['scheduleId']
    timestamp = request["timestamp"]
    tenant_id = request["tenantId"]
    do_use_default_mimetype_map = request["doUseDefaultMimetypeMap"]
    mimetype_map = request["mimetypeMap"]

    payload = {
        "allowed_domains": json.dumps(allowed_domains),
        "body_tag": body_tag,
        "excluded_body_tags": json.dumps(excluded_body_tags),
        "title_tag": title_tag,
        "datasource_id": datasource_id,
        "schedule_id": schedule_id,
        "ingestion_url": ingestion_url,
        "timestamp": timestamp,
        "max_length": max_length,
        "max_size_bytes": max_size_bytes,
        "tenant_id": tenant_id,
        "replace_rule": json.dumps(replace_rule),
        "links_to_follow": json.dumps(links_to_follow),
        "excluded_paths": json.dumps(excluded_paths),
        "allowed_paths": json.dumps(allowed_paths),
        "document_file_extensions": json.dumps(document_file_extensions),
        "do_use_default_mimetype_map": json.dumps(do_use_default_mimetype_map),
        "mimetype_map": json.dumps(mimetype_map),
        "custom_metadata": json.dumps(custom_metadata),
        "do_extract_docs": json.dumps(do_extract_docs),
        "cert_verification": json.dumps(cert_verification),
        "additional_metadata": json.dumps(additional_metadata),
    }

    return payload


def set_up_sitemap_endpoint(request):
    request = request.dict()

    logging.info("======= RECEIVED SITEMAP REQUEST =======")
    logger.info(request)

    sitemap_urls = request['sitemapUrls']
    links_to_follow = request["linksToFollow"]
    use_playwright = request["usePlaywright"]
    playwright_selector = request["playwrightSelector"]
    playwright_timeout = request["playwrightTimeout"]
    page_count = request["pageCount"]
    replace_rule = request["replaceRule"]

    payload = {
        "project": "generic_crawler",
        "spider": "genericSitemapSpider",
        "sitemap_urls": json.dumps(sitemap_urls),
        "ingestion_url": ingestion_url,
        "replace_rule": json.dumps(replace_rule),
        "links_to_follow": json.dumps(links_to_follow),
        "use_playwright": use_playwright,
        "playwright_selector": playwright_selector,
        "playwright_timeout": playwright_timeout,
        "setting": ["CLOSESPIDER_PAGECOUNT=%s" % page_count, "LOG_LEVEL=%s" % log_level],
    }

    base_request_payload = get_base_request_payload(request)
    payload.update(base_request_payload)

    logging.info("======= GENERATED SITEMAP PAYLOAD =======")
    logging.info(payload)

    return payload


def set_up_crawl_endpoint(request):
    request = request.dict()

    logging.info("======= RECEIVED CRAWL REQUEST =======")
    logging.info(request)

    start_urls = request['startUrls']
    depth = request["depth"]
    follow = request["follow"]
    close_spider_page_count = request["pageCount"]

    payload = {
        "project": "generic_crawler",
        "spider": "genericCrawlSpider",
        "start_urls": json.dumps(start_urls),
        "ingestion_url": ingestion_url,
        "follow": follow,
        "setting": ["CLOSESPIDER_PAGECOUNT=%s" % close_spider_page_count, "DEPTH_LIMIT=%s" % depth, "LOG_LEVEL=%s" % log_level],
    }

    base_request_payload = get_base_request_payload(request)
    payload.update(base_request_payload)

    logging.info("======= GENERATED CRAWL PAYLOAD =======")
    logging.info(payload)

    return payload


@app.post("/startSitemapCrawling",
          tags=["sitemap urls crawling"],
          summary="Perform a generic crawling starting from sitemap urls",
          response_description="Return HTTP Status Code 200 (OK)",
          status_code=status.HTTP_200_OK
          )
def execute_sitemap_request(request: SitemapRequest):
    payload = set_up_sitemap_endpoint(request)
    response = post_message("http://localhost:6800/schedule.json", payload)

    if response and response["status"] == 'ok':
        logging.info("Crawling process started")
        return "Crawling process started with job " + str(response["jobid"])
    else:
        return response


@app.post("/startUrlsCrawling",
          tags=["generic urls crawling"],
          summary="Perform a generic crawling starting from urls",
          response_description="Return HTTP Status Code 200 (OK)",
          status_code=status.HTTP_200_OK
          )
def execute_crawl_request(request: CrawlRequest):
    payload = set_up_crawl_endpoint(request)
    response = post_message("http://localhost:6800/schedule.json", payload)

    if response and response["status"] == 'ok':
        logging.info("Crawling process started")
        return "Crawling process started with job " + str(response["jobid"])
    else:
        return response    


@app.get("/getResults/{spider_name}/{job_id}")
def get_results(spider_name: str, job_id: str):
    result_csv_file = f"result/{spider_name}/output_{job_id}.csv"
    response = FileResponse(result_csv_file, media_type="text/csv")
    response.headers["Content-Disposition"] = f"attachment; filename=output_{job_id}.csv"
    return response


@app.post("/cancel-job/{project}/{job_id}")
def cancel_job(project: str, job_id: str):
    payload = {
        "project": str(project),
        "job": str(job_id)
    }

    response = post_message("http://localhost:6800/cancel.json", payload, 10)

    if response and response["status"] == 'ok':
        return "Cancelled job with id " + str(job_id)
    else:
        return response


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
        scrapyd_response = requests.get("http://localhost:6800")
        if fastapi_response.status_code == 200 and scrapyd_response.status_code == 200:
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
