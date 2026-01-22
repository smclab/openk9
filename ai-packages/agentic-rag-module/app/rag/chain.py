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
from typing import List, TypedDict

from IPython.display import Image, display
from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
from langchain_core.documents.base import Document
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import RunnablePassthrough
from langchain_core.tools import tool
from langgraph.graph import END, StateGraph
from opensearchpy import OpenSearch
from pydantic import BaseModel, Field

from app.rag.agentic_rag import RagGraph
from app.rag.retrievers.retriever import OpenSearchRetriever
from app.utils.llm import (
    generate_conversation_title,
    initialize_language_model,
    stream_rag_conversation,
)
from app.utils.logger import logger

UNEXPECTED_ERROR_MESSAGE = "Unexpected error"


def get_agentic_rag(
    rag_type,
    search_query,
    range_values,
    after_key,
    suggest_keyword,
    suggestion_category_id,
    jwt,
    extra,
    sort,
    sort_after_key,
    language,
    virtual_host,
    search_text,
    chat_id,
    user_id,
    tenant_id,
    retrieve_from_uploaded_documents,
    chat_history,
    timestamp,
    chat_sequence_number,
    enable_real_time_evaluation,
    rag_configuration,
    llm_configuration,
    opensearch_host,
    grpc_host_embedding,
    grpc_host_datasource,
):
    try:
        prompt_template = rag_configuration.get("prompt")
        # prompt_template = (
        #     "Here is context to help: {context}. ### QUESTION: {{question}}"
        #     + prompt_template
        # )
        rephrase_prompt_template = rag_configuration.get("rephrase_prompt")
        prompt_no_rag = rag_configuration.get("prompt_no_rag")
        analyze_query_prompt_template = rag_configuration.get("analyze_query_prompt")
        reformulate = rag_configuration.get("reformulate")
        rerank = rag_configuration.get("rerank")
        chunk_window = rag_configuration.get("chunk_window")
        metadata = rag_configuration.get("metadata")
        rag_tool_description = rag_configuration.get("rag_tool_description")

        no_rag = False

        api_url = llm_configuration.get("api_url")
        api_key = llm_configuration.get("api_key")
        model_type = llm_configuration.get("model_type")
        model = llm_configuration.get("model")
        context_window = llm_configuration.get("context_window")
        retrieve_citations = llm_configuration.get("retrieve_citations")
        retrieve_type = llm_configuration.get("retrieve_type")
        watsonx_project_id = llm_configuration.get("watsonx_project_id")
        chat_vertex_ai_credentials = llm_configuration.get("chat_vertex_ai_credentials")
        chat_vertex_ai_model_garden = llm_configuration.get(
            "chat_vertex_ai_model_garden"
        )

        llm_configuration = {
            "api_url": api_url,
            "api_key": api_key,
            "model_type": model_type,
            "model": model,
            "prompt_template": prompt_template,
            "rephrase_prompt_template": rephrase_prompt_template,
            "context_window": context_window,
            "retrieve_citations": retrieve_citations,
            "rerank": rerank,
            "chunk_window": chunk_window,
            "metadata": metadata,
            "retrieve_type": retrieve_type,
            "watsonx_project_id": watsonx_project_id,
            "chat_vertex_ai_credentials": chat_vertex_ai_credentials,
            "chat_vertex_ai_model_garden": chat_vertex_ai_model_garden,
        }

        llm = initialize_language_model(llm_configuration)

        graph_configuration = {
            "rag_type": rag_type,
            "search_query": search_query,
            "tenant_id": tenant_id,
            "user_id": user_id,
            "chat_id": chat_id,
            "chat_sequence_number": chat_sequence_number,
            "chat_history": chat_history,
            "rerank": rerank,
            "no_rag": no_rag,
            "chunk_window": chunk_window,
            "range_values": range_values,
            "after_key": after_key,
            "suggest_keyword": suggest_keyword,
            "suggestion_category_id": suggestion_category_id,
            "virtual_host": virtual_host,
            "jwt": jwt,
            "extra": extra,
            "sort": sort,
            "sort_after_key": sort_after_key,
            "language": language,
            "context_window": context_window,
            "metadata": metadata,
            "retrieve_type": retrieve_type,
            "opensearch_host": opensearch_host,
            "grpc_host": grpc_host_datasource,
            "prompt_template": prompt_template,
            "rephrase_prompt_template": rephrase_prompt_template,
            "prompt_no_rag": prompt_no_rag,
            "rag_tool_description": rag_tool_description,
            "analyze_query_prompt_template": analyze_query_prompt_template,
            "enable_real_time_evaluation": enable_real_time_evaluation,
        }

        router = RagGraph(llm, graph_configuration)
        # result = router.invoke(search_text)
        yield from router.stream(search_text)

    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE}: {e}")
        yield json.dumps({"chunk": UNEXPECTED_ERROR_MESSAGE, "type": "ERROR"})
