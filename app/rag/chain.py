import json

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_ollama import ChatOllama
from langchain_openai import ChatOpenAI

from app.external_services.grpc.grpc_client import get_llm_configuration
from app.rag.retriever import OpenSearchRetriever

OLLAMA_URL = 'http://35.207.116.4:11434'


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
    chat_model_type = json_config["modelType"]

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

    # if chat_model_type == 'openai':
    #     model = ChatOpenAI(openai_api_key=api_key)
    # elif chat_model_type == 'ollama-external':
    #     model = OllamaExternalChatModel(OLLAMA_URL)

    model = ChatOpenAI(openai_api_key=api_key)
        
    prompt = ChatPromptTemplate.from_template(
        f"{json_config["prompt"]}"
    )

    parser = StrOutputParser()

    chain = prompt | model | parser

    for chunk in chain.stream({"question": question, "context": documents}):
            yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    yield json.dumps({"chunk": "", "type": "END"})


# def get_chat_chain(
#     searchQuery,
#     range,
#     afterKey,
#     suggestKeyword,
#     suggestionCategoryId,
#     jwt,
#     extra,
#     sort,
#     sortAfterKey,
#     language,
#     vectorIndices,
#     virtualHost,
#     searchText
# ):
#     configuration = get_llm_configuration(virtualHost)
#     api_url = configuration["api_url"]
#     api_key = configuration["api_key"]
#     json_config = configuration["json_config"]
#     chat_model_type = json_config["modelType"]

#     documents = OpenSearchRetriever._get_relevant_documents(
#         searchQuery,
#         range,
#         afterKey,
#         suggestKeyword,
#         suggestionCategoryId,
#         virtualHost,
#         jwt,
#         extra,
#         sort,
#         sortAfterKey,
#         language,
#         vectorIndices,
#     )

#     if chat_model_type == 'openai':
#         model = ChatOpenAI(openai_api_key=api_key)
#     elif chat_model_type == 'ollama-external':
#         model = OllamaExternalChatModel(OLLAMA_URL)

#     prompt = ChatPromptTemplate.from_template(
#         f"{json_config["prompt"]}"
#     )

#     parser = StrOutputParser()

#     chain = prompt | model | parser

#     for chunk in chain.stream({"question": searchText, "context": documents}):
#         yield json.dumps({"chunk": chunk, "type": "CHUNK"})

#     yield json.dumps({"chunk": "", "type": "END"})    


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
    # chat_model_type = json_config["modelType"]

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

    model = ChatOpenAI(openai_api_key=api_key)

    prompt = ChatPromptTemplate.from_template(
        f"{json_config["prompt"]}"
    )
    model = ChatOllama(model="llama3", base_url=OLLAMA_URL)

    # prompt = ChatPromptTemplate.from_template(
    #     f"{json_config["prompt"]}"
    # )
    
    parser = StrOutputParser()

    chain = model | parser

    for chunk in chain.stream(searchText):
        yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    for element in documents:
        yield json.dumps({"chunk": dict(element.metadata), "type": "DOCUMENT"})
        yield json.dumps({"chunk": "", "type": "END"})
