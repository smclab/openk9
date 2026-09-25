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

"""get_rag_configuration asks the datasource for one tenant and one RAG type,
and flattens the answer into the configuration dict the graph reads.

Every response field and every jsonConfig key lands on its own output key; a
key absent from jsonConfig falls back to its documented default, so a tenant
that configures nothing still gets a working, guarded pipeline.
"""

from unittest.mock import MagicMock, patch

import grpc
import pytest
from fastapi import HTTPException
from google.protobuf.struct_pb2 import Struct

from app.external_services.grpc import grpc_client
from app.external_services.grpc.searcher import searcher_pb2

FULL_JSON_CONFIG = {
    "rerank": True,
    "metadata": {"source": "kb"},
    "analyze_query_prompt": "analizza la domanda",
    "enable_real_time_evaluation": True,
    "bypass_rag": True,
    "answer_only_with_context": False,
    "score_threshold": 0.45,
    "enable_input_guardrail": False,
    "input_guardrail_threshold": 0.8,
    "input_guardrail_provider": "aws_bedrock",
    "input_guardrail_aws_bedrock": {"guardrail_id": "in-bedrock"},
    "input_guardrail_google_model_armor": {"template_id": "in-armor"},
    "input_guardrail_openai_moderation": {"model": "in-moderation"},
    "enable_output_guardrail": False,
    "output_guardrail_type": 3,
    "output_guardrail_chunk_interval": 25,
    "output_guardrail_provider": "google_model_armor",
    "output_guardrail_aws_bedrock": {"guardrail_id": "out-bedrock"},
    "output_guardrail_google_model_armor": {"template_id": "out-armor"},
    "output_guardrail_openai_moderation": {"model": "out-moderation"},
    "scope_gate_prefix_chars": 600,
    "scope_gate_domain_description": "corsi Liferay",
    "scope_gate_redirect_message": "Solo corsi Liferay.",
    "domain_threshold": 0.9,
    "guardrail_categories": ["violence", "hate"],
}


def _response(json_config):
    struct = Struct()
    struct.update(json_config)
    return searcher_pb2.GetRAGConfigurationsResponse(
        name="rag-1",
        chunkWindow=3,
        reformulate=True,
        prompt="usa il contesto",
        promptNoRag="rispondi da solo",
        ragToolDescription="cerca nei documenti",
        rephrasePrompt="riformula",
        jsonConfig=struct,
        enableConversationTitle=True,
        range=[0, 7],
    )


def _get_rag_configuration(get_rag_configurations, rag_type="CHAT_RAG"):
    """Call get_rag_configuration against a stub, returning the configuration
    and the stub so the request it received can be checked."""
    stub = MagicMock()
    stub.GetRAGConfigurations = get_rag_configurations

    with (
        patch.object(grpc_client.grpc, "insecure_channel") as channel,
        patch.object(
            grpc_client.searcher_pb2_grpc, "SearcherStub", return_value=stub
        ) as stub_class,
    ):
        configuration = grpc_client.get_rag_configuration(
            grpc_host="localhost:50051",
            tenant_id="tenant-1",
            rag_type=rag_type,
        )

    channel.assert_called_once_with("localhost:50051")
    stub_class.assert_called_once_with(channel.return_value.__enter__.return_value)
    return configuration, stub


