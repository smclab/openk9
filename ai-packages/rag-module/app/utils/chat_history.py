from langchain.schema import AIMessage, HumanMessage
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory


def get_chat_history(
    open_search_client, user_id: str, chat_id: str
) -> BaseChatMessageHistory:
    """Retrieve chat history from OpenSearch for a specific user and chat session.

        Queries the OpenSearch index associated with the given user ID to fetch message pairs
        (human questions and AI answers) from the specified chat session. If the index does not exist,
        returns an empty chat history.

        :param open_search_client: OpenSearch client instance used to interact with the OpenSearch cluster
        :type open_search_client: opensearchpy.OpenSearch
        :param user_id: Unique identifier for the user (used as OpenSearch index name)
        :type user_id: str
        :param chat_id: Unique identifier for the specific chat session to retrieve
        :type chat_id: str
        :return: Chat message history populated with messages from OpenSearch if found
        :rtype: BaseChatMessageHistory

        .. note::
            - Returns empty ChatMessageHistory if the user index doesn't exist
            - Each message pair (question/answer) from OpenSearch is added as
                HumanMessage and AIMessage respectively
        """
    chat_history = ChatMessageHistory()

    if open_search_client.indices.exists(index=user_id):
        query = {"query": {"term": {"chat_id.keyword": chat_id}}}
        response = open_search_client.search(body=query, index=user_id)
        memory = response["hits"]["hits"]

        for item in memory:
            question = item["_source"]["question"]
            answer = item["_source"]["answer"]
            chat_history.add_message(HumanMessage(content=question))
            chat_history.add_message(AIMessage(content=answer))

    return chat_history


def get_chat_history_from_frontend(
    chat_history_from_frontend,
) -> BaseChatMessageHistory:
    """Convert frontend-formatted chat history to standardized message history.

        Processes a list of message pairs from frontend format and converts them into
        a chat message history containing HumanMessage and AIMessage objects.

        :param chat_history_from_frontend: List of message dictionaries from frontend
        :type chat_history_from_frontend: list[dict]
        :return: Chat message history populated with converted messages
        :rtype: BaseChatMessageHistory

        .. note::
            - Expects each input dictionary to contain 'question' and 'answer' keys
            - Empty input list will return an empty ChatMessageHistory
            - Converts each question/answer pair to HumanMessage/AIMessage respectively
        """
    chat_history = ChatMessageHistory()

    for item in chat_history_from_frontend:
        question = item["question"]
        answer = item["answer"]
        chat_history.add_message(HumanMessage(content=question))
        chat_history.add_message(AIMessage(content=answer))

    return chat_history


def save_chat_message(
    open_search_client,
    question: str,
    answer: str,
    title: str,
    sources: list,
    chat_id: str,
    user_id: str,
    timestamp: str,
    chat_sequence_number: int,
):
    """Save chat message and related documents to OpenSearch index.

        Processes conversation data and source documents, then stores them in a user-specific
        OpenSearch index. Creates the index with appropriate mappings if it doesn't exist.

        :param open_search_client: OpenSearch client instance for cluster interaction
        :type open_search_client: opensearchpy.OpenSearch
        :param question: User's question/input message
        :type question: str
        :param answer: AI's response message
        :type answer: str
        :param title: Title/heading for the chat session
        :type title: str
        :param sources: List of source documents with metadata and citations
        :type sources: list[dict]
        :param chat_id: Unique identifier for the chat session
        :type chat_id: str
        :param user_id: User identifier (used as index name)
        :type user_id: str
        :param timestamp: timestamp of the message
        :type timestamp: str
        :param chat_sequence_number: Sequential position in chat history
        :type chat_sequence_number: int

        :return: None

        .. note::
            - Creates index with mappings if not exists (user_id as index name)
            - Source documents require specific structure:
                - metadata: dict with title, url, source
                - citations: list of citation texts
            - Index mapping enforces:
                - timestamp as date type
                - Other fields use dynamic mapping
            - Message structure includes conversation data and processed sources
        """
    documents = []

    for source in sources:
        document_title = source["metadata"]["title"]
        document_url = source["metadata"]["url"]
        document_source = source["metadata"]["source"]
        document_citations = source["citations"]
        document = {
            "title": document_title,
            "url": document_url,
            "source": document_source,
            "citations": document_citations,
        }
        documents.append(document)

    message = {
        "question": question,
        "answer": answer,
        "title": title,
        "sources": documents,
        "chat_id": chat_id,
        "user_id": user_id,
        "timestamp": timestamp,
        "chat_sequence_number": chat_sequence_number,
    }

    if not open_search_client.indices.exists(index=user_id):
        index_body = {
            "mappings": {
                "properties": {
                    # "question": {"type": "text"},
                    # "answer": {"type": "text"},
                    # "sources": {"type": "text"},
                    # "chat_id": {
                    #     "type": "text",
                    #     "fields": {"keyword": {"type": "keyword", "ignore_above": 256}},
                    # },
                    # "user_id": {
                    #     "type": "text",
                    #     "fields": {"keyword": {"type": "keyword", "ignore_above": 256}},
                    # },
                    "timestamp": {"type": "date"},
                    # "chat_sequence_number": {"type": "integer"},
                }
            },
        }
        open_search_client.indices.create(
            index=user_id,
            body=index_body,
        )

    open_search_client.index(
        index=user_id,
        body=message,
    )
