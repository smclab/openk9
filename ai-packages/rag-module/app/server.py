import os
from typing import Optional
from urllib.parse import urlparse

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse
from opensearchpy import OpenSearch
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse

from app.rag.chain import get_chain, get_chat_chain
from app.utils.keycloak import unauthorized_response, verify_token

app = FastAPI()

load_dotenv()

ORIGINS = os.getenv("ORIGINS")
ORIGINS = ORIGINS.split(",")
OPENSEARCH_HOST = os.getenv("OPENSEARCH_HOST")
GRPC_DATASOURCE_HOST = os.getenv("GRPC_DATASOURCE_HOST")
GRPC_TENANT_MANAGER_HOST = os.getenv("GRPC_TENANT_MANAGER_HOST")
RERANKER_API_URL = os.getenv("RERANKER_API_URL")
OPENK9_ACL_HEADER = "OPENK9_ACL"
TOKEN_PREFIX = "Bearer "


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


class SearchQuery(BaseModel):
    """SearchQuery class model."""

    range: list
    afterKey: Optional[str] = None
    suggestKeyword: Optional[str] = None
    suggestionCategoryId: Optional[int] = None
    extra: Optional[dict[str, list]] = {}
    sort: Optional[list] = None
    sortAfterKey: Optional[str] = None
    language: Optional[str] = None
    vectorIndices: Optional[bool] = False
    searchText: str
    reformulate: Optional[bool] = True


@app.post("/api/rag/generate")
async def rag_generate(
    search_query_request: SearchQuery,
    request: Request,
    authorization: Optional[str] = Header(None),
    openk9_acl: Optional[list[str]] = Header(None),
):
    """Definition of /api/rag/generate api."""
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
    reformulate = search_query_request.reformulate
    virtual_host = urlparse(str(request.base_url)).hostname

    if openk9_acl:
        extra[OPENK9_ACL_HEADER] = openk9_acl

    token = authorization.replace(TOKEN_PREFIX, "") if authorization else None
    if token and not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        unauthorized_response()

    chain = get_chain(
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
        reformulate,
        RERANKER_API_URL,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


class SearchQueryChat(BaseModel):
    """SearchQueryChat class model."""

    chatId: Optional[str] = None
    range: Optional[list] = [0, 5]
    afterKey: Optional[str] = None
    suggestKeyword: Optional[str] = None
    suggestionCategoryId: Optional[int] = None
    extra: Optional[dict[str, list]] = {}
    sort: Optional[list] = None
    sortAfterKey: Optional[str] = None
    language: Optional[str] = None
    vectorIndices: Optional[bool] = True
    searchText: str
    userId: str
    timestamp: str
    chatSequenceNumber: int
    retrieveCitations: Optional[bool] = False


@app.post("/api/rag/chat")
async def rag_chat(
    search_query_chat: SearchQueryChat,
    request: Request,
    authorization: Optional[str] = Header(None),
    openk9_acl: Optional[list[str]] = Header(None),
):
    """Definition of /api/rag/chat api."""
    chat_id = search_query_chat.chatId
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
    retrieve_citations = search_query_chat.retrieveCitations
    virtual_host = urlparse(str(request.base_url)).hostname

    if openk9_acl:
        extra[OPENK9_ACL_HEADER] = openk9_acl

    token = authorization.replace(TOKEN_PREFIX, "") if authorization else None
    if token and not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        unauthorized_response()

    chain = get_chat_chain(
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
        retrieve_citations,
        RERANKER_API_URL,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


class UserChats(BaseModel):
    """UserChats class model."""

    userId: str
    chatSequenceNumber: int = 1
    paginationFrom: int = 0
    paginationSize: int = 10


@app.post("/api/rag/user_chats")
async def get_user_chats(
    user_chats: UserChats, request: Request, authorization: str = Header()
):
    user_id = user_chats.userId
    chat_sequence_number = user_chats.chatSequenceNumber
    pagination_from = user_chats.paginationFrom
    pagination_size = user_chats.paginationSize
    virtual_host = urlparse(str(request.base_url)).hostname
    token = authorization.replace(TOKEN_PREFIX, "")

    if not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        unauthorized_response()

    open_search_client = OpenSearch(
        hosts=[OPENSEARCH_HOST],
    )

    query = {
        "from": pagination_from,
        "size": pagination_size,
        "query": {
            "bool": {
                "must": [
                    {"match": {"user_id.keyword": user_id}},
                    {"match": {"chat_sequence_number": chat_sequence_number}},
                ]
            }
        },
        "sort": [{"timestamp": {"order": "desc"}}],
        "_source": {
            "includes": ["title", "question", "timestamp", "chat_id"],
            "excludes": [],
        },
    }

    result = {"result": []}

    if open_search_client.indices.exists(index=user_id):
        response = open_search_client.search(body=query, index=user_id)

        for chat in response["hits"]["hits"]:
            result["result"].append(chat["_source"])

    return result


@app.get("/api/rag/getChat/{user_id}/{chat_id}")
async def get_chat(
    user_id: str, chat_id: str, request: Request, authorization: str = Header()
):
    virtual_host = urlparse(str(request.base_url)).hostname
    token = authorization.replace(TOKEN_PREFIX, "")

    if not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        unauthorized_response()

    open_search_client = OpenSearch(
        hosts=[OPENSEARCH_HOST],
    )

    query = {
        "query": {"match": {"chat_id.keyword": chat_id}},
        "sort": [{"chat_sequence_number": {"order": "asc"}}],
        "_source": {
            "includes": [],
            "excludes": ["chat_id", "user_id", "sources.page_content"],
        },
    }

    if (
        open_search_client.indices.exists(index=user_id)
        and (
            open_search_response := open_search_client.search(body=query, index=user_id)
        )["hits"]["hits"]
    ):
        result = {
            "chat_id": chat_id,
            "messages": [
                {
                    "question": chat["_source"]["question"],
                    "answer": chat["_source"]["answer"],
                    "timestamp": chat["_source"]["timestamp"],
                    "chat_sequence_number": chat["_source"]["chat_sequence_number"],
                    "sources": chat["_source"]["sources"],
                }
                for chat in open_search_response["hits"]["hits"]
            ],
        }

        return result

    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail="Item not found.",
    )
