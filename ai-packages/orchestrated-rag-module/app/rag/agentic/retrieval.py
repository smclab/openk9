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

"""
Retrieval on OpenSearch as plain functions.

:func:`retrieve_documents` does what ``OpenSearchRetriever`` of
agentic-rag-module does, and returns plain dicts: the
metadata of a chunk become keys of the dict, next to its ``content``. A
retrieval injected in place of this one only has to return the same dicts,
which is what makes the agents testable without OpenSearch.
"""

import logging
from typing import Optional

import requests

from app.external_services.grpc.grpc_client import query_parser
from app.rag.chunk_window import get_context_window_merged
from app.utils.embedding import query_embedding
from app.utils.logger import get_logger
from app.utils.opensearch_client import get_opensearch_client

logger = get_logger(__name__)

TOKEN_SIZE = 3.5
MAX_CONTEXT_WINDOW_PERCENTAGE = 0.85
HYBRID_RETRIEVE_TYPE = "HYBRID"
VECTORIAL_RETRIEVE_TYPES = ["KNN", "HYBRID"]
#: Characters of ``rawContent`` that a chunk without text, as an image chunk, brings to the
#: model in place of its own: ``rawContent`` is the whole document.
MEDIA_CHUNK_TEXT_CHARS = 2000

#: Keys that describe a single chunk: the merged window does not carry them.
CHUNK_KEYS = ("chunk_idx", "prev", "next", "content")


def within_token_budget(documents: list, max_tokens: float) -> list:
    """
    Return the first documents whose content stays below ``max_tokens``.

    The tokens are estimated at ``TOKEN_SIZE`` characters each, as for the cap
    of the retrieval itself.

    :param documents: The documents, in the order they are kept.
    :type documents: list
    :param max_tokens: The token budget.
    :type max_tokens: float

    :return: The longest prefix of ``documents`` within the budget.
    :rtype: list
    """
    kept, total_tokens = [], 0.0
    for document in documents:
        total_tokens += len(document.get("content") or "") / TOKEN_SIZE
        if total_tokens >= max_tokens:
            break
        kept.append(document)
    return kept


TOKEN_FIELDS = (
    "entityType",
    "entityName",
    "tokenType",
    "keywordKey",
    "values",
    "extra",
    "filter",
)


def _media_dict(media) -> dict:
    """A ``models.Media``, or a dict with the same keys, as ``query_parser`` expects it."""
    return media.model_dump() if hasattr(media, "model_dump") else dict(media)


def _token_dict(token) -> dict:
    """Turn a search token, a ``models.SearchToken`` or a dict with the same
    keys, into the dict ``query_parser`` expects. ``media`` is added only when
    the token carries one, so that a text query keeps exactly its shape."""
    if isinstance(token, dict):
        token_dict = {key: token.get(key) for key in TOKEN_FIELDS}
        media = token.get("media")
    else:
        token_dict = {key: getattr(token, key, None) for key in TOKEN_FIELDS}
        media = getattr(token, "media", None)
    if media:
        token_dict["media"] = _media_dict(media)
    return token_dict


def _filter_token(token_type: str, keyword_key: str, values: list) -> dict:
    return {
        "entityType": "",
        "entityName": "",
        "tokenType": token_type,
        "keywordKey": keyword_key,
        "values": values,
        "extra": {},
        "filter": True,
    }