def test_every_field_and_json_key_reaches_its_output_key():
    configuration, _ = _get_rag_configuration(
        MagicMock(return_value=_response(FULL_JSON_CONFIG))
    )

    assert configuration == {
        "prompt": "usa il contesto",
        "prompt_no_rag": "rispondi da solo",
        "rephrase_prompt": "riformula",
        "rag_tool_description": "cerca nei documenti",
        "analyze_query_prompt": "analizza la domanda",
        "enable_real_time_evaluation": True,
        "chunk_window": 3,
        "reformulate": True,
        "enable_conversation_title": True,
        "bypass_rag": True,
        "answer_only_with_context": False,
        "score_threshold": 0.45,
        "range_values": [0, 7],
        "rerank": True,
        "metadata": {"source": "kb"},
        "guardrails_configuration": {
            "input_guardrail": {
                "enable_input_guardrail": False,
                "input_guardrail_threshold": 0.8,
                "input_guardrail_provider": "aws_bedrock",
                "input_guardrail_aws_bedrock": {"guardrail_id": "in-bedrock"},
                "input_guardrail_google_model_armor": {"template_id": "in-armor"},
                "input_guardrail_openai_moderation": {"model": "in-moderation"},
            },
            "output_guardrail": {
                "enable_output_guardrail": False,
                "output_guardrail_type": 3,
                "output_guardrail_chunk_interval": 25,
                "output_guardrail_provider": "google_model_armor",
                "output_guardrail_aws_bedrock": {"guardrail_id": "out-bedrock"},
                "output_guardrail_google_model_armor": {"template_id": "out-armor"},
                "output_guardrail_openai_moderation": {"model": "out-moderation"},
                "scope_gate_prefix_chars": 600,
                "scope_gate_domain_description": "corsi Liferay",
                "scope_gate_redirect_message": "Solo corsi Liferay.",
            },
            "guardrail_categories": ["violence", "hate"],
        },
        "domain_threshold": 0.9,
    }


def test_an_empty_json_config_yields_the_defaults():
    configuration, _ = _get_rag_configuration(MagicMock(return_value=_response({})))

    assert configuration == {
        "prompt": "usa il contesto",
        "prompt_no_rag": "rispondi da solo",
        "rephrase_prompt": "riformula",
        "rag_tool_description": "cerca nei documenti",
        "analyze_query_prompt": "",
        "enable_real_time_evaluation": False,
        "chunk_window": 3,
        "reformulate": True,
        "enable_conversation_title": True,
        "bypass_rag": False,
        "answer_only_with_context": True,
        "score_threshold": 0.3,
        "range_values": [0, 7],
        # no default: the graph reads their absence as "not configured"
        "rerank": None,
        "metadata": None,
        "guardrails_configuration": {
            "input_guardrail": {
                "enable_input_guardrail": True,
                "input_guardrail_threshold": 0.5,
                "input_guardrail_provider": "",
                "input_guardrail_aws_bedrock": {},
                "input_guardrail_google_model_armor": {},
                "input_guardrail_openai_moderation": {},
            },
            "output_guardrail": {
                "enable_output_guardrail": True,
                "output_guardrail_type": 1,
                "output_guardrail_chunk_interval": 10,
                "output_guardrail_provider": "",
                "output_guardrail_aws_bedrock": {},
                "output_guardrail_google_model_armor": {},
                "output_guardrail_openai_moderation": {},
                "scope_gate_prefix_chars": 250,
                "scope_gate_domain_description": "",
                "scope_gate_redirect_message": (
                    "Posso aiutarti solo su temi di questo dominio."
                ),
            },
            "guardrail_categories": [],
        },
        "domain_threshold": 0.7,
    }


@pytest.mark.parametrize("rag_type", ["CHAT_RAG", "CHAT_RAG_TOOL", "SIMPLE_GENERATE"])
def test_the_request_names_the_tenant_and_the_rag_type(rag_type):
    _, stub = _get_rag_configuration(
        MagicMock(return_value=_response({})), rag_type=rag_type
    )

    stub.GetRAGConfigurations.assert_called_once_with(
        searcher_pb2.GetRAGConfigurationsRequest(tenantId="tenant-1", ragType=rag_type)
    )


class _RpcError(grpc.RpcError):
    def details(self):
        return "unavailable"


@pytest.mark.parametrize("failure", [_RpcError(), ValueError("broken answer")])
def test_any_failure_is_an_opaque_internal_error(failure):
    with pytest.raises(HTTPException) as error:
        _get_rag_configuration(MagicMock(side_effect=failure))

    assert error.value.status_code == 500
    assert error.value.detail == grpc_client.UNEXPECTED_ERROR_MESSAGE
