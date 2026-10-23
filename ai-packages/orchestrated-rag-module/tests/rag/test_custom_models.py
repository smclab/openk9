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
Provider ``hugging-face-custom`` e ``watsonx`` su PydanticAI.

Quello che questi test bloccano e' la **parita' di contratto** con le controparti di
``agentic-rag-module``, perche' e' l'unica cosa che le configurazioni tenant gia' esistenti danno
per scontata: il body ``{"input_str": ...}`` dell'endpoint HuggingFace bespoke, e i parametri di
generazione che agentic-rag-module spediva alla Chat API. Che i due modelli producano ``TextPart`` e
delta e' la parte facile; e' il payload in uscita quello che, se cambia, rompe in produzione senza
che nessun test lo veda.
"""

import asyncio
import json

import httpx
import pytest
from pydantic_ai.messages import (
    InstructionPart,
    ModelRequest,
    ModelResponse,
    SystemPromptPart,
    TextPart,
    ToolCallPart,
    ToolReturnPart,
    UserPromptPart,
)
from pydantic_ai.models import ModelRequestParameters
from pydantic_ai.tools import ToolDefinition

from app.rag.agentic.custom_hugging_face_model import (
    CustomHuggingFaceModel,
    flatten_messages,
)
from app.rag.agentic.models_factory import build_model
from app.rag.agentic.watsonx_model import BASE_PARAMS, WatsonxModel, map_messages

NO_PARAMS = ModelRequestParameters()


# --------------------------------------------------------------------------------------------
# hugging-face-custom
# --------------------------------------------------------------------------------------------


def _hf_model(handler, **kwargs):
    """Modello HF con un transport httpx finto: `handler(request) -> httpx.Response`."""
    client = httpx.AsyncClient(transport=httpx.MockTransport(handler))
    return CustomHuggingFaceModel("http://hf.local/generate", http_client=client, **kwargs)


def test_hugging_face_posts_input_str_body():
    """Il contratto con l'endpoint bespoke: POST sull'api_url, body ``{"input_str": ...}``."""
    seen = {}

    def handler(request):
        seen["url"] = str(request.url)
        seen["body"] = json.loads(request.content)
        return httpx.Response(200, text="ok")

    model = _hf_model(handler)
    messages = [ModelRequest(parts=[UserPromptPart(content="Chi e' il RUP?")])]

    asyncio.run(model.request(messages, None, NO_PARAMS))

    assert seen["url"] == "http://hf.local/generate"
    assert list(seen["body"]) == ["input_str"]
    assert "Chi e' il RUP?" in seen["body"]["input_str"]


def test_hugging_face_request_aggregates_the_stream():
    """``request`` esiste e ritorna il testo completo: ``_generate`` dell'originale
    sollevava AttributeError (self.n), rompendo titolo conversazione / guardrail / intent."""

    def handler(request):
        return httpx.Response(200, text="Il RUP e' Mario Rossi.")

    model = _hf_model(handler)
    messages = [ModelRequest(parts=[UserPromptPart(content="Chi e' il RUP?")])]

    response = asyncio.run(model.request(messages, None, NO_PARAMS))

    assert [part.content for part in response.parts] == ["Il RUP e' Mario Rossi."]
    assert response.model_name == "hugging-face-custom"


def test_hugging_face_streams_deltas_as_they_arrive():
    """I frammenti di testo grezzo diventano delta separati, non un unico blocco finale."""

    def handler(request):
        return httpx.Response(200, stream=httpx.ByteStream(b"Il RUP e' Mario Rossi."))

    async def scenario():
        model = _hf_model(handler)
        messages = [ModelRequest(parts=[UserPromptPart(content="Chi e' il RUP?")])]
        async with model.request_stream(messages, None, NO_PARAMS) as stream:
            async for _ in stream:
                pass
            return stream.get()

    response = asyncio.run(scenario())

    assert "".join(part.content for part in response.parts) == "Il RUP e' Mario Rossi."


def test_hugging_face_flattens_instructions_and_history():
    """Il prompt inviato contiene istruzioni e storico: in PydanticAI non stanno tutti
    nell'ultimo messaggio come nella catena di agentic-rag-module, prenderne solo uno perderebbe il contesto."""
    messages = [
        ModelRequest(parts=[SystemPromptPart(content="Sei un assistente.")]),
        ModelResponse(parts=[TextPart(content="Certo.")]),
        ModelRequest(parts=[UserPromptPart(content="Chi e' il RUP?")]),
    ]

    flattened = flatten_messages(
        messages, [InstructionPart(content="Rispondi in italiano.")]
    )

    assert flattened == (
        "Rispondi in italiano.\n\n"
        "Sei un assistente.\n\n"
        "Assistant: Certo.\n\n"
        "User: Chi e' il RUP?"
    )


