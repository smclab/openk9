import json

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_ollama import ChatOllama
from langchain_openai import ChatOpenAI

from app.external_services.grpc.grpc_client import get_llm_configuration
from app.rag.custom_hugging_face_model import CustomChatHuggingFaceModel
from app.rag.retriever import OpenSearchRetriever

DEFAULT_MODEL_TYPE = "openai"
DEFAULT_MODEL = "gpt-3.5-turbo"


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
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtualHost)
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    model = configuration["model"] if configuration["model"] else DEFAULT_MODEL

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
        grpc_host,
    )

    if model_type == "openai":
        llm = ChatOpenAI(model=model, openai_api_key=api_key)
    elif model_type == "ollama":
        llm = ChatOllama(model=model, base_url=api_url)
    elif model_type == "hugging-face-custom":
        llm = CustomChatHuggingFaceModel(base_url=api_url)

    prompt = ChatPromptTemplate.from_template(configuration["prompt"])

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
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtualHost)
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    model = configuration["model"] if configuration["model"] else DEFAULT_MODEL

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
        grpc_host,
    )

    if model_type == "openai":
        llm = ChatOpenAI(model=model, openai_api_key=api_key)
    elif model_type == "ollama":
        llm = ChatOllama(model=model, base_url=api_url)
    elif model_type == "hugging-face-custom":
        llm = CustomChatHuggingFaceModel(base_url=api_url)

    prompt = ChatPromptTemplate.from_template(configuration["prompt"])

    parser = StrOutputParser()

    chain = prompt | llm | parser

    for chunk in chain.stream({"question": searchText, "context": documents}):
        yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    for element in documents:
        yield json.dumps({"chunk": dict(element.metadata), "type": "DOCUMENT"})
        yield json.dumps({"chunk": "", "type": "END"})
