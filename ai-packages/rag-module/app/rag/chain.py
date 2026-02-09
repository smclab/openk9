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

from langchain.prompts import ChatPromptTemplate, PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import RunnablePassthrough
from langchain_core.tools import tool
from opensearchpy import OpenSearch

from app.rag.retrievers.retriever import OpenSearchRetriever
from app.utils.chat_history import (
    get_chat_history,
    get_chat_history_from_frontend,
    save_chat_message,
)
from app.utils.llm import (
    generate_conversation_title,
    initialize_language_model,
    stream_rag_conversation,
)
from app.utils.logger import logger

UNEXPECTED_ERROR_MESSAGE = "Unexpected error"


def get_chain(
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
    question,
    rag_configuration,
    llm_configuration,
    reranker_api_url,
    opensearch_host,
    grpc_host,
):
    try:
        prompt_template = rag_configuration.get("prompt")
        prompt_template = (
            "Here is context to help: {context}. ### QUESTION: {question}"
            + prompt_template
        )
        rephrase_prompt_template = rag_configuration.get("rephrase_prompt")
        reformulate = rag_configuration.get("reformulate")
        rerank = rag_configuration.get("rerank")
        metadata = rag_configuration.get("metadata")

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

        configuration = {
            "api_url": api_url,
            "api_key": api_key,
            "model_type": model_type,
            "model": model,
            "context_window": context_window,
            "retrieve_citations": retrieve_citations,
            "rerank": rerank,
            "metadata": metadata,
            "retrieve_type": retrieve_type,
            "watsonx_project_id": watsonx_project_id,
            "chat_vertex_ai_credentials": chat_vertex_ai_credentials,
            "chat_vertex_ai_model_garden": chat_vertex_ai_model_garden,
        }

        llm = initialize_language_model(configuration)
        parser = StrOutputParser()

        if reformulate:
            rephrase_prompt = PromptTemplate.from_template(rephrase_prompt_template)
            rephrase_chain = rephrase_prompt | llm | parser
            question = rephrase_chain.invoke({"question": question})

        retriever = OpenSearchRetriever(
            search_query=search_query,
            search_text=question,
            rerank=rerank,
            reranker_api_url=reranker_api_url,
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

        prompt = ChatPromptTemplate.from_template(prompt_template)

        chain = (
            {"context": retriever, "question": RunnablePassthrough()}
            | prompt
            | llm
            | parser
        )

        thinking_chunk = True
        start_chunk = True

        for chunk in chain.stream({"question": question}):
            if chunk == "" and thinking_chunk:
                continue
            else:
                if start_chunk:
                    yield json.dumps({"chunk": "", "type": "START"})
                    thinking_chunk = False
                    start_chunk = False
                yield json.dumps({"chunk": chunk, "type": "CHUNK"})

        yield json.dumps({"chunk": "", "type": "END"})

    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE}: {e}")
        yield json.dumps({"chunk": UNEXPECTED_ERROR_MESSAGE, "type": "ERROR"})


def get_chat_chain(
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
    rag_configuration,
    llm_configuration,
    embedding_model_configuration,
    reranker_api_url,
    opensearch_host,
    grpc_host_embedding,
    grpc_host_datasource,
):
    try:
        prompt_template = rag_configuration.get("prompt")
        prompt_template = (
            "Here is context to help: {context}. ### QUESTION: {{question}}"
            + prompt_template
        )
        rephrase_prompt_template = rag_configuration.get("rephrase_prompt")
        reformulate = rag_configuration.get("reformulate")
        rerank = rag_configuration.get("rerank")
        chunk_window = rag_configuration.get("chunk_window")
        enable_conversation_title = rag_configuration.get("enable_conversation_title")
        metadata = rag_configuration.get("metadata")

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

        configuration = {
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
            "enable_conversation_title": enable_conversation_title,
            "metadata": metadata,
            "retrieve_type": retrieve_type,
            "watsonx_project_id": watsonx_project_id,
            "chat_vertex_ai_credentials": chat_vertex_ai_credentials,
            "chat_vertex_ai_model_garden": chat_vertex_ai_model_garden,
        }

        yield from stream_rag_conversation(
            search_text,
            reranker_api_url,
            range_values,
            after_key,
            suggest_keyword,
            suggestion_category_id,
            virtual_host,
            jwt,
            extra,
            sort,
            sort_after_key,
            language,
            opensearch_host,
            grpc_host_embedding,
            grpc_host_datasource,
            chat_id,
            user_id,
            tenant_id,
            retrieve_from_uploaded_documents,
            chat_history,
            timestamp,
            chat_sequence_number,
            configuration,
            embedding_model_configuration,
        )

    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE}: {e}")
        yield json.dumps({"chunk": UNEXPECTED_ERROR_MESSAGE, "type": "ERROR"})


