import json
import logging
import hashlib
from minio import Minio
from datetime import datetime
from logging.config import dictConfig

from minio.error import MinioException
from .log_config import LogConfig
from .utility import get_as_base64, IngestionHandler

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


class MinioExtractor:

    def __init__(self, host, port, access_key, secret_key, bucket_name, prefix, additional_metadata,
                 datasource_id, timestamp, schedule_id, tenant_id, ingestion_url):

        super(MinioExtractor, self).__init__()
        self.datasource_id = datasource_id
        self.ingestion_url = ingestion_url
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id
        self.access_key = access_key
        self.secret_key = secret_key
        self.bucket_name = bucket_name
        self.prefix = prefix
        self.additional_metadata = dict(additional_metadata)
        self.timestamp = timestamp
        self.url = str(host) + ":" + str(port)

        self.ingestion_handler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id)
        self.status_logger = logging.getLogger("status-logger")

        try:
            with open("./extraction/mapping_config.json") as config_file:
                self.config = json.load(config_file)
        except (FileNotFoundError, json.decoder.JSONDecodeError):
            self.status_logger.error("Ingestion configuration file is missing or there is some error in it.")
            return

        self.type_mapping = self.config["TYPE_MAPPING"]

    def extract_data(self):

        try:

            client = Minio(self.url, self.access_key, self.secret_key, secure=False)

            end_timestamp = datetime.utcnow().timestamp() * 1000

            objects = client.list_objects(self.bucket_name, self.prefix, recursive=True)

            self.status_logger.info(objects)

        except MinioException as e:
            self.ingestion_handler.post_halt(exception=e, end_timestamp=None)
            return

        try:
            for obj in objects:

                try:
                    metadata = client.stat_object(self.bucket_name, obj.object_name)

                    try:
                        file_type = self.type_mapping[metadata.content_type]
                    except KeyError:
                        file_type = None

                    datasource_payload = {"file": {
                        "name": metadata.object_name,
                        "contentType": metadata.content_type,
                        "size": metadata.size,
                        "type": file_type
                    }, file_type: {
                        "name": metadata.object_name
                    },
                        "document": {
                           "title": metadata.object_name
                        },
                    }

                    for key, value in self.additional_metadata.items():
                        datasource_payload[key] = value

                    binaries = []

                    data = client.get_object(self.bucket_name, obj.object_name)

                    name = metadata.object_name

                    content_id = int(hashlib.sha1(name.encode("utf-8")).hexdigest(), 16)

                    binary_item = {
                        "id": content_id,
                        "name": metadata.object_name,
                        "contentType": metadata.content_type,
                        "data": get_as_base64(data.data)
                    }

                    binaries.append(binary_item)

                    payload = {
                        "datasourceId": self.datasource_id,
                        "contentId": content_id,
                        "parsingDate": int(end_timestamp),
                        "rawContent": "",
                        "datasourcePayload": datasource_payload,
                        "resources": {
                            "binaries": binaries
                        },
                        "scheduleId": self.schedule_id,
                        "tenantId": self.tenant_id
                    }

                    self.ingestion_handler.post_message(payload=payload)

                    self.status_logger.info("posted " + str(metadata.object_name))

                    # self.status_logger.info(datasource_payload)

                except Exception as e:

                    self.status_logger.error("Something went wrong")
                    self.status_logger.error(e)
                    self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)
        except Exception as e:
            self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)

        self.ingestion_handler.post_last(end_timestamp=end_timestamp)
