import os
from typing import Optional
from urllib.parse import urlparse

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse
from google.protobuf.json_format import ParseDict
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse

from app.external_services.grpc.searcher.searcher_pb2 import SearchTokenRequest, Value
from app.rag.chain import get_chain, get_chat_chain
from app.utils.keycloak import Keycloak

app = FastAPI()

load_dotenv()

ORIGINS = os.getenv("ORIGINS")
ORIGINS = ORIGINS.split(",")
OPENSEARCH_HOST = os.getenv("OPENSEARCH_HOST")
GRPC_DATASOURCE_HOST = os.getenv("GRPC_DATASOURCE_HOST")
GRPC_TENANT_MANAGER_HOST = os.getenv("GRPC_TENANT_MANAGER_HOST")
OPENK9_ACL_HEADER = "OPENK9_ACL"


app.add_middleware(
    CORSMiddleware,
    allow_origins=ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def redirect_root_to_docs():
    return RedirectResponse("/docs")


class SearchToken(BaseModel):
    tokenType: str
    keywordKey: str
    values: list[str]
    filter: bool
    entityType: str
    entityName: str
    extra: dict[str, str]


class SearchQuery(BaseModel):
    searchQuery: list[SearchToken]
    range: list
    afterKey: Optional[str] = None
    suggestKeyword: Optional[str] = None
    suggestionCategoryId: Optional[int] = None
    extra: Optional[dict] = None
    sort: Optional[list] = None
    sortAfterKey: Optional[str] = None
    language: Optional[str] = None
    vectorIndices: Optional[bool] = False
    searchText: str
    reformulate: Optional[bool] = True


@app.post("/api/rag/generate")
async def search_query(
    search_query: SearchQuery,
    request: Request,
    authorization: Optional[str] = Header(None),
    openk9_acl: Optional[list[str]] = Header(None),
):
    searchQuery = search_query.searchQuery
    range = search_query.range
    afterKey = search_query.afterKey
    suggestKeyword = search_query.suggestKeyword
    suggestionCategoryId = search_query.suggestionCategoryId
    extra = search_query.extra
    sort = search_query.sort
    sortAfterKey = search_query.sortAfterKey
    language = search_query.language
    vectorIndices = search_query.vectorIndices
    searchText = search_query.searchText
    reformulate = search_query.reformulate
    virtualHost = urlparse(str(request.base_url)).hostname
    # TODO remove line
    virtualHost = "test.openk9.io"

    openk9_acl_header_values = ParseDict({"value": openk9_acl}, Value())
    extra = {OPENK9_ACL_HEADER: openk9_acl_header_values} if openk9_acl else extra

    search_query_to_proto_list = []
    for query in searchQuery:
        search_query_to_proto = SearchTokenRequest()
        search_query_to_proto.tokenType = query.tokenType
        search_query_to_proto.keywordKey = query.keywordKey
        search_query_to_proto.values.extend(query.values)
        search_query_to_proto.filter = query.filter
        search_query_to_proto.entityType = query.entityType
        search_query_to_proto.entityName = query.entityName
        search_query_to_proto.extra.update(query.extra)
        search_query_to_proto_list.append(search_query_to_proto)

    token = authorization.replace("Bearer ", "") if authorization else None

    if token and not Keycloak.verify_token(
        GRPC_TENANT_MANAGER_HOST, virtualHost, token
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    chain = get_chain(
        search_query_to_proto_list,
        range,
        afterKey,
        suggestKeyword,
        suggestionCategoryId,
        token,
        extra,
        sort,
        sortAfterKey,
        language,
        vectorIndices,
        virtualHost,
        searchText,
        reformulate,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


class SearchQueryChat(BaseModel):
    chatId: Optional[str] = None
    searchQuery: list[SearchToken]
    range: list = [0, 7]
    afterKey: Optional[str] = None
    suggestKeyword: Optional[str] = None
    suggestionCategoryId: Optional[int] = None
    extra: Optional[dict] = None
    sort: Optional[list] = None
    sortAfterKey: Optional[str] = None
    language: Optional[str] = None
    vectorIndices: Optional[bool] = True
    searchText: str
    userId: str
    timestamp: str
    chatSequenceNumber: int


@app.post("/api/rag/chat")
async def search_query(
    search_query: SearchQueryChat,
    request: Request,
    authorization: Optional[str] = Header(None),
):
    chatId = search_query.chatId
    searchQuery = search_query.searchQuery
    range = search_query.range
    afterKey = search_query.afterKey
    suggestKeyword = search_query.suggestKeyword
    suggestionCategoryId = search_query.suggestionCategoryId
    extra = search_query.extra
    sort = search_query.sort
    sortAfterKey = search_query.sortAfterKey
    language = search_query.language
    vectorIndices = search_query.vectorIndices
    searchText = search_query.searchText
    userId = search_query.userId
    timestamp = search_query.timestamp
    chatSequenceNumber = search_query.chatSequenceNumber
    virtualHost = urlparse(str(request.base_url)).hostname

    search_query_to_proto_list = []
    for query in searchQuery:
        search_query_to_proto = SearchTokenRequest()
        search_query_to_proto.tokenType = query.tokenType
        search_query_to_proto.keywordKey = query.keywordKey
        search_query_to_proto.values.extend(query.values)
        search_query_to_proto.filter = query.filter
        search_query_to_proto.entityType = query.entityType
        search_query_to_proto.entityName = query.entityName
        search_query_to_proto.extra.update(query.extra)
        search_query_to_proto_list.append(search_query_to_proto)

    token = authorization.replace("Bearer ", "") if authorization else None

    if token and not Keycloak.verify_token(
        GRPC_TENANT_MANAGER_HOST, virtualHost, token
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    chain = get_chat_chain(
        search_query_to_proto_list,
        range,
        afterKey,
        suggestKeyword,
        suggestionCategoryId,
        token,
        extra,
        sort,
        sortAfterKey,
        language,
        vectorIndices,
        virtualHost,
        searchText,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)
