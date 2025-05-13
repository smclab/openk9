import os
from contextlib import asynccontextmanager
from typing import Optional
from urllib.parse import urlparse

import uvicorn
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Header, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse, JSONResponse
from opensearchpy import OpenSearch
from phoenix.otel import register
from pydantic import BaseModel, Field
from sse_starlette.sse import EventSourceResponse

from app.rag.chain import get_chain, get_chat_chain, get_chat_chain_tool
from app.utils.authentication import unauthorized_response, verify_token
from app.utils.scheduler import start_document_deletion_scheduler

load_dotenv()

ORIGINS = os.getenv("ORIGINS")
ORIGINS = ORIGINS.split(",")
OPENSEARCH_HOST = os.getenv("OPENSEARCH_HOST")
GRPC_DATASOURCE_HOST = os.getenv("GRPC_DATASOURCE_HOST")
GRPC_TENANT_MANAGER_HOST = os.getenv("GRPC_TENANT_MANAGER_HOST")
RERANKER_API_URL = os.getenv("RERANKER_API_URL")
SCHEDULE = bool(os.getenv("SCHEDULE", False))
CRON_EXPRESSION = os.getenv("CRON_EXPRESSION", "0 00 * * *")
ARIZE_PHOENIX_ENABLED = bool(os.getenv("ARIZE_PHOENIX_ENABLED", False))
ARIZE_PHOENIX_PROJECT_NAME = os.getenv("ARIZE_PHOENIX_PROJECT_NAME", "default")
ARIZE_PHOENIX_ENDPOINT = os.getenv(
    "ARIZE_PHOENIX_ENDPOINT", "http://127.0.0.1:6006/v1/traces"
)
OPENK9_ACL_HEADER = "OPENK9_ACL"
TOKEN_PREFIX = "Bearer "
KEYCLOAK_USER_INFO_KEY = "sub"

if ARIZE_PHOENIX_ENABLED:
    tracer_provider = register(
        project_name=ARIZE_PHOENIX_PROJECT_NAME,
        endpoint=ARIZE_PHOENIX_ENDPOINT,
        auto_instrument=True,
    )


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Start scheduler on startup
    start_document_deletion_scheduler(
        opensearch_host=OPENSEARCH_HOST,
        schedule=SCHEDULE,
        cron_expression=CRON_EXPRESSION,
    )

    yield

    # Stop scheduler on shutdown
    start_document_deletion_scheduler(
        opensearch_host=OPENSEARCH_HOST,
        schedule=False,
        cron_expression=CRON_EXPRESSION,
    )


app = FastAPI(
    title="OpenK9 RAG API",
    description="API for Retrieval-Augmented Generation (RAG) operations and chat interactions",
    version="3.0.0-SNAPSHOT",
    openapi_tags=[
        {
            "name": "RAG",
            "description": "Endpoints for Retrieval-Augmented Generation operations",
        },
        {
            "name": "Chat",
            "description": "Endpoints for fetch chat history",
        },
    ],
    contact={
        "name": "OpenK9 Support",
        "email": "dev@openk9.io",
    },
    license_info={
        "name": "GNU Affero General Public License v3.0",
        "url": "https://github.com/smclab/openk9/blob/main/LICENSE",
    },
    lifespan=lifespan,
)

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
    """
    SearchToken class model.
    """

    tokenType: str = Field(..., description="Type of the search token")
    keywordKey: Optional[str] = Field(
        "", description="Keyword key for the token", example=""
    )
    values: list[str] = Field(..., description="List of values for the token")
    filter: Optional[bool] = Field(
        False, description="Whether this token is used for filtering", example=False
    )
    entityType: Optional[str] = Field(
        "", description="Type of entity for the token", example=""
    )
    entityName: Optional[str] = Field(
        "", description="Name of the entity for the token", example=""
    )
    extra: Optional[dict[str, str]] = Field(
        {}, description="Additional metadata for the token", example={}
    )


