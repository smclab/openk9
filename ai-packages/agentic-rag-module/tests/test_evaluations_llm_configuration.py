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

"""The evaluation builds its own LLM from the configuration of the chat it is
judging: every key of that configuration has to survive the passage, or the
evaluation runs against a differently configured model."""

from unittest.mock import MagicMock

from app.rag import evaluations

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
    "rephrase_prompt": "rephrase prompt",
    "rerank": True,
    "chunk_window": 2,
    "metadata": {"source": "documents"},
    "rag_tool_description": "tool description",
}


def _run_evaluations(monkeypatch):
    """Run evaluations over no span at all and return the configuration it
    built for its LLM."""
    captured = {}

    def initialize_language_model(configuration):
        captured.update(configuration)
        return MagicMock()

    client = MagicMock()
    client.spans.get_spans.return_value = []

    monkeypatch.setattr(
        evaluations, "initialize_language_model", initialize_language_model
    )
    monkeypatch.setattr(evaluations, "Client", lambda base_url: client)

    evaluations.evaluations(
        RAG_CONFIGURATION,
        LLM_CONFIGURATION,
        "project",
        "http://localhost:6006",
        10,
        None,
        None,
        evaluate_rag_router=True,
        evaluate_retriever=True,
        evaluate_response=True,
    )

    return captured


def test_evaluation_llm_keeps_every_provider_key(monkeypatch):
    configuration = _run_evaluations(monkeypatch)

    for key, value in LLM_CONFIGURATION.items():
        assert configuration[key] == value


def test_evaluation_llm_takes_the_prompts_from_the_rag_configuration(monkeypatch):
    configuration = _run_evaluations(monkeypatch)

    assert configuration["prompt_template"] == RAG_CONFIGURATION["prompt"]
    assert (
        configuration["rephrase_prompt_template"]
        == RAG_CONFIGURATION["rephrase_prompt"]
    )
    assert configuration["chunk_window"] == RAG_CONFIGURATION["chunk_window"]
    assert configuration["metadata"] == RAG_CONFIGURATION["metadata"]
