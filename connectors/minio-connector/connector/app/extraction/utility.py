import base64
import requests
import logging
from datetime import datetime
from logging.config import dictConfig

from .log_config import LogConfig

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")

def get_as_base64(response):
    data = base64.b64encode(response).decode('utf-8')
    return data


class IngestionHandler:
    def __init__(self, ingestion_url, datasource_id, schedule_id, tenant_id):
        self.ingestion_url = ingestion_url
        self.datasource_id = datasource_id
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id

        self.status_logger = logging.getLogger("status-logger")

    def post_message(self, payload):
        try:
            r = requests.post(self.ingestion_url, json=payload, timeout=20)
            if r.status_code == 200:
                return
            else:
                r.raise_for_status()
        except requests.RequestException as e:
            logger.error(str(e) + " during request at url: " + str(self.ingestion_url))
            return
        except Exception as e:
            logger.error(str(e) + " during request at url: " + str(self.ingestion_url))
            raise e

    def post_halt(self, exception: Exception, end_timestamp: float | None):
        end_timestamp = end_timestamp if end_timestamp else datetime.utcnow().timestamp() * 1000

        payload = {
            "datasourceId": self.datasource_id,
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "contentId": -1,
            "parsingDate": int(end_timestamp),
            "rawContent": str(exception),
            "datasourcePayload": {

            },
            "resources": {
                "binaries": []
            },
            "type": "HALT"
        }
        self.status_logger.error(exception)
        try:
            self.post_message(payload)
        except Exception as e:
            logger.error(str(e) + " during HALT request at url: " + str(self.ingestion_url))
            raise e

    def post_last(self, end_timestamp: float | None):
        end_timestamp = end_timestamp if end_timestamp else datetime.utcnow().timestamp() * 1000

        payload = {
            "datasourceId": self.datasource_id,
            "parsingDate": int(end_timestamp),
            "contentId": None,
            "rawContent": None,
            "datasourcePayload": {},
            "resources": {
                "binaries": []
            },
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "last": True
        }

        try:
            self.post_message(payload)
        except Exception as e:
            logger.error(str(e) + " during LAST request at url: " + str(self.ingestion_url))
            raise e
