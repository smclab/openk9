import json
import logging
import threading

from logging.config import dictConfig

import requests
from typing import Optional, List
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from starlette import status

from wordpress_api.data_extraction import DataExtraction, DataExtractionType
from wordpress_api.util.log_config import LogConfig

dictConfig(LogConfig().dict())
logger = logging.getLogger('wordpress_logger')

app = FastAPI()


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
	exc_str = f'{exc}'.replace('\n', ' ').replace('   ', ' ')
	logging.error(f"{request}: {exc_str}")
	content = {'status_code': 10422, 'message': exc_str, 'data': None}
	return JSONResponse(content=content, status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)


class WordpressRequest(BaseModel):
	hostName: str
	dataType: List[DataExtractionType]
	doAuth: bool
	username: Optional[str] = None
	password: Optional[str] = None
	itemsPerPage: Optional[int] = 10
	timestamp: int
	datasourceId: int
	scheduleId: str
	tenantId: str


@app.post('/getData')
def get_data(request: WordpressRequest):
	request = request.dict()

	host_name = request['hostName']
	data_type = request['dataType']
	do_auth = request['doAuth']
	username = request['username']
	password = request['password']
	items_per_page = request['itemsPerPage']
	timestamp = request['timestamp']
	datasource_id = request['datasourceId']
	schedule_id = request['scheduleId']
	tenant_id = request['tenantId']

	if do_auth and (username is None or password is None):
		logger.error('Username and Password expected')
		return 'extraction aborted: Username and Password expected'

	data_extraction = DataExtraction(data_type, host_name, do_auth, username, password, items_per_page, timestamp, datasource_id, schedule_id, tenant_id)

	thread = threading.Thread(target=data_extraction.extract_recent)
	thread.start()

	return 'extraction started'


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
	f = open('data/wordpress-form.json')

	# returns JSON object as
	# a dictionary
	data = json.load(f)

	f.close()

	return data
