import requests
import logging
from logging.config import dictConfig
from ..util.log_config import LogConfig


dictConfig(LogConfig().dict())

logger = logging.getLogger("database-parser")


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
