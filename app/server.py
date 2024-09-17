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
from app.utils.keycloak import verify_token

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
async def rag_generatey(
    search_query_request: SearchQuery,
    request: Request,
    authorization: Optional[str] = Header(None),
    openk9_acl: Optional[list[str]] = Header(None),
):
    """Definition of /api/rag/generate api."""
    search_query = search_query_request.searchQuery
    range_values = search_query_request.range
    after_key = search_query_request.afterKey
    suggest_keyword = search_query_request.suggestKeyword
    suggestion_category_id = search_query_request.suggestionCategoryId
    extra = search_query_request.extra
    sort = search_query_request.sort
    sort_after_key = search_query_request.sortAfterKey
    language = search_query_request.language
    vector_indices = search_query_request.vectorIndices
    search_text = search_query_request.searchText
    virtual_host = urlparse(str(request.base_url)).hostname

    openk9_acl_header_values = ParseDict({"value": openk9_acl}, Value())
    extra = {OPENK9_ACL_HEADER: openk9_acl_header_values} if openk9_acl else extra

    search_query_to_proto_list = []
    for query in search_query:
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

    if token and not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    chain = get_chain(
        search_query_to_proto_list,
        range_values,
        after_key,
        suggest_keyword,
        suggestion_category_id,
        token,
        extra,
        sort,
        sort_after_key,
        language,
        vector_indices,
        virtual_host,
        search_text,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


class SearchQueryChat(BaseModel):
    """SearchQueryChat class model."""

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
async def rag_chat(
    search_query_chat: SearchQueryChat,
    request: Request,
    authorization: Optional[str] = Header(None),
):
    """Definition of /api/rag/chat api."""
    chat_id = search_query_chat.chatId
    search_query = search_query_chat.searchQuery
    range_values = search_query_chat.range
    after_key = search_query_chat.afterKey
    suggest_keyword = search_query_chat.suggestKeyword
    suggestion_category_id = search_query_chat.suggestionCategoryId
    extra = search_query_chat.extra
    sort = search_query_chat.sort
    sort_after_key = search_query_chat.sortAfterKey
    language = search_query_chat.language
    vector_indices = search_query_chat.vectorIndices
    search_text = search_query_chat.searchText
    user_id = search_query_chat.userId
    timestamp = search_query_chat.timestamp
    chat_sequence_number = search_query_chat.chatSequenceNumber
    virtual_host = urlparse(str(request.base_url)).hostname

    search_query_to_proto_list = []
    for query in search_query:
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

    if token and not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    chain = get_chat_chain(
        search_query_to_proto_list,
        range_values,
        after_key,
        suggest_keyword,
        suggestion_category_id,
        token,
        extra,
        sort,
        sort_after_key,
        language,
        vector_indices,
        virtual_host,
        search_text,
        chat_id,
        user_id,
        timestamp,
        chat_sequence_number,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)
