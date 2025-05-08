import threading
import logging
import os
import base64
import requests

from logging.config import dictConfig
from datetime import datetime
from .util.utility import format_raw_content, post_message

from .util.log_config import LogConfig

dictConfig(LogConfig().dict())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
	ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"


class DataExtraction(threading.Thread):
	def __init__(self, request_list, do_auth, username, password, timestamp, datasource_id, schedule_id, tenant_id):

		super(DataExtraction, self).__init__()
		self.request_list = request_list
		self.do_auth = do_auth
		self.username = username
		self.password = password
		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.status_logger = logging.getLogger("rest_api_logger")

	def manage_data(self, entry: requests.Response):
		end_timestamp = datetime.utcnow().timestamp() * 1000
		
		raw_content_elements = []
		raw_content = format_raw_content(''.join(raw_content_elements))

		content_id = None

		datasource_payload = entry.json()

		binary = {}

		payload = {
			"datasourceId": self.datasource_id,
			"scheduleId": self.schedule_id,
			"tenantId": self.tenant_id,
			"contentId": content_id,
			"parsingDate": int(end_timestamp),
			"rawContent": raw_content,
			"datasourcePayload": datasource_payload,
			"resources": {
				"binaries": [
					
				]
			}
		}
		try:
			self.status_logger.info(datasource_payload)
			post_message(ingestion_url, payload, 10)
		except requests.RequestException:
			self.status_logger.error("Problems during posting")
			return 0
		return 1

	def extract_recent(self):
		extraction_count = 0
		for request in self.request_list:
			auth = (self.username, self.password) if self.do_auth else None
			response = requests.request(method=request.requestMethod, url=request.requestUrl, auth=auth)
			self.manage_data(response)

		self.status_logger.info('Extracted: ' + str(extraction_count) + ' elements')
