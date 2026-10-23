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
Port PydanticAI del provider ``ollama`` di ``agentic-rag-module/app/utils/llm.py``.

Il provider di agentic-rag-module parla con la **API nativa** ``/api/chat``. PydanticAI 2.6 ha solo modelli Ollama
sull'endpoint OpenAI-compatibile ``/v1/chat/completions`` (anche ``OllamaModel`` e' un
``OpenAIChatModel``), che non accetta ``options``: ``num_ctx`` e ``num_gpu`` non arriverebbero mai.
Da qui la sottoclasse ``Model``, su httpx come ``CustomHuggingFaceModel``.

Parita' dei parametri con ``initialize_language_model`` (ModelType.OLLAMA):

- ``options.num_ctx`` = ``context_window`` della LLM configuration;
- ``options.num_gpu`` = jsonConfig ``num_gpu``; omesso quando assente (lo split lo decide Ollama);
- ``think`` = ``bool(reasoning)``: sempre inviato, perche' sui modelli che lo supportano Ollama
  accende il thinking da solo;
- ``keep_alive`` = jsonConfig ``keep_alive``, default :data:`DEFAULT_KEEP_ALIVE`;
- ``options.temperature`` arriva dalle ``ModelSettings`` ed e' omessa quando ``None``.

Il thinking restituito da Ollama diventa ``ThinkingPart`` e non viene rimandato nei turni
successivi. ``/api/chat`` non ha ``tool_choice``: quando l'Agent richiede un output tool, a
forzarlo resta il retry di PydanticAI sulla risposta testuale.
"""

import json
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any, Optional

import httpx
from pydantic_ai.messages import (
    FinishReason,
    InstructionPart,
    ModelMessage,
    ModelRequest,
    ModelResponse,
    ModelResponsePart,
    ModelResponseStreamEvent,
    RetryPromptPart,
    SystemPromptPart,
    TextPart,
    ThinkingPart,
    ToolCallPart,
    ToolReturnPart,
    UserPromptPart,
)
from pydantic_ai.models import Model, ModelRequestParameters, StreamedResponse
from pydantic_ai.settings import ModelSettings
from pydantic_ai.tools import ToolDefinition
from pydantic_ai.usage import RequestUsage

from app.utils.logger import get_logger

logger = get_logger(__name__)

SYSTEM = "ollama"

# Stesso default di ``agentic-rag-module/app/utils/llm.py``.
DEFAULT_KEEP_ALIVE = 1800

# Generazione su hardware self-hosted (e caricamento del modello alla prima chiamata): il default
# di httpx (5s) taglierebbe le risposte.
DEFAULT_TIMEOUT = 300.0

_FINISH_REASONS: dict[str, FinishReason] = {
    "stop": "stop",
    "length": "length",
}


def map_messages(
    messages: list[ModelMessage],
    instruction_parts: Optional[list[InstructionPart]] = None,
) -> list[dict[str, Any]]:
    """Converte i ``ModelMessage`` PydanticAI nei message dict di ``/api/chat``.

    ``instruction_parts`` arriva da ``Model._get_instruction_parts`` e diventa un ``system`` in
    testa (vedi ``watsonx_model.map_messages``).
    """
    mapped: list[dict[str, Any]] = [
        {"role": "system", "content": part.content} for part in instruction_parts or []
    ]

    for message in messages:
        if isinstance(message, ModelRequest):
            mapped.extend(
                mapped_part
                for part in message.parts
                if (mapped_part := _map_request_part(part)) is not None
            )
        elif isinstance(message, ModelResponse):
            mapped.append(_assistant_message(message))

    return mapped


def _map_request_part(part) -> Optional[dict[str, Any]]:
    if isinstance(part, SystemPromptPart):
        return {"role": "system", "content": part.content}
    if isinstance(part, UserPromptPart):
        return {"role": "user", "content": _user_prompt_text(part)}
    if isinstance(part, ToolReturnPart):
        return _tool_message(part.tool_name, part.tool_call_id, part.model_response_str())
    if isinstance(part, RetryPromptPart):
        if part.tool_name is None:
            return {"role": "user", "content": part.model_response()}
        return _tool_message(part.tool_name, part.tool_call_id, part.model_response())
    return None


def _tool_message(tool_name: str, tool_call_id: str, content: str) -> dict[str, Any]:
    """Esito di un tool. ``/api/chat`` lo correla per ``tool_name``; ``tool_call_id`` lo leggono
    solo le versioni di Ollama che assegnano un id alle tool call, le altre lo ignorano."""
    return {
        "role": "tool",
        "tool_name": tool_name,
        "tool_call_id": tool_call_id,
        "content": content,
    }


def _assistant_message(message: ModelResponse) -> dict[str, Any]:
    """Un solo message ``assistant`` per ``ModelResponse``: testo concatenato + tool_calls con gli
    argomenti come oggetto (``/api/chat`` non accetta la stringa JSON dell'API OpenAI)."""
    texts: list[str] = []
    tool_calls: list[dict[str, Any]] = []

    for part in message.parts:
        if isinstance(part, TextPart):
            texts.append(part.content)
        elif isinstance(part, ToolCallPart):
            tool_calls.append(
                {
                    "id": part.tool_call_id,
                    "function": {"name": part.tool_name, "arguments": part.args_as_dict()},
                }
            )

    assistant: dict[str, Any] = {"role": "assistant", "content": "".join(texts)}
    if tool_calls:
        assistant["tool_calls"] = tool_calls
    return assistant


def _user_prompt_text(part: UserPromptPart) -> str:
    """Testo di uno ``UserPromptPart``; le parti binarie sono scartate (nessun input multimodale
    passa da questo flusso RAG)."""
    if isinstance(part.content, str):
        return part.content
    return "\n".join(item for item in part.content if isinstance(item, str))


def map_tools(tools: list[ToolDefinition]) -> list[dict[str, Any]]:
    """``ToolDefinition`` → schema ``tools`` di ``/api/chat`` (stessa forma di OpenAI)."""
    return [
        {
            "type": "function",
            "function": {
                "name": tool.name,
                "description": tool.description or "",
                "parameters": tool.parameters_json_schema,
            },
        }
        for tool in tools
    ]


def _usage(payload: dict[str, Any]) -> RequestUsage:
    return RequestUsage(
        input_tokens=payload.get("prompt_eval_count") or 0,
        output_tokens=payload.get("eval_count") or 0,
    )


def _finish_reason(payload: dict[str, Any], has_tool_calls: bool) -> Optional[FinishReason]:
    # Ollama chiude con ``done_reason: "stop"`` anche quando ha emesso tool call.
    if has_tool_calls:
        return "tool_call"
    return _FINISH_REASONS.get(payload.get("done_reason") or "")


def _raise_on_error(payload: dict[str, Any]) -> None:
    """Ollama puo' rispondere 200 e mettere l'errore nel corpo (es. OOM a stream iniziato)."""
    if error := payload.get("error"):
        raise RuntimeError(f"Ollama error: {error}")


@dataclass
class OllamaStreamedResponse(StreamedResponse):
    """Righe NDJSON di ``/api/chat`` in streaming → eventi di delta PydanticAI."""

    _model_name: str
    _provider_url: str
    _chunks: AsyncIterator[dict[str, Any]]
    _timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))

    async def _get_event_iterator(self) -> AsyncIterator[ModelResponseStreamEvent]:
        tool_calls = 0
        async for chunk in self._chunks:
            message = chunk.get("message") or {}

            if thinking := message.get("thinking"):
                for event in self._parts_manager.handle_thinking_delta(
                    vendor_part_id="thinking", content=thinking
                ):
                    yield event

            if content := message.get("content"):
                for event in self._parts_manager.handle_text_delta(
                    vendor_part_id="content", content=content
                ):
                    yield event

            # le tool call arrivano gia' complete, una per chunk o piu' insieme
            for tool_call in message.get("tool_calls") or []:
                function = tool_call.get("function") or {}
                yield self._parts_manager.handle_tool_call_part(
                    vendor_part_id=f"tool-{tool_calls}",
                    tool_name=function.get("name", ""),
                    args=function.get("arguments"),
                    tool_call_id=tool_call.get("id"),
                )
                tool_calls += 1

            if chunk.get("done"):
                self._usage = _usage(chunk)
                self.finish_reason = _finish_reason(chunk, tool_calls > 0)

    @property
    def model_name(self) -> str:
        return self._model_name

    @property
    def provider_name(self) -> str:
        return SYSTEM

    @property
    def provider_url(self) -> str:
        return self._provider_url

    @property
    def timestamp(self) -> datetime:
        return self._timestamp


