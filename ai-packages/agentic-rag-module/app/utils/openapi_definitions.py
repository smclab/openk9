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

from fastapi import status

OPENAPI_TAGS = [
    {
        "name": "RAG",
        "description": "Endpoints for Retrieval-Augmented Generation operations",
    },
    {
        "name": "Chat",
        "description": "Endpoints for fetch chat history",
    },
]

CONTACT = {
    "name": "OpenK9 Support",
    "email": "dev@openk9.io",
}

LICENSE_INFO = {
    "name": "GNU Affero General Public License v3.0",
    "url": "https://github.com/smclab/openk9/blob/main/LICENSE",
}

API_RAG_GENERATE_RESPONSES = {
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
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}

API_RAG_GENERATE_OPENAPI_EXTRA = {
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
}

API_RAG_CHAT_RESPONSES = {
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
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}

API_RAG_CHAT_OPENAPI_EXTRA = {
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
                            "chatId": "chat-456",
                            "retrieveFromUploadedDocuments": True,
                            "range": [0, 5],
                            "afterKey": "some-key",
                            "suggestKeyword": "OpenK9",
                            "suggestionCategoryId": 1,
                            "extra": {"filter": ["example"]},
                            "sort": ["field1:asc"],
                            "sortAfterKey": "sort-key",
                            "language": "en",
                            "searchText": "What is OpenK9?",
                            "timestamp": "1731928126578",
                            "chatSequenceNumber": 1,
                        },
                    },
                    "Example for logged users, retrieving from uploaded documents": {
                        "summary": "Retrieving from uploaded documents",
                        "value": {
                            "searchText": "What is OpenK9?",
                            "retrieveFromUploadedDocuments": True,
                            "timestamp": "1731928126578",
                            "chatSequenceNumber": 1,
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
                                {
                                    "question": "a cosa porta?",
                                    "answer": "La garanzia Infortuni del Conducente porta i seguenti benefici:\n\n1. **Spese Mediche**: Copertura delle spese mediche in caso di infortunio durante la guida.\n2. **Indennizzo per Incidenti Gravi**: Sicurezza economica in caso di incidenti gravi che comportano invalidità permanente.\n3. **Tutela della Salute del Conducente**: Protezione per la salute del guidatore e dei passeggeri, offrendo supporto in situazioni di emergenza.\n\nIn generale, questa garanzia offre una protezione aggiuntiva rispetto all'assicurazione obbligatoria RC auto, garantendo maggiore tranquillità al conducente.",
                                    "title": "",
                                    "sources": [
                                        {
                                            "title": "Assicurazione Infortuni Conducente | AXA - AXA.it - AXA",
                                            "url": "https://www.axa.it/assicurazione-infortuni-del-conducente",
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
}

API_RAG_CHAT_TOOL_RESPONSES = {
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
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}

API_RAG_CHAT_TOOL_OPENAPI_EXTRA = {
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
                            "chatId": "chat-456",
                            "retrieveFromUploadedDocuments": False,
                            "range": [0, 5],
                            "afterKey": "some-key",
                            "suggestKeyword": "OpenK9",
                            "suggestionCategoryId": 1,
                            "extra": {"filter": ["example"]},
                            "sort": ["field1:asc"],
                            "sortAfterKey": "sort-key",
                            "language": "en",
                            "searchText": "What is OpenK9?",
                            "timestamp": "1731928126578",
                            "chatSequenceNumber": 1,
                        },
                    },
                    "Example for logged users, retrieving from uploaded documents": {
                        "summary": "Retrieving from uploaded documents",
                        "value": {
                            "searchText": "What is OpenK9?",
                            "retrieveFromUploadedDocuments": True,
                            "timestamp": "1731928126578",
                            "chatSequenceNumber": 1,
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
                                {
                                    "question": "a cosa porta?",
                                    "answer": "La garanzia Infortuni del Conducente porta i seguenti benefici:\n\n1. **Spese Mediche**: Copertura delle spese mediche in caso di infortunio durante la guida.\n2. **Indennizzo per Incidenti Gravi**: Sicurezza economica in caso di incidenti gravi che comportano invalidità permanente.\n3. **Tutela della Salute del Conducente**: Protezione per la salute del guidatore e dei passeggeri, offrendo supporto in situazioni di emergenza.\n\nIn generale, questa garanzia offre una protezione aggiuntiva rispetto all'assicurazione obbligatoria RC auto, garantendo maggiore tranquillità al conducente.",
                                    "title": "",
                                    "sources": [
                                        {
                                            "title": "Assicurazione Infortuni Conducente | AXA - AXA.it - AXA",
                                            "url": "https://www.axa.it/assicurazione-infortuni-del-conducente",
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
}

API_RAG_USER_CHATS_RESPONSES = {
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
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}

API_RAG_USER_CHATS_OPENAPI_EXTRA = {
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
}

API_RAG_CHAT_GET_RESPONSES = {
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
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}

API_RAG_CHAT_DELETE_RESPONSES = {
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
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}

API_RAG_CHAT_PATCH_RESPONSES = {
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
                    "chat_not_found": {"value": {"detail": "Chat document not found"}},
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
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}

API_RAG_UPLOAD_FILES_RESPONSES = {
    status.HTTP_401_UNAUTHORIZED: {
        "description": "Unauthorized - Invalid token.",
        "content": {
            "application/json": {"example": {"detail": "Invalid or expired token"}}
        },
    },
    status.HTTP_400_BAD_REQUEST: {
        "description": "All files failed to process.",
        "content": {
            "application/json": {
                "example": {
                    "failed_files": [
                        {
                            "filename": "filename.docx",
                            "error": "Failed to process file content.",
                        },
                        {
                            "filename": "filename.pdf",
                            "error": "Failed to process file content.",
                        },
                    ]
                }
            }
        },
    },
    status.HTTP_207_MULTI_STATUS: {
        "description": "Some files were processed successfully, but others failed.",
        "content": {
            "application/json": {
                "example": {
                    "processed_files": ["filename.pdf"],
                    "failed_files": [
                        {
                            "filename": "filename.docx",
                            "error": "Failed to process file content.",
                        }
                    ],
                }
            }
        },
    },
    status.HTTP_500_INTERNAL_SERVER_ERROR: {
        "description": "Internal Server Error - Unexpected server-side error",
        "content": {
            "application/json": {"example": {"detail": "An unexpected error occurred"}}
        },
    },
}
