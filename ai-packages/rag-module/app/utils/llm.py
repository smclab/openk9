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

import json
import os
from enum import Enum
from typing import List

from google.auth import default, transport
from google.protobuf.struct_pb2 import Struct
from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import ConfigurableFieldSpec
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_google_vertexai import ChatVertexAI
from langchain_ibm import ChatWatsonx
from langchain_ollama import ChatOllama
from langchain_openai import ChatOpenAI
from opensearchpy import OpenSearch
from pydantic import BaseModel, Field

from app.external_services.grpc.grpc_client import (
    generate_documents_embeddings,
    get_embedding_model_configuration,
    get_llm_configuration,
    get_rag_configuration,
)
from app.rag.custom_hugging_face_model import CustomChatHuggingFaceModel
from app.rag.retriever import OpenSearchRetriever
from app.utils.chat_history import (
    get_chat_history,
    get_chat_history_from_frontend,
    save_chat_message,
)
from app.utils.logger import logger

DEFAULT_MODEL_TYPE = "openai"
DEFAULT_MODEL = "gpt-4o-mini"


class ModelType(Enum):
    OPENAI = "openai"
    OLLAMA = "ollama"
    HUGGING_FACE_CUSTOM = "hugging-face-custom"
    IBM_WATSONX = "watsonx"
    CHAT_VERTEX_AI = "chat_vertex_ai"
    CHAT_VERTEX_AI_MODEL_GARDEN = "chat_vertex_ai_model_garden"


def save_google_application_credentials(credentials):
    """
    Save Google Application credentials to a JSON file and configure environment variables.

    Serializes credentials to a JSON file and sets the GOOGLE_APPLICATION_CREDENTIALS environment
    variable to enable automatic credential discovery by Google Cloud client libraries.

    .. note::
        The environment variable modification only affects the current process and child processes.

    :param dict credentials: Dictionary containing Google Application credentials data.
        Expected to contain service account or user credential fields.
        Must be JSON-serializable (typically contains key/values with primitive types).

    :raises json.JSONEncodeError: If credentials contain non-serializable data types
    :raises OSError: If file writing operations fail (e.g., permission issues)

    Example::

        "credentials": {
            "account": "",
            "client_id": "client_id",
            "client_secret": "client_secret",
            "quota_project_id": "quota_project_id",
            "refresh_token": "refresh_token",
            "type": "type",
            "universe_domain": "universe_domain"
            }
        save_google_cloud_credentials(credentials)
    """
    json_credentials = json.dumps(credentials, indent=2, sort_keys=True)
    credential_file_path = "application_default_credentials.json"

    with open(credential_file_path, "w", encoding="utf-8") as outfile:
        outfile.write(json_credentials)

    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = credential_file_path


def get_configurations(
    rag_type,
    grpc_host,
    virtual_host,
):
    rag_configuration = get_rag_configuration(grpc_host, virtual_host, rag_type)
    llm_configuration = get_llm_configuration(grpc_host, virtual_host)

    configurations = {
        "rag_configuration": rag_configuration,
        "llm_configuration": llm_configuration,
    }

    return configurations