class OllamaNativeModel(Model):
    """``pydantic_ai.models.Model`` sulla API nativa di Ollama, equivalente di ``ChatOllama``."""

    def __init__(
        self,
        model_name: str,
        *,
        api_url: str,
        context_window: Optional[int] = None,
        num_gpu: Optional[int] = None,
        reasoning: Optional[bool] = None,
        keep_alive: Optional[Any] = None,
        timeout: float = DEFAULT_TIMEOUT,
        http_client: Optional[httpx.AsyncClient] = None,
        settings: Optional[ModelSettings] = None,
        profile=None,
    ) -> None:
        super().__init__(settings=settings, profile=profile)
        self._model_name = model_name
        # Le config scritte per l'endpoint OpenAI-compatibile hanno il suffisso ``/v1``: lo
        # togliamo, cosi' lo stesso ``apiUrl`` vale per entrambi i moduli.
        base_url = (api_url or "").rstrip("/")
        self._base_url = base_url[: -len("/v1")] if base_url.endswith("/v1") else base_url
        self._context_window = context_window
        self._num_gpu = num_gpu
        self._reasoning = reasoning
        self._keep_alive = keep_alive
        self._timeout = timeout
        self._http_client = http_client

    @property
    def model_name(self) -> str:
        return self._model_name

    @property
    def system(self) -> str:
        return SYSTEM

    @property
    def base_url(self) -> str:
        return self._base_url

    async def request(
        self,
        messages: list[ModelMessage],
        model_settings: Optional[ModelSettings],
        model_request_parameters: ModelRequestParameters,
    ) -> ModelResponse:
        model_settings, model_request_parameters = self.prepare_request(
            model_settings, model_request_parameters
        )
        body = self._body(messages, model_settings, model_request_parameters, stream=False)

        async with _client_scope(self._http_client, self._timeout) as client:
            response = await client.post(f"{self._base_url}/api/chat", json=body)
            response.raise_for_status()
            payload = response.json()
        _raise_on_error(payload)

        message = payload.get("message") or {}
        parts: list[ModelResponsePart] = []
        if thinking := message.get("thinking"):
            parts.append(ThinkingPart(content=thinking))
        if content := message.get("content"):
            parts.append(TextPart(content=content))
        tool_calls = message.get("tool_calls") or []
        for tool_call in tool_calls:
            function = tool_call.get("function") or {}
            call = {"tool_name": function.get("name", ""), "args": function.get("arguments")}
            # senza id lo genera ToolCallPart: alcune versioni di Ollama non lo assegnano
            if tool_call_id := tool_call.get("id"):
                call["tool_call_id"] = tool_call_id
            parts.append(ToolCallPart(**call))

        return ModelResponse(
            parts=parts,
            usage=_usage(payload),
            model_name=payload.get("model") or self._model_name,
            provider_name=SYSTEM,
            provider_url=self._base_url,
            finish_reason=_finish_reason(payload, bool(tool_calls)),
        )

    @asynccontextmanager
    async def request_stream(
        self,
        messages: list[ModelMessage],
        model_settings: Optional[ModelSettings],
        model_request_parameters: ModelRequestParameters,
        run_context=None,
    ) -> AsyncIterator[StreamedResponse]:
        model_settings, model_request_parameters = self.prepare_request(
            model_settings, model_request_parameters
        )
        body = self._body(messages, model_settings, model_request_parameters, stream=True)

        async with _client_scope(self._http_client, self._timeout) as client:
            async with client.stream(
                "POST", f"{self._base_url}/api/chat", json=body
            ) as response:
                if response.is_error:
                    await response.aread()
                    response.raise_for_status()
                yield OllamaStreamedResponse(
                    model_request_parameters=model_request_parameters,
                    _model_name=self._model_name,
                    _provider_url=self._base_url,
                    _chunks=_ndjson_lines(response),
                )

    def _body(
        self,
        messages: list[ModelMessage],
        model_settings: Optional[ModelSettings],
        model_request_parameters: ModelRequestParameters,
        *,
        stream: bool,
    ) -> dict[str, Any]:
        body: dict[str, Any] = {
            "model": self._model_name,
            "messages": map_messages(
                messages,
                self._get_instruction_parts(messages, model_request_parameters),
            ),
            "stream": stream,
            **self._chat_fields(),
        }

        temperature = (model_settings or {}).get("temperature")
        if temperature is not None:
            body["options"] = {**body.get("options", {}), "temperature": temperature}

        tools = [
            *model_request_parameters.function_tools,
            *model_request_parameters.output_tools,
        ]
        if tools:
            body["tools"] = map_tools(tools)

        logger.debug(
            f"[ollama] chat model={self._model_name} messages={len(body['messages'])} "
            f"tools={len(tools)} stream={stream} options={body.get('options')}"
        )
        return body

    def _chat_fields(self) -> dict[str, Any]:
        """Parametri fissi per modello, gli stessi che ``ChatOllama`` riceve nel costruttore."""
        fields: dict[str, Any] = {
            "think": bool(self._reasoning),
            "keep_alive": DEFAULT_KEEP_ALIVE if self._keep_alive is None else self._keep_alive,
        }
        options: dict[str, Any] = {}
        # contextWindow 0 = non valorizzato nel proto: va lasciato al default del modello
        if self._context_window:
            options["num_ctx"] = self._context_window
        if self._num_gpu is not None:
            options["num_gpu"] = self._num_gpu
        if options:
            fields["options"] = options
        return fields


async def _ndjson_lines(response: httpx.Response) -> AsyncIterator[dict[str, Any]]:
    async for line in response.aiter_lines():
        if not line.strip():
            continue
        chunk = json.loads(line)
        _raise_on_error(chunk)
        yield chunk


@asynccontextmanager
async def _client_scope(
    client: Optional[httpx.AsyncClient], timeout: float
) -> AsyncIterator[httpx.AsyncClient]:
    """Usa il client iniettato (test) oppure ne apre e chiude uno per la richiesta."""
    if client is not None:
        yield client
        return
    async with httpx.AsyncClient(timeout=timeout) as owned:
        yield owned
