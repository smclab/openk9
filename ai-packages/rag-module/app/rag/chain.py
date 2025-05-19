import json
from enum import Enum

from langchain.prompts import ChatPromptTemplate, PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.tools import tool
from opensearchpy import OpenSearch

from app.external_services.grpc.grpc_client import (
    get_llm_configuration,
    get_rag_configuration,
)
from app.rag.retriever import OpenSearchRetriever
from app.utils.chat_history import get_chat_history_from_frontend, save_chat_message
from app.utils.llm import (
    generate_conversation_title,
    initialize_language_model,
    stream_rag_conversation,
)


class RagType(Enum):
    RAG_TYPE_UNSPECIFIED = "RAG_TYPE_UNSPECIFIED"
    CHAT_RAG = "CHAT_RAG"
    CHAT_RAG_TOOL = "CHAT_RAG_TOOL"
    SIMPLE_GENERATE = "SIMPLE_GENERATE"


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
    reformulate,
    reranker_api_url,
    opensearch_host,
    grpc_host,
):

    rag_configuration = get_rag_configuration(
        grpc_host, virtual_host, RagType.SIMPLE_GENERATE.value
    )

    prompt_template = rag_configuration.get("prompt")
    prompt_template = (
        "Here is context to help: {{context}}. ### QUESTION: {{question}}"
        + prompt_template
    )
    rephrase_prompt_template = rag_configuration.get("rephrase_prompt")
    reformulate = rag_configuration.get("reformulate")
    rerank = rag_configuration.get("rerank")
    metadata = rag_configuration.get("metadata")

    llm_configuration = get_llm_configuration(grpc_host, virtual_host)
    api_url = llm_configuration.get("api_url")
    api_key = llm_configuration.get("api_key")
    model_type = llm_configuration.get("model_type")
    model = llm_configuration.get("model")
    context_window = llm_configuration.get("context_window")
    retrieve_citations = llm_configuration.get("retrieve_citations")
    retrieve_type = llm_configuration.get("retrieve_type")
    watsonx_project_id = llm_configuration.get("watsonx_project_id")
    chat_vertex_ai_credentials = llm_configuration.get("chat_vertex_ai_credentials")
    chat_vertex_ai_model_garden = llm_configuration.get("chat_vertex_ai_model_garden")

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
        retrieve_type=retrieve_type,
        opensearch_host=opensearch_host,
        grpc_host=grpc_host,
    )

    documents = retriever.invoke(question)
    llm = initialize_language_model(configuration)
    prompt = ChatPromptTemplate.from_template(prompt_template)
    parser = StrOutputParser()
    chain = prompt | llm | parser

    if reformulate:
        rephrase_prompt = PromptTemplate.from_template(rephrase_prompt_template)
        rephrase_chain = rephrase_prompt | llm | parser
        question = rephrase_chain.invoke({"question": question})

    yield json.dumps({"chunk": "", "type": "START"})

    for chunk in chain.stream({"question": question, "context": documents}):
        yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    yield json.dumps({"chunk": "", "type": "END"})


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
    chat_history,
    timestamp,
    chat_sequence_number,
    reranker_api_url,
    opensearch_host,
    grpc_host,
):
    rag_configuration = get_rag_configuration(
        grpc_host, virtual_host, RagType.CHAT_RAG.value
    )
    prompt_template = rag_configuration.get("prompt")
    prompt_template = (
        "Here is context to help: {context}. ### QUESTION: {{question}}"
        + prompt_template
    )
    rephrase_prompt_template = rag_configuration.get("rephrase_prompt")
    reformulate = rag_configuration.get("reformulate")
    rerank = rag_configuration.get("rerank")
    chunk_window = rag_configuration.get("chunk_window")
    metadata = rag_configuration.get("metadata")

    llm_configuration = get_llm_configuration(grpc_host, virtual_host)
    api_url = llm_configuration.get("api_url")
    api_key = llm_configuration.get("api_key")
    model_type = llm_configuration.get("model_type")
    model = llm_configuration.get("model")
    context_window = llm_configuration.get("context_window")
    retrieve_citations = llm_configuration.get("retrieve_citations")
    retrieve_type = llm_configuration.get("retrieve_type")
    watsonx_project_id = llm_configuration.get("watsonx_project_id")
    chat_vertex_ai_credentials = llm_configuration.get("chat_vertex_ai_credentials")
    chat_vertex_ai_model_garden = llm_configuration.get("chat_vertex_ai_model_garden")

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
        grpc_host,
        chat_id,
        user_id,
        chat_history,
        timestamp,
        chat_sequence_number,
        configuration,
    )


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
    chat_history,
    timestamp,
    chat_sequence_number,
    reranker_api_url,
    opensearch_host,
    grpc_host,
):
    rag_configuration = get_rag_configuration(
        grpc_host, virtual_host, RagType.CHAT_RAG_TOOL.value
    )
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
    metadata = rag_configuration.get("metadata")
    rag_tool_description = rag_configuration.get("rag_tool_description")

    llm_configuration = get_llm_configuration(grpc_host, virtual_host)
    api_url = llm_configuration.get("api_url")
    api_key = llm_configuration.get("api_key")
    model_type = llm_configuration.get("model_type")
    model = llm_configuration.get("model")
    context_window = llm_configuration.get("context_window")
    retrieve_citations = llm_configuration.get("retrieve_citations")
    retrieve_type = llm_configuration.get("retrieve_type")
    watsonx_project_id = llm_configuration.get("watsonx_project_id")
    chat_vertex_ai_credentials = llm_configuration.get("chat_vertex_ai_credentials")
    chat_vertex_ai_model_garden = llm_configuration.get("chat_vertex_ai_model_garden")

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
        "metadata": metadata,
        "retrieve_type": retrieve_type,
        "watsonx_project_id": watsonx_project_id,
        "chat_vertex_ai_credentials": chat_vertex_ai_credentials,
        "chat_vertex_ai_model_garden": chat_vertex_ai_model_garden,
    }

    llm = initialize_language_model(configuration)
    parser = StrOutputParser()

    if reformulate and chat_history:
        rephrase_prompt = PromptTemplate.from_template(rephrase_prompt_template)
        rephrase_chain = rephrase_prompt | llm | parser
        search_text = rephrase_chain.invoke(
            {"input": search_text, "chat_history": chat_history},
        )

    rag_tool.description = rag_tool_description
    tools = [rag_tool]
    llm_with_tools = llm.bind_tools(tools)
    llm_with_tools_response = llm_with_tools.invoke(search_text)

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
            grpc_host,
            chat_id,
            user_id,
            chat_history,
            timestamp,
            chat_sequence_number,
            configuration,
        )

    else:
        prompt_no_rag = (
            "### QUESTION: {question}. Here is the chat history: {history}"
            + prompt_no_rag
            if chat_history
            else "### QUESTION: {question}." + prompt_no_rag
        )

        prompt = ChatPromptTemplate.from_template(prompt_no_rag)
        input_data = {"question": search_text}
        if chat_history:
            input_data["history"] = get_chat_history_from_frontend(chat_history)

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
            yield json.dumps({"chunk": conversation_title.strip('"'), "type": "TITLE"})

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
                timestamp,
                chat_sequence_number,
            )

        yield json.dumps({"chunk": "", "type": "END"})
