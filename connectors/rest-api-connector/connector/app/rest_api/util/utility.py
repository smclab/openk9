import base64
import json
import xmltodict
import yaml
import csv

import requests
import logging
from logging.config import dictConfig
from ..util.log_config import LogConfig

dictConfig(LogConfig().dict())

logger = logging.getLogger("rest_api_logger")


def post_message(url, payload, timeout=20):

    try:
        r = requests.post(url, json=payload, timeout=timeout)
        if r.status_code == 200:
            return
        else:
            r.raise_for_status()
    except requests.RequestException as e:
        logger.error(str(e) + " during request at url: " + str(url))
        raise e


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


def handle_response_content(response: requests.Response):
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

        def handle_response_content(response: requests.Response):
            content_type = response.headers['content-type']

            if ResponseContentTypes.JSON in content_type:
                return response.json()
            elif content_type in ResponseContentTypes.XML:
                return xmltodict.parse(response.content)
            elif content_type == ResponseContentTypes.TEXT_PLAIN or content_type == ResponseContentTypes.TEXT_HTML:
                return response.content
            elif content_type == ResponseContentTypes.YAML:
                return yaml.safe_load(response.content)
            elif content_type == ResponseContentTypes.CSV:
                csv_reader = csv.DictReader(response.content.splitlines(), delimiter=',')
                return { [ row for row in csv_reader ] }
            elif content_type == ResponseContentTypes.BINARY or content_type == ResponseContentTypes.PDF or ResponseContentTypes.IMAGE in content_type:
                return base64.b64encode(response.content)
            
            raise NotImplementedError(f'Error parsing content-type: {content_type}')


    return ResponseContentTypes.handle_response_content(response)


