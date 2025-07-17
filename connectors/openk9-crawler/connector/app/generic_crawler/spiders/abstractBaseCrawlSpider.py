import ast
import hashlib
import logging
import mimetypes
import random
import re
from abc import ABC, abstractmethod
from datetime import datetime
from urllib.parse import urlparse

import requests
from requests_futures.sessions import FuturesSession
from scrapy import Spider

from generic_crawler.items import FileItem, DocumentItem, BinaryItem, Payload
from generic_crawler.spiders.util.file.utility import get_path, extension_from_mimetype
from generic_crawler.spiders.util.generic.utility import get_as_base64, post_message, str_to_bool
from twisted.python.log import logerr

logger = logging.getLogger(__name__)


class AbstractBaseCrawlSpider(ABC, Spider):

	crawled_ids = []

	def __init__(self, ingestion_url, body_tag, title_tag, allowed_domains, excluded_paths,
				 allowed_paths, max_length, document_file_extensions, custom_metadata, additional_metadata,
				 do_extract_docs, datasource_id, schedule_id, timestamp, tenant_id, *a, **kw):
		if self.__class__ == AbstractBaseCrawlSpider:
			raise Exception("Error: Abstract class initialization")
		super(AbstractBaseCrawlSpider, self).__init__(*a, **kw)

		self.ingestion_url = ingestion_url

		self.body_tag = body_tag
		self.title_tag = title_tag

		self.allowed_domains = ast.literal_eval(allowed_domains)
		self.excluded_paths = ast.literal_eval(excluded_paths)
		self.allowed_paths = ast.literal_eval(allowed_paths)
		self.max_length = int(max_length)
		self.document_file_extensions = ast.literal_eval(document_file_extensions)
		self.custom_metadata = ast.literal_eval(custom_metadata)
		self.additional_metadata = ast.literal_eval(additional_metadata)

		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.end_timestamp = datetime.utcnow().timestamp() * 1000

		self.do_extract_docs = str_to_bool(do_extract_docs)
		self.count = 0

	def try_parse_documents(self, hrefs, url_request):

		if not self.do_extract_docs:
			return

		if len(hrefs) > 0 and len(self.document_file_extensions) > 0:
			for href in hrefs:
				if any(document_file_extension in href for document_file_extension in self.document_file_extensions):
					self._parse_for_document(href, url_request)

		elif len(self.document_file_extensions) == 0:
			logger.warning("Could not parse documents, be sure to add values to 'documentFileExtensions'")

	def _parse_for_document(self, href, url_request):

		user_agent_list = [
			'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36',
			'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36',
			'Mozilla/5.0 (Macintosh; Intel Mac OS X 13_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15',
		]
		user_agent = random.choice(user_agent_list)
		headers = {'User-Agent': user_agent}

		# region GET AND PARSE URL


		if href.startswith(('http://', 'https://')):
			document_url = href
			parsed_url = urlparse(document_url)
			domain = parsed_url.hostname
		elif href.startswith('/'):
			parsed_url = urlparse(url_request)
			domain = parsed_url.hostname
			http_s_domain = parsed_url.scheme + "://" + domain
			href = http_s_domain + href
			document_url = href
		else:
			return

		if domain not in  self.allowed_domains:
			logger.warning(f"Document Url: Could not parse document with href: {href} because domain {domain} is not allowed")
			return

		if not document_url:
			logger.warning(f"Document Url: Could not parse document with href: {href}, extracted url: {document_url}")
			return

		response = requests.get(document_url, headers=headers, allow_redirects=True, timeout=5)

		if not (response.status_code == 200):
			logger.warning(f"Error sending get request to url {document_url}")
			return

		document_mime_type = response.headers.get('Content-Type')
		try:
			document_file_name = re.findall('filename=(.+)', response.headers.get('Content-Disposition'))[0]
		except Exception as e:
			document_file_name = None
		document_content = response.content

		file_item = FileItem()
		file_item['path'] = get_path(document_url)
		file_item['fileName'] = document_file_name

		document_item = DocumentItem()
		document_item['url'] = document_url
		document_item['mimeType'] = document_mime_type
		try:
			document_item['extension'] = extension_from_mimetype(document_mime_type)
		except Exception as e:
			document_item['extension'] = None

		datasource_payload = {
			"file": dict(file_item),
			"document": dict(document_item)
		}

		for key, value in self.additional_metadata.items():
			datasource_payload[key] = value

		content_id = str(int(hashlib.sha1(document_url.encode("utf-8")).hexdigest(), 16))

		binary_item = BinaryItem()
		binary_item["id"] = content_id
		binary_item["name"] = document_url
		binary_item["contentType"] = document_mime_type
		binary_item["data"] = get_as_base64(document_content)

		binaries = [dict(binary_item)]

		payload = Payload()

		payload["parsingDate"] = int(self.end_timestamp)
		payload['datasourceId'] = self.datasource_id
		payload['contentId'] = content_id
		payload['rawContent'] = None
		payload['datasourcePayload'] = datasource_payload
		payload["resources"] = {
			"binaries": binaries
		}
		payload["scheduleId"] = self.schedule_id
		payload["tenantId"] = self.tenant_id

		if content_id not in self.crawled_ids:

			post_message(self.ingestion_url, dict(payload))

			self.crawled_ids.append(content_id)

			logger.info("Crawled document from url: " + str(document_url))

			self.count = self.count + 1

			logger.info("Crawled " + str(self.count) + " elements")
		else:
			logger.info("Duplicate document with id " + str(content_id))
			return
