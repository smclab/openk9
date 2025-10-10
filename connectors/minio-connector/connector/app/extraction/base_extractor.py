import abc
import logging
from datetime import datetime, UTC
from logging.config import dictConfig

from minio import Minio
from minio.error import MinioException

from .utility import IngestionHandler
from .log_config import LogConfig


dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


class BaseMinioExtractor(abc.ABC):
	def __init__(self, host, port, access_key, secret_key, bucket_name, columns, prefix, datasource_payload_key,
				 datasource_id, timestamp, schedule_id, tenant_id, ingestion_url):
		super(BaseMinioExtractor, self).__init__()
		self.datasource_id = datasource_id
		self.ingestion_url = ingestion_url
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id
		self.access_key = access_key
		self.secret_key = secret_key
		self.bucket_name = bucket_name
		self.columns = columns
		self.prefix = prefix
		self.datasource_payload_key = datasource_payload_key
		self.timestamp = timestamp
		self.url = str(host) + ":" + str(port)

		self.ingestion_handler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id)
		self.status_logger = logging.getLogger("status-logger")

	@abc.abstractmethod
	def manage_data(self, obj):
		raise NotImplementedError(f"Method 'manage_data' not implemented in {self.__class__.__name__}")

	def extract_data(self):
		try:
			client = Minio(self.url, self.access_key, self.secret_key, secure=False)

			end_timestamp = datetime.now(UTC).timestamp() * 1000

			objects = client.list_objects(self.bucket_name, self.prefix, recursive=True)

			self.status_logger.info(objects)

		except MinioException as e:
			self.ingestion_handler.post_halt(exception=e, end_timestamp=None)
			return

		try:
			for obj in objects:
				self.manage_data(obj)
		except Exception as e:
			self.status_logger.error("Something went wrong")
			self.status_logger.error(e)
			self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)
		finally:
			self.ingestion_handler.post_last(end_timestamp=end_timestamp)
