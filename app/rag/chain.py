import json

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_openai import ChatOpenAI

from app.external_services.grpc.grpc_client import get_llm_configuration
from app.rag.retriever import OpenSearchRetriever


def get_chain(
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
    question,
    opensearch_host,
    grpc_host
):
    configuration = get_llm_configuration(grpc_host, virtualHost)
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    json_config = configuration["json_config"]
    model_type = configuration["type"]
    model = configuration["model"] if configuration["model"] else 'gpt-3.5-turbo'

    documents = OpenSearchRetriever._get_relevant_documents(
        searchQuery,
        range,
        afterKey,
        suggestKeyword,
        suggestionCategoryId,
        virtualHost,
        jwt,
        extra,
        sort,
        sortAfterKey,
        language,
        vectorIndices,
        opensearch_host,
        grpc_host
    )

    if model_type == 'opeani':
        llm = ChatOpenAI(model=model,openai_api_key=api_key)

    prompt = ChatPromptTemplate.from_template(
        f"{json_config["prompt"]}"
    )

    parser = StrOutputParser()

    chain = prompt | llm | parser

    for chunk in chain.stream({"question": question, "context": documents}):
            yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    yield json.dumps({"chunk": "", "type": "END"})


def get_chat_chain(
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
    opensearch_host,
    grpc_host
):
    configuration = get_llm_configuration(grpc_host, virtualHost)
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    json_config = configuration["json_config"]

    documents = OpenSearchRetriever._get_relevant_documents(
        searchQuery,
        range,
        afterKey,
        suggestKeyword,
        suggestionCategoryId,
        virtualHost,
        jwt,
        extra,
        sort,
        sortAfterKey,
        language,
        vectorIndices,
        opensearch_host,
        grpc_host
    )

    llm = ChatOpenAI(openai_api_key=api_key)

    prompt = ChatPromptTemplate.from_template(
        f"{json_config["prompt"]}"
    )

    parser = StrOutputParser()

    chain = prompt | llm | parser

    for chunk in chain.stream({"question": searchText, "context": documents}):
        yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    for element in documents:
        yield json.dumps({"chunk": dict(element.metadata), "type": "DOCUMENT"})
        yield json.dumps({"chunk": "", "type": "END"})
