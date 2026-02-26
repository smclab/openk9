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


class IncrementalIntParamModel(BaseModel):
    paramName: str
    paramValue: int
    paramIncrementAmount: Optional[int] = None


class PageBasedPaginationModel(BaseModel):
    pageParam: IncrementalIntParamModel = IncrementalIntParamModel(paramName="page", paramValue=1, paramIncrementAmount=1)
    pageSizeParam: Optional[IntParamModel] = None
    maxPages: Optional[int] = None     # TODO: Set from request body/header


class OffsetBasedPaginationModel(BaseModel):
    offsetParam: IncrementalIntParamModel = IncrementalIntParamModel(paramName="offset", paramValue=0)
    limitParam: IntParamModel
    total: Optional[int] = None     # TODO: Set from request body/header


class PaginationModel(BaseModel):
    nextInResponse: Optional[str] = None
    pageBasedPagination: Optional[PageBasedPaginationModel] = None
    offsetBasedPagination: Optional[OffsetBasedPaginationModel] = None


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
