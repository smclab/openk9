import base64
import dataclasses
import hashlib
import traceback
import os
from typing import Dict

import requests
import logging
from datetime import datetime
from logging.config import dictConfig
from ..util.log_config import LogConfig

dictConfig(LogConfig().dict())

logger = logging.getLogger("youtube_logger")


def log_error_location(exception: Exception) -> None:
    tb = traceback.extract_tb(exception.__traceback__)
    for frame in tb:
        logger.error(f"File: {os.path.relpath(frame.filename)}, line {frame.lineno}, in {frame.name}")


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
            # Converts datetime and uuid fileds
            r = requests.post(self.ingestion_url, json=payload, timeout=20)
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


def validate_model(model):
    if model is not None:
        for k, v in model.items():
            model[k] = check_field_element(v)
    return model


def check_field_element(field):
    if field is not None:
        if isinstance(field, str):
            field = field.strip()
            if len(field) == 0:
                return None
            else:
                return field.lower()
        else:
            return field


def format_raw_content(model):
    if isinstance(model, str):
        raw_content = model
    elif isinstance(model, list):
        raw_content = ' '.join([str(check_field_element(value)) for value in model if value is not None])
    else:
        raw_content = ' '.join([str(key + ': ' + str(check_field_element(value)))
                                for key, value in model.items() if value is not None])

    return raw_content.replace('\t', ' ').replace("\n", " ").replace("\\", " ") \
        .replace("..", "").replace("__", "").replace(";", "").replace(",", "").lower().strip()


def hash_str_to_int(s: str) -> int:
    """
    Args:
        s: String to be hashed

    Returns:
        String hashed to int
    """
    return int(hashlib.sha1(s.encode()).hexdigest(), 16)


def get_as_base64(response):
    data = base64.b64encode(response).decode()
    return data


@dataclasses.dataclass
class FileData:
    name: str
    data: bytes


@dataclasses.dataclass
class FutureResult:
    url: str
    video_info: Dict | None
    audio_file_data: FileData | None
    subtitle_files_data: Dict[str, FileData]

