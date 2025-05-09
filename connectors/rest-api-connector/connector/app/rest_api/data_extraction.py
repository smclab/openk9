import json
import threading
import logging
import os
import base64
import requests

from logging.config import dictConfig
from datetime import datetime
from .util.utility import format_raw_content, post_message, handle_response_content
from .util.base_model import RequestModel, AuthModel
from .util.log_config import LogConfig

dictConfig(LogConfig().dict())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
	ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"


class DataExtraction(threading.Thread):
	def __init__(self, request_list: list[RequestModel], auth: AuthModel | None, timestamp, datasource_id, schedule_id, tenant_id):

		super(DataExtraction, self).__init__()
		self.request_list = request_list
		self.auth = auth
		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.status_logger = logging.getLogger("rest_api_logger")

	def manage_data(self, response: requests.Response):
		try:
			end_timestamp = datetime.utcnow().timestamp() * 1000

			data = handle_response_content(response)
			if not data:
				raise Exception(f'{response.request} returned null content on handle_response_content')

			binary = None
			if isinstance(data, dict):
				datasource_payload = data
			elif isinstance(data, str):
				datasource_payload = json.loads(data)
			elif isinstance(data, bytes):
				datasource_payload = {}
				binary = {
					"id": 0,
					"name": "",
					"contentType": response.headers['content-type'],
					"data": data,
					"resourceId": None
				}
			
			raw_content_elements = []
			raw_content = format_raw_content(''.join(raw_content_elements))

			content_id = None

			payload = {
				"datasourceId": self.datasource_id,
				"scheduleId": self.schedule_id,
				"tenantId": self.tenant_id,
				"contentId": content_id,
				"parsingDate": int(end_timestamp),
				"rawContent": raw_content,
				"datasourcePayload": datasource_payload,
				"resources": {
					"binaries": [] if not binary else [binary]
				}
			}
			try:
				self.status_logger.info(datasource_payload)
				post_message(ingestion_url, payload, 10)
				return 1
			except requests.RequestException:
				self.status_logger.error("Problems during posting")
				return 0
		except Exception as e:
			payload = {
				"datasourceId": self.datasource_id,
				"scheduleId": self.schedule_id,
				"tenantId": self.tenant_id,
				"contentId": -1,
				"parsingDate": int(end_timestamp),
				"rawContent": e,
				"datasourcePayload": {

				},
				"resources": {
					"binaries": []
				},
				"type": "HALT"
			}
			post_message(ingestion_url, payload, 10)
			return 0

	def extract_recent(self):
		extraction_count = 0
		for request in self.request_list:
			auth = None
			if request.auth:
				auth = (request.auth.username, request.auth.password)
			elif self.auth:
				auth = (self.auth.username, self.auth.password)
			response = requests.request(method=request.requestMethod, url=request.requestUrl, auth=auth)
			self.manage_data(response)

		self.status_logger.info('Extracted: ' + str(extraction_count) + ' elements')
