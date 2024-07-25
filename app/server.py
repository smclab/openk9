from fastapi import FastAPI, Request
from fastapi.responses import RedirectResponse, StreamingResponse
from pydantic import BaseModel

from app.chain import get_chain

app = FastAPI()


@app.get("/")
async def redirect_root_to_docs():
    return RedirectResponse("/docs")


class SearchQuery(BaseModel):
    searchQuery: list
    range: list
    afterKey: str
    suggestKeyword: str
    suggestionCategoryId: int
    jwt: str
    extra: dict
    sort: list
    sortAfterKey: str
    language: str
    vectorIndices: bool
    searchText: str


@app.post("/api/rag/generate")
async def search_query(search_query: SearchQuery, request: Request):
    searchQuery = search_query.searchQuery
    range = search_query.range
    afterKey = search_query.afterKey
    suggestKeyword = search_query.suggestKeyword
    suggestionCategoryId = search_query.suggestionCategoryId
    jwt = search_query.jwt
    extra = search_query.extra
    sort = search_query.sort
    sortAfterKey = search_query.sortAfterKey
    language = search_query.language
    vectorIndices = search_query.vectorIndices
    searchText = search_query.searchText

    # TODO: replace "gamahiro.openk9.io" with virtualHost
    # virtualHost = request.client.host
    virtualHost = "gamahiro.openk9.io"

    chain = get_chain(
        searchQuery,
        range,
        afterKey,
        suggestKeyword,
        suggestionCategoryId,
        jwt,
        extra,
        sort,
        sortAfterKey,
        language,
        vectorIndices,
        virtualHost,
        searchText,
    )
    return StreamingResponse(chain, media_type="text/event-stream")
