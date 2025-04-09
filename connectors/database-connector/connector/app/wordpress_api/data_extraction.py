import dataclasses
import logging
import os
import threading
from typing import List

import dateutil.parser
from datetime import datetime
from enum import Enum
from logging.config import dictConfig

import requests

from .util.utility import format_raw_content, post_message, extract_data_pagination
from .util.log_config import LogConfig

dictConfig(LogConfig().dict())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
	ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"


KEYS_TO_REMOVE = ["yoast_head_json", "yoast-schema-graph", "yoast_head"]


class DataExtractionType(Enum):
	POSTS = 'Posts'
	PAGES = 'Pages'
	COMMENTS = 'Comments'
	USERS = 'Users'


@dataclasses.dataclass
class DataExtractionItem:
	data_type: DataExtractionType
	route: str
	id_to_value: list[tuple[str, str]]


class DataExtractionTypeItem(Enum):
	POST = DataExtractionItem(data_type=DataExtractionType.POSTS, route='/wp/v2/posts', id_to_value=[('categories', '/wp/v2/categories'), ('tags', '/wp/v2/tags'), ('author', '/wp/v2/users')])
	PAGES = DataExtractionItem(data_type=DataExtractionType.PAGES, route='/wp/v2/pages', id_to_value=[('author', '/wp/v2/users')])
	COMMENTS = DataExtractionItem(data_type=DataExtractionType.COMMENTS, route='/wp/v2/comments', id_to_value=[('author', '/wp/v2/users')])
	USERS = DataExtractionItem(data_type=DataExtractionType.USERS, route='/wp/v2/users', id_to_value=[])

	@classmethod
	def get(cls, data_type: DataExtractionType):
		for el in cls:
			if el.value.data_type == data_type:
				return el.value
		raise NotImplementedError(f'Error getting Data Type: {data_type}')


def delete_unused_keys(element, keys):

	for key in keys:
		try:
			del element[key]
		except KeyError:
			continue
	return element


