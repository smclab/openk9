from datetime import date

from langchain.schema import AIMessage, HumanMessage
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory
from opensearchpy import OpenSearch

from app.utils.logger import logger


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
            - metadata: dict with title, url
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
        document_citations = source["citations"]
        document = {
            "title": document_title,
            "url": document_url,
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


def delete_documents(opensearch_host, interval_in_days=180):
    """
    Delete documents from OpenSearch indices that are older than a specified number of days.

    This function connects to an OpenSearch instance, retrieves all indices, and for each index
    that contains 'chat_id' field, it identifies documents grouped by `chat_id`. It checks the
    latest document for each group and deletes all documents associated with that `chat_id` if
    the latest document is older than the specified interval in days.

    Parameters
    ----------
    opensearch_host : str
        The host URL of the OpenSearch instance (e.g., "http://localhost:9200").

    interval_in_days : int, optional
        The number of days to use as a threshold for deletion. Documents older than this
        value will be deleted. Default is 180 days.

    Returns
    -------
    None
        This function does not return any value. It performs deletions directly on the OpenSearch instance.

    Raises
    ------
    Exception
        Raises an exception if there are errors during the connection to OpenSearch or during
        the execution of search or delete operations.

    Notes
    -----
    - The function logs the number of indices found and the number of documents deleted.
    - It performs bulk deletions to optimize the deletion process.
    - Ensure that the OpenSearch client is properly configured and that the necessary permissions
        are in place to delete documents.

    Examples
    --------
    >>> delete_documents("http://localhost:9200", interval_in_days=180)
    """

    open_search_client = OpenSearch(
        hosts=[opensearch_host],
    )

    all_indices = open_search_client.indices.get(index="*")
    all_indices = list(all_indices.keys())
    index_field_names = {"user_id", "chat_id", "chat_sequence_number"}
    indices_to_process = []

    today = date.today()
    delete_actions = []

    for index in all_indices:
        index_mapping = open_search_client.indices.get_mapping(index=index)
        index_properties = set(index_mapping[index]["mappings"]["properties"])
        if index_field_names.issubset(index_properties):
            indices_to_process.append(index)

    logger.info(f"Found {len(indices_to_process)} indices: {indices_to_process}")

    for index_to_process in indices_to_process:
        logger.info(f"Processing index: {index_to_process}")

        # Query to group documents by chat_id and get the latest document for each group
        query = {
            "size": 0,
            "aggs": {
                "group_by_chat_id": {
                    "terms": {
                        "field": "chat_id.keyword",
                        "size": 10000,
                    },
                    "aggs": {
                        "latest_document": {
                            "top_hits": {
                                "size": 1,
                                "sort": [{"chat_sequence_number": {"order": "desc"}}],
                                "_source": [
                                    "timestamp",
                                    "chat_sequence_number",
                                    "chat_id",
                                ],
                            }
                        }
                    },
                }
            },
        }
        response = open_search_client.search(index=index_to_process, body=query)
        buckets = response["aggregations"]["group_by_chat_id"]["buckets"]

        for bucket in buckets:
            latest_document = bucket["latest_document"]["hits"]["hits"][0]
            index_id = latest_document["_index"]
            chat_id = latest_document["_source"]["chat_id"]
            document_timestamp = int(latest_document["_source"]["timestamp"]) / 1000
            document_date = date.fromtimestamp(document_timestamp)
            delta = (today - document_date).days

            if delta > interval_in_days:
                query = {"query": {"match": {"chat_id.keyword": chat_id}}}
                response = open_search_client.search(
                    index=index_id, body=query, size=10000
                )
                documents_to_delete = response["hits"]["hits"]

                for document_to_delete in documents_to_delete:
                    document_id = document_to_delete["_id"]
                    delete_action = {"delete": {"_index": index_id, "_id": document_id}}
                    delete_actions.append(delete_action)

    if delete_actions:
        logger.info(f"Deleting {len(delete_actions)} documents in bulk")
        bulk_delete = open_search_client.bulk(delete_actions)
        if bulk_delete.get("errors"):
            logger.error("Errors occurred during bulk delete:")
            for item in bulk_delete["items"]:
                if "delete" in item and item["delete"].get("error"):
                    logger.error(f"Failed to delete document: {item['delete']}")
        else:
            logger.info("Bulk delete completed successfully")