class SearchQuery(BaseModel):
    """Represents a search query with various parameters for filtering, sorting, and pagination."""

    searchQuery: list[SearchToken] = Field(..., description="List of search tokens")
    range: list = Field(
        ...,
        description="Range filter as [start, end]",
        example=[0, 5],
    )
    afterKey: Optional[str] = Field(
        None, description="Pagination key for subsequent requests", example="page_2"
    )
    suggestKeyword: Optional[str] = Field(
        None,
        description="Partial keyword for suggestion autocomplete",
        example="OpenK9",
    )
    suggestionCategoryId: Optional[int] = Field(
        None, description="Category ID to filter suggestions", example=1
    )
    extra: Optional[dict[str, list]] = Field(
        default_factory=dict,
        description="Additional filter parameters",
        example={"filter": ["example"]},
    )
    sort: Optional[list] = Field(
        None,
        description="Sorting criteria with field:direction format",
        example=["field1:asc"],
    )
    sortAfterKey: Optional[str] = Field(
        None, description="Pagination key for sorted results", example="sort-key"
    )
    language: Optional[str] = Field(
        None, description="Language code for localized results", example="it_IT"
    )
    searchText: str = Field(
        ..., description="Primary search text input", example="What is OpenK9?"
    )
    reformulate: Optional[bool] = Field(
        True, description="Enable query reformulation", example=True
    )


