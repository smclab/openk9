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
Test del filtro dominio nella query di retrieval (port di ``opensearch_retriever_node``).

I domini rilevati dall'intent detection devono diventare un token ``keywordKey="domain"``
**aggiunto** ai token esistenti. Se sostituisse il token testuale, la ricerca partirebbe senza
testo cercato: e' la issue #2137, e questi test la presidiano.

``query_parser`` (gRPC) e il client OpenSearch sono stubbati: nessun servizio esterno.
"""

from types import SimpleNamespace


import pytest

from app.rag.agentic import retrieval


@pytest.fixture
def captured_tokens(monkeypatch):
    """Cattura i SearchToken passati a query_parser e taglia corto: il resto di
    retrieve_documents (OpenSearch) non serve a questi test."""
    captured = {}

    def fake_query_parser(**kwargs):
        captured.update(kwargs)
        raise _StopHere

    monkeypatch.setattr(retrieval, "query_parser", fake_query_parser)
    return captured


class _StopHere(Exception):
    """Interrompe retrieve_documents subito dopo la costruzione dei token."""


def _retrieve(**overrides):
    kwargs = {
        "query": "chi e' il RUP?",
        "search_text": "chi e' il RUP?",
        "retrieve_type": "HYBRID",
        "tenant_id": "t1",
        "opensearch_host": "http://os",
        "grpc_host": "ds:9000",
        "context_window": 4096,
        "range_values": [],
    }
    kwargs.update(overrides)
    with pytest.raises(_StopHere):
        retrieval.retrieve_documents(**kwargs)


def _tokens(captured):
    return captured["search_query"]


# --------------------------------------------------------------------------- #
# Il token dominio si AGGIUNGE, non sostituisce (issue #2137)                   #
# --------------------------------------------------------------------------- #
def test_domain_token_is_appended_to_the_synthetic_text_token(captured_tokens):
    _retrieve(domains=["legal"])

    tokens = _tokens(captured_tokens)
    assert len(tokens) == 2, "il token testuale deve sopravvivere accanto a quello di dominio"

    text_token, domain_token = tokens
    assert text_token["values"] == ["chi e' il RUP?"]
    assert text_token["keywordKey"] == ""
    assert domain_token["keywordKey"] == "domain"
    assert domain_token["values"] == ["legal"]
    assert domain_token["tokenType"] == "TEXT"
    assert domain_token["filter"] is True


def test_domain_token_is_appended_to_incoming_search_query(captured_tokens):
    # SearchToken arriva come oggetto (retrieve_documents lo legge con getattr), non come dict
    incoming = [
        SimpleNamespace(
            entityType="",
            entityName="",
            tokenType="TEXT",
            keywordKey="title",
            values=["bando"],
            extra={},
            filter=True,
        )
    ]

    _retrieve(search_query=incoming, domains=["legal", "hr"])

    tokens = _tokens(captured_tokens)
    assert len(tokens) == 2
    assert tokens[0]["keywordKey"] == "title"
    assert tokens[1]["keywordKey"] == "domain"
    assert tokens[1]["values"] == ["legal", "hr"]


# --------------------------------------------------------------------------- #
# Nessun dominio -> query invariata                                            #
# --------------------------------------------------------------------------- #
@pytest.mark.parametrize("domains", [None, []])
def test_without_domains_the_query_is_unchanged(captured_tokens, domains):
    _retrieve(domains=domains)

    tokens = _tokens(captured_tokens)
    assert len(tokens) == 1
    assert tokens[0]["keywordKey"] == ""


def test_domains_are_not_mutated_by_the_caller_list(captured_tokens):
    """Il token porta una copia: mutare la lista del chiamante non deve alterare la query."""
    domains = ["legal"]
    _retrieve(domains=domains)
    domains.append("hr")

    assert _tokens(captured_tokens)[1]["values"] == ["legal"]


# --------------------------------------------------------------------------- #
# Precedenza: intent detection > scelta dell'LLM (vedi discovery.retriever)     #
# --------------------------------------------------------------------------- #
def _discovery_domains(*, detected, llm_choice):
    """Chiama la funzione usata da ``discovery.retriever``, non una sua reimplementazione."""
    from app.rag.agentic.deps import AgenticDeps
    from app.rag.agentic.discovery import resolve_filter_domains

    deps = AgenticDeps(
        rag_configuration={},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="emb:9000",
        tenant_id="t1",
        detected_domains=list(detected),
    )
    return resolve_filter_domains(deps, llm_choice)


def test_intent_detection_wins_over_the_llm_choice():
    assert _discovery_domains(detected=["legal"], llm_choice=["hr"]) == ["legal"]


def test_llm_choice_is_used_when_detection_found_nothing():
    assert _discovery_domains(detected=[], llm_choice=["hr"]) == ["hr"]


def test_no_scoping_when_neither_produced_domains():
    assert _discovery_domains(detected=[], llm_choice=None) is None
