import threading
import logging
import requests
import json

from typing import Dict, Optional
from fastapi import FastAPI
from pydantic import BaseModel, model_validator
from starlette import status
from logging.config import dictConfig

from youtube_api.data_extraction import DataExtraction
from youtube_api.util.log_config import LogConfig

dictConfig(LogConfig().dict())
logger = logging.getLogger("youtube_logger")

app = FastAPI()


class YoutubeRequest(BaseModel):
	youtubeChannelUrl: str
	subtitleLang: list
	doExtractAudio: bool = False
	audioFormat: str = "m4a"
	doExtractComments: bool = False
	maxTotalComments: int = None
	maxRootComments: int = None
	maxTotalReplies: int = None
	maxRootCommentsReplies: int = None
	socketTimeout: int = 20
	sleepIntervalRequests: int = 5
	sleepInterval: int = 5
	doUseRandomWaitTime: bool = True
	maxSleepInterval: int = 30
	retriesCount: int = 10
	maxReadBytesSize: int = 2048
	timestamp: int
	datasourceId: int
	scheduleId: str
	tenantId: str

	@model_validator(mode='after')
	def validate(self) -> 'YoutubeRequest':
		if self.doUseRandomWaitTime and self.maxSleepInterval < self.sleepInterval:
			logger.warning(f"maxSleepInterval less than sleepInterval: Setting maxSleepInterval to sleepInterval + 5 seconds")
			self.maxSleepInterval = self.sleepInterval + 5
		return self


@app.post('/getData')
def get_data(request: YoutubeRequest):
	request = request.dict()

	youtube_channel_url = request['youtubeChannelUrl']
	subtitle_lang = request['subtitleLang']
	do_extract_audio = request['doExtractAudio']
	audio_format = request['audioFormat']
	timestamp = request["timestamp"]
	datasource_id = request["datasourceId"]
	schedule_id = request['scheduleId']
	tenant_id = request['tenantId']

	socket_timeout = request['socketTimeout']
	sleep_interval_requests = request['sleepIntervalRequests']
	sleep_interval = request['sleepInterval']
	do_use_random_wait_time = request['doUseRandomWaitTime']
	max_sleep_interval = request['maxSleepInterval']
	retries_count = request['retriesCount']
	max_read_bytes_size = request['maxReadBytesSize']

	do_extract_comments = request['doExtractComments']
	max_total_comments = request['maxTotalComments']
	max_root_comments = request['maxRootComments']
	max_total_replies = request['maxTotalReplies']
	max_root_comments_replies = request['maxRootCommentsReplies']
	max_comments = [str(max_total_comments or 'all'), str(max_root_comments or 'all'), str(max_total_replies or 'all'), str(max_root_comments_replies or 'all')]

	data_extraction = DataExtraction(youtube_channel_url, subtitle_lang, do_extract_audio, audio_format, do_extract_comments, max_comments, socket_timeout, sleep_interval_requests, sleep_interval, do_use_random_wait_time, max_sleep_interval, retries_count, max_read_bytes_size, timestamp, datasource_id, schedule_id, tenant_id)

	thread = threading.Thread(target=data_extraction.extract_recent)
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
	f = open('data/form.json')

	# returns JSON object as
	# a dictionary
	data = json.load(f)

	f.close()

	return data
