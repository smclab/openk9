import os
from contextlib import asynccontextmanager
from enum import Enum
from typing import Annotated, Optional
from urllib.parse import urlparse

import uvicorn
from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, RedirectResponse
from opensearchpy import OpenSearch
from phoenix.otel import register
from sse_starlette.sse import EventSourceResponse

from app.models import models
from app.rag.chain import get_chain, get_chat_chain, get_chat_chain_tool
from app.utils import openapi_definitions as openapi
from app.utils.authentication import unauthorized_response, verify_token
from app.utils.llm import get_configurations
from app.utils.scheduler import start_document_deletion_scheduler

load_dotenv()

ORIGINS = os.getenv("ORIGINS")
ORIGINS = ORIGINS.split(",")
OPENSEARCH_HOST = os.getenv("OPENSEARCH_HOST")
GRPC_DATASOURCE_HOST = os.getenv("GRPC_DATASOURCE_HOST")
GRPC_TENANT_MANAGER_HOST = os.getenv("GRPC_TENANT_MANAGER_HOST")
RERANKER_API_URL = os.getenv("RERANKER_API_URL")
SCHEDULE = bool(os.getenv("SCHEDULE", False))
CRON_EXPRESSION = os.getenv("CRON_EXPRESSION", "0 0 0 ? * * *")
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


class RagType(Enum):
    RAG_TYPE_UNSPECIFIED = "RAG_TYPE_UNSPECIFIED"
    CHAT_RAG = "CHAT_RAG"
    CHAT_RAG_TOOL = "CHAT_RAG_TOOL"
    SIMPLE_GENERATE = "SIMPLE_GENERATE"


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
    openapi_tags=openapi.OPENAPI_TAGS,
    contact=openapi.CONTACT,
    license_info=openapi.LICENSE_INFO,
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


@app.post(
    "/api/rag/generate",
    tags=["RAG"],
    summary="Generate RAG-powered search results",
    description="""Processes a complex search query with multiple parameters and returns results
    via Server-Sent Events stream. Supports faceted search, suggestions, and vector-based retrieval.""",
    response_description="Stream of search results in SSE format",
    responses=openapi.API_RAG_GENERATE_RESPONSES,
    openapi_extra=openapi.API_RAG_GENERATE_OPENAPI_EXTRA,
)
async def rag_generate(
    search_query_request: models.SearchQuery,
    request: Request,
    headers: Annotated[models.CommonHeaders, Header()],
):
    """Process complex search queries and stream RAG-powered results using Server-Sent Events.

    Args:
        search_query_request (models.SearchQuery): Request object containing:
            - searchQuery: Main search query parameters
            - range: Range filter as [start, end]
            - afterKey: Pagination key for subsequent requests
            - suggestKeyword: Partial keyword for suggestion autocomplete
            - suggestionCategoryId: Category ID to filter suggestions
            - extra: Additional filter parameters
            - sort: Sorting criteria with field:direction format
            - sortAfterKey: Pagination key for sorted results
            - language: Language code for localized result
            - searchText: Primary search text input
        request (Request): FastAPI Request object
        authorization (Optional[str]): Bearer token for authentication
        openk9_acl (Optional[list[str]]): Access control list for tenant isolation
        x_forwarded_host (Optional[str]): Original host header for reverse proxy setups

    Returns:
        EventSourceResponse: Server-Sent Events stream containing:
            - Search results in real-time
            - Processing status updates
            - Completion signals

    Raises:
        HTTPException:
            - 401 Unauthorized if invalid token provided
            - 400 Bad Request if invalid parameters
            - 500 Internal Server Error for processing failures

    Notes:
        - Supports both authenticated and unauthenticated access
        - Combines traditional search with vector retrieval
        - Implements faceted search with range filters
        - Provides query suggestions
        - Uses tenant isolation via ACL headers
        - Streams results progressively via SSE
        - Configuration is tenant-specific (pulled from GRPC_DATASOURCE_HOST)
        - Supports multiple languages
        - Implements result re-ranking
    """
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
    virtual_host = headers.x_forwarded_host or urlparse(str(request.base_url)).hostname

    if headers.openk9_acl:
        extra[OPENK9_ACL_HEADER] = headers.openk9_acl

    token = (
        headers.authorization.replace(TOKEN_PREFIX, "")
        if headers.authorization
        else None
    )
    if token and not verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token):
        unauthorized_response()

    configurations = get_configurations(
        rag_type=RagType.SIMPLE_GENERATE.value,
        grpc_host=GRPC_DATASOURCE_HOST,
        virtual_host=virtual_host,
    )

    rag_configuration = configurations["rag_configuration"]
    llm_configuration = configurations["llm_configuration"]

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
        rag_configuration,
        llm_configuration,
        RERANKER_API_URL,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