@app.post(
    "/api/rag/generate",
    tags=["RAG"],
    summary="Generate RAG-powered search results",
    description="""Processes a complex search query with multiple parameters and returns results
    via Server-Sent Events stream. Supports faceted search, suggestions, and vector-based retrieval.""",
    response_description="Stream of search results in SSE format",
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "Unauthorized - Invalid token.",
            "content": {
                "application/json": {"example": {"detail": "Invalid or expired token"}}
            },
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "Forbidden - Insufficient permissions or access denied",
            "content": {
                "application/json": {
                    "example": {"detail": "Access denied for this resource"}
                }
            },
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Validation Error - Invalid request body or parameters",
            "content": {
                "application/json": {
                    "example": {
                        "detail": [
                            {
                                "loc": ["body", "searchText"],
                                "msg": "field required",
                                "type": "value_error.missing",
                            }
                        ]
                    }
                }
            },
        },
        status.HTTP_500_INTERNAL_SERVER_ERROR: {
            "description": "Internal Server Error - Unexpected server-side error",
            "content": {
                "application/json": {
                    "example": {"detail": "An unexpected error occurred"}
                }
            },
        },
    },
    openapi_extra={
        "requestBody": {
            "content": {
                "application/json": {
                    "examples": {
                        "Basic Example": {
                            "summary": "A basic example with minimal fields",
                            "value": {
                                "searchQuery": [
                                    {
                                        "entityType": "",
                                        "entityName": "",
                                        "tokenType": "TEXT",
                                        "keywordKey": "",
                                        "values": ["value"],
                                        "extra": {},
                                        "filter": True,
                                    }
                                ],
                                "range": [],
                                "searchText": "What is OpenK9?",
                            },
                        },
                        "Advanced Example": {
                            "summary": "An advanced example with all fields",
                            "value": {
                                "searchQuery": [
                                    {
                                        "entityType": "",
                                        "entityName": "",
                                        "tokenType": "TEXT",
                                        "keywordKey": "",
                                        "values": ["value"],
                                        "extra": {},
                                        "filter": True,
                                    }
                                ],
                                "range": [],
                                "afterKey": "page_2",
                                "suggestKeyword": "OpenK9",
                                "suggestionCategoryId": 1,
                                "extra": {"filter": ["example"]},
                                "sort": ["field1:asc"],
                                "sortAfterKey": "sort-key",
                                "language": "it_IT",
                                "searchText": "What is OpenK9?",
                                "reformulate": True,
                            },
                        },
                    }
                }
            }
        }
    },
)
async def rag_generate(
    search_query_request: SearchQuery,
    request: Request,
    authorization: Optional[str] = Header(
        None,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
    openk9_acl: Optional[list[str]] = Header(
        None,
        description="Access control list for tenant resources",
        example=["group:admins", "project:openk9"],
    ),
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
    search_text = search_query_request.searchText
    reformulate = search_query_request.reformulate
    virtual_host = urlparse(str(request.base_url)).hostname

    if openk9_acl:
        extra[OPENK9_ACL_HEADER] = openk9_acl

    token = authorization.replace(TOKEN_PREFIX, "") if authorization else None
    if token and not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        unauthorized_response()

    chain = get_chain(
        search_query,
        range_values,
        after_key,
        suggest_keyword,
        suggestion_category_id,
        token,
        extra,
        sort,
        sort_after_key,
        language,
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

    chatId: Optional[str] = Field(
        None,
        description="Unique identifier for chat session",
        example="chat_abc123def456",
    )
    range: Optional[list] = Field(
        [0, 5],
        description="Result window range as [offset, limit]",
        example=[0, 5],
    )
    afterKey: Optional[str] = Field(
        None, description="Pagination key for subsequent requests", example="page_2"
    )
    suggestKeyword: Optional[str] = Field(
        None,
        description="Partial keyword for suggestion autocomplete",
        example="OpenK9",
    )
    suggestionCategoryId: Optional[int] = Field(
        None, description="Category ID to filter suggestions", example=1
    )
    extra: Optional[dict[str, list]] = Field(
        default_factory=dict,
        description="Additional filter parameters",
        example={"filter": ["example"]},
    )
    sort: Optional[list] = Field(
        None,
        description="Sorting criteria with field:direction format",
        example=["field1:asc"],
    )
    sortAfterKey: Optional[str] = Field(
        None, description="Pagination key for sorted results", example="sort-key"
    )
    language: Optional[str] = Field(
        None, description="Language code for localized results", example="it_IT"
    )
    searchText: str = Field(
        ..., description="Primary search text input", example="What is OpenK9?"
    )
    chatHistory: Optional[list] = Field(
        None,
        description="Previous chat messages in conversation",
        example=[
            {
                "question": "Che cos’è la garanzia Infortuni del Conducente?",
                "answer": "La garanzia Infortuni del Conducente è una garanzia accessoria offerta da AXA che protegge il conducente in caso di invalidità permanente o, nei casi più gravi, indennizza gli eredi in caso di morte mentre si guida il veicolo assicurato. Inoltre, l'estensione della garanzia copre anche le spese mediche sostenute a seguito di un infortunio.",
                "title": "Spiegazione della Garanzia Infortuni del Conducente di AXA",
                "sources": [
                    {
                        "title": "Assicurazione Infortuni Conducente | AXA",
                        "url": "https://www.axa.it/assicurazione-infortuni-del-conducente",
                        "source": "local",
                        "citations": [],
                    },
                    {
                        "title": "Garanzie Accessorie Assicurazione Veicoli | AXA",
                        "url": "https://www.axa.it/garanzie-accessorie-per-veicoli",
                        "source": "local",
                        "citations": [],
                    },
                ],
                "chat_id": "1740389549494",
                "timestamp": "1740389552570",
                "chat_sequence_number": 1,
            },
        ],
    )
    timestamp: str = Field(
        ..., description="Timestamp of request", example="1740389552570"
    )
    chatSequenceNumber: int = Field(
        ..., description="Incremental conversation turn number", example=3
    )


@app.post(
    "/api/rag/chat",
    tags=["RAG"],
    summary="Chat with RAG system",
    description="Streaming endpoint for RAG-powered chat interactions using Server-Sent Events (SSE)",
    response_description="Stream of chat events in SSE format",
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "Unauthorized - Invalid token.",
            "content": {
                "application/json": {"example": {"detail": "Invalid or expired token"}}
            },
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "Forbidden - Insufficient permissions or access denied",
            "content": {
                "application/json": {
                    "example": {"detail": "Access denied for this resource"}
                }
            },
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Validation Error - Invalid request body or parameters",
            "content": {
                "application/json": {
                    "example": {
                        "detail": [
                            {
                                "loc": ["body", "searchText"],
                                "msg": "field required",
                                "type": "value_error.missing",
                            }
                        ]
                    }
                }
            },
        },
        status.HTTP_500_INTERNAL_SERVER_ERROR: {
            "description": "Internal Server Error - Unexpected server-side error",
            "content": {
                "application/json": {
                    "example": {"detail": "An unexpected error occurred"}
                }
            },
        },
    },
    openapi_extra={
        "requestBody": {
            "content": {
                "application/json": {
                    "examples": {
                        "Basic Example": {
                            "summary": "A basic example with minimal fields",
                            "value": {
                                "searchText": "What is OpenK9?",
                                "timestamp": "1731928126578",
                                "chatSequenceNumber": 1,
                            },
                        },
                        "Advanced Example": {
                            "summary": "An advanced example with all fields",
                            "value": {
                                "searchText": "What is OpenK9?",
                                "timestamp": "1731928126578",
                                "chatSequenceNumber": 1,
                                "retrieveCitations": True,
                                "rerank": False,
                                "chunk_window": False,
                                "range": [0, 5],
                                "chatId": "chat-456",
                                "afterKey": "some-key",
                                "suggestKeyword": "OpenK9",
                                "suggestionCategoryId": 1,
                                "extra": {"filter": ["example"]},
                                "sort": ["field1:asc"],
                                "sortAfterKey": "sort-key",
                                "language": "en",
                            },
                        },
                        "Example for not logged users, first question": {
                            "summary": "An example for not logged users, first question",
                            "value": {
                                "searchText": "Che cos’è la garanzia Infortuni del Conducente?",
                                "chatSequenceNumber": 1,
                                "timestamp": "1731928126578",
                                "chatHistory": [],
                            },
                        },
                        "Example for not logged users, third question": {
                            "summary": "An example for not logged users, third question",
                            "value": {
                                "searchText": "quanto vale?",
                                "chatSequenceNumber": 3,
                                "timestamp": "1731928126578",
                                "chatHistory": [
                                    {
                                        "question": "Che cos’è la garanzia Infortuni del Conducente?",
                                        "answer": "La garanzia Infortuni del Conducente è una garanzia accessoria offerta da AXA che protegge il conducente in caso di invalidità permanente o, nei casi più gravi, indennizza gli eredi in caso di morte mentre si guida il veicolo assicurato. Inoltre, l'estensione della garanzia copre anche le spese mediche sostenute a seguito di un infortunio.",
                                        "title": "",
                                        "sources": [
                                            {
                                                "title": "Assicurazione Infortuni Conducente | AXA",
                                                "url": "https://www.axa.it/assicurazione-infortuni-del-conducente",
                                                "source": "local",
                                                "citations": [],
                                            },
                                            {
                                                "title": "Garanzie Accessorie Assicurazione Veicoli | AXA",
                                                "url": "https://www.axa.it/garanzie-accessorie-per-veicoli",
                                                "source": "local",
                                                "citations": [],
                                            },
                                        ],
                                        "chat_id": "1740389549494",
                                        "timestamp": "1740389552570",
                                        "chat_sequence_number": 1,
                                    },
                                    {
                                        "question": "a cosa porta?",
                                        "answer": "La garanzia Infortuni del Conducente porta i seguenti benefici:\n\n1. **Spese Mediche**: Copertura delle spese mediche in caso di infortunio durante la guida.\n2. **Indennizzo per Incidenti Gravi**: Sicurezza economica in caso di incidenti gravi che comportano invalidità permanente.\n3. **Tutela della Salute del Conducente**: Protezione per la salute del guidatore e dei passeggeri, offrendo supporto in situazioni di emergenza.\n\nIn generale, questa garanzia offre una protezione aggiuntiva rispetto all'assicurazione obbligatoria RC auto, garantendo maggiore tranquillità al conducente.",
                                        "title": "",
                                        "sources": [
                                            {
                                                "title": "Assicurazione Infortuni Conducente | AXA - AXA.it - AXA",
                                                "url": "https://www.axa.it/assicurazione-infortuni-del-conducente",
                                                "source": "local",
                                                "citations": [
                                                    {
                                                        "quote": "La garanzia accessoria Infortuni del Conducente è una garanzia che rafforza la tua polizza assicurativa che protegge auto, moto, ciclomotore, quadriciclo o autocarro da eventi non coperti dall’assicurazione obbligatoria RC. Sei coperto anche se ti fermi a causa di un guasto o incidente e ti fai male durante le operazioni per riprendere la marcia o mentre segnali un pericolo ad altri conducenti. E non solo, sono inclusi anche gli infortuni dovuti a malore, incoscienza, asfissia, annegamento, assideramento o congelamento."
                                                    },
                                                    {
                                                        "quote": "Grazie alla garanzia Infortuni del Conducente, ho potuto affrontare la situazione con serenità."
                                                    },
                                                ],
                                            },
                                            {
                                                "title": "Assicurazione auto online: la tua polizza su misura | AXA - AXA.it - AXA",
                                                "url": "https://www.axa.it/assicurazione-auto",
                                                "source": "local",
                                                "citations": [],
                                            },
                                        ],
                                        "chat_id": "1740389549494",
                                        "timestamp": "1731928126578",
                                        "chat_sequence_number": 2,
                                    },
                                ],
                            },
                        },
                    }
                }
            }
        }
    },
)
async def rag_chat(
    search_query_chat: SearchQueryChat,
    request: Request,
    authorization: Optional[str] = Header(
        None,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
    openk9_acl: Optional[list[str]] = Header(
        None,
        description="Access control list for tenant resources",
        example=["group:admins", "project:openk9"],
    ),
):
    """
    Handle RAG chat interactions with streaming response.

    - **search_query_chat**: Request body with chat parameters
    - **authorization**: JWT bearer token for authentication
    - **openk9_acl**: Access control list for tenant isolation
    """
    chat_id = search_query_chat.chatId
    range_values = search_query_chat.range
    after_key = search_query_chat.afterKey
    suggest_keyword = search_query_chat.suggestKeyword
    suggestion_category_id = search_query_chat.suggestionCategoryId
    extra = search_query_chat.extra
    sort = search_query_chat.sort
    sort_after_key = search_query_chat.sortAfterKey
    language = search_query_chat.language
    search_text = search_query_chat.searchText
    chat_history = search_query_chat.chatHistory
    timestamp = search_query_chat.timestamp
    chat_sequence_number = search_query_chat.chatSequenceNumber
    virtual_host = urlparse(str(request.base_url)).hostname

    if openk9_acl:
        extra[OPENK9_ACL_HEADER] = openk9_acl

    token = authorization.replace(TOKEN_PREFIX, "") if authorization else None
    user_id = None

    if token:
        user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)
        if not user_info:
            unauthorized_response()
        user_id = user_info[KEYCLOAK_USER_INFO_KEY]

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
        virtual_host,
        search_text,
        chat_id,
        user_id,
        chat_history,
        timestamp,
        chat_sequence_number,
        RERANKER_API_URL,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


@app.post(
    "/api/rag/chat-tool",
    tags=["RAG"],
    summary="Chat with RAG system",
    description="Streaming endpoint for RAG-powered chat interactions using Server-Sent Events (SSE)",
    response_description="Stream of chat events in SSE format",
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "Unauthorized - Invalid token.",
            "content": {
                "application/json": {"example": {"detail": "Invalid or expired token"}}
            },
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "Forbidden - Insufficient permissions or access denied",
            "content": {
                "application/json": {
                    "example": {"detail": "Access denied for this resource"}
                }
            },
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Validation Error - Invalid request body or parameters",
            "content": {
                "application/json": {
                    "example": {
                        "detail": [
                            {
                                "loc": ["body", "searchText"],
                                "msg": "field required",
                                "type": "value_error.missing",
                            }
                        ]
                    }
                }
            },
        },
        status.HTTP_500_INTERNAL_SERVER_ERROR: {
            "description": "Internal Server Error - Unexpected server-side error",
            "content": {
                "application/json": {
                    "example": {"detail": "An unexpected error occurred"}
                }
            },
        },
    },
    openapi_extra={
        "requestBody": {
            "content": {
                "application/json": {
                    "examples": {
                        "Basic Example": {
                            "summary": "A basic example with minimal fields",
                            "value": {
                                "searchText": "What is OpenK9?",
                                "timestamp": "1731928126578",
                                "chatSequenceNumber": 1,
                            },
                        },
                        "Advanced Example": {
                            "summary": "An advanced example with all fields",
                            "value": {
                                "searchText": "What is OpenK9?",
                                "timestamp": "1731928126578",
                                "chatSequenceNumber": 1,
                                "retrieveCitations": True,
                                "rerank": False,
                                "chunk_window": False,
                                "range": [0, 5],
                                "chatId": "chat-456",
                                "afterKey": "some-key",
                                "suggestKeyword": "OpenK9",
                                "suggestionCategoryId": 1,
                                "extra": {"filter": ["example"]},
                                "sort": ["field1:asc"],
                                "sortAfterKey": "sort-key",
                                "language": "en",
                            },
                        },
                        "Example for not logged users, first question": {
                            "summary": "An example for not logged users, first question",
                            "value": {
                                "searchText": "Che cos’è la garanzia Infortuni del Conducente?",
                                "chatSequenceNumber": 1,
                                "timestamp": "1731928126578",
                                "chatHistory": [],
                            },
                        },
                        "Example for not logged users, third question": {
                            "summary": "An example for not logged users, third question",
                            "value": {
                                "searchText": "quanto vale?",
                                "chatSequenceNumber": 3,
                                "timestamp": "1731928126578",
                                "chatHistory": [
                                    {
                                        "question": "Che cos’è la garanzia Infortuni del Conducente?",
                                        "answer": "La garanzia Infortuni del Conducente è una garanzia accessoria offerta da AXA che protegge il conducente in caso di invalidità permanente o, nei casi più gravi, indennizza gli eredi in caso di morte mentre si guida il veicolo assicurato. Inoltre, l'estensione della garanzia copre anche le spese mediche sostenute a seguito di un infortunio.",
                                        "title": "",
                                        "sources": [
                                            {
                                                "title": "Assicurazione Infortuni Conducente | AXA",
                                                "url": "https://www.axa.it/assicurazione-infortuni-del-conducente",
                                                "source": "local",
                                                "citations": [],
                                            },
                                            {
                                                "title": "Garanzie Accessorie Assicurazione Veicoli | AXA",
                                                "url": "https://www.axa.it/garanzie-accessorie-per-veicoli",
                                                "source": "local",
                                                "citations": [],
                                            },
                                        ],
                                        "chat_id": "1740389549494",
                                        "timestamp": "1740389552570",
                                        "chat_sequence_number": 1,
                                    },
                                    {
                                        "question": "a cosa porta?",
                                        "answer": "La garanzia Infortuni del Conducente porta i seguenti benefici:\n\n1. **Spese Mediche**: Copertura delle spese mediche in caso di infortunio durante la guida.\n2. **Indennizzo per Incidenti Gravi**: Sicurezza economica in caso di incidenti gravi che comportano invalidità permanente.\n3. **Tutela della Salute del Conducente**: Protezione per la salute del guidatore e dei passeggeri, offrendo supporto in situazioni di emergenza.\n\nIn generale, questa garanzia offre una protezione aggiuntiva rispetto all'assicurazione obbligatoria RC auto, garantendo maggiore tranquillità al conducente.",
                                        "title": "",
                                        "sources": [
                                            {
                                                "title": "Assicurazione Infortuni Conducente | AXA - AXA.it - AXA",
                                                "url": "https://www.axa.it/assicurazione-infortuni-del-conducente",
                                                "source": "local",
                                                "citations": [
                                                    {
                                                        "quote": "La garanzia accessoria Infortuni del Conducente è una garanzia che rafforza la tua polizza assicurativa che protegge auto, moto, ciclomotore, quadriciclo o autocarro da eventi non coperti dall’assicurazione obbligatoria RC. Sei coperto anche se ti fermi a causa di un guasto o incidente e ti fai male durante le operazioni per riprendere la marcia o mentre segnali un pericolo ad altri conducenti. E non solo, sono inclusi anche gli infortuni dovuti a malore, incoscienza, asfissia, annegamento, assideramento o congelamento."
                                                    },
                                                    {
                                                        "quote": "Grazie alla garanzia Infortuni del Conducente, ho potuto affrontare la situazione con serenità."
                                                    },
                                                ],
                                            },
                                            {
                                                "title": "Assicurazione auto online: la tua polizza su misura | AXA - AXA.it - AXA",
                                                "url": "https://www.axa.it/assicurazione-auto",
                                                "source": "local",
                                                "citations": [],
                                            },
                                        ],
                                        "chat_id": "1740389549494",
                                        "timestamp": "1731928126578",
                                        "chat_sequence_number": 2,
                                    },
                                ],
                            },
                        },
                    }
                }
            }
        }
    },
)
async def rag_chat(
    search_query_chat: SearchQueryChat,
    request: Request,
    authorization: Optional[str] = Header(
        None,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
    openk9_acl: Optional[list[str]] = Header(
        None,
        description="Access control list for tenant resources",
        example=["group:admins", "project:openk9"],
    ),
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
    search_text = search_query_chat.searchText
    chat_history = search_query_chat.chatHistory
    timestamp = search_query_chat.timestamp
    chat_sequence_number = search_query_chat.chatSequenceNumber
    virtual_host = urlparse(str(request.base_url)).hostname

    if openk9_acl:
        extra[OPENK9_ACL_HEADER] = openk9_acl

    token = authorization.replace(TOKEN_PREFIX, "") if authorization else None
    user_id = None

    if token:
        user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)
        if not user_info:
            unauthorized_response()
        user_id = user_info[KEYCLOAK_USER_INFO_KEY]

    chain = get_chat_chain_tool(
        range_values,
        after_key,
        suggest_keyword,
        suggestion_category_id,
        token,
        extra,
        sort,
        sort_after_key,
        language,
        virtual_host,
        search_text,
        chat_id,
        user_id,
        chat_history,
        timestamp,
        chat_sequence_number,
        RERANKER_API_URL,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


class UserChats(BaseModel):
    """Model for retrieving user chat history."""

    chatSequenceNumber: int = Field(
        1, description="Incremental conversation turn number", example=1
    )
    paginationFrom: int = Field(0, description="Pagination start index", example=0)
    paginationSize: int = Field(10, description="Number of items per page", example=10)


@app.post(
    "/api/rag/user-chats",
    tags=["Chat"],
    summary="Retrieve user chat history",
    description="Fetches paginated chat history for a specific user",
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "Unauthorized - Invalid token.",
            "content": {
                "application/json": {"example": {"detail": "Invalid or expired token"}}
            },
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "Forbidden - Insufficient permissions or access denied",
            "content": {
                "application/json": {
                    "example": {"detail": "Access denied for this resource"}
                }
            },
        },
        status.HTTP_500_INTERNAL_SERVER_ERROR: {
            "description": "Internal Server Error - Unexpected server-side error",
            "content": {
                "application/json": {
                    "example": {"detail": "An unexpected error occurred"}
                }
            },
        },
    },
    openapi_extra={
        "requestBody": {
            "content": {
                "application/json": {
                    "examples": {
                        "Basic Example": {
                            "summary": "A basic example with minimal fields",
                            "value": {},
                        },
                        "Advanced Example": {
                            "summary": "An advanced example with all fields",
                            "value": {
                                "chatSequenceNumber": 1,
                                "paginationFrom": 0,
                                "paginationSize": 10,
                            },
                        },
                    }
                }
            }
        }
    },
)
async def get_user_chats(
    user_chats: UserChats,
    request: Request,
    authorization: str = Header(
        ...,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
):
    chat_sequence_number = user_chats.chatSequenceNumber
    pagination_from = user_chats.paginationFrom
    pagination_size = user_chats.paginationSize
    virtual_host = urlparse(str(request.base_url)).hostname
    token = authorization.replace(TOKEN_PREFIX, "")

    user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)

    if not user_info:
        unauthorized_response()

    user_id = user_info[KEYCLOAK_USER_INFO_KEY]

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


