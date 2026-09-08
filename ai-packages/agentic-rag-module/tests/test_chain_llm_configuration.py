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

"""The chat builds its LLM from the configuration read over gRPC: every key of
that configuration has to survive the passage, or a provider parameter added
upstream is lost here without an error."""

from unittest.mock import MagicMock

from app.rag import chain

LLM_CONFIGURATION = {
    "api_url": "http://localhost:11434",
    "api_key": "",
    "model_type": "ollama",
    "model": "qwen3.6:35b-a3b",
    "context_window": 8192,
    "retrieve_citations": True,
    "retrieve_type": "HYBRID",
    "watsonx_project_id": None,
    "chat_vertex_ai_credentials": None,
    "chat_vertex_ai_model_garden": None,
    "aws_bedrock": {"region_name": "eu-west-1"},
    "reasoning": True,
    "keep_alive": 60,
    "num_gpu": 34,
}

RAG_CONFIGURATION = {
    "prompt": "prompt",
    "prompt_no_rag": "prompt without rag",
    "rephrase_prompt": "rephrase prompt",
    "analyze_query_prompt": "analyze prompt",
    "rag_tool_description": "tool description",
    "reformulate": False,
    "rerank": True,
    "chunk_window": 2,
    "enable_conversation_title": False,
    "metadata": {"source": "documents"},
    "range_values": [0, 10],
    "enable_real_time_evaluation": False,
    "bypass_rag": False,
    "answer_only_with_context": True,
    "domain_threshold": 0.7,
}


def _run_chain(monkeypatch):
    """Run the chat over a graph that yields nothing and return the
    configuration it built for its LLM."""
    captured = {}

    def initialize_language_model(configuration, temperature=None):
        captured.update(configuration)
        return MagicMock()

    graph = MagicMock()
    graph.stream.return_value = []

    monkeypatch.setattr(chain, "initialize_language_model", initialize_language_model)
    monkeypatch.setattr(chain, "RagGraph", lambda *args, **kwargs: graph)

    list(
        chain.get_agentic_rag(
            rag_type="CHAT_RAG",
            search_query=[],
            after_key=None,
            suggest_keyword=None,
            suggestion_category_id=None,
            jwt=None,
            extra=None,
            sort=None,
            sort_after_key=None,
            language=None,
            search_text="pikachu",
            chat_id="chat-1",
            user_id="user-1",
            tenant_id="tenant-1",
            retrieve_from_uploaded_documents=False,
            chat_history=[],
            timestamp=None,
            chat_sequence_number=1,
            rag_configuration=RAG_CONFIGURATION,
            llm_configuration=LLM_CONFIGURATION,
            guardrails_configuration={},
            opensearch_host="http://localhost:9200",
            grpc_host_embedding="localhost:50053",
            grpc_host_datasource="localhost:50051",
        )
    )

    return captured


def test_chat_llm_keeps_every_provider_key(monkeypatch):
    configuration = _run_chain(monkeypatch)

    for key, value in LLM_CONFIGURATION.items():
        assert configuration[key] == value


def test_chat_llm_takes_the_prompts_from_the_rag_configuration(monkeypatch):
    configuration = _run_chain(monkeypatch)

    assert configuration["prompt_template"] == RAG_CONFIGURATION["prompt"]
    assert (
        configuration["rephrase_prompt_template"]
        == RAG_CONFIGURATION["rephrase_prompt"]
    )
    assert configuration["chunk_window"] == RAG_CONFIGURATION["chunk_window"]
    assert configuration["metadata"] == RAG_CONFIGURATION["metadata"]
