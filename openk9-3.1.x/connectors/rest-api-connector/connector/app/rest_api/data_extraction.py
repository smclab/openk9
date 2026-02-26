import threading
import logging
import os
from typing import Optional

import requests
from logging.config import dictConfig
from datetime import datetime, UTC

from requests import HTTPError, JSONDecodeError

from .util.utility import post_message, handle_response_content, HandleResponseContentReturnObject
from .util.base_model import RequestModel, AuthModel, RequestMethod, ExtractedData, PaginationModel
from .util.log_config import LogConfig

dictConfig(LogConfig().dict())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
	ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"


class DataExtraction(threading.Thread):
	def __init__(self, request_list: list[RequestModel | str], auth: AuthModel | None, timestamp, datasource_id, schedule_id, tenant_id):

		super(DataExtraction, self).__init__()
		self.request_list = request_list
		self.auth = auth
		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.status_logger = logging.getLogger("rest_api_logger")

	def post_halt_message(self, exception: Exception):
		"""
		Handles posting HALT message based on exception.

		:param exception: Exception to send in payload rawContent
		"""

		end_timestamp = datetime.now(UTC).timestamp() * 1000

		payload = {
			"datasourceId": self.datasource_id,
			"scheduleId": self.schedule_id,
			"tenantId": self.tenant_id,
			"contentId": -1,
			"parsingDate": int(end_timestamp),
			"rawContent": exception,
			"datasourcePayload": {

			},
			"resources": {
				"binaries": []
			},
			"type": "HALT"
		}
		self.status_logger.error(exception)
		post_message(ingestion_url, payload, 10)

	def manage_data_payload(self, extracted_data: ExtractedData, raw_content: str, content_id: int, binary: dict | None, datasource_payload: dict) -> ExtractedData:
		"""
		Handles payload creation and post to Openk9.

		:param extracted_data: Used for item count and to check post_message had errors
		:param raw_content: rawContent in payload
		:param content_id: contentId in payload
		:param binary: binary in payload (Optional)
		:param datasource_payload: datasourcePayload in payload

		:return: ExtractedData: updated extracted_data
		"""

		end_timestamp = datetime.now(UTC).timestamp() * 1000

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
			extracted_data.count += 1
			extracted_data.is_clean_finish = True
		except requests.RequestException as e:
			self.status_logger.error("Problems during posting")
			self.post_halt_message(exception=e)
			extracted_data.is_clean_finish = False

		return extracted_data

	def get_response_data(self, response: requests.Response) -> HandleResponseContentReturnObject | None:
		"""
		Handles response and data errors. Halts extraction if an error occurs 'self.post_halt_message'

		:param response: The response of request
		:return: data or None: data is based on response content
		"""

		try:
			response.raise_for_status()

			if len(response.json()) == 0:
				self.status_logger.warning(f"Warning on request: method={response.request.method}, url={response.request.url}")
				self.status_logger.warning("Extraction stopped, content is empty")
				return None
		except HTTPError as e:
			self.status_logger.error(f"Error on request: method={response.request.method}, url={response.request.url}")
			self.post_halt_message(exception=e)
			return None
		except JSONDecodeError:
			pass

		try:
			data = handle_response_content(response)
			if not data:
				raise Exception(f'{response.request} returned null content on handle_response_content')
			return data
		except Exception as e:
			self.status_logger.error(f"Error on get_response_data: response={response}")
			self.post_halt_message(exception=e)
		return None

	def execute_request(self, request_method: str, request_url: str, request_auth: tuple[str, str] | None, request_item_list: str | None, request_params: Optional[dict] = None) -> tuple[HandleResponseContentReturnObject | None, ExtractedData]:
		"""
		Handle request creation, and data handling.

		:param request_method: Request method
		:param request_url: Request url
		:param request_auth: Request authentication
		:param request_params: (Optional) Request params used in pagination
		:param request_item_list: Handle the case if there is a list of items that needs to get sent one by one as Openk9 payload
		:return: data and extracted_data: data is used in pagination, extracted_data has data of url, item count and if it extraction ended with or without errors.
		"""

		response = requests.request(method=request_method, url=request_url, auth=request_auth, params=request_params)
		request_url = response.url
		data = self.get_response_data(response)
		if not data:
			self.status_logger.info(f'get_response_data returned null content in on_extract_request_new')
			return None, ExtractedData(url=request_url, count=0, is_clean_finish=False)

		extracted_data = ExtractedData(url=request_url, count=0, is_clean_finish=False)

		if request_item_list:
			item_list = data.dict_item.get(request_item_list)
			if item_list and isinstance(item_list, list):
				if len(item_list) == 0:
					return None, ExtractedData(url=request_url, count=0, is_clean_finish=False)
				for item in item_list:
					datasource_payload = data.datasource_payload.copy()
					datasource_payload['item'] = item
					extracted_data = self.manage_data_payload(extracted_data=extracted_data, raw_content=data.raw_content, content_id=data.content_id, binary=data.binary, datasource_payload=datasource_payload)
				return data, extracted_data
			else:
				return None, ExtractedData(url=request_url, count=0, is_clean_finish=False)
		elif request_item_list == "":
			# expected data.dict_item is a list
			if isinstance(data.dict_item, list):
				if len(data.dict_item) == 0:
					return None, ExtractedData(url=request_url, count=0, is_clean_finish=False)
				for item in data.dict_item:
					datasource_payload = data.datasource_payload.copy()
					datasource_payload['item'] = item
					extracted_data = self.manage_data_payload(extracted_data=extracted_data, raw_content=data.raw_content, content_id=data.content_id, binary=data.binary, datasource_payload=datasource_payload)
				return data, extracted_data
			else:
				return None, ExtractedData(url=request_url, count=0, is_clean_finish=False)

		datasource_payload = data.datasource_payload.copy()
		datasource_payload['item'] = data.dict_item
		extracted_data = self.manage_data_payload(extracted_data=extracted_data, raw_content=data.raw_content, content_id=data.content_id, binary=data.binary, datasource_payload=data.datasource_payload)
		return data, extracted_data

	def on_extract_request_generator(self, request: str | RequestModel):
		"""
		Handle request creation and pagination.

		:param request: One of the elements passed in the request body 'self.request_list'
		:return: Generator of 'ExtractedData'
		"""
		request_method = RequestMethod.GET.value
		request_url = None
		request_auth = self.auth if self.auth else None

		request_item_list: str | None = None
		request_pagination: PaginationModel | None = None

		if isinstance(request, str):
			request_url = request
		elif isinstance(request, RequestModel):
			request_method = request.requestMethod
			request_url = request.requestUrl
			request_auth = (request.requestAuth.username, request.requestAuth.password) if request.requestAuth else request_auth
			request_item_list = request.requestItemList
			request_pagination = request.requestPagination
		else:
			self.status_logger.error(f"Could not extract request: {request} of type: {type(request)}. Acceptable types are str or RequestModel")
			yield ExtractedData(url=request_url, count=0, is_clean_finish=False)

		if not request_pagination:
			data, extracted_data = self.execute_request(request_method=request_method, request_url=request_url, request_auth=request_auth, request_item_list=request_item_list)
			yield extracted_data
		else:
			if request_pagination.nextInResponse:
				data, extracted_data = self.execute_request(request_method=request_method, request_url=request_url, request_auth=request_auth, request_item_list=request_item_list)
				yield extracted_data
				nxt = data.dict_item.get(request_pagination.nextInResponse)
				while nxt:
					data, extracted_data = self.execute_request(request_method=request_method, request_url=nxt, request_auth=request_auth, request_item_list=request_item_list)
					if data is None:
						break
					nxt = data.dict_item.get(request_pagination.nextInResponse)
					yield extracted_data
			if request_pagination.pageBasedPagination:
				page_param = request_pagination.pageBasedPagination.pageParam
				page_size_param = request_pagination.pageBasedPagination.pageSizeParam
				max_pages = request_pagination.pageBasedPagination.maxPages

				request_params = {page_param.paramName: page_param.paramValue}

				if page_size_param:
					request_params[page_size_param.paramName] = page_size_param.paramValue

				page_param_increment = 1 if not page_param.paramIncrementAmount else page_param.paramIncrementAmount
				while True:
					if max_pages and request_params[page_param.paramName] >= max_pages:
						self.status_logger.warning(f"Extraction Stopped: Reached `max_pages` set at {max_pages} during page pagination extraction.")
						break
					data, extracted_data = self.execute_request(request_method=request_method, request_url=request_url, request_params=request_params, request_auth=request_auth, request_item_list=request_item_list)
					if data is None:
						self.status_logger.warning(f"Extraction Stopped: data values is None during pagination extraction.")
						break
					request_params[page_param.paramName] = request_params[page_param.paramName] + page_param_increment
					yield extracted_data
			if request_pagination.offsetBasedPagination:
				offset_param = request_pagination.offsetBasedPagination.offsetParam
				limit_param = request_pagination.offsetBasedPagination.limitParam
				total = request_pagination.offsetBasedPagination.total

				request_params = {offset_param.paramName: offset_param.paramValue, limit_param.paramName: limit_param.paramValue}

				# sets offset increment to limit_param or specified value
				offset_param_increment = limit_param.paramValue if not offset_param.paramIncrementAmount else offset_param.paramIncrementAmount
				while True:
					if total and request_params[offset_param.paramName] >= total:
						self.status_logger.warning(f"Extraction Stopped: Reached `total` set at {total} during offset pagination extraction.")
						break
					data, extracted_data = self.execute_request(request_method=request_method, request_url=request_url, request_params=request_params, request_auth=request_auth, request_item_list=request_item_list)
					if data is None:
						self.status_logger.warning(f"Extraction Stopped: data values is None during pagination extraction.")
						break
					request_params[offset_param.paramName] = request_params[offset_param.paramName] + offset_param_increment
					yield extracted_data

	def clean_request(self, request: RequestModel | str) -> RequestModel | str:
		"""
		request is automatically converted from RequestModel to dict when passed to DataExtraction.
		This method reverts request: dict -> RequestModel

		:param request: request passed to DataExtraction in 'self.request_list'
		:return: RequestModel | str: request converted from dict
		"""
		if isinstance(request, dict):
			return RequestModel(**request)
		return request

	def extract_recent(self):
		"""
		Extraction starting point
		"""
		extraction_count = 0
		for request in self.request_list:
			request = self.clean_request(request)
			for extracted_data in self.on_extract_request_generator(request):
				extraction_count += extracted_data.count
				self.status_logger.info("Extracted: " + str(extracted_data.count) + " elements from request: " + str(extracted_data.url) + " extraction ended " + ("without errors" if extracted_data.is_clean_finish else "with halting error"))
		self.status_logger.info("Extracted: " + str(extraction_count) + " elements")