@app.get(
    "/api/rag/chat/{chat_id}",
    tags=["Chat"],
    summary="Retrieve user specific chat",
    description="Fetches user complete conversation history for a specific chat_id",
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "Unauthorized - Invalid token.",
            "content": {
                "application/json": {"example": {"detail": "Invalid or expired token"}}
            },
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "Forbidden - Insufficient permissions or access denied",
            "content": {
                "application/json": {
                    "example": {"detail": "Access denied for this resource"}
                }
            },
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Validation Error - Invalid request parameters or structure",
            "content": {
                "application/json": {
                    "example": {
                        "detail": [
                            {
                                "loc": ["path", "chat_id"],
                                "msg": "field required",
                                "type": "value_error.missing",
                            }
                        ]
                    }
                }
            },
        },
        status.HTTP_500_INTERNAL_SERVER_ERROR: {
            "description": "Internal Server Error - Unexpected server-side error",
            "content": {
                "application/json": {
                    "example": {"detail": "An unexpected error occurred"}
                }
            },
        },
    },
)
async def get_chat(
    chat_id: str,
    request: Request,
    authorization: str = Header(
        ...,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
):
    virtual_host = urlparse(str(request.base_url)).hostname
    token = authorization.replace(TOKEN_PREFIX, "")

    user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)

    if not user_info:
        unauthorized_response()

    user_id = user_info[KEYCLOAK_USER_INFO_KEY]

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