def test_hugging_face_profile_routes_structured_output_through_the_prompt():
    """L'endpoint non fa tool calling: le due chiavi del profilo vanno insieme, perche'
    ``supports_tools=False`` con la modalita' 'tool' fa alzare UserError, ed e' la modalita'
    'prompted' a portare lo schema JSON nel prompt."""
    model = CustomHuggingFaceModel("http://hf.local/generate")

    assert model.profile.get("supports_tools") is False
    assert model.profile.get("default_structured_output_mode") == "prompted"


def test_hugging_face_raises_on_http_error():
    """Un 500 dell'endpoint non deve passare come risposta vuota."""

    def handler(request):
        return httpx.Response(500, text="boom")

    model = _hf_model(handler)
    messages = [ModelRequest(parts=[UserPromptPart(content="ciao")])]

    with pytest.raises(httpx.HTTPStatusError):
        asyncio.run(model.request(messages, None, NO_PARAMS))


# --------------------------------------------------------------------------------------------
# watsonx
# --------------------------------------------------------------------------------------------


class FakeInference:
    """Sta al posto di ``ibm_watsonx_ai.foundation_models.ModelInference``: registra le chiamate
    e restituisce payload nella forma della Chat API."""

    def __init__(self, payload=None, chunks=None):
        self._payload = payload or {"choices": [{"message": {"content": ""}}]}
        self._chunks = chunks or []
        self.calls = []

    async def achat(self, **kwargs):
        self.calls.append(kwargs)
        return self._payload

    async def achat_stream(self, **kwargs):
        self.calls.append(kwargs)

        async def generator():
            for chunk in self._chunks:
                yield chunk

        return generator()


def _watsonx_model(inference):
    return WatsonxModel(
        "ibm/granite-13b-chat-v2",
        api_url="https://eu-de.ml.cloud.ibm.com",
        api_key="apikey",
        project_id="project-1",
        inference=inference,
    )


def test_watsonx_sends_the_same_generation_params_as_chatwatsonx():
    """Parita' col dict ``parameters`` di ``initialize_language_model``."""
    inference = FakeInference()
    model = _watsonx_model(inference)
    messages = [ModelRequest(parts=[UserPromptPart(content="ciao")])]

    asyncio.run(model.request(messages, {"temperature": 0}, NO_PARAMS))

    assert inference.calls[0]["params"] == {
        "decoding_method": "sample",
        "max_new_tokens": 100,
        "min_new_tokens": 1,
        "top_k": 50,
        "top_p": 1,
        "temperature": 0,
    }


def test_watsonx_omits_temperature_when_unset():
    """Senza ModelSettings non si manda ``"temperature": null`` (agentic-rag-module lo faceva)."""
    inference = FakeInference()
    model = _watsonx_model(inference)
    messages = [ModelRequest(parts=[UserPromptPart(content="ciao")])]

    asyncio.run(model.request(messages, None, NO_PARAMS))

    assert inference.calls[0]["params"] == BASE_PARAMS
    assert "temperature" not in inference.calls[0]["params"]


def test_watsonx_maps_messages_to_chat_api_roles():
    """Istruzioni, storico e ritorni di tool nella forma attesa da ``/ml/v1/text/chat``."""
    messages = [
        ModelRequest(parts=[UserPromptPart(content="Chi e' il RUP?")]),
        ModelResponse(
            parts=[ToolCallPart(tool_name="discover", args={"q": "RUP"}, tool_call_id="call-1")]
        ),
        ModelRequest(
            parts=[
                ToolReturnPart(tool_name="discover", content="Mario Rossi", tool_call_id="call-1")
            ]
        ),
    ]

    mapped = map_messages(messages, [InstructionPart(content="Rispondi in italiano.")])

    assert mapped == [
        {"role": "system", "content": "Rispondi in italiano."},
        {"role": "user", "content": "Chi e' il RUP?"},
        {
            "role": "assistant",
            "content": "",
            "tool_calls": [
                {
                    "id": "call-1",
                    "type": "function",
                    "function": {"name": "discover", "arguments": '{"q":"RUP"}'},
                }
            ],
        },
        {"role": "tool", "tool_call_id": "call-1", "content": "Mario Rossi"},
    ]


