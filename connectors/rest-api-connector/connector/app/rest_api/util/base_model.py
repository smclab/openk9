from enum import Enum
from typing import Optional

from pydantic import BaseModel


class RequestMethod(str, Enum):
    GET = "GET"
    POST = "POST"


class AuthModel(BaseModel):
    username: str
    password: str


class IntParamModel(BaseModel):
    paramName: str
    paramValue: int


class StrParamModel(BaseModel):
    paramName: str
    paramValue: str


class PageBasedPaginationModel(BaseModel):
    pageParam: IntParamModel
    itemsPerPageParam: IntParamModel


class OffsetBasedPaginationModel(BaseModel):
    offsetParam: IntParamModel
    limitParam: IntParamModel


class CursorBasedPaginationModel(BaseModel):
    cursorParam: StrParamModel
    limitParam: IntParamModel


class PaginationModel(BaseModel):
    nextInResponse: Optional[list[str]] = None
    pageBasedPagination: Optional[PageBasedPaginationModel] = None
    offsetBasedPagination: Optional[OffsetBasedPaginationModel] = None
    cursorBasedPagination: Optional[CursorBasedPaginationModel] = None


class RequestModel(BaseModel):
    requestMethod: Optional[RequestMethod] = RequestMethod.GET
    requestUrl: str
    requestItemList: Optional[str] = None   # TODO
    requestPagination: Optional[PaginationModel] = None     # TODO
    requestAuth: Optional[AuthModel] = None
