import base64
import json
import hashlib
import xmltodict
import yaml
import csv
import traceback
import os

import requests
import logging
from datetime import datetime
from logging.config import dictConfig
from ..util.log_config import LogConfig

dictConfig(LogConfig().dict())

logger = logging.getLogger("rest_api_logger")


def validate_model(model):
    if model is not None:
        for k, v in model.items():
            model[k] = check_field_element(v)
    return model


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


def hash_string(string: str) -> int:
    return int(hashlib.md5(string.encode('utf-8')).hexdigest(), 16)


class HandleResponseContentReturnObject:
    def __init__(self, raw_content: str, content_id: int, binary: dict | None, datasource_payload: dict, dict_item: dict):
        self.raw_content = raw_content
        self.content_id = content_id
        self.binary = binary
        self.datasource_payload = datasource_payload
        self.dict_item = dict_item


def handle_response_content(response: requests.Response) -> HandleResponseContentReturnObject:
    """
    Handles response based on 'content-type'

    :param response: requests.Response
    :return: HandleResponseContentReturnObject
    """
    class ResponseContentTypes:
        JSON = 'json'
        XML = ['application/xml', 'text/xml']
        TEXT_PLAIN = 'text/plain'
        TEXT_HTML = 'text/html'
        YAML = 'application/x-yaml'
        CSV = 'text/csv'
        BINARY = 'application/octet-stream'
        IMAGE = 'image/'
        PDF = 'application/pdf'

        @staticmethod
        def handle_response_content(response: requests.Response) -> HandleResponseContentReturnObject:
            content_type = response.headers.get('content-type')

            if content_type:
                raw_content_elements = [str(content_type or ''), str(response.headers.get('content-length') or ''), str(response.headers.get('date') or ''), str(response.headers.get('server') or '')]
                raw_content = format_raw_content(''.join(raw_content_elements))
                content_id = hash_string(response.url)

                binary = None
                datasource_payload = {
                    'requestUrl': response.url,
                    'contentType': content_type
                }

                if ResponseContentTypes.JSON in content_type:
                    dict_item = response.json()
                elif any(ct_xml in content_type for ct_xml in ResponseContentTypes.XML):
                    dict_item = dict(xmltodict.parse(response.content))
                elif ResponseContentTypes.TEXT_PLAIN in content_type or ResponseContentTypes.TEXT_HTML in content_type:
                    dict_item = {response.text}
                elif ResponseContentTypes.YAML in content_type:
                    dict_item = yaml.safe_load(response.content)
                elif ResponseContentTypes.CSV in content_type:
                    csv_reader = csv.reader(response.content.splitlines(), delimiter=',')
                    dict_item = [row for row in csv_reader]
                elif ResponseContentTypes.BINARY in content_type or ResponseContentTypes.PDF in content_type or ResponseContentTypes.IMAGE in content_type:
                    binary = {
                        "id": content_id,
                        "name": response.url,
                        "contentType": content_type,
                        "data": base64.b64encode(response.content)
                    }
                    dict_item = {}
                else:
                    raise NotImplementedError(f'Error parsing content-type: {content_type}')

                return HandleResponseContentReturnObject(raw_content, content_id, binary, datasource_payload, dict_item)

            raise NotImplementedError('Missing content-type in response')

    return ResponseContentTypes.handle_response_content(response)


