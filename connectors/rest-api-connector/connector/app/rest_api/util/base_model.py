from enum import Enum
from typing import Optional

from pydantic import BaseModel


class RequestMethod(str, Enum):
    GET = "GET"
    POST = "POST"


class AuthModel(BaseModel):
    username: str
    password: str


class RequestModel(BaseModel):
    requestMethod: RequestMethod
    requestUrl: str
    auth: Optional[AuthModel] = None