def retrieve_documents(
    *,
    query: str,
    search_text: str,
    retrieve_type: str,
    tenant_id: str,
    opensearch_host: str,
    grpc_host: str,
    context_window: int,
    range_values: list,
    search_query: Optional[list] = None,
    rerank: bool = False,
    reranker_api_url: str = "",
    chunk_window: int = 0,
    after_key: Optional[str] = None,
    suggest_keyword: Optional[str] = None,
    suggestion_category_id: Optional[int] = None,
    jwt: Optional[str] = None,
    extra: Optional[dict] = None,
    sort: Optional[list] = None,
    sort_after_key: Optional[str] = None,
    language: Optional[str] = None,
    metadata: Optional[dict] = None,
    score_threshold: Optional[float] = None,
    domains: Optional[list] = None,
    datasource_ids: Optional[list] = None,
    filter_tokens: Optional[list] = None,
    media=None,
) -> list:
    """
    Search OpenSearch for ``query`` and return the chunks that fit the context.

    :param query: The text to search, when ``search_query`` is empty.
    :param search_text: The question of the user, sent to the reranker.
    :param search_query: The search tokens of the request; without them the
        query is a single token of type ``retrieve_type`` on ``query``.
    :param domains: The domains of the intent detection. They become a filter
        token added to the others: replacing them would leave the query without
        its text (#2137).
    :param datasource_ids: The datasources the request is limited to, added as
        a filter token (#1883).
    :param filter_tokens: The filters of the search query of the request, added
        as they are.
    :param media: The image of the request (#2500): it goes on the query token,
        which searches the image and the text together, or the image alone
        when there is no text.
    :param score_threshold: The share of the best score a hit must reach
        (#2274); ``None`` or ``0`` keeps every hit.

    The other parameters are those of ``OpenSearchRetriever``.

    :return: One dict per chunk, with ``document_id``, ``content`` and, for KNN
        and HYBRID, ``score``, ``chunk_idx``, ``prev``, ``next`` and ``domain``,
        plus the metadata configured for its document types. With
        ``chunk_window`` the chunks of a document are merged into windows that
        keep the metadata of their first chunk.
    :rtype: list
    """
    if search_query:
        parsed_query = [_token_dict(token) for token in search_query]
    else:
        # with an image and no text there is no term to search for: the image alone drives it
        image_only = media and not query.strip()
        token = _filter_token(retrieve_type, "", [] if image_only else [query])
        if media:
            token["media"] = _media_dict(media)
        parsed_query = [token]

    if filter_tokens:
        parsed_query += [_token_dict(token) for token in filter_tokens]
    if domains:
        parsed_query.append(_filter_token("TEXT", "domain", list(domains)))
    if datasource_ids:
        parsed_query.append(
            _filter_token("DATASOURCE", "", [str(id_) for id_ in datasource_ids])
        )

    query_data = query_parser(
        search_query=parsed_query,
        range_values=range_values,
        after_key=after_key,
        suggest_keyword=suggest_keyword,
        suggestion_category_id=suggestion_category_id,
        tenant_id=tenant_id,
        jwt=jwt,
        extra=extra or {},
        sort=sort,
        sort_after_key=sort_after_key,
        language=language,
        grpc_host=grpc_host,
    )

    os_query = query_data["query"]
    index_name = list(query_data["index_name"])
    params = (
        dict(query_data["query_parameters"])
        if retrieve_type == HYBRID_RETRIEVE_TYPE
        else None
    )

    if logger.isEnabledFor(logging.DEBUG):
        query_body = os_query.decode("utf-8") if isinstance(os_query, bytes) else os_query
        logger.debug(
            f"[opensearch_query] retrieve_type={retrieve_type}, "
            f"chunk_window={chunk_window}, index_name={index_name}, "
            f"params={params}, body={query_body}"
        )

    if not index_name:
        logger.debug("[opensearch_query] empty index list, search skipped")
        return []

    client = get_opensearch_client(opensearch_host)
    response = client.search(body=os_query, index=index_name, params=params)
    hits = response["hits"]["hits"]

    max_tokens = context_window * MAX_CONTEXT_WINDOW_PERCENTAGE
    total_tokens = 0

    # The cutoff is relative to the best score of this result set: a custom
    # sort can override relevance ordering, so the top score is taken across
    # all hits rather than from the first one.
    top_score = max((hit.get("_score") or 0 for hit in hits), default=0)
    score_cutoff = top_score * (score_threshold or 0)
    if logger.isEnabledFor(logging.DEBUG):
        logger.debug(
            f"[opensearch_query] score_threshold={score_threshold}, "
            f"top_score={top_score}, score_cutoff={score_cutoff}"
        )

    documents = []
    for row in hits:
        # OpenSearch reports an explicit "_score": null when the query carries
        # a field sort: a .get default would not cover it.
        if (score := row.get("_score") or 0) < score_cutoff:
            continue

        document_source = row.get("_source")
        dynamic_metadata = {}
        for document_type in document_source.get("documentTypes", []):
            if metadata and document_type in metadata:
                source_section = document_source.get(document_type)
                if not source_section:
                    continue
                for key, value in metadata[document_type].items():
                    if metadata_value := source_section.get(value):
                        dynamic_metadata[key] = metadata_value

        if retrieve_type in VECTORIAL_RETRIEVE_TYPES:
            # an image chunk has no text: the start of its document tells the model what it is
            content = document_source.get("chunkText") or (
                document_source.get("rawContent") or ""
            )[:MEDIA_CHUNK_TEXT_CHARS]
            # OpenSearch omits previous/next at the edges of a document: empty
            # lists keep the chunk_window merge working (#2298)
            record = {
                "document_id": document_source.get("contentId"),
                "score": score,
                "chunk_idx": document_source.get("number"),
                "prev": [
                    chunk.get("chunkText")
                    for chunk in document_source.get("previous") or []
                ],
                "next": [
                    chunk.get("chunkText") for chunk in document_source.get("next") or []
                ],
                "domain": document_source.get("domain"),
                "content": content,
            }
        else:
            content = document_source.get("rawContent", "")
            record = {"document_id": document_source.get("contentId"), "content": content}

        total_tokens += len(content) / TOKEN_SIZE
        if total_tokens < max_tokens:
            record.update(dynamic_metadata)
            documents.append(record)

    if rerank:
        documents_to_rerank = [
            {"document_id": doc.get("document_id"), "content": doc.get("content")}
            for doc in documents
        ]
        response = requests.get(
            reranker_api_url,
            json={
                "query": search_text,
                "context": documents_to_rerank,
                "limit": len(documents_to_rerank),
                "threshold": 0,
                "max_length": 512,
            },
            timeout=None,
        )
        reranked_ids = [doc["document_id"] for doc in response.json()["context"]]
        documents = sorted(documents, key=lambda doc: reranked_ids.index(doc["document_id"]))

    # only the vectorial retrieve types carry the per-chunk metadata the merge
    # needs
    if chunk_window > 0 and retrieve_type in VECTORIAL_RETRIEVE_TYPES:
        merged = get_context_window_merged(
            [
                {
                    "document_id": doc["document_id"],
                    "chunk_idx": doc["chunk_idx"],
                    "prev": doc["prev"],
                    "next": doc["next"],
                    "metadata": {
                        key: value for key, value in doc.items() if key not in CHUNK_KEYS
                    },
                    "content": doc["content"],
                }
                for doc in documents
            ],
            window_size=chunk_window,
        )
        # The cap above counts the chunks before the merge, while each window
        # grows up to 2 * chunk_window + 1 chunks: it is applied again to the
        # text that reaches the model.
        documents = within_token_budget(
            [{**window["metadata"], "content": window["content"]} for window in merged],
            max_tokens,
        )

    return documents