@app.post(
    "/api/rag/chat",
    tags=["RAG"],
    summary="Chat with RAG system",
    description="Streaming endpoint for RAG-powered chat interactions using Server-Sent Events (SSE)",
    response_description="Stream of chat events in SSE format",
    responses=openapi.API_RAG_CHAT_RESPONSES,
    openapi_extra=openapi.API_RAG_CHAT_OPENAPI_EXTRA,
)
async def rag_chat(
    search_query_chat: models.SearchQueryChat,
    request: Request,
    headers: Annotated[models.CommonHeaders, Header()],
):
    """Process conversational chat interactions with RAG system using Server-Sent Events streaming.

    Args:
        search_query_chat (models.SearchQueryChat): Request object containing:
            - chatId: Unique identifier for the chat session
            - range: Result window range as [offset, limit]
            - afterKey: Pagination key for subsequent requests
            - suggestKeyword: Partial keyword for suggestion autocomplete
            - suggestionCategoryId: Category ID to filter suggestions
            - extra: Additional filter parameters
            - sort: Sorting criteria
            - sortAfterKey: Pagination key for sorted results
            - language: Language code for localized results
            - searchText: Primary search text input
            - chatHistory: Previous chat messages in the conversation
            - timestamp: Timestamp of the request
            - chatSequenceNumber: Sequence number of the message in chat
        request (Request): FastAPI Request object
        authorization (Optional[str]): Bearer token for authentication
        openk9_acl (Optional[list[str]]): Access control list for tenant isolation
        x_forwarded_host (Optional[str]): Original host header for reverse proxy setups

    Returns:
        EventSourceResponse: Server-Sent Events stream containing:
            - Chat responses in real-time
            - Contextual information
            - Processing status updates
            - Completion signals

    Raises:
        HTTPException:
            - 401 Unauthorized if invalid token provided
            - 400 Bad Request if invalid parameters
            - 500 Internal Server Error for processing failures

    Notes:
        - Supports both authenticated and unauthenticated access
        - Maintains conversation context through chat history
        - Implements tenant isolation via ACL headers
        - Streams responses progressively via SSE
        - Supports multi-language interactions
        - Tracks conversation sequence numbers
        - Uses RAG for contextual responses
        - Configuration is tenant-specific (pulled from GRPC_DATASOURCE_HOST)
        - Implements result re-ranking
        - Stores conversation history for authenticated users
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
    virtual_host = headers.x_forwarded_host or urlparse(str(request.base_url)).hostname

    if headers.openk9_acl:
        extra[OPENK9_ACL_HEADER] = headers.openk9_acl

    token = (
        headers.authorization.replace(TOKEN_PREFIX, "")
        if headers.authorization
        else None
    )
    user_id = None

    if token:
        user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)
        if not user_info:
            unauthorized_response()
        user_id = user_info[KEYCLOAK_USER_INFO_KEY]

    configurations = get_configurations(
        rag_type=RagType.CHAT_RAG.value,
        grpc_host=GRPC_DATASOURCE_HOST,
        virtual_host=virtual_host,
    )

    rag_configuration = configurations["rag_configuration"]
    llm_configuration = configurations["llm_configuration"]

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
        rag_configuration,
        llm_configuration,
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
    responses=openapi.API_RAG_CHAT_TOOL_RESPONSES,
    openapi_extra=openapi.API_RAG_CHAT_TOOL_OPENAPI_EXTRA,
)
async def rag_chat_tool(
    search_query_chat: models.SearchQueryChat,
    request: Request,
    headers: Annotated[models.CommonHeaders, Header()],
):
    """Process conversational chat interactions with RAG as tool using Server-Sent Events streaming.

    Args:
        search_query_chat (models.SearchQueryChat): Request object containing:
            - chatId: Unique identifier for the chat session
            - range: Result window range as [offset, limit]
            - afterKey: Pagination key for subsequent requests
            - suggestKeyword: Partial keyword for suggestion autocomplete
            - suggestionCategoryId: Category ID to filter suggestions
            - extra: Additional filter parameters
            - sort: Sorting criteria
            - sortAfterKey: Pagination key for sorted results
            - language: Language code for localized results
            - searchText: Primary search text input
            - chatHistory: Previous chat messages in the conversation
            - timestamp: Timestamp of the request
            - chatSequenceNumber: Sequence number of the message in chat
        request (Request): FastAPI Request object
        authorization (Optional[str]): Bearer token for authentication
        openk9_acl (Optional[list[str]]): Access control list for tenant isolation
        x_forwarded_host (Optional[str]): Original host header for reverse proxy setups

    Returns:
        EventSourceResponse: Server-Sent Events stream containing:
            - Chat responses in real-time
            - Tool execution results
            - Contextual information
            - Processing status updates
            - Completion signals

    Raises:
        HTTPException:
            - 401 Unauthorized if invalid token provided
            - 400 Bad Request if invalid parameters
            - 500 Internal Server Error for processing failures

    Notes:
        - Supports both authenticated and unauthenticated access
        - Integrates external tools with RAG capabilities
        - Maintains conversation context through chat history
        - Implements tenant isolation via ACL headers
        - Streams responses progressively via SSE
        - Supports multi-language interactions
        - Tracks conversation sequence numbers
        - Uses RAG for contextual responses with tool augmentation
        - Configuration is tenant-specific (pulled from GRPC_DATASOURCE_HOST)
        - Implements result re-ranking
        - Stores conversation history for authenticated users
        - Tools are selected based on query intent analysis
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
    virtual_host = headers.x_forwarded_host or urlparse(str(request.base_url)).hostname

    if headers.openk9_acl:
        extra[OPENK9_ACL_HEADER] = headers.openk9_acl

    token = (
        headers.authorization.replace(TOKEN_PREFIX, "")
        if headers.authorization
        else None
    )
    user_id = None

    if token:
        user_info = verify_token(GRPC_TENANT_MANAGER_HOST, virtual_host, token)
        if not user_info:
            unauthorized_response()
        user_id = user_info[KEYCLOAK_USER_INFO_KEY]

    configurations = get_configurations(
        rag_type=RagType.CHAT_RAG_TOOL.value,
        grpc_host=GRPC_DATASOURCE_HOST,
        virtual_host=virtual_host,
    )

    rag_configuration = configurations["rag_configuration"]
    llm_configuration = configurations["llm_configuration"]

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
        rag_configuration,
        llm_configuration,
        RERANKER_API_URL,
        OPENSEARCH_HOST,
        GRPC_DATASOURCE_HOST,
    )
    return EventSourceResponse(chain)


