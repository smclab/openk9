import abc
import inspect
import logging
from logging.config import dictConfig

from minio import Minio
from minio.datatypes import Object
from minio.error import MinioException
from requests import RequestException

from .log_config import LogConfig
from .utility import IngestionHandler, log_error_location

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


class BaseMinioExtractor(abc.ABC):
	def __init__(self, host, port, access_key, secret_key, bucket_name, prefix, datasource_id,
				 timestamp, schedule_id, tenant_id, ingestion_url):
		super(BaseMinioExtractor, self).__init__()
		self.datasource_id = datasource_id
		self.ingestion_url = ingestion_url
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id
		self.access_key = access_key
		self.secret_key = secret_key
		self.bucket_name = bucket_name
		self.prefix = prefix
		self.timestamp = timestamp
		self.url = str(host) + ":" + str(port)

		self.ingestion_handler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id, do_raise_error=False)
		self.status_logger = logging.getLogger("status-logger")

	@abc.abstractmethod
	def manage_data(self, client: Minio, obj: Object, end_timestamp: float):
		raise NotImplementedError(f"Method '{inspect.currentframe().f_code.co_name}' not implemented in {self.__class__.__name__}")

	def extract_data(self):
		try:
			client = Minio(self.url, self.access_key, self.secret_key, secure=False)

			end_timestamp = self.ingestion_handler.get_end_timestamp()

			objects = client.list_objects(self.bucket_name, self.prefix, recursive=True)

		except MinioException as e:
			log_error_location(e)
			self.ingestion_handler.post_halt(exception=e, end_timestamp=None)
			return

		try:
			for obj in objects:
				self.manage_data(client, obj, end_timestamp)

			self.ingestion_handler.post_last(end_timestamp=end_timestamp)

		except (RequestException, Exception) as e:
			self.status_logger.error("Something went wrong")

			# if e is RequestException means it failed to post to ingestion_url
			if not isinstance(e, RequestException):
				self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)
			else:
				self.status_logger.warning("Could not post HALT request. Failed posting to ingestion_url.")
				self.status_logger.error(e)
