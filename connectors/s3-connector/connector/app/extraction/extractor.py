import hashlib
import json
import logging
from logging.config import dictConfig

from .base_extractor import BaseMinioExtractor
from .log_config import LogConfig
from .utility import get_as_base64, IngestionHandler

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


class MinioExtractor(BaseMinioExtractor):

    def __init__(self, host, port, access_key, secret_key, bucket_name, prefix, additional_metadata,
                 datasource_id, timestamp, schedule_id, tenant_id, ingestion_url):

        super(MinioExtractor, self).__init__(host, port, access_key, secret_key, bucket_name, prefix,
                                             datasource_id, timestamp, schedule_id, tenant_id, ingestion_url)

        self.additional_metadata = dict(additional_metadata)

        self.ingestion_handler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id)
        self.status_logger = logging.getLogger("status-logger")

        try:
            with open("./extraction/mapping_config.json") as config_file:
                self.config = json.load(config_file)
        except (FileNotFoundError, json.decoder.JSONDecodeError):
            self.status_logger.error("Ingestion configuration file is missing or there is some error in it.")
            return

        self.type_mapping = self.config["TYPE_MAPPING"]

    def manage_data(self, client, obj, end_timestamp):
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

        try:
            # self.status_logger.info(datasource_payload)
            self.ingestion_handler.post_message(payload=payload)
            self.status_logger.info("posted " + str(metadata.object_name))
        except Exception as e:
            raise e