@app.post(
    "/api/rag/user-chats",
    tags=["Chat"],
    summary="Retrieve user chat history",
    description="Fetches paginated chat history for a specific user",
    responses=openapi.API_RAG_USER_CHATS_RESPONSES,
    openapi_extra=openapi.API_RAG_USER_CHATS_OPENAPI_EXTRA,
)
async def get_user_chats(
    user_chats: models.UserChats,
    request: Request,
    authorization: str = Header(
        ...,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
    x_forwarded_host: Optional[str] = Header(
        None,
        description="""The original host requested by the client in the Host HTTP request header. 
        This header is typically used in reverse proxy setups to identify the original host of the request.""",
        example="example.com",
    ),
):
    """Retrieve paginated chat history for a specific user.

    Args:
        user_chats (models.UserChats): The user chat request object containing:
            - chatSequenceNumber: Sequence number identifying the chat
            - paginationFrom: Starting index for pagination
            - paginationSize: Number of items to return per page
        request (Request): FastAPI Request object
        authorization (str): JWT bearer token for authentication
        x_forwarded_host (Optional[str]): Original host header from client, used in reverse proxy setups

    Returns:
        dict: Dictionary containing:
            - result (list): List of chat history items, each containing:
                - title: Chat title
                - question: User question
                - timestamp: Timestamp of chat
                - chat_id: Unique identifier for the chat

    Raises:
        HTTPException:
            - 401 if authentication fails
            - 500 if there are server errors

    Notes:
        - Requires valid JWT token in Authorization header
        - Uses OpenSearch for chat history storage
        - Results are sorted by timestamp in descending order
    """
    chat_sequence_number = user_chats.chatSequenceNumber
    pagination_from = user_chats.paginationFrom
    pagination_size = user_chats.paginationSize
    virtual_host = x_forwarded_host or urlparse(str(request.base_url)).hostname
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
    responses=openapi.API_RAG_CHAT_GET_RESPONSES,
)
async def get_chat(
    chat_id: str,
    request: Request,
    authorization: str = Header(
        ...,
        description="Bearer token in format: 'Bearer <JWT>'",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ),
    x_forwarded_host: Optional[str] = Header(
        None,
        description="""The original host requested by the client in the Host HTTP request header. 
        This header is typically used in reverse proxy setups to identify the original host of the request.""",
        example="example.com",
    ),
):
    """Retrieve complete conversation history for a specific chat.

    Args:
        chat_id (str): Unique identifier of the chat to retrieve
        request (Request): FastAPI Request object
        authorization (str): JWT bearer token for authentication
        x_forwarded_host (Optional[str]): Original host header from client, used in reverse proxy setups

    Returns:
        dict: Dictionary containing:
            - chat_id (str): The chat identifier
            - messages (list): List of message dictionaries, each containing:
                - question (str): User's question
                - answer (str): System's answer
                - timestamp (str): Timestamp of the message
                - chat_sequence_number (int): Sequence number of the message in chat
                - sources (list): List of source documents used for the answer

    Raises:
        HTTPException:
            - 401 Unauthorized if authentication fails
            - 404 Not Found if the chat doesn't exist or user has no access

    Notes:
        - Requires valid JWT token in Authorization header
        - Messages are sorted by chat_sequence_number in ascending order
        - Only returns messages belonging to the authenticated user
        - Uses OpenSearch for data storage and retrieval
    """
    virtual_host = x_forwarded_host or urlparse(str(request.base_url)).hostname
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
    responses=openapi.API_RAG_CHAT_DELETE_RESPONSES,
)
async def delete_chat(
    chat_id: str,
    request: Request,
    headers: Annotated[models.CommonHeaders, Header()],
):
    """Permanently delete all messages belonging to a specific chat conversation.

    Args:
        chat_id (str): Unique identifier of the chat to delete
        request (Request): FastAPI Request object
        authorization (str): JWT bearer token for authentication
        x_forwarded_host (Optional[str]): Original host header from client, used in reverse proxy setups

    Returns:
        JSONResponse: Response containing:
            - message (str): Operation result message
            - status (str): "success" or "error"

    Raises:
        HTTPException:
            - 401 Unauthorized if authentication fails
            - 404 Not Found if:
                - User index doesn't exist
                - No messages found for specified chat_id
                - Chat doesn't belong to authenticated user
            - 500 Internal Server Error for database operation failures

    Notes:
        - Requires valid JWT token in Authorization header
        - Performs a hard delete - removed data cannot be recovered
        - Deletes all messages associated with the chat_id
        - Only affects chats belonging to the authenticated user
        - Uses OpenSearch's delete_by_query operation
    """
    virtual_host = headers.x_forwarded_host or urlparse(str(request.base_url)).hostname
    token = headers.authorization.replace(TOKEN_PREFIX, "")

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


