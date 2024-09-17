import json

from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import ConfigurableFieldSpec
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_ollama import ChatOllama
from langchain_openai import ChatOpenAI
from opensearchpy import OpenSearch

from app.external_services.grpc.grpc_client import get_llm_configuration
from app.rag.custom_hugging_face_model import CustomChatHuggingFaceModel
from app.rag.retriever import OpenSearchRetriever
from app.utils.chat_history import get_chat_history, save_chat_message

DEFAULT_MODEL_TYPE = "openai"
DEFAULT_MODEL = "gpt-4o-mini"


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
        opensearch_host=opensearch_host,
        grpc_host=grpc_host,
    )

    documents = retriever.invoke(question)

    if model_type == "openai":
        llm = ChatOpenAI(model=model, openai_api_key=api_key)
    elif model_type == "ollama":
        llm = ChatOllama(model=model, base_url=api_url)
    elif model_type == "hugging-face-custom":
        llm = CustomChatHuggingFaceModel(base_url=api_url)

    prompt = ChatPromptTemplate.from_template(prompt_template)
    parser = StrOutputParser()
    chain = prompt | llm | parser

    if reformulate:
        rephrase_prompt = PromptTemplate.from_template(rephrase_prompt_template)
        chain = rephrase_prompt | llm | parser
        question = chain.invoke({"question": question})

    for chunk in chain.stream({"question": question, "context": documents}):
        yield json.dumps({"chunk": chunk, "type": "CHUNK"})

    for element in documents:
        yield json.dumps({"chunk": dict(element.metadata), "type": "DOCUMENT"})

    yield json.dumps({"chunk": "", "type": "END"})


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
    opensearch_host,
    grpc_host,
):
    configuration = get_llm_configuration(grpc_host, virtual_host)
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]

    # TODO: hardcoded opensearch host
    open_search_client = OpenSearch(
        hosts=["https://opensearch-test.openk9.io/"],
    )

    llm = ChatOpenAI(openai_api_key=api_key)

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
        opensearch_host=opensearch_host,
        grpc_host=grpc_host,
    )

    documents = retriever.invoke(search_text)

    contextualize_q_system_prompt = (
        "Given a chat history and the latest user question "
        "which might reference context in the chat history, "
        "formulate a standalone question which can be understood "
        "without the chat history. Do NOT answer the question, "
        "just reformulate it if needed and otherwise return it as is."
    )

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

    system_prompt = (
        "You are an assistant for question-answering tasks. "
        "Use the following pieces of retrieved context to answer "
        "the question. If you don't know the answer, say that you "
        "don't know. Use three sentences maximum and keep the "
        "answer concise."
        "\n\n"
        "{context}"
    )

    qa_prompt = ChatPromptTemplate.from_messages(
        [
            ("system", system_prompt),
            MessagesPlaceholder("chat_history"),
            ("human", "{input}"),
        ]
    )

    question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)

    rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)

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

    for chunk in result:
        if "answer" in chunk.keys():
            result_answer += chunk
            yield json.dumps({"chunk": chunk["answer"], "type": "CHUNK"})

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

    for element in documents:
        yield json.dumps({"chunk": dict(element.metadata), "type": "DOCUMENT"})

    yield json.dumps({"chunk": "", "type": "END"})
