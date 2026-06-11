import abc
import logging
from logging.config import dictConfig

from minio import Minio
from minio.datatypes import Object
from minio.error import MinioException
from requests import HTTPError

from .log_config import LogConfig
from .utility import IngestionHandler

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

		self.ingestion_handler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id, verbose=True)
		self.status_logger = logging.getLogger("status-logger")

	@abc.abstractmethod
	def manage_data(self, client: Minio, obj: Object, end_timestamp: float):
		raise NotImplementedError(f"Method not implemented in {self.__class__.__name__}")

	def _is_object_modified_after(self, obj: Object, timestamp_ms: float) -> bool:
		if not obj.last_modified:
			return False

		last_modified = obj.last_modified.timestamp() * 1000
		return last_modified >= timestamp_ms

	def extract_data(self):
		try:
			client = Minio(self.url, self.access_key, self.secret_key, secure=False)

			end_timestamp = self.ingestion_handler.get_end_timestamp()

			objects = client.list_objects(self.bucket_name, self.prefix, recursive=True)

		except MinioException as e:
			self.ingestion_handler.post_halt(exception=e, end_timestamp=None)
			return

		try:
			for obj in objects:
				if not self._is_object_modified_after(obj, self.timestamp):
					continue
				self.manage_data(client, obj, end_timestamp)

			self.ingestion_handler.post_last(end_timestamp=end_timestamp)

		except Exception as e:
			self.status_logger.error("Something went wrong")
			self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)