@app.delete(
    "/api/rag/chat/{chat_id}",
    tags=["Chat"],
    summary="Delete a specific chat conversation",
    description="Permanently removes all messages and metadata associated with a specific chat_id",
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "Unauthorized - Invalid token.",
            "content": {
                "application/json": {"example": {"detail": "Invalid or expired token"}}
            },
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "Forbidden - Insufficient permissions or access denied",
            "content": {
                "application/json": {
                    "example": {"detail": "Access denied for this resource"}
                }
            },
        },
        status.HTTP_404_NOT_FOUND: {
            "description": "Requested resource not found",
            "content": {
                "application/json": {
                    "examples": {
                        "user_not_found": {"value": {"detail": "User index not found"}},
                        "chat_not_found": {
                            "value": {"detail": "No messages found for specified chat"}
                        },
                    }
                }
            },
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Invalid request parameters or structure",
            "content": {
                "application/json": {
                    "example": {
                        "detail": [
                            {
                                "loc": ["path", "chat_id"],
                                "msg": "value is not a valid chat id",
                                "type": "type_error.uuid",
                            }
                        ]
                    }
                }
            },
        },
        status.HTTP_500_INTERNAL_SERVER_ERROR: {
            "description": "Internal Server Error - Unexpected server-side error",
            "content": {
                "application/json": {
                    "example": {"detail": "An unexpected error occurred"}
                }
            },
        },
    },
)
async def delete_chat(
    chat_id: str,
    request: Request,
    authorization: str = Header(
        ...,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
):
    virtual_host = urlparse(str(request.base_url)).hostname
    token = authorization.replace(TOKEN_PREFIX, "")

    user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)

    if not user_info:
        unauthorized_response()

    user_id = user_info[KEYCLOAK_USER_INFO_KEY]
    open_search_client = OpenSearch(
        hosts=[OPENSEARCH_HOST],
    )

    if not open_search_client.indices.exists(index=user_id):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Item not found.",
        )

    delete_query = {"query": {"match": {"chat_id.keyword": chat_id}}}

    try:
        response = open_search_client.delete_by_query(index=user_id, body=delete_query)
    except OpenSearch.exceptions.NotFoundError:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Item not found.",
        )

    if response.get("deleted", 0) == 0:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Item not found.",
        )

    content = {"message": "Chat deleted successfully.", "status": "success"}
    return JSONResponse(status_code=status.HTTP_200_OK, content=content)