def test_watsonx_forwards_tools_and_parses_tool_calls():
    """Il tool calling e' la differenza che rende watsonx usabile dall'orchestratore (a differenza
    dell'endpoint HF): i tool vanno in richiesta e le tool_call tornano come ``ToolCallPart``."""
    inference = FakeInference(
        payload={
            "id": "chat-1",
            "model_id": "ibm/granite-13b-chat-v2",
            "choices": [
                {
                    "message": {
                        "content": None,
                        "tool_calls": [
                            {
                                "id": "call-9",
                                "function": {"name": "discover", "arguments": '{"q":"RUP"}'},
                            }
                        ],
                    },
                    "finish_reason": "tool_calls",
                }
            ],
            "usage": {"prompt_tokens": 12, "completion_tokens": 3},
        }
    )
    model = _watsonx_model(inference)
    params = ModelRequestParameters(
        function_tools=[
            ToolDefinition(
                name="discover",
                description="Delega una sotto-domanda",
                parameters_json_schema={"type": "object", "properties": {}},
            )
        ]
    )

    response = asyncio.run(
        model.request([ModelRequest(parts=[UserPromptPart(content="ciao")])], None, params)
    )

    assert inference.calls[0]["tools"] == [
        {
            "type": "function",
            "function": {
                "name": "discover",
                "description": "Delega una sotto-domanda",
                "parameters": {"type": "object", "properties": {}},
            },
        }
    ]
    assert [(part.tool_name, part.tool_call_id) for part in response.parts] == [
        ("discover", "call-9")
    ]
    assert response.finish_reason == "tool_call"
    assert response.usage.input_tokens == 12
    assert response.usage.output_tokens == 3


def test_watsonx_streams_content_deltas():
    """I chunk SSE della Chat API diventano delta incrementali."""
    inference = FakeInference(
        chunks=[
            {"id": "chat-1", "choices": [{"delta": {"content": "Il RUP "}}]},
            {"choices": [{"delta": {"content": "e' Mario."}, "finish_reason": "stop"}]},
            {"choices": [], "usage": {"prompt_tokens": 8, "completion_tokens": 4}},
        ]
    )

    async def scenario():
        model = _watsonx_model(inference)
        messages = [ModelRequest(parts=[UserPromptPart(content="Chi e' il RUP?")])]
        async with model.request_stream(messages, None, NO_PARAMS) as stream:
            async for _ in stream:
                pass
            return stream.get()

    response = asyncio.run(scenario())

    assert "".join(part.content for part in response.parts) == "Il RUP e' Mario."
    assert response.finish_reason == "stop"
    assert response.usage.output_tokens == 4


# --------------------------------------------------------------------------------------------
# factory
# --------------------------------------------------------------------------------------------


def test_build_model_does_not_touch_the_network_for_watsonx():
    """``build_model`` gira dentro il request path async (``entrypoint.get_agentic_rag_v2``), e il
    costruttore dell'SDK IBM fa handshake **sincroni** (token IAM, dettagli progetto, model specs).
    Costruirli li' bloccherebbe l'event loop a ogni richiesta: la creazione va rimandata alla prima
    chiamata e spostata fuori dal loop."""
    model = build_model(
        {
            "model_type": "watsonx",
            "api_url": "https://eu-de.ml.cloud.ibm.com",
            "api_key": "chiave",
            "watsonx_project_id": "progetto",
            "model": "ibm/granite-3-8b-instruct",
        }
    )

    # Nessuna eccezione di autenticazione: con credenziali finte l'SDK fallirebbe subito se fosse
    # stato istanziato qui.
    assert isinstance(model, WatsonxModel)
    assert model.model_name == "ibm/granite-3-8b-instruct"


def test_build_model_returns_hugging_face_custom():
    """``build_model`` non alza piu' NotImplementedError per ``hugging-face-custom``."""
    model = build_model(
        {
            "model_type": "hugging-face-custom",
            "api_url": "http://hf.local/generate",
            "api_key": None,
            "model": "my-endpoint",
        }
    )

    assert isinstance(model, CustomHuggingFaceModel)
    assert model.base_url == "http://hf.local/generate"
    assert model.model_name == "my-endpoint"
