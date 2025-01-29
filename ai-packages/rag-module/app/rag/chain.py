import json
import os
from enum import Enum
from typing import List

from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import ConfigurableFieldSpec
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.tools import tool
from langchain_ibm import ChatWatsonx
from langchain_ollama import ChatOllama
from langchain_openai import ChatOpenAI
from langchain_google_vertexai import ChatVertexAI
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
    IBM_WATSONX = "watsonx"
    CHAT_VERTEX_AI = "chat_vertex_ai"


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
            - "prompt": str
                The initial prompt to be used with the model.
            - "rephrase_prompt": str
                A prompt for rephrasing tasks, if applicable.
            - "context_window": int
                Size of the context window for the model's input.
            - "retrieve_type": str
                Specifies the type of retrieval mechanism to be used with the model.
            - "watsonx_project_id": str
                Project ID for IBM WatsonX (required if using WatsonX).
            - "chat_vertex_ai_credentials": dict
                Credentials for Google Vertex AI (required if using Vertexai).

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
            llm = ChatOpenAI(model=model, openai_api_key=api_key)
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
            credentials = configuration["chat_vertex_ai_credentials"]
            project_id = credentials["quota_project_id"]
            json_credentials = json.dumps(credentials, indent=2, sort_keys=True)
            credential_file_path = "application_default_credentials.json"
            with open(credential_file_path, "w", encoding="utf-8") as outfile:
                outfile.write(json_credentials)

            os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = credential_file_path

            llm = ChatVertexAI(
                model=model,
                project=project_id,
                temperature=0,
                max_tokens=None,
                max_retries=6,
                stop=None,
            )
        case _:
            llm = ChatOpenAI(model=model, openai_api_key=api_key)

    return llm


def get_chain(
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
    rerank,
    reranker_api_url,
    opensearch_host,
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtual_host)
    prompt_template = configuration["prompt"]
    rephrase_prompt_template = configuration["rephrase_prompt"]
    context_window = configuration["context_window"]
    retrieve_type = configuration["retrieve_type"]

    retriever = OpenSearchRetriever(
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
    rerank,
    reranker_api_url,
    chunk_window,
    opensearch_host,
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtual_host)
    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    prompt_template = configuration["prompt"]
    rephrase_prompt_template = configuration["rephrase_prompt"]
    context_window = configuration["context_window"]
    retrieve_type = configuration["retrieve_type"]

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
        vector_indices=vector_indices,
        context_window=context_window,
        retrieve_type=retrieve_type,
        opensearch_host=opensearch_host,
        grpc_host=grpc_host,
    )

    llm = initialize_language_model(configuration)

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
    documents_id = set()
    citations = []
    conversation_title = ""

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
            and "annotations" in chunk.keys()
        ):
            citations = chunk

    if chat_sequence_number == 1:
        title_prompt = PromptTemplate(
            input_variables=["question", "answer"],
            template="""Generate a title for a conversation where the user asks:
            '{question}' and the AI responds: '{answer}'.""",
        )
        title_chain = title_prompt | llm | StrOutputParser()
        conversation_title = title_chain.invoke(
            {"question": search_text, "answer": result_answer["answer"]},
        )
        yield json.dumps({"chunk": conversation_title.strip('"'), "type": "TITLE"})

    all_citations = (
        citations.get("annotations").dict()["citations"]
        if citations
        and retrieve_citations
        and model_type != ModelType.HUGGING_FACE_CUSTOM.value
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


@tool
def rag_tool(
    search_text,
):
    """Risponde a domande relative ad AXA, alle sue polizze assicurative e ai suoi strumenti finanziari.
    AXA è una società che fornisce polizze assicurative a protezione di beni come case e auto che a protezione
    di persone come polizze vita e assicurazioni sanitarie. Inoltre AXA fornisce servizi finanziari come fondi
    pensione e piani di investimento."""

    return search_text


async def get_chat_chain_tool(
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
    rerank,
    reranker_api_url,
    chunk_window,
    opensearch_host,
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtual_host)
    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    prompt_template = configuration["prompt"]
    rephrase_prompt_template = configuration["rephrase_prompt"]
    context_window = configuration["context_window"]
    retrieve_type = configuration["retrieve_type"]

    llm = initialize_language_model(configuration)

    tools = [rag_tool]
    llm_with_tools = llm.bind_tools(tools)

    llm_with_tools_response = llm_with_tools.invoke(search_text)

    if llm_with_tools_response.tool_calls:
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
            vector_indices=vector_indices,
            context_window=context_window,
            retrieve_type=retrieve_type,
            opensearch_host=opensearch_host,
            grpc_host=grpc_host,
        )

        llm = initialize_language_model(configuration)

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

        rag_chain = create_retrieval_chain(
            history_aware_retriever, question_answer_chain
        )

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
        documents_id = set()
        citations = []
        conversation_title = ""

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
                and "annotations" in chunk.keys()
            ):
                citations = chunk

        if chat_sequence_number == 1:
            title_prompt = PromptTemplate(
                input_variables=["question", "answer"],
                template="""Generate a title for a conversation where the user asks:
                    '{question}' and the AI responds: '{answer}'.""",
            )
            title_chain = title_prompt | llm | StrOutputParser()
            conversation_title = title_chain.invoke(
                {"question": search_text, "answer": result_answer["answer"]},
            )
            yield json.dumps({"chunk": conversation_title.strip('"'), "type": "TITLE"})

        all_citations = (
            citations.get("annotations").dict()["citations"]
            if citations
            and retrieve_citations
            and model_type != ModelType.HUGGING_FACE_CUSTOM.value
            else []
        )
        all_citations_dict = (
            {
                citation["document_id"]: citation["citations"]
                for citation in all_citations
            }
            if retrieve_citations and model_type != ModelType.HUGGING_FACE_CUSTOM.value
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

    else:
        prompt_template = configuration["prompt_no_rag"]
        prompt = ChatPromptTemplate.from_template(prompt_template)
        parser = StrOutputParser()
        chain = prompt | llm | parser

        yield json.dumps({"chunk": "", "type": "START"})

        for chunk in chain.stream({"question": search_text}):
            yield json.dumps({"chunk": chunk, "type": "CHUNK"})

        yield json.dumps({"chunk": "", "type": "END"})
