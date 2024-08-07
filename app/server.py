import os
from typing import Optional
from urllib.parse import urlparse

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse

from app.rag.chain import get_chain, get_chat_chain
from app.utils.keycloak import Keycloak

app = FastAPI()

load_dotenv()

ORIGINS = os.getenv("ORIGINS")
ORIGINS = ORIGINS.split(",")
KEYKLOAK_INFO_API_URL = os.getenv("KEYKLOAK_INFO_API_URL")
OPENSEARCH_HOST = os.getenv("OPENSEARCH_HOST")
GRPC_HOST = os.getenv("GRPC_HOST")


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
    searchQuery: list
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


@app.post("/api/rag/generate")
async def search_query(
    search_query: SearchQuery,
    request: Request,
    authorization: Optional[str] = Header(None),
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
    virtualHost = urlparse(str(request.base_url)).hostname

    token = authorization.replace("Bearer ", "") if authorization else None

    if token and not Keycloak.verify_token(KEYKLOAK_INFO_API_URL, token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    chain = get_chain(
        searchQuery,
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
        GRPC_HOST,
    )
    return EventSourceResponse(chain)


class SearchQueryChat(BaseModel):
    chatId: Optional[str] = None
    searchQuery: list
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


@app.post("/api/rag/chat")
async def search_query(
    search_query: SearchQueryChat,
    request: Request,
    authorization: Optional[str] = Header(None),
):
    searchText = search_query.searchText
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
    virtualHost = urlparse(str(request.base_url)).hostname

    token = authorization.replace("Bearer ", "") if authorization else None

    if token and not Keycloak.verify_token(KEYKLOAK_INFO_API_URL, token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    chain = get_chat_chain(
        searchQuery,
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
        GRPC_HOST,
    )
    return EventSourceResponse(chain)