@app.patch(
    "/api/rag/chat/{chat_id}",
    tags=["Chat"],
    summary="Rename a specific chat conversation",
    description="Updates the title of a specific chat conversation using the provided new title",
    responses=openapi.API_RAG_CHAT_PATCH_RESPONSES,
)
async def rename_chat(
    chat_id: str,
    chat_message: models.ChatMessage,
    request: Request,
    headers: Annotated[models.CommonHeaders, Header()],
):
    """Update the title of an existing chat conversation.

    Args:
        chat_id (str): Unique identifier of the chat to rename
        chat_message (models.ChatMessage): Object containing:
            - newTitle (str): The new title to assign to the chat
        request (Request): FastAPI Request object
        authorization (str): JWT bearer token for authentication
        x_forwarded_host (Optional[str]): Original host header from client, used in reverse proxy setups

    Returns:
        JSONResponse: Response containing:
            - message (str): Operation result message
            - status (str): "success" or "error"

    Raises:
        HTTPException:
            - 401 Unauthorized if authentication fails
            - 404 Not Found if:
                - User index doesn't exist
                - Chat document not found (invalid chat_id)
            - 500 Internal Server Error for:
                - OpenSearch search failures
                - OpenSearch update failures

    Notes:
        - Requires valid JWT token in Authorization header
        - Only updates the first message in the chat (sequence_number=1)
        - Title changes are immediately visible (refresh=True)
        - Only affects chats belonging to the authenticated user
        - The chat must contain at least one message to be renamed
    """
    virtual_host = headers.x_forwarded_host or urlparse(str(request.base_url)).hostname
    token = headers.authorization.replace(TOKEN_PREFIX, "")

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
        open_search_client.update(
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
