import json

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_openai import ChatOpenAI
from pydantic import BaseModel

from app.grpc.grpc_client import get_llm_configuration
from app.retriever import OpenSearchRetriever


class QueryInput(BaseModel):
    searchQuery: list
    range: list
    afterKey: str
    suggestKeyword: str
    suggestionCategoryId: int
    virtualHost: str
    jwt: str
    extra: dict
    sort: list
    sortAfterKey: str
    language: str


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
):
    # TODO: replace "jynx" with virtualHost
    configuration = get_llm_configuration("jynx")
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
    )

    model = ChatOpenAI(openai_api_key=api_key)

    prompt = ChatPromptTemplate.from_template(
        f"{json_config["prompt"]}"
    )

    parser = StrOutputParser()

    chain = prompt | model | parser

    for chunk in chain.stream({"question": question, "context": documents}):
        yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    yield json.dumps({"chunk": "", "type": "END"})    
