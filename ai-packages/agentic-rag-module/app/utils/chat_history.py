#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
import json
from datetime import datetime, timezone

from langchain.schema import AIMessage, HumanMessage
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory
from opensearchpy import OpenSearch, helpers

from app.utils.logger import logger

SEARCH_PIPELINE = "nlp-uploaded-documents-search-pipeline"


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
    tenant_id: str,
    timestamp: str,
    chat_sequence_number: int,
    retrieve_from_uploaded_documents: bool,
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
    :param retrieve_from_uploaded_documents: Whether to search in user's uploaded documents
    :type retrieve_from_uploaded_documents: bool

    :return: None

    .. note::
        - Creates index with mappings if not exists (user_id as index name)
        - Source documents require specific structure:
            - metadata (optional): dict with metadata (title, url, etc.)
            - citations (optional): list of citation texts
        - Index mapping enforces:
            - timestamp as date type
            - Other fields use dynamic mapping
        - Message structure includes conversation data and processed sources
    """
    documents = []
    for source in sources:
        document = {
            **source.get("metadata", {}),
            "citations": source.get("citations", []),
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
        "retrieve_from_uploaded_documents": retrieve_from_uploaded_documents,
    }

    # open_search_index = f"{tenant_id}-{user_id}"

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
    that contains 'thread_id', 'checkpoint_id', and 'checkpoint' fields, it identifies documents
    grouped by `thread_id`. It checks the latest document for each group (based on the step number
    in metadata) and deletes all documents associated with that `thread_id` if the latest document
    is older than the specified interval in days.

    Parameters
    ----------
    opensearch_host : str
        The host URL of the OpenSearch instance (e.g., "http://localhost:9200"). This should include
        the protocol (http/https), hostname, and port if not using default ports.

    interval_in_days : int, optional
        The number of days to use as a threshold for deletion. Documents older than this
        value will be deleted. For example, if set to 180, any thread whose most recent document
        is more than 180 days old will have all its documents deleted. Default is 180 days.

    Returns
    -------
    None
        This function does not return any value. It performs deletions directly on the OpenSearch instance
        and logs the progress and results of the operation.

    Raises
    ------
    opensepy.exceptions.OpenSearchException
        Raises an exception if there are errors during the connection to OpenSearch or during
        the execution of search or delete operations. Common exceptions include connection errors,
        authentication failures, or permission issues.

    Notes
    -----
    - The function logs the number of indices found, indices being processed, and the number of
        documents deleted using the configured logger.
    - It performs bulk deletions using the `helpers.bulk` method to optimize the deletion process
        and minimize the number of API calls.
    - The function identifies the latest document for each thread by sorting on the
        'metadata.step.keyword' field using a Painless script.
    - Documents are only considered for deletion if their index contains all required fields:
        'thread_id', 'checkpoint_id', and 'checkpoint'.
    - The checkpoint field is expected to contain a JSON string with a 'ts' (timestamp) field
        that is used to determine document age.
    - Ensure that the OpenSearch client is properly configured and that the necessary permissions
        are in place to delete documents from the indices.

    Examples
    --------
    >>> # Delete threads older than 180 days from local OpenSearch instance
    >>> delete_documents("http://localhost:9200")

    >>> # Delete threads older than 90 days from a remote OpenSearch instance
    >>> delete_documents("https://opensearch.example.com:9200", interval_in_days=90)
    """

    open_search_client = OpenSearch(
        hosts=[opensearch_host],
    )

    all_indices = open_search_client.indices.get(index="*")
    all_indices = list(all_indices.keys())
    index_field_names = {"thread_id", "checkpoint_id", "checkpoint"}
    indices_to_process = []

    today = datetime.now(timezone.utc)
    delete_actions = []

    for index in all_indices:
        index_mapping = open_search_client.indices.get_mapping(index=index)
        index_properties = set(index_mapping[index]["mappings"]["properties"])
        if index_field_names.issubset(index_properties):
            indices_to_process.append(index)

    logger.info(f"Found {len(indices_to_process)} indices: {indices_to_process}")

    for index_to_process in indices_to_process:
        logger.info(f"Processing index: {index_to_process}")

        query = {
            "aggs": {
                "threads": {
                    "terms": {
                        "field": "thread_id",
                        "order": {"_key": "asc"},
                    },
                    "aggs": {
                        "max_step_doc": {
                            "top_hits": {
                                "size": 1,
                                "sort": [
                                    {
                                        "_script": {
                                            "type": "number",
                                            "script": {
                                                "source": """
                                            if (doc['metadata.step.keyword'].size() > 0) {
                                                return Long.parseLong(doc['metadata.step.keyword'].value);
                                            }
                                            return -1;
                                        """,
                                                "lang": "painless",
                                            },
                                            "order": "desc",
                                        }
                                    }
                                ],
                            }
                        },
                    },
                }
            },
        }

        response = open_search_client.search(index=index_to_process, body=query)
        documents = response.get("aggregations").get("threads").get("buckets")

        for item in documents:
            hits = item.get("max_step_doc", {}).get("hits", {}).get("hits", [])

            for hit in hits:
                source = hit.get("_source", {})
                checkpoint_data = json.loads(source.get("checkpoint", "{}"))
                document_timestamp = checkpoint_data.get("ts")
                document_date = datetime.fromisoformat(document_timestamp)

                delta = (today - document_date).days

                if delta > interval_in_days:
                    thread_id = source.get("thread_id")
                    query = {"query": {"match": {"thread_id": thread_id}}}
                    response = open_search_client.search(
                        index=index_to_process, body=query, size=10000
                    )
                    documents_to_delete = response["hits"]["hits"]

                    for document_to_delete in documents_to_delete:
                        document_id = document_to_delete["_id"]
                        delete_action = {
                            "_op_type": "delete",
                            "_index": index_to_process,
                            "_id": document_id,
                        }
                        delete_actions.append(delete_action)

        if delete_actions:
            logger.info(f"Deleting {len(delete_actions)} documents in bulk")

            try:
                success, failed = helpers.bulk(
                    open_search_client,
                    delete_actions,
                    stats_only=False,
                    raise_on_error=False,
                )

                logger.info(f"Successfully deleted {success} documents")
                if failed:
                    logger.error(f"Failed to delete {len(failed)} documents")
                    for item in failed:
                        logger.error(f"Failed delete: {item}")

            except Exception as e:
                logger.error(f"Bulk delete error: {e}")


