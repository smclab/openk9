import grpc
import searcher_pb2
import searcher_pb2_grpc


def run(
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
):
    with grpc.insecure_channel("159.122.129.226:30370") as channel:
        stub = searcher_pb2_grpc.SearcherStub(channel)
        response = stub.QueryParser(
            searcher_pb2.QueryParserRequest(
                searchQuery=searchQuery,
                range=range,
                afterKey=afterKey,
                suggestKeyword=suggestKeyword,
                suggestionCategoryId=suggestionCategoryId,
                virtualHost=virtualHost,
                jwt=jwt,
                extra=extra,
                sort=sort,
                sortAfterKey=sortAfterKey,
                language=language,
            )
        )
    query = response.query
    index = response.indexName
    print(f"query: {query}")
    print(f"index: {index}")
    return response
    # print(f"response: {response}")


if __name__ == "__main__":
    searchQuery = [
        {
            "entityType": "",
            "entityName": "",
            "tokenType": "DOCTYPE",
            "keywordKey": None,
            "values": ["web"],
            "extra": {},
            "filter": True,
        },
        {
            "entityType": "",
            "entityName": "",
            "tokenType": "TEXT",
            "keywordKey": None,
            "values": ["liferay"],
            "extra": {},
            "filter": False,
        },
    ]
    range = [0, 20]
    afterKey = ""
    suggestKeyword = ""
    suggestionCategoryId = 1
    virtualHost = "gamahiro.openk9.io"
    jwt = ""
    extra = {}
    sort = []
    sortAfterKey = ""
    language = "it_IT"

    run(
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
    )