class ChatMessage(BaseModel):
    """
    Represents the payload for updating a chat conversation title.

    Attributes:
        newTitle: The new title to assign to the chat conversation
    """

    newTitle: str = Field(
        ...,
        min_length=1,
        max_length=100,
        description="New title for the chat conversation",
        example="Project Discussion",
    )


@app.patch(
    "/api/rag/chat/{chat_id}",
    tags=["Chat"],
    summary="Rename a specific chat conversation",
    description="Updates the title of a specific chat conversation using the provided new title",
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "Unauthorized - Invalid token.",
            "content": {
                "application/json": {"example": {"detail": "Invalid or expired token"}}
            },
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "Forbidden - Insufficient permissions or access denied",
            "content": {
                "application/json": {
                    "example": {"detail": "Access denied for this resource"}
                }
            },
        },
        status.HTTP_404_NOT_FOUND: {
            "description": "Requested resource not found",
            "content": {
                "application/json": {
                    "examples": {
                        "user_not_found": {"value": {"detail": "User index not found"}},
                        "chat_not_found": {
                            "value": {"detail": "Chat document not found"}
                        },
                    }
                }
            },
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Invalid request parameters or structure",
            "content": {
                "application/json": {
                    "example": {
                        "detail": [
                            {
                                "loc": ["path", "chat_id"],
                                "msg": "value is not a valid chat id",
                                "type": "type_error.uuid",
                            }
                        ]
                    }
                }
            },
        },
        status.HTTP_500_INTERNAL_SERVER_ERROR: {
            "description": "Internal Server Error - Unexpected server-side error",
            "content": {
                "application/json": {
                    "example": {"detail": "An unexpected error occurred"}
                }
            },
        },
    },
)
async def rename_chat(
    chat_id: str,
    chat_message: ChatMessage,
    request: Request,
    authorization: str = Header(
        ...,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
):
    virtual_host = urlparse(str(request.base_url)).hostname
    token = authorization.replace(TOKEN_PREFIX, "")

    user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)

    if not user_info:
        unauthorized_response()

    user_id = user_info[KEYCLOAK_USER_INFO_KEY]

    open_search_client = OpenSearch(
        hosts=[OPENSEARCH_HOST],
    )

    if not open_search_client.indices.exists(index=user_id):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User index not found.",
        )

    query = {
        "query": {
            "bool": {
                "must": [
                    {"match": {"chat_id.keyword": chat_id}},
                    {"match": {"chat_sequence_number": 1}},
                ]
            }
        }
    }

    try:
        response = open_search_client.search(index=user_id, body=query)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"OpenSearch search error: {str(e)}",
        )

    hits = response["hits"]["hits"]
    if not hits:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Chat document not found.",
        )

    document_id = hits[0]["_id"]

    try:
        update_response = open_search_client.update(
            index=user_id,
            id=document_id,
            body={"doc": {"title": chat_message.newTitle}},
            refresh=True,
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"OpenSearch update error: {str(e)}",
        )

    content = {"message": "Title updated successfully.", "status": "success"}
    return JSONResponse(status_code=status.HTTP_200_OK, content=content)


@app.get(
    "/health",
    summary="Check the health status of the Rag module",
    description="Returns the current health status of the Rag module.",
    tags=["Health Checks"],
)
async def get_health():
    return {"status": "UP"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5000)