def _knn_documents(
    *,
    opensearch_host: str,
    grpc_host_embedding: str,
    embedding_model_configuration: dict,
    index: str,
    search_text: str,
    label: str,
) -> list[dict]:
    """
    Search ``search_text`` in the labelled documents of a tenant: guardrail or domain documents.

    Tenant isolation comes from ``index``: the query applies no filter. It is a
    pure KNN query, so each score is an absolute similarity that the caller can
    hold against a fixed threshold. A text with nothing to embed, or a missing
    index, finds nothing.

    :return: One dict per hit, best first, with ``document_id``, ``score``,
        ``content`` and ``domain``.
    """
    vector = query_embedding(grpc_host_embedding, embedding_model_configuration, search_text)
    if vector is None:
        logger.debug(f"[{label}] nothing to embed, search skipped index={index}")
        return []
    client = get_opensearch_client(opensearch_host)
    if not client.indices.exists(index=index):
        logger.debug(f"[{label}] index missing, search skipped index={index}")
        return []
    response = client.search(
        body={"query": {"knn": {"vector": {"vector": vector, "k": 10}}}}, index=index
    )
    hits = response["hits"]["hits"]
    logger.debug(f"[{label}] index={index} hits={len(hits)}")
    return [
        {
            "document_id": hit["_source"].get("document_id"),
            "score": hit.get("_score") or 0,
            "content": hit["_source"].get("chunkText", ""),
            "domain": hit["_source"].get("domain"),
        }
        for hit in hits
    ]


def retrieve_guardrail_documents(**search) -> list[dict]:
    """The guardrail documents of a tenant closest to a text: see :func:`_knn_documents`."""
    return _knn_documents(**search, label="guardrail_retriever")


def retrieve_domain_documents(**search) -> list[dict]:
    """The domain documents of a tenant closest to a text, each with its ``domain``: see
    :func:`_knn_documents`."""
    return _knn_documents(**search, label="domain_retriever")


def get_domains(*, opensearch_host: str, index: str, size: int = 1000) -> list[str]:
    """The domains of the domain documents of a tenant: none if the index does not exist."""
    client = get_opensearch_client(opensearch_host)
    if not client.indices.exists(index=index):
        return []
    response = client.search(
        index=index,
        body={
            "size": 0,
            "aggs": {"distinct_domains": {"terms": {"field": "domain", "size": size}}},
        },
    )
    return [bucket["key"] for bucket in response["aggregations"]["distinct_domains"]["buckets"]]


def _search_pipeline(*, grpc_host: str, tenant_id: str, jwt) -> Optional[str]:
    """Name of the search pipeline of the Search Config of the tenant, or None when the bucket
    has none.

    The datasource reports it only for a query with a hybrid token, and embeds
    the text of the token to build that query: the token is sent without text,
    since only the name of the pipeline is read back.
    """
    query_data = query_parser(
        search_query=[
            {
                "entityType": "",
                "entityName": "",
                "tokenType": HYBRID_RETRIEVE_TYPE,
                "keywordKey": "",
                "values": [],
                "extra": {},
                "filter": True,
            }
        ],
        range_values=[],
        after_key=None,
        suggest_keyword=None,
        suggestion_category_id=None,
        tenant_id=tenant_id,
        jwt=jwt,
        extra={},
        sort=None,
        sort_after_key=None,
        language=None,
        grpc_host=grpc_host,
    )
    return dict(query_data["query_parameters"]).get("search_pipeline")


