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
from app.utils.chat_history import save_chat_message
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
    vector_indices,
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
    print(rag_configuration)
    configuration = get_llm_configuration(grpc_host, virtual_host)
    prompt_template = configuration["prompt"]
    rephrase_prompt_template = configuration["rephrase_prompt"]
    context_window = configuration["context_window"]
    retrieve_type = configuration["retrieve_type"]
    rerank = configuration["rerank"]

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
        vector_indices=vector_indices,
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
    vector_indices,
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
    print(rag_configuration)
    configuration = get_llm_configuration(grpc_host, virtual_host)

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
        vector_indices,
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
    vector_indices,
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
    print(rag_configuration)
    configuration = get_llm_configuration(grpc_host, virtual_host)
    rag_tool_description = configuration["rag_tool_description"]

    llm = initialize_language_model(configuration)

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
            vector_indices,
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
        prompt_template = configuration["prompt_no_rag"]
        prompt = ChatPromptTemplate.from_template(prompt_template)
        parser = StrOutputParser()
        chain = prompt | llm | parser

        result = chain.stream({"question": search_text})

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
