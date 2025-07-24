import json
import threading
import logging
import os
import base64
import requests
from typing import Union
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

	def manage_data(self, response: requests.Response) -> Union[int, bool]:
		count = 0
		end_timestamp = datetime.utcnow().timestamp() * 1000

		try:
			# Checks for successful responses or raise HTTPError
			response.raise_for_status()

			data = handle_response_content(response)
			if not data:
				raise Exception(f'{response.request} returned null content on handle_response_content')

			if isinstance(data.dict_item, list):
				#TODO: Check if can do post_message for every item
				data.datasource_payload.update({'items': data.dict_item})
			else:
				data.datasource_payload.update(data.dict_item)

			payload = {
				"datasourceId": self.datasource_id,
				"scheduleId": self.schedule_id,
				"tenantId": self.tenant_id,
				"contentId": data.content_id,
				"parsingDate": int(end_timestamp),
				"rawContent": data.raw_content,
				"datasourcePayload": data.datasource_payload,
				"resources": {
					"binaries": [] if not data.binary else [data.binary]
				}
			}
			try:
				self.status_logger.info(data.datasource_payload)
				post_message(ingestion_url, payload, 10)
			except requests.RequestException:
				self.status_logger.error("Problems during posting")
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
			return count, False
		return count, True

	def extract_recent(self):
		extraction_count = 0
		for request in self.request_list:
			count = 0
			self.status_logger.info('Extracting request: ' + request.requestUrl + ' using method: ' + request.requestMethod)
			auth = None
			if request.requestAuth:
				auth = (request.requestAuth.username, request.requestAuth.password)
			elif self.auth:
				auth = (self.auth.username, self.auth.password)
			response = requests.request(method=request.requestMethod, url=request.requestUrl, auth=auth)
			count, is_clean_finish = self.manage_data(response)
			extraction_count += count
			self.status_logger.info('Extracted: ' + str(count) + ' elements from request: ' + request.requestUrl + ' extraction ended ' + 'without errors' if is_clean_finish else 'with halting error')

		self.status_logger.info('Extracted: ' + str(extraction_count) + ' elements')
