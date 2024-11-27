from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory


def get_chat_history(
    open_search_client, user_id: str, chat_id: str
) -> BaseChatMessageHistory:
    chat_history = ChatMessageHistory()

    if open_search_client.indices.exists(index=user_id):
        query = {"query": {"term": {"chat_id.keyword": chat_id}}}
        response = open_search_client.search(body=query, index=user_id)
        memory = response["hits"]["hits"]

        for item in memory:
            question = item["_source"]["question"]
            answer = item["_source"]["answer"]
            chat_history.add_message({"role": "human", "content": question})
            chat_history.add_message({"role": "ai", "content": answer})

    return chat_history


def save_chat_message(
    open_search_client,
    question: str,
    answer: str,
    sources: list,
    chat_id: str,
    user_id: str,
    timestamp: str,
    chat_sequence_number: int,
):
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