def initialize_language_model(configuration):
    """
    Initialize and return a language model based on the specified model type
    and configuration settings.

    Parameters:
    ----------

    configuration : dict
        A dictionary containing configuration settings required for model initialization.
        Expected keys include:
            - "api_url": str
                URL for the API endpoint.
            - "api_key": str
                API key for authentication.
            - "model_type": str
                The type of model to instantiate. Should match one of the values defined
                in the ModelType enumeration (e.g., 'OPENAI', 'OLLAMA', 'HUGGING_FACE_CUSTOM',
                'IBM_WATSONX', 'CHAT_VERTEX_AI').
            - "model": str
                Name of the model to use; defaults to DEFAULT_MODEL if not provided.
            - "prompt_template": str
                The initial prompt to be used with the model.
            - "rephrase_prompt_template": str
                A prompt for rephrasing tasks, if applicable.
            - "context_window": int
                Size of the context window for the model's input.
            - "retrieve_citations": bool
                Flag to enable citation extraction.
            - "rerank": bool
                Flag to enable document reranking.
            - "chunk_window": int
                If 0 disable context window merging, if > 0 and <=2 enable context window merging.
            - "retrieve_type": str
                Specifies the type of retrieval mechanism to be used with the model.
            - "watsonx_project_id": str
                Project ID for IBM WatsonX (required if using IBM_WATSONX).
            - "chat_vertex_ai_credentials": dict
                Credentials for Google Vertex AI (required if using CHAT_VERTEX_AI).
            - "chat_vertex_ai_model_garden": dict
                Configurations for Google Vertex AI Model Garden (required if using CHAT_VERTEX_AI_MODEL_GARDEN).

    Returns:
    -------
    llm : object
        An instance of a language model corresponding to the specified model type.
        The returned object can be used to perform various natural language processing tasks.

    """

    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    model = configuration["model"] if configuration["model"] else DEFAULT_MODEL
    match model_type:
        case ModelType.OPENAI.value:
            llm = ChatOpenAI(
                model=model,
                openai_api_key=api_key,
                openai_api_base=api_url,
                stream_usage=True,
            )
        case ModelType.OLLAMA.value:
            context_window = configuration["context_window"]
            llm = ChatOllama(model=model, base_url=api_url, num_ctx=context_window)
        case ModelType.HUGGING_FACE_CUSTOM.value:
            llm = CustomChatHuggingFaceModel(base_url=api_url)
        case ModelType.IBM_WATSONX.value:
            watsonx_project_id = configuration["watsonx_project_id"]
            parameters = {
                "decoding_method": "sample",
                "max_new_tokens": 100,
                "min_new_tokens": 1,
                "temperature": 0.5,
                "top_k": 50,
                "top_p": 1,
            }
            llm = ChatWatsonx(
                model_id=model,
                url=api_url,
                apikey=api_key,
                project_id=watsonx_project_id,
                params=parameters,
            )
        case ModelType.CHAT_VERTEX_AI.value:
            google_credentials = configuration["chat_vertex_ai_credentials"]
            save_google_application_credentials(google_credentials)
            project_id = google_credentials["quota_project_id"]

            llm = ChatVertexAI(
                model=model,
                project=project_id,
                temperature=0,
                max_tokens=None,
                max_retries=6,
                stop=None,
            )
        case ModelType.CHAT_VERTEX_AI_MODEL_GARDEN.value:
            chat_vertex_ai_model_garden = configuration["chat_vertex_ai_model_garden"]
            google_credentials = chat_vertex_ai_model_garden["credentials"]
            save_google_application_credentials(google_credentials)
            project_id = google_credentials["quota_project_id"]
            endpoint_id = chat_vertex_ai_model_garden["endpoint_id"]
            location = chat_vertex_ai_model_garden["location"]

            credentials, _ = default()
            auth_request = transport.requests.Request()
            credentials.refresh(auth_request)

            api_key = credentials.token
            base_url = f"https://{endpoint_id}/v1/projects/{project_id}/locations/{location}/endpoints/openapi"

            llm = ChatOpenAI(model=model, api_key=api_key, base_url=base_url)
        case _:
            llm = ChatOpenAI(model=model, openai_api_key=api_key)

    return llm


def generate_conversation_title(llm, search_text, result_answer):
    """
    Generate a conversation title based on user input and AI response.

    This function creates a title for a conversation by utilizing a language model.
    It takes the user's question and the AI's answer as input, formats them into a
    prompt, and invokes the language model to generate an appropriate title in
    Italian.

    Args:
        llm: An instance of a language model that processes the prompt and generates text.
        search_text (str): The question posed by the user, which serves as the basis for
                        the conversation title.
        result_answer (str): The response provided by the AI, which complements the
                            user's question in the title generation process.

    Returns:
        str: A generated title for the conversation in Italian.

    Example:
        >>> title = generate_conversation_title(llm_instance, "Qual è la tua opinione sul clima?",
                                "Credo che sia un problema serio.")
        >>> print(title)
        "Discussione sul cambiamento climatico"
    """
    title_prompt = PromptTemplate(
        input_variables=["question", "answer"],
        template="""Generate a title for a conversation where the user asks:
                            '{question}' and the AI responds: '{answer}'.""",
    )
    title_chain = title_prompt | llm | StrOutputParser()
    conversation_title = title_chain.invoke(
        {"question": search_text, "answer": result_answer},
    )

    return conversation_title


class Citation(BaseModel):
    quote: str = Field(
        ...,
        description="The VERBATIM quote from the specified source that justifies the answer.",
    )


