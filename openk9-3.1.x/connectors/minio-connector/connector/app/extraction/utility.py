import traceback
import os
import base64
import requests
import logging
from datetime import datetime
from logging.config import dictConfig

from pandas import DataFrame

from .log_config import LogConfig

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


def get_as_base64(response):
    data = base64.b64encode(response).decode('utf-8')
    return data


def log_error_location(exception: Exception) -> None:
    tb = traceback.extract_tb(exception.__traceback__)
    for frame in tb:
        logger.error(f"File: {os.path.relpath(frame.filename)}, line {frame.lineno}, in {frame.name}")


def has_valid_header(df: DataFrame) -> bool:
    # Checks if columns are all strings
    return all([
        isinstance(el, str)
        for el in df.columns
    ])


class IngestionHandler:
    def __init__(self, ingestion_url, datasource_id, schedule_id, tenant_id, do_raise_error: bool = True):
        self.ingestion_url = ingestion_url
        self.datasource_id = datasource_id
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id
        self.do_raise_error = do_raise_error

        self.status_logger = logging.getLogger("status-logger")

    def get_end_timestamp(self) -> float:
        return datetime.utcnow().timestamp() * 1000

    def post_message(self, payload):
        try:
            r = requests.post(self.ingestion_url, json=payload, timeout=20)
            if r.status_code == 200:
                return
            else:
                r.raise_for_status()
        except Exception as e:
            logger.error(str(e) + " during request at url: " + str(self.ingestion_url))
            if self.do_raise_error:
                log_error_location(e)
                raise e

    def post_halt(self, exception: Exception, end_timestamp: float | None):
        end_timestamp = end_timestamp if end_timestamp else self.get_end_timestamp()

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
        self.post_message(payload)

    def post_last(self, end_timestamp: float | None):
        end_timestamp = end_timestamp if end_timestamp else self.get_end_timestamp()

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
        self.post_message(payload)
