from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory


def get_chat_history(
    open_search_client, user_id: str, chat_id: str
) -> BaseChatMessageHistory:
    chat_history = ChatMessageHistory()

    if open_search_client.indices.exists(index=user_id):
        query = {"query": {"term": {"chat_id": chat_id}}}
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

    for document in sources:
        documents.append(document.dict())

    message = {
        "question": question,
        "answer": answer,
        # TODO: context length
        "sources": documents[0:2],
        "chat_id": chat_id,
        "user_id": user_id,
        "timestamp": timestamp,
        "chat_sequence_number": chat_sequence_number,
    }

    if not open_search_client.indices.exists(index=user_id):
        index_body = {
            "settings": {"index": {"number_of_shards": 4}},
            "mappings": {
                "properties": {
                    "question": {"type": "text"},
                    "answer": {"type": "text"},
                    "sources": {"type": "nested"},
                    "chat_id": {"type": "text"},
                    "user_id": {"type": "text"},
                    "timestamp": {"type": "text"},
                    "chat_sequence_number": {"type": "integer"},
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
        id=chat_sequence_number,
    )