class DocumentCitations(BaseModel):
    """Annotate the answer to the user question with quote citations that justify the answer."""

    document_id: str = Field(
        ...,
        description="The document_id of the SPECIFIC source which justifies the answer.",
    )

    citations: List[Citation] = Field(
        ..., description="Citations from the given sources that justify the answer."
    )


class Citations(BaseModel):
    citations: List[DocumentCitations] = Field(
        ..., description="Citations from the given sources that justify the answer."
    )


def stream_rag_conversation(
    search_text: str,
    reranker_api_url: str,
    range_values: list,
    after_key: str,
    suggest_keyword: str,
    suggestion_category_id: int,
    virtual_host: str,
    jwt: str,
    extra: dict,
    sort: list,
    sort_after_key: str,
    language: str,
    opensearch_host: str,
    grpc_host: str,
    chat_id: str,
    user_id: str,
    chat_history: list,
    timestamp: str,
    chat_sequence_number: int,
    configuration: dict,
):
    """
    Orchestrates a conversational RAG (Retrieval-Augmented Generation) pipeline with memory.

    This function handles the complete flow from query processing to response generation, including:
    - Document retrieval from OpenSearch
    - Context-aware response generation using language models
    - Conversation history management
    - Real-time streaming of partial results
    - Citation handling and document metadata processing

    Args:
        search_text (str): User's query text to process.
        reranker_api_url (str): Endpoint URL for the reranking service.
        range_values (list): Range filters for document retrieval.
        after_key (str): Pagination key for search results.
        suggest_keyword (str): Suggested keyword for query expansion.
        suggestion_category_id (int): Category ID for suggestions.
        virtual_host (str): Virtual host configuration for OpenSearch.
        jwt (str): Authentication token for secured services.
        extra (dict): Additional parameters for search customization.
        sort (list): Sorting criteria for search results.
        sort_after_key (str): Key for sorted pagination.
        language (str): Language code for localization.
        opensearch_host (str): OpenSearch cluster endpoint.
        grpc_host (str): gRPC service endpoint for embeddings.
        chat_id (str): Unique identifier for the chat session.
        user_id (str): Unique identifier for the user.
        chat_history (list): Chat history for not logged users.
        timestamp (str): ISO format timestamp of the request.
        chat_sequence_number (int): Sequence number in conversation history.
        configuration (dict): Configuration dictionary containing:
            - api_url (str): URL for the API endpoint
            - api_key (str): API key for authentication
            - model_type (str): Type of LLM to use (default: DEFAULT_MODEL_TYPE)
            - model (str):  Name of the model to use; defaults to DEFAULT_MODEL if not provided
            - prompt_template (str): Main prompt template
            - rephrase_prompt_template (str): Contextualization prompt template
            - context_window (int): Model context window size
            - retrieve_citations (bool): Flag to enable citation extraction
            - rerank (bool): Whether to enable document reranking
            - chunk_window (int): if 0 disable context window merging, if > 0 and <=2 enable context window merging
            - metadata (dict): metadata for document fields extraction
            - retrieve_type (str): Document retrieval strategy
            - watsonx_project_id (str): Project ID for IBM WatsonX (required if using IBM_WATSONX)
            - chat_vertex_ai_credentials (dict): Credentials for Google Vertex AI (required if using CHAT_VERTEX_AI)
            - chat_vertex_ai_model_garden (dict): Configurations for Google Vertex AI Model Garden (required if using CHAT_VERTEX_AI_MODEL_GARDEN)

    Yields:
        Iterator[str]: JSON-encoded stream objects with following formats:
        - {"chunk": "", "type": "START"} - Stream initialization
        - {"chunk": str, "type": "CHUNK"} - Partial response chunks
        - {"chunk": str, "type": "TITLE"} - Generated conversation title
        - {"chunk": dict, "citations": list, "type": "DOCUMENT"} - Processed documents with metadata
        - {"chunk": "", "type": "END"} - Stream termination

    Raises:
        ConnectionError: If unable to connect to OpenSearch or gRPC services
        ValueError: For invalid configuration parameters or missing required fields
        RuntimeError: For failures in the RAG chain execution

    Notes:
        - Requires running OpenSearch cluster and gRPC embedding service
        - Uses LangChain components for RAG pipeline construction
        - Maintains conversation history through OpenSearch integrations
        - Response streaming implemented using generator pattern
        - Document processing includes deduplication and citation mapping
    """
    model_type = configuration.get("model_type", DEFAULT_MODEL_TYPE)
    model = configuration.get("model", DEFAULT_MODEL)
    prompt_template = configuration.get("prompt_template")
    rephrase_prompt_template = configuration.get("rephrase_prompt_template")
    context_window = configuration.get("context_window")
    retrieve_citations = configuration.get("retrieve_citations")
    retrieve_type = configuration.get("retrieve_type")
    rerank = configuration.get("rerank")
    chunk_window = configuration.get("chunk_window")
    metadata = configuration.get("metadata")

    open_search_client = OpenSearch(
        hosts=[opensearch_host],
    )

    retriever = OpenSearchRetriever(
        search_text=search_text,
        rerank=rerank,
        reranker_api_url=reranker_api_url,
        chunk_window=chunk_window,
        range_values=range_values,
        after_key=after_key,
        suggest_keyword=suggest_keyword,
        suggestion_category_id=suggestion_category_id,
        virtual_host=virtual_host,
        jwt=jwt,
        extra=extra,
        sort=sort,
        sort_after_key=sort_after_key,
        language=language,
        context_window=context_window,
        metadata=metadata,
        retrieve_type=retrieve_type,
        opensearch_host=opensearch_host,
        grpc_host=grpc_host,
    )

    llm = initialize_language_model(configuration)

    info = {
        "chain": "chat_chain",
        "user_id": user_id,
        "model_type": model_type,
        "model": model,
        "question": search_text[:200],
    }
    logger.info(json.dumps(info))

    contextualize_q_system_prompt = rephrase_prompt_template

    contextualize_q_prompt = ChatPromptTemplate.from_messages(
        [
            ("system", contextualize_q_system_prompt),
            MessagesPlaceholder("chat_history"),
            ("human", "{input}"),
        ]
    )

    history_aware_retriever = create_history_aware_retriever(
        llm, retriever, contextualize_q_prompt
    )

    qa_prompt = ChatPromptTemplate.from_messages(
        [
            ("system", prompt_template),
            MessagesPlaceholder("chat_history"),
            ("human", "{input}"),
        ]
    )

    question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)

    rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)

    history_factory = (
        get_chat_history_from_frontend if not user_id else get_chat_history
    )

    history_factory_config = (
        [
            ConfigurableFieldSpec(
                id="chat_history_from_frontend",
                annotation=str,
                name="chat_history_from_frontend",
                description="chat_history_from_frontend.",
                default="",
            ),
        ]
        if not user_id
        else [
            ConfigurableFieldSpec(
                id="open_search_client",
                annotation=str,
                name="Opensearch client",
                description="Opensearch client.",
                default="",
            ),
            ConfigurableFieldSpec(
                id="user_id",
                annotation=str,
                name="User ID",
                description="Unique identifier for the user.",
                default="",
            ),
            ConfigurableFieldSpec(
                id="chat_id",
                annotation=str,
                name="Chat ID",
                description="Unique identifier for the chat.",
                default="",
            ),
        ]
    )

    if (
        retrieve_citations
        and model_type != ModelType.HUGGING_FACE_CUSTOM.value
        and model_type != ModelType.CHAT_VERTEX_AI_MODEL_GARDEN.value
    ):
        citations_chain = qa_prompt | llm.with_structured_output(
            schema=Citations, include_raw=True, method="function_calling"
        )
        conversational_rag_chain = RunnableWithMessageHistory(
            rag_chain,
            history_factory,
            input_messages_key="input",
            history_messages_key="chat_history",
            output_messages_key="answer",
            history_factory_config=history_factory_config,
        ).assign(annotations=citations_chain)
    else:
        conversational_rag_chain = RunnableWithMessageHistory(
            rag_chain,
            history_factory,
            input_messages_key="input",
            history_messages_key="chat_history",
            output_messages_key="answer",
            history_factory_config=history_factory_config,
        )

    if not user_id:
        result = conversational_rag_chain.stream(
            {"input": search_text},
            config={
                "configurable": {
                    "chat_history_from_frontend": chat_history,
                }
            },
        )
    else:
        result = conversational_rag_chain.stream(
            {"input": search_text},
            config={
                "configurable": {
                    "open_search_client": open_search_client,
                    "user_id": user_id,
                    "chat_id": chat_id,
                }
            },
        )

    result_answer = ""
    documents = []
    documents_id = set()
    citations = []
    conversation_title = ""
    citations_response = []
    parsing_error = ""

    for chunk in result:
        if chunk and "answer" in chunk.keys() and result_answer == "":
            result_answer += chunk
            yield json.dumps({"chunk": "", "type": "START"})
        elif chunk and "answer" in chunk.keys():
            result_answer += chunk
            yield json.dumps({"chunk": chunk["answer"], "type": "CHUNK"})
        elif chunk and "context" in chunk.keys():
            for element in chunk["context"]:
                document = element.dict()
                document_id = document["metadata"]["document_id"]
                if document_id not in documents_id:
                    documents_id.add(document_id)
                    documents.append(document)
        elif (
            chunk
            and retrieve_citations
            and model_type != ModelType.HUGGING_FACE_CUSTOM.value
            and model_type != ModelType.CHAT_VERTEX_AI_MODEL_GARDEN.value
            and "annotations" in chunk.keys()
        ):
            citations_response.append(chunk["annotations"])

    for element in citations_response:
        if "parsing_error" in element:
            parsing_error = element["parsing_error"]
        elif "parsed" in element:
            citations = element["parsed"]

    all_citations = (
        citations.dict()["citations"]
        if citations
        and retrieve_citations
        and parsing_error is None
        and model_type != ModelType.HUGGING_FACE_CUSTOM.value
        and model_type != ModelType.CHAT_VERTEX_AI_MODEL_GARDEN.value
        else []
    )
    all_citations_dict = (
        {citation["document_id"]: citation["citations"] for citation in all_citations}
        if retrieve_citations
        and model_type != ModelType.HUGGING_FACE_CUSTOM.value
        and model_type != ModelType.CHAT_VERTEX_AI_MODEL_GARDEN.value
        else {}
    )

    for document in documents:
        document_id = document["metadata"]["document_id"]
        document_chunk = document["metadata"]
        document_chunk.pop("document_id", None)
        document_chunk.pop("chunk_idx", None)
        document_chunk.pop("prev", None)
        document_chunk.pop("next", None)

        document_citations = all_citations_dict.get(document_id, [])
        document["citations"] = document_citations

        yield json.dumps(
            {
                "chunk": document_chunk,
                "citations": document_citations,
                "type": "DOCUMENT",
            }
        )

    if chat_sequence_number == 1 and user_id:
        conversation_title = generate_conversation_title(
            llm, search_text, result_answer
        )
        yield json.dumps({"chunk": conversation_title.strip('"'), "type": "TITLE"})

        info = {
            "chain": "chat_chain",
            "user_id": user_id,
            "conversation_title": conversation_title.strip('"'),
        }
        logger.info(json.dumps(info))

    info = {
        "chain": "chat_chain",
        "user_id": user_id,
        "answer": result_answer["answer"][:200] + "...",
        "retrieved_documents_number": len(documents),
    }
    logger.info(json.dumps(info))

    if user_id:
        save_chat_message(
            open_search_client,
            search_text,
            result_answer["answer"],
            conversation_title,
            documents,
            chat_id,
            user_id,
            timestamp,
            chat_sequence_number,
        )

    yield json.dumps({"chunk": "", "type": "END"})


def embedding(grpc_host, virtual_host, openserach_host, document):
    # embedding_model_configuration = get_embedding_model_configuration(
    #     grpc_host=grpc_host,
    #     virtual_host=virtual_host,
    # )

    json_config = Struct()
    json_config.update(
        {
            "separator": ".",
            "size": 100,
            "overlap": 20,
            "model_name": "gpt-4",
            "encoding": "cl100k_base",
        }
    )
    chunk = {"type": 1, "jsonConfig": json_config}
    json_config = Struct()
    json_config.update(
        {
            "api_url": "api_url",
            "watsonx_project_id": "watsonx_project_id",
            "chat_vertex_ai_model_garden": "chat_vertex_ai_model_garden",
        }
    )
    provider_model = {"provider": "openai", "model": "text-embedding-3-small"}
    embedding_model = {
        "apiKey": "",
        "providerModel": provider_model,
        "jsonConfig": json_config,
    }

    generate_documents_embeddings(
        grpc_host, openserach_host, chunk, embedding_model, document
    )
