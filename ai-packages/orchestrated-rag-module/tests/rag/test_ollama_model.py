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
Provider ``ollama`` sulla API nativa ``/api/chat``.

Il motivo per cui esiste il modello custom e' il payload: ``num_ctx``, ``num_gpu``, ``think`` e
``keep_alive`` sono i parametri che ``ChatOllama`` spedisce in ``agentic-rag-module`` e che
l'endpoint OpenAI-compatibile ``/v1`` non accetta. Questi test bloccano quel payload e il giro di
tool calling, che e' quello su cui si regge l'orchestratore.
"""

import asyncio
import json

import httpx
import pytest
from pydantic_ai import Agent
from pydantic_ai.messages import (
    InstructionPart,
    ModelRequest,
    ModelResponse,
    TextPart,
    ToolCallPart,
    ToolReturnPart,
    UserPromptPart,
)
from pydantic_ai.models import ModelRequestParameters
from pydantic_ai.settings import ModelSettings

from app.rag.agentic.models_factory import build_model
from app.rag.agentic.ollama_model import (
    DEFAULT_KEEP_ALIVE,
    OllamaNativeModel,
    map_messages,
)

NO_PARAMS = ModelRequestParameters()


def _ollama(handler, **kwargs):
    """Modello Ollama con un transport httpx finto: `handler(request) -> httpx.Response`."""
    client = httpx.AsyncClient(transport=httpx.MockTransport(handler))
    kwargs.setdefault("api_url", "http://ollama.local:11434")
    return OllamaNativeModel("llama3.1", http_client=client, **kwargs)


def _chat_reply(content="", tool_calls=None, done_reason="stop"):
    message = {"role": "assistant", "content": content}
    if tool_calls:
        message["tool_calls"] = tool_calls
    return {
        "model": "llama3.1",
        "message": message,
        "done": True,
        "done_reason": done_reason,
        "prompt_eval_count": 12,
        "eval_count": 5,
    }


def _ndjson(*lines):
    return "\n".join(json.dumps(line) for line in lines) + "\n"


def _user(text):
    return [ModelRequest(parts=[UserPromptPart(content=text)])]


def test_request_sends_the_chatollama_parameters():
    """Stesso payload di ``ChatOllama``: ``num_ctx``/``num_gpu`` in ``options``, ``think`` e
    ``keep_alive`` al primo livello, POST su ``/api/chat``."""
    seen = {}

    def handler(request):
        seen["url"] = str(request.url)
        seen["body"] = json.loads(request.content)
        return httpx.Response(200, json=_chat_reply("ok"))

    model = _ollama(handler, context_window=8192, num_gpu=20, reasoning=True, keep_alive=600)
    asyncio.run(model.request(_user("ciao"), None, NO_PARAMS))

    assert seen["url"] == "http://ollama.local:11434/api/chat"
    body = seen["body"]
    assert body["model"] == "llama3.1"
    assert body["stream"] is False
    assert body["think"] is True
    assert body["keep_alive"] == 600
    assert body["options"] == {"num_ctx": 8192, "num_gpu": 20}


def test_defaults_match_agentic_rag_module():
    """``reasoning`` assente → ``think: false``; ``keep_alive`` assente → 1800 s; ``num_gpu`` e
    ``num_ctx`` non impostati non finiscono in ``options`` (decide Ollama)."""
    seen = {}

    def handler(request):
        seen["body"] = json.loads(request.content)
        return httpx.Response(200, json=_chat_reply("ok"))

    asyncio.run(_ollama(handler).request(_user("ciao"), None, NO_PARAMS))

    body = seen["body"]
    assert body["think"] is False
    assert body["keep_alive"] == DEFAULT_KEEP_ALIVE == 1800
    assert "options" not in body


def test_openai_compat_suffix_is_stripped_from_api_url():
    """Le config scritte per l'endpoint ``/v1`` continuano a funzionare."""
    seen = {}

    def handler(request):
        seen["url"] = str(request.url)
        return httpx.Response(200, json=_chat_reply("ok"))

    model = _ollama(handler, api_url="http://ollama.local:11434/v1/")
    asyncio.run(model.request(_user("ciao"), None, NO_PARAMS))

    assert seen["url"] == "http://ollama.local:11434/api/chat"


def test_temperature_goes_into_options():
    seen = {}

    def handler(request):
        seen["body"] = json.loads(request.content)
        return httpx.Response(200, json=_chat_reply("ok"))

    model = _ollama(handler)
    asyncio.run(model.request(_user("ciao"), ModelSettings(temperature=0), NO_PARAMS))

    assert seen["body"]["options"] == {"temperature": 0}


def test_request_parses_text_and_usage():
    def handler(request):
        return httpx.Response(200, json=_chat_reply("Il RUP e' Mario Rossi."))

    response = asyncio.run(_ollama(handler).request(_user("Chi e' il RUP?"), None, NO_PARAMS))

    assert response.parts == [TextPart(content="Il RUP e' Mario Rossi.")]
    assert response.usage.input_tokens == 12
    assert response.usage.output_tokens == 5
    assert response.finish_reason == "stop"


