#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from typing import Optional

from pydantic import BaseModel, Field


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
        example=[0, 10],
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
                        "citations": [],
                    },
                    {
                        "title": "Garanzie Accessorie Assicurazione Veicoli | AXA",
                        "url": "https://www.axa.it/garanzie-accessorie-per-veicoli",
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


class UserChats(BaseModel):
    """Model for retrieving user chat history."""

    chatSequenceNumber: int = Field(
        1, description="Incremental conversation turn number", example=1
    )
    paginationFrom: int = Field(0, description="Pagination start index", example=0)
    paginationSize: int = Field(10, description="Number of items per page", example=10)


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


class CommonHeadersMinimal(BaseModel):
    """
    A minimal collection of common HTTP headers used across the APIs.

    This model represents a subset of standard HTTP headers commonly used in requests,
    focusing on essential authentication and routing headers.

    Attributes:
        authorization (Optional[str]): Bearer token for authentication.
            Example: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        x_forwarded_host (Optional[str]): Original host header from the client request,
            typically used in reverse proxy setups. Example: "example.com"
    """

    authorization: Optional[str] = Field(
        None,
        description="Bearer token for authentication.",
        example="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..",
    )
    x_forwarded_host: Optional[str] = Field(
        None,
        description="Original host header from the client request, typically used in reverse proxy setups.",
        example="example.com",
    )


class CommonHeaders(CommonHeadersMinimal):
    """
    A collection of common HTTP headers used across the APIs.

    This model represents standard HTTP headers that are commonly used in requests,
    particularly for authentication and request routing purposes.

    Attributes:
        authorization (Optional[str]): Bearer token for authentication.
            Example: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        x_forwarded_host (Optional[str]): Original host header from the client request,
            typically used in reverse proxy setups. Example: "example.com"
        openk9_acl (Optional[List[str]]): Access control list for tenant resources.
            Example: ["group:admins", "project:openk9"]
    """

    openk9_acl: Optional[list[str]] = Field(
        None,
        description="Access control list for tenant resources.",
        example='["group:admins", "project:openk9"]',
    )
