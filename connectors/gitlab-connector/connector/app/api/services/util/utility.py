from datetime import datetime
from uuid import UUID


import uuid

from gitlab.base import RESTObject


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
    return field


def format_raw_content(model: str | list | dict) -> str | None:
    """
    Formats datasource payload raw content

    :param model: raw_content
    :type model: str | list | dict
    :return: Formatted raw_content or None
    :rtype: str | None
    """
    if isinstance(model, str):
        raw_content = model
    elif isinstance(model, list):
        raw_content = ' '.join([str(check_field_element(value)) for value in model if value is not None])
    elif isinstance(model, dict):
        raw_content = ' '.join([str(key + ': ' + str(check_field_element(value)))
                                for key, value in model.items() if value is not None])
    else:
        return None

    return raw_content.replace('\t', ' ').replace("\n", " ").replace("\\", " ") \
        .replace("..", "").replace("__", "").replace(";", "").replace(",", "").lower().strip()

def get_object_content_id(resource: RESTObject)-> UUID:
    primary_id = resource.get_id()
    
    if primary_id is not None:
        return uuid.uuid3(uuid.NAMESPACE_OID, str(primary_id))
    
    # Fallback to web_url
    elif hasattr(resource, 'web_url') and resource.web_url:
        return uuid.uuid3(uuid.NAMESPACE_URL, resource.web_url)
        
    else:
        raise ValueError(f"Resource {type(resource).__name__} lacks an identifier.")


def strftime_datetime_filter(dt: datetime) -> str:
    return dt.strftime(format="%Y-%m-%dT%H:%M:%SZ")