def retrieve_uploaded_documents(
    *,
    opensearch_host: str,
    grpc_host_embedding: str,
    embedding_model_configuration: dict,
    index: str,
    retrieve_type: str,
    user_id: str,
    chat_id: str,
    search_text: str,
    grpc_host: Optional[str] = None,
    tenant_id: Optional[str] = None,
    jwt=None,
) -> list[dict]:
    """
    Search ``search_text`` among the documents the user uploaded in the chat, as
    ``OpenSearchUploadedDocumentsRetriever`` of agentic-rag-module does.

    Both branches of the query are restricted to the chat of the user while
    scoring: an approximate KNN would pick its neighbours among every chat of
    the tenant, and indices created on nmslib reject a KNN filter, so the vector
    branch scores the chunks of the chat exactly, in the l2 space of the index.
    A hybrid search goes through the search pipeline of the Search Config of
    the tenant, which also ranks the documents found by meaning only.

    :return: One dict per hit, with ``document_id``, ``filename``,
        ``file_extension``, ``title``, ``score`` and ``content``.
    """
    vector = query_embedding(grpc_host_embedding, embedding_model_configuration, search_text)
    if vector is None:
        return []
    client = get_opensearch_client(opensearch_host)
    if not client.indices.exists(index=index):
        return []
    filters = [
        {"term": {"user_id.keyword": user_id}},
        {"term": {"chat_id.keyword": chat_id}},
    ]
    if retrieve_type in VECTORIAL_RETRIEVE_TYPES:
        body = {
            "query": {
                # the text branch first, as the datasource builds it, so that the
                # weights of a Search Config mean the same here
                "hybrid": {
                    "queries": [
                        {
                            "bool": {
                                "must": [
                                    {"match": {"chunkText": {"query": search_text, "boost": 0.5}}}
                                ],
                                "filter": filters,
                            }
                        },
                        {
                            "script_score": {
                                "query": {"bool": {"filter": filters}},
                                "script": {
                                    "source": "knn_score",
                                    "lang": "knn",
                                    "params": {
                                        "field": "vector",
                                        "query_value": vector,
                                        "space_type": "l2",
                                    },
                                },
                            }
                        },
                    ]
                }
            }
        }
    else:
        body = {
            "query": {
                "bool": {
                    "must": [{"match": {"chunkText": {"query": search_text}}}],
                    "filter": filters,
                }
            }
        }
    pipeline = (
        _search_pipeline(grpc_host=grpc_host, tenant_id=tenant_id, jwt=jwt)
        if retrieve_type == HYBRID_RETRIEVE_TYPE and grpc_host and tenant_id
        else None
    )
    if pipeline:
        response = client.search(body=body, index=index, params={"search_pipeline": pipeline})
    else:
        response = client.search(body=body, index=index)
    documents = []
    for hit in response["hits"]["hits"]:
        source = hit["_source"]
        filename, extension = source.get("filename"), source.get("file_extension")
        documents.append(
            {
                "document_id": source.get("document_id"),
                "filename": filename,
                "file_extension": extension,
                "title": f"{filename or ''}{extension or ''}",
                "score": hit.get("_score") or 0,
                "content": source.get("chunkText", ""),
            }
        )
    return documents


def uploaded_documents_retrieve_fn(
    *, user_id: str, chat_id: str, grpc_host_embedding: str, embedding_model_configuration: dict
):
    """
    The ``retrieve_fn`` of a turn on the uploaded documents: the Discovery
    searches only the documents the user uploaded in the chat, instead of the
    documents of the tenant, as in agentic-rag-module.

    It takes the arguments of :func:`retrieve_documents`, so that the retriever
    tool of the Discovery does not change; the ones that belong to the documents
    of the tenant (domains, datasources, chunk window) do not apply.
    """

    def retrieve(
        *,
        search_text: str,
        opensearch_host: str,
        retrieve_type: str,
        tenant_id: str,
        grpc_host: str,
        jwt=None,
        **_,
    ) -> list[dict]:
        return retrieve_uploaded_documents(
            opensearch_host=opensearch_host,
            grpc_host_embedding=grpc_host_embedding,
            embedding_model_configuration=embedding_model_configuration,
            index=f"{tenant_id}-uploaded-documents-index",
            retrieve_type=retrieve_type,
            user_id=user_id,
            chat_id=chat_id,
            search_text=search_text,
            grpc_host=grpc_host,
            tenant_id=tenant_id,
            jwt=jwt,
        )

    return retrieve
