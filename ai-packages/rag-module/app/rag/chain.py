import json
from enum import Enum
from typing import List

from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import ConfigurableFieldSpec
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_ollama import ChatOllama
from langchain_openai import ChatOpenAI
from opensearchpy import OpenSearch
from pydantic import BaseModel, Field

from app.external_services.grpc.grpc_client import get_llm_configuration
from app.rag.custom_hugging_face_model import CustomChatHuggingFaceModel
from app.rag.retriever import OpenSearchRetriever
from app.utils.chat_history import get_chat_history, save_chat_message

DEFAULT_MODEL_TYPE = "openai"
DEFAULT_MODEL = "gpt-4o-mini"


class ModelType(Enum):
    OPENAI = "openai"
    OLLAMA = "ollama"
    HUGGING_FACE_CUSTOM = "hugging-face-custom"


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
    opensearch_host,
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtual_host)
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    model = configuration["model"] if configuration["model"] else DEFAULT_MODEL
    prompt_template = configuration["prompt"]
    rephrase_prompt_template = configuration["rephrase_prompt"]
    context_window = configuration["context_window"]
    retrieve_type = configuration["retrieve_type"]

    retriever = OpenSearchRetriever(
        search_query=search_query,
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

    match model_type:
        case ModelType.OPENAI.value:
            llm = ChatOpenAI(model=model, openai_api_key=api_key)
        case ModelType.OLLAMA.value:
            llm = ChatOllama(model=model, base_url=api_url)
        case ModelType.HUGGING_FACE_CUSTOM.value:
            llm = CustomChatHuggingFaceModel(base_url=api_url)
        case _:
            llm = ChatOpenAI(model=model, openai_api_key=api_key)

    prompt = ChatPromptTemplate.from_template(prompt_template)
    parser = StrOutputParser()
    chain = prompt | llm | parser

    if reformulate:
        rephrase_prompt = PromptTemplate.from_template(rephrase_prompt_template)
        rephrase_chain = rephrase_prompt | llm | parser
        question = rephrase_chain.invoke({"question": question})

    for chunk in chain.stream({"question": question, "context": documents}):
        yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    for element in documents:
        yield json.dumps({"chunk": dict(element.metadata), "type": "DOCUMENT"})

    yield json.dumps({"chunk": "", "type": "END"})


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


def get_chat_chain(
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
    search_text,
    chat_id,
    user_id,
    timestamp,
    chat_sequence_number,
    retrieve_citations,
    opensearch_host,
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtual_host)
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    model = configuration["model"] if configuration["model"] else DEFAULT_MODEL
    prompt_template = configuration["prompt"]
    rephrase_prompt_template = configuration["rephrase_prompt"]
    context_window = configuration["context_window"]
    retrieve_type = configuration["retrieve_type"]

    open_search_client = OpenSearch(
        hosts=[opensearch_host],
    )

    retriever = OpenSearchRetriever(
        search_query=search_query,
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

    match model_type:
        case ModelType.OPENAI.value:
            llm = ChatOpenAI(model=model, openai_api_key=api_key)
        case ModelType.OLLAMA.value:
            llm = ChatOllama(model=model, base_url=api_url)
        case ModelType.HUGGING_FACE_CUSTOM.value:
            llm = CustomChatHuggingFaceModel(base_url=api_url)
        case _:
            llm = ChatOpenAI(model=model, openai_api_key=api_key)

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

    if retrieve_citations and model_type != ModelType.HUGGING_FACE_CUSTOM.value:
        citations_chain = qa_prompt | llm.with_structured_output(Citations)
        conversational_rag_chain = RunnableWithMessageHistory(
            rag_chain,
            get_chat_history,
            input_messages_key="input",
            history_messages_key="chat_history",
            output_messages_key="answer",
            history_factory_config=[
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
            ],
        ).assign(annotations=citations_chain)
    else:
        conversational_rag_chain = RunnableWithMessageHistory(
            rag_chain,
            get_chat_history,
            input_messages_key="input",
            history_messages_key="chat_history",
            output_messages_key="answer",
            history_factory_config=[
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
            ],
        )

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
    citations = []

    for chunk in result:
        if "answer" in chunk.keys():
            result_answer += chunk
            yield json.dumps({"chunk": chunk["answer"], "type": "CHUNK"})
        if "context" in chunk.keys():
            for element in chunk["context"]:
                documents.append(element.dict())
        if (
            retrieve_citations
            and model_type != ModelType.HUGGING_FACE_CUSTOM.value
            and "annotations" in chunk.keys()
        ):
            citations = chunk

    all_citations = (
        citations["annotations"]["citations"]
        if retrieve_citations and model_type != ModelType.HUGGING_FACE_CUSTOM.value
        else []
    )
    all_citations_dict = (
        {citation["document_id"]: citation["citations"] for citation in all_citations}
        if retrieve_citations and model_type != ModelType.HUGGING_FACE_CUSTOM.value
        else {}
    )

    for document in documents:
        document_id = document["metadata"]["document_id"]
        document_chunk = document["metadata"]
        del document_chunk["document_id"]

        document_citations = all_citations_dict.get(document_id, [])
        document["citations"] = document_citations

        yield json.dumps(
            {
                "chunk": document_chunk,
                "citations": document_citations,
                "type": "DOCUMENT",
            }
        )

    save_chat_message(
        open_search_client,
        search_text,
        result_answer["answer"],
        documents,
        chat_id,
        user_id,
        timestamp,
        chat_sequence_number,
    )

    yield json.dumps({"chunk": "", "type": "END"})