@tool
def rag_tool(
    search_text,
):
    """rag as a tool."""

    return search_text


def get_chat_chain_tool(
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
    rag_configuration,
    llm_configuration,
    embedding_model_configuration,
    reranker_api_url,
    opensearch_host,
    grpc_host_embedding,
    grpc_host_datasource,
):
    try:
        prompt_template = rag_configuration.get("prompt")
        prompt_template = (
            "Here is context to help: {context}. ### QUESTION: {{question}}"
            + prompt_template
        )
        rephrase_prompt_template = rag_configuration.get("rephrase_prompt")
        prompt_no_rag = rag_configuration.get("prompt_no_rag")
        reformulate = rag_configuration.get("reformulate")
        rerank = rag_configuration.get("rerank")
        chunk_window = rag_configuration.get("chunk_window")
        enable_conversation_title = rag_configuration.get("enable_conversation_title")
        metadata = rag_configuration.get("metadata")
        rag_tool_description = rag_configuration.get("rag_tool_description")

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

        configuration = {
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
            "enable_conversation_title": enable_conversation_title,
            "metadata": metadata,
            "retrieve_type": retrieve_type,
            "watsonx_project_id": watsonx_project_id,
            "chat_vertex_ai_credentials": chat_vertex_ai_credentials,
            "chat_vertex_ai_model_garden": chat_vertex_ai_model_garden,
        }

        llm = initialize_language_model(configuration)
        parser = StrOutputParser()

        search_query = search_text
        retrieved_chat_history = []
        input_data = {"question": search_text}

        if chat_sequence_number > 1:
            retrieved_chat_history = (
                get_chat_history(
                    open_search_client=OpenSearch(
                        hosts=[opensearch_host],
                    ),
                    user_id=user_id,
                    chat_id=chat_id,
                )
                if user_id and chat_id
                else get_chat_history_from_frontend(chat_history)
            )

            input_data["retrieved_chat_history"] = retrieved_chat_history

        if reformulate:
            rephrase_prompt_template = (
                "Here is the chat history: {retrieved_chat_history}, and the user's latest question: {question}."
                + rephrase_prompt_template
                if retrieved_chat_history
                else "### QUESTION: {question}." + rephrase_prompt_template
            )
            rephrase_prompt = PromptTemplate.from_template(rephrase_prompt_template)
            rephrase_chain = rephrase_prompt | llm | parser
            search_query = rephrase_chain.invoke(input_data)

        rag_tool.description = rag_tool_description
        tools = [rag_tool]
        llm_with_tools = llm.bind_tools(tools)
        llm_with_tools_response = llm_with_tools.invoke(search_query)

        if llm_with_tools_response.tool_calls:
            yield from stream_rag_conversation(
                search_text,
                reranker_api_url,
                range_values,
                after_key,
                suggest_keyword,
                suggestion_category_id,
                virtual_host,
                jwt,
                extra,
                sort,
                sort_after_key,
                language,
                opensearch_host,
                grpc_host_embedding,
                grpc_host_datasource,
                chat_id,
                user_id,
                tenant_id,
                retrieve_from_uploaded_documents,
                chat_history,
                timestamp,
                chat_sequence_number,
                configuration,
                embedding_model_configuration,
            )

        else:
            prompt_no_rag = (
                "### QUESTION: {question}. Here is the chat history: {retrieved_chat_history}."
                + prompt_no_rag
                if retrieved_chat_history
                else "### QUESTION: {question}." + prompt_no_rag
            )

            prompt = ChatPromptTemplate.from_template(prompt_no_rag)
            chain = prompt | llm | parser
            result = chain.stream(input_data)

            result_answer = ""
            documents = []
            conversation_title = ""
            start = True

            for chunk in result:
                if start:
                    start = False
                    yield json.dumps({"chunk": "", "type": "START"})

                result_answer += chunk
                yield json.dumps({"chunk": chunk, "type": "CHUNK"})

            if chat_sequence_number == 1:
                conversation_title = generate_conversation_title(
                    llm, search_text, result_answer
                )
                yield json.dumps(
                    {"chunk": conversation_title.strip('"'), "type": "TITLE"}
                )

            if user_id:
                open_search_client = OpenSearch(
                    hosts=[opensearch_host],
                )

                save_chat_message(
                    open_search_client,
                    search_text,
                    result_answer,
                    conversation_title.strip('"'),
                    documents,
                    chat_id,
                    user_id,
                    tenant_id,
                    timestamp,
                    chat_sequence_number,
                    retrieve_from_uploaded_documents,
                )

            yield json.dumps({"chunk": "", "type": "END"})

    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE}: {e}")
        yield json.dumps({"chunk": UNEXPECTED_ERROR_MESSAGE, "type": "ERROR"})