class DataExtraction(threading.Thread):
	def __init__(self, data_extraction_type: List[DataExtractionType], host_name, do_auth, username, password, items_per_page, timestamp, datasource_id, schedule_id, tenant_id):
		super(DataExtraction, self).__init__()
		self.data_extraction_items = []
		for data_type in data_extraction_type:
			self.data_extraction_items.append(DataExtractionTypeItem.get(data_type))

		self.host_name = host_name
		self.do_auth = do_auth
		self.username = username
		self.password = password
		self.items_per_page = items_per_page
		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.status_logger = logging.getLogger("wordpress_logger")

	def get_data_payload_keys(self, data_extraction_item) -> tuple[list[str], str, dict or None]:
		"""
		Get keys for payload to be used as element["key"]
		:return:
		raw_content_elements_keys: list[str]: keys from element to form raw content,
		content_id_key: str: content id key,
		datasource_payload_key_value_key: dict[str, str] or None: datasource payload key and value key used as {"key": element["value key"]} if None use element as is
		"""
		data_extraction_type = data_extraction_item.data_type
		datasource_payload_key_value_key: dict or None = None
		if data_extraction_type == DataExtractionType.POSTS:
			# POSTS
			raw_content_elements_keys = ['date_gmt', 'link']
			content_id_key = 'id'
		elif data_extraction_type == DataExtractionType.PAGES:
			# PAGES
			raw_content_elements_keys = ['date_gmt', 'link']
			content_id_key = 'id'
		elif data_extraction_type == DataExtractionType.COMMENTS:
			# COMMENTS
			raw_content_elements_keys = ['date_gmt', 'link']
			content_id_key = 'id'
		elif data_extraction_type == DataExtractionType.USERS:
			# USERS
			raw_content_elements_keys = ['name', 'link']
			content_id_key = 'id'
		else:
			raise NotImplementedError(data_extraction_type)

		return raw_content_elements_keys, content_id_key, datasource_payload_key_value_key

	def manage_data(self, data, data_extraction_item):
		"""
		:param data: dict [key: str = directory path], [value = list(tuple(file_name: str, file_data: dict)) = list of all files in the directory]
		:param data_extraction_item: DataExtractionTypeItem
		:return:
		"""
		count = 0
		end_timestamp = datetime.utcnow().timestamp() * 1000

		raw_content_elements_keys, content_id_key, datasource_payload_key_value_key = self.get_data_payload_keys(data_extraction_item)
		for element in data:
			# If user check registered_date and compare to timestamp
			if data_extraction_item.data_type == DataExtractionType.USERS:
				last_timestamp = dateutil.parser.parse(element['registered_date']).timestamp()
				if last_timestamp < self.timestamp:
					break

			raw_content_elements = [str(element[key] or '') for key in raw_content_elements_keys]
			raw_content = format_raw_content(''.join(raw_content_elements))

			content_id = element[content_id_key]

			datasource_payload = element if datasource_payload_key_value_key is None else {key: element[datasource_payload_key_value_key[key]] for key in datasource_payload_key_value_key}

			for item in data_extraction_item.id_to_value:
				key, route = item
				if isinstance(datasource_payload[key], int):
					value = requests.get(self.get_rest_url_by_id(route, datasource_payload[key]))
					value = value.json()
					value = delete_unused_keys(value, KEYS_TO_REMOVE)
					datasource_payload[key] = value
				elif isinstance(datasource_payload[key], list):
					values = []
					for _id in datasource_payload[key]:
						value = requests.get(self.get_rest_url_by_id(route, _id)).json()
						value = delete_unused_keys(value, KEYS_TO_REMOVE)
						values.append(value)
					datasource_payload[key] = values

			datasource_payload = delete_unused_keys(datasource_payload, KEYS_TO_REMOVE)

			payload = {
				"datasourceId": self.datasource_id,
				"scheduleId": self.schedule_id,
				"tenantId": self.tenant_id,
				"contentId": content_id,
				"parsingDate": int(end_timestamp),
				"rawContent": raw_content,
				"datasourcePayload": {
						data_extraction_item.data_type.value: datasource_payload
				},
				"resources": {
					"binaries": []
				}
			}

			try:
				# self.status_logger.info(payload)
				post_message(ingestion_url, payload, 10)
				count = count + 1
			except requests.RequestException:

				self.status_logger.error("Problems during posting")

				continue
		return count

	def get_rest_url(self, route: str):
		return f'http://{self.host_name}/index.php?rest_route={route}'

	def get_rest_url_by_id(self, route: str, _id):
		self.status_logger.info(f'http://{self.host_name}/index.php?rest_route={route}/{str(_id)}')
		return f'http://{self.host_name}/index.php?rest_route={route}/{str(_id)}'

	def get_extraction_url(self, data_extraction_item):
		modified_after = datetime.fromtimestamp(self.timestamp).isoformat()

		url = self.get_rest_url(data_extraction_item.route) + f'&per_page={self.items_per_page}'
		data_extraction_type = data_extraction_item.data_type

		if data_extraction_type == DataExtractionType.POSTS or data_extraction_type == DataExtractionType.PAGES:
			url = url + f'&modified_after={modified_after}'
		elif data_extraction_type == DataExtractionType.COMMENTS:
			url = url + f'&after={modified_after}'
		elif data_extraction_type == DataExtractionType.USERS:
			# order desc = most recent first
			url = url + '&context=edit'
		else:
			raise NotImplementedError(data_extraction_type)

		return url

	def extract_recent(self):
		session = requests.Session()
		session.auth = (self.username, self.password) if self.do_auth else None

		for data_extraction_item in self.data_extraction_items:
			url = self.get_extraction_url(data_extraction_item)

			for data in extract_data_pagination(session, url):
				self.manage_data(data, data_extraction_item)
			session.close()