def test_maps_messages_to_api_chat_roles():
    """Istruzioni in testa come ``system``; argomenti delle tool call come **oggetto** (non
    stringa JSON, come vuole ``/api/chat``); esito del tool come ``role: tool`` con ``tool_name``."""
    messages = [
        ModelRequest(parts=[UserPromptPart(content="Chi e' il RUP?")]),
        ModelResponse(
            parts=[
                ToolCallPart(tool_name="cerca", args={"query": "RUP"}, tool_call_id="c1")
            ]
        ),
        ModelRequest(
            parts=[ToolReturnPart(tool_name="cerca", content="Mario Rossi", tool_call_id="c1")]
        ),
    ]

    mapped = map_messages(messages, [InstructionPart(content="Sei un assistente.")])

    assert mapped[0] == {"role": "system", "content": "Sei un assistente."}
    assert mapped[1] == {"role": "user", "content": "Chi e' il RUP?"}
    assert mapped[2]["role"] == "assistant"
    assert mapped[2]["tool_calls"] == [
        {"id": "c1", "function": {"name": "cerca", "arguments": {"query": "RUP"}}}
    ]
    assert mapped[3] == {
        "role": "tool",
        "tool_name": "cerca",
        "tool_call_id": "c1",
        "content": "Mario Rossi",
    }


def test_agent_completes_a_tool_round_trip():
    """Il giro dell'orchestratore: tool call dal modello, esecuzione, esito rimandato a Ollama."""
    calls = []
    replies = [
        _chat_reply(
            tool_calls=[{"function": {"name": "cerca_rup", "arguments": {"query": "RUP"}}}],
            done_reason="stop",
        ),
        _chat_reply("Il RUP e' Mario Rossi."),
    ]

    def handler(request):
        calls.append(json.loads(request.content))
        return httpx.Response(200, json=replies[len(calls) - 1])

    agent = Agent(_ollama(handler))

    @agent.tool_plain
    def cerca_rup(query: str) -> str:
        """Cerca il RUP nei documenti."""
        return "Mario Rossi"

    result = asyncio.run(agent.run("Chi e' il RUP?"))

    assert result.output == "Il RUP e' Mario Rossi."
    assert calls[0]["tools"][0]["function"]["name"] == "cerca_rup"
    second_turn = calls[1]["messages"]
    assistant = next(m for m in second_turn if m["role"] == "assistant")
    tool_result = next(m for m in second_turn if m["role"] == "tool")
    assert assistant["tool_calls"][0]["function"]["arguments"] == {"query": "RUP"}
    assert tool_result["tool_name"] == "cerca_rup"
    assert tool_result["content"] == "Mario Rossi"


def test_streams_ndjson_text_deltas():
    seen = {}

    def handler(request):
        seen["body"] = json.loads(request.content)
        return httpx.Response(
            200,
            text=_ndjson(
                {"message": {"role": "assistant", "content": "Il RUP "}, "done": False},
                {"message": {"role": "assistant", "content": "e' Mario."}, "done": False},
                {
                    "message": {"role": "assistant", "content": ""},
                    "done": True,
                    "done_reason": "stop",
                    "prompt_eval_count": 7,
                    "eval_count": 3,
                },
            ),
        )

    agent = Agent(_ollama(handler))

    async def run():
        async with agent.run_stream("Chi e' il RUP?") as stream:
            deltas = [d async for d in stream.stream_text(delta=True)]
            return deltas, stream.usage

    deltas, usage = asyncio.run(run())

    assert seen["body"]["stream"] is True
    assert "".join(deltas) == "Il RUP e' Mario."
    assert usage.input_tokens == 7
    assert usage.output_tokens == 3


def test_streams_tool_calls():
    """In streaming Ollama manda la tool call intera in un solo chunk."""
    rounds = [
        _ndjson(
            {
                "message": {
                    "role": "assistant",
                    "content": "",
                    "tool_calls": [
                        {"function": {"name": "cerca_rup", "arguments": {"query": "RUP"}}}
                    ],
                },
                "done": False,
            },
            {"message": {"role": "assistant", "content": ""}, "done": True},
        ),
        _ndjson(
            {"message": {"role": "assistant", "content": "Mario Rossi."}, "done": False},
            {"message": {"role": "assistant", "content": ""}, "done": True},
        ),
    ]
    calls = []

    def handler(request):
        calls.append(json.loads(request.content))
        return httpx.Response(200, text=rounds[len(calls) - 1])

    agent = Agent(_ollama(handler))
    executed = []

    @agent.tool_plain
    def cerca_rup(query: str) -> str:
        """Cerca il RUP nei documenti."""
        executed.append(query)
        return "Mario Rossi"

    async def run():
        async with agent.run_stream("Chi e' il RUP?") as stream:
            return await stream.get_output()

    assert asyncio.run(run()) == "Mario Rossi."
    assert executed == ["RUP"]


def test_raises_on_http_error():
    def handler(request):
        return httpx.Response(404, json={"error": "model 'llama3.1' not found"})

    with pytest.raises(httpx.HTTPStatusError):
        asyncio.run(_ollama(handler).request(_user("ciao"), None, NO_PARAMS))


def test_raises_on_error_line_in_stream():
    """Ollama puo' rispondere 200 e mettere l'errore in una riga NDJSON: non va ingoiato."""

    def handler(request):
        return httpx.Response(200, text=_ndjson({"error": "out of memory"}))

    agent = Agent(_ollama(handler))

    async def run():
        async with agent.run_stream("ciao") as stream:
            return await stream.get_output()

    with pytest.raises(RuntimeError, match="out of memory"):
        asyncio.run(run())


def test_build_model_returns_native_ollama_with_llm_config():
    model = build_model(
        {
            "model_type": "ollama",
            "api_url": "http://ollama.local:11434",
            "model": "llama3.1",
            "context_window": 4096,
            "reasoning": True,
            "keep_alive": 60,
            "num_gpu": 10,
        }
    )

    assert isinstance(model, OllamaNativeModel)
    assert model.model_name == "llama3.1"
    assert model.base_url == "http://ollama.local:11434"
    assert model._chat_fields() == {
        "think": True,
        "keep_alive": 60,
        "options": {"num_ctx": 4096, "num_gpu": 10},
    }
