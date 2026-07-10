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
from unittest.mock import MagicMock

from app.rag.agentic_rag import RagGraph

QUERY = "Qual è la copertura per i danni da grandine?"

# Verbatim shape of the litellm/Azure content-policy block reported by clients:
# an HTTP 400 whose message carries the content-management markers.
CONTENT_POLICY_ERROR = (
    "Error code: 400 - litellm.BadRequestError: "
    "litellm.ContentPolicyViolationError: The response was filtered due to "
    "the prompt triggering Azure OpenAI's content management policy. "
    "model=gpt-5.4-mini 'innererror': {'code': 'ResponsibleAIPolicyViolation', "
    "'content_filter_result': {'jailbreak': {'detected': True, "
    "'filtered': True}}}"
)


def _graph(stream_exception):
    """Bare RagGraph whose graph.stream immediately raises, so stream() drops
    straight into the outer except. output_guardrail is empty, routing through
    the non-output-guardrail branch."""
    graph = RagGraph.__new__(RagGraph)
    graph.output_guardrail = {}
    graph.output_guardrail_type = 0
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "SIMPLE_GENERATE"
    graph.graph = MagicMock()
    graph.graph.stream.side_effect = stream_exception
    return graph


def _events(graph):
    return [json.loads(event) for event in graph.stream(QUERY)]


def test_content_policy_block_emits_guardrail_and_end():
    graph = _graph(Exception(CONTENT_POLICY_ERROR))

    events = _events(graph)

    # Coherent with the input-guardrail path: GUARDRAIL then END, and the raw
    # Azure/litellm detail is never streamed to the client.
    assert events == [
        {"chunk": "Guardrail violation", "type": "GUARDRAIL"},
        {"chunk": "", "type": "END"},
    ]
    assert not any(event["type"] == "ERROR" for event in events)


def test_rate_limit_still_reported_as_error():
    graph = _graph(Exception("litellm.RateLimitError: rate_limit exceeded (429)"))

    events = _events(graph)

    assert events == [
        {"chunk": "Rate limit exceeded. Try again later.", "type": "ERROR"},
    ]


def test_generic_error_still_reported_raw():
    graph = _graph(Exception("boom"))

    events = _events(graph)

    assert events == [{"chunk": "boom", "type": "ERROR"}]
