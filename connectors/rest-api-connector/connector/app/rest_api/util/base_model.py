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
    nextInResponse: Optional[str] = None
    pageBasedPagination: Optional[PageBasedPaginationModel] = None          # TODO
    offsetBasedPagination: Optional[OffsetBasedPaginationModel] = None      # TODO
    cursorBasedPagination: Optional[CursorBasedPaginationModel] = None      # TODO


class RequestModel(BaseModel):
    requestMethod: Optional[RequestMethod] = RequestMethod.GET
    requestUrl: str
    requestItemList: Optional[str] = None
    requestPagination: Optional[PaginationModel] = None
    requestAuth: Optional[AuthModel] = None


# Used in data_extraction
class ExtractedData(BaseModel):
    url: Optional[str] = None
    count: int = 0
    is_clean_finish: bool = False
