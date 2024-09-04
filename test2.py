from langchain_community.utilities import DuckDuckGoSearchAPIWrapper
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.output_parsers import StrOutputParser
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables import RunnablePassthrough, RunnableWithMessageHistory
from langchain_openai import ChatOpenAI
import os
import os
from typing import List, Optional
import logging

from langchain.schema import Document
from langchain_core.callbacks import CallbackManagerForRetrieverRun
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch
from app.external_services.grpc.grpc_client import query_parser


class OpenSearchRetriever2(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving documents."""

    searchQuery: list
    range: list = [0, 5]
    afterKey: Optional[str] = None
    suggestKeyword: Optional[str] = None
    suggestionCategoryId: Optional[int] = None
    extra: Optional[dict] = None
    sort: Optional[list] = None
    sortAfterKey: Optional[str] = None
    language: Optional[str] = None
    vectorIndices: Optional[bool] = True
    virtualHost: Optional[str] = None
    jwt: Optional[str] = None
    opensearchHost: Optional[str] = None
    grpcHost: Optional[str] = None

    def _get_relevant_documents(self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:

        # searchQuery = [
        #     {
        #         "tokenType": "HYBRID",
        #         "values": [
        #             query
        #         ]
        #     }
        # ]

        search_token = dict(self.searchQuery[0])
        search_token["values"] = [query]

        new_search_query = [search_token]

        query_data = query_parser(
            new_search_query,
            self.range,
            self.afterKey,
            self.suggestKeyword,
            self.suggestionCategoryId,
            self.virtualHost,
            self.jwt,
            self.extra,
            self.sort,
            self.sortAfterKey,
            self.language,
            self.vectorIndices,
            self.grpcHost
        )

        query = query_data.query
        print(query)
        index_name = list(query_data.indexName)

        client = OpenSearch(
            hosts=[self.opensearchHost],
        )

        response = client.search(body=query, index=index_name)

        documents = []

        urls = []

        for row in response["hits"]["hits"]:

            if self.vectorIndices:
                url = str(row["_source"]["url"])
                page_content = row["_source"]["chunkText"]
                title = row["_source"]["title"]
                try:
                    source = row["_source"]["web"]["source"]
                except KeyError:
                    source = None
                document = Document(page_content, metadata={"source": source, "title": title, "url": url})
                if url not in urls:
                    documents.append(document)
                    urls.append(url)
            else:
                document = Document(row["_source"]["rawContent"], metadata={})
                documents.append(document)

        return documents


store = {}


def get_session_history(session_id: str) -> BaseChatMessageHistory:
    if session_id not in store:
        store[session_id] = ChatMessageHistory()
    # store[session_id] = store[session_id]["messages"][-2:]
    return store[session_id]


os.environ["OPENAI_API_KEY"] = "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"

model = ChatOpenAI()

searchQuery = [
    {
        "tokenType": "HYBRID",
        "values": [
            "bonus mobili"
        ]
    }
]

retriever = OpenSearchRetriever2(searchQuery=searchQuery,
                                 range=[0, 5],
                                 virtualHost="k9-backend.openk9.io",
                                 opensearchHost="https://opensearch-backend.openk9.io",
                                 grpcHost="159.122.129.226:30370")

### Contextualize question ###
contextualize_q_system_prompt = """Given a chat history and the latest user question \
    which might reference context in the chat history, formulate a standalone question \
    which can be understood without the chat history. Do NOT answer the question, \
    just reformulate it if needed and otherwise return it as is."""

contextualize_q_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", contextualize_q_system_prompt),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ]
)

prompt_x = """Instruction: Answer the question based on your knowledge. Use Italian language only to answer. Answer comprehensively.
    Here is context to help:

    {context}

    [/INST]"""

qa_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", prompt_x),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ]
)


def format_docs(docs):
    return "\n\n".join(doc.page_content for doc in docs)


rag_chain = (contextualize_q_prompt | model | StrOutputParser() |
             {"context": retriever | format_docs, "input": lambda x: x, "chat_history": MessagesPlaceholder("chat_history").} |
             qa_prompt | model | StrOutputParser())

conversational_rag_chain = RunnableWithMessageHistory(
    rag_chain,
    get_session_history,
    input_messages_key="input",
    history_messages_key="chat_history",
    output_messages_key="answer",
)

x = conversational_rag_chain.invoke({"input": "bonus mobili"}, config={
        "configurable": {"session_id": str("test")}
    })

print(x)