from enum import Enum

from pydantic import BaseModel


class RequestMethod(str, Enum):
    GET = "GET"
    POST = "POST"
    OPTIONS = "OPTIONS"
    HEAD = "HEAD"


class RequestModel(BaseModel):
    requestMethod: RequestMethod
    requestUrl: str