def save_uploaded_documents(
    opensearch_host: str, tenant_id: str, documents: list, vector_size: int
):
    """
    Save uploaded documents to OpenSearch index.

    Stores uploaded documents with vector embeddings in a realm-specific OpenSearch index.
    Creates the index with proper mappings and search pipeline if it doesn't exist, then
    performs bulk indexing of all documents.

    :param opensearch_host: The host URL of the OpenSearch instance (e.g., "http://localhost:9200")
    :type opensearch_host: str
    :param tenant_id: The id of the Tenant for the user (used for index naming and isolation)
    :type tenant_id: str
    :param documents: List of document dictionaries containing text, metadata, and vector embeddings
    :type documents: list
    :param vector_size: The dimensionality of the vector embeddings for proper index mapping
    :type vector_size: int

    :return: None

    :raises Exception: If bulk indexing operation fails

    :Example:

    .. code-block:: python

        save_uploaded_documents(
            opensearch_host="http://localhost:9200",
            tenant_id="my-realm",
            documents=[
                {
                    "filename": "doc1.pdf",
                    "file_extension": ".pdf",
                    "user_id": "user_123",
                    "chat_id": "chat_456",
                    "chunk_number": 1,
                    "total_chunks": 3,
                    "chunkText": "Document content...",
                    "vector": [0.1, 0.2, 0.3, ...],
                    "timestamp": "2023-01-01T00:00:00Z"
                }
            ],
            vector_size=1536
        )

    .. note::
        - Creates index with KNN (k-Nearest Neighbors) support if it doesn't exist
        - Sets up a hybrid search pipeline for combining vector and text search
        - Uses realm-based index naming for multi-tenant isolation
        - Performs bulk indexing for better performance with multiple documents
        - Includes error handling and logging for indexing operations
        - Automatically adds timestamp to documents during indexing

    .. warning::
        - Ensure OpenSearch instance is running and accessible at opensearch_host
        - Vector size must match the actual dimension of embedding vectors
        - Bulk indexing may fail if documents exceed OpenSearch limits
        - Index creation requires appropriate OpenSearch permissions

    .. seealso::
        - :func:`documents_embedding` For generating the document embeddings
        - :func:`generate_documents_embeddings` For creating vector embeddings
        - OpenSearch KNN Vector Documentation for vector search capabilities

    Index Configuration:
        The created index includes:

        * **KNN enabled**: For efficient vector similarity search
        * **Vector field**: Configured with the specified dimension for embeddings
        * **Keyword fields**: For efficient filtering on user_id and chat_id
        * **Timestamp**: For temporal filtering and sorting
        * **Hybrid search pipeline**: Combines vector and text search results

    Search Pipeline:
        Configures a normalization processor that:

        * Uses min-max normalization technique
        * Applies arithmetic mean combination with equal weights (0.5, 0.5)
        * Enables hybrid search combining multiple relevance scores
    """
    open_search_client = OpenSearch(
        hosts=[opensearch_host],
    )
    uploaded_documents_index = f"{tenant_id}-uploaded-documents-index"

    index_actions = []
    for doc in documents:
        index_actions.append({"index": {"_index": uploaded_documents_index}})
        index_actions.append(doc)

    if not open_search_client.indices.exists(index=uploaded_documents_index):
        index_body = {
            "settings": {"index": {"knn": True}},
            "mappings": {
                "properties": {
                    "timestamp": {"type": "date"},
                    "user_id": {
                        "type": "text",
                        "fields": {"keyword": {"type": "keyword"}},
                    },
                    "chat_id": {
                        "type": "text",
                        "fields": {"keyword": {"type": "keyword"}},
                    },
                    "vector": {
                        "type": "knn_vector",
                        "dimension": vector_size,
                    },
                }
            },
        }
        open_search_client.indices.create(
            index=uploaded_documents_index,
            body=index_body,
        )

        pipeline_body = {
            "description": "Post processor for hybrid search",
            "phase_results_processors": [
                {
                    "normalization-processor": {
                        "normalization": {"technique": "min_max"},
                        "combination": {
                            "technique": "arithmetic_mean",
                            "parameters": {"weights": [0.5, 0.5]},
                        },
                    }
                }
            ],
        }

        open_search_client.transport.perform_request(
            "PUT", f"/_search/pipeline/{SEARCH_PIPELINE}", body=pipeline_body
        )

        open_search_client.indices.put_settings(
            index=uploaded_documents_index,
            body={"index": {"search": {"default_pipeline": SEARCH_PIPELINE}}},
        )

    if index_actions:
        try:
            logger.info(f"Indexing {len(documents)} documents in bulk")
            response = open_search_client.bulk(body=index_actions)

            if response.get("errors"):
                logger.error("Some documents failed to index:")
                for item in response.get("items", []):
                    if "error" in item.get("index", {}):
                        logger.error(
                            f"Failed to index document: {item['index']['error']}"
                        )
            else:
                logger.info(f"Successfully indexed {len(documents)} documents")

        except Exception as e:
            print(f"Bulk indexing failed: {e}")
