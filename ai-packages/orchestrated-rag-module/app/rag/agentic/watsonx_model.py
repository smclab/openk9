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
Port PydanticAI del provider ``watsonx`` di ``agentic-rag-module/app/utils/llm.py``.

PydanticAI 2.6 non ha un provider IBM, quindi serve una sottoclasse ``Model``. Sotto usiamo lo
**stesso SDK** che usa agentic-rag-module (``ibm_watsonx_ai.foundation_models.ModelInference``): URL
(``/ml/v1/text/chat`` e ``/ml/v1/text/chat_stream``), ``?version=``, token IAM e refresh restano
gestiti dall'SDK esattamente come oggi, invece di essere riscritti a mano su httpx.

L'SDK e' importato **lazy** (dentro ``__init__``), coerentemente con gli altri provider di
``models_factory``: chi non usa watsonx non deve installare ``ibm-watsonx-ai``.

Parita' dei parametri di generazione con ``initialize_language_model``:

- ``decoding_method``, ``max_new_tokens``, ``min_new_tokens``, ``top_k``, ``top_p`` sono passati
  identici (:data:`BASE_PARAMS`). Nota: l'SDK li spalma in cima al payload della Chat API, e la
  Chat API di watsonx non li documenta (sono parametri di *text generation*) — cioe' oggi in
  agentic-rag ``max_new_tokens: 100`` non tronca nulla. Li teniamo comunque per non cambiare il
  payload inviato.
- ``temperature`` arriva dalle ``ModelSettings`` di PydanticAI, non dal costruttore, ed e' omessa
  quando ``None`` (agentic-rag-module in quel caso inviava ``"temperature": null``).
"""

import asyncio
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any, Optional

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

SYSTEM = "watsonx"

# Identici a ``initialize_language_model`` (ModelType.IBM_WATSONX). ``temperature`` non e' qui:
# in PydanticAI sta nelle ModelSettings e viene unita per richiesta (vedi ``_build_params``).
BASE_PARAMS: dict[str, Any] = {
    "decoding_method": "sample",
    "max_new_tokens": 100,
    "min_new_tokens": 1,
    "top_k": 50,
    "top_p": 1,
}

_FINISH_REASONS: dict[str, FinishReason] = {
    "stop": "stop",
    "eos_token": "stop",
    "length": "length",
    "max_tokens": "length",
    "tool_calls": "tool_call",
    "content_filter": "content_filter",
}


def map_messages(
    messages: list[ModelMessage],
    instruction_parts: Optional[list[InstructionPart]] = None,
) -> list[dict[str, Any]]:
    """Converte i ``ModelMessage`` PydanticAI nei message dict della Chat API di watsonx.

    ``instruction_parts`` arriva da ``Model._get_instruction_parts`` e diventa un ``system`` in
    testa; non va letto da ``ModelRequest.instructions``, che nel flusso normale dell'Agent e'
    vuoto (le istruzioni, incluse quelle di prompted output, stanno nei request parameters).
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
        return {
            "role": "tool",
            "tool_call_id": part.tool_call_id,
            "content": part.model_response_str(),
        }
    if isinstance(part, RetryPromptPart):
        if part.tool_name is None:
            return {"role": "user", "content": part.model_response()}
        return {
            "role": "tool",
            "tool_call_id": part.tool_call_id,
            "content": part.model_response(),
        }
    return None


def _assistant_message(message: ModelResponse) -> dict[str, Any]:
    """Un solo message ``assistant`` per ``ModelResponse``: testo concatenato + tool_calls."""
    texts: list[str] = []
    tool_calls: list[dict[str, Any]] = []

    for part in message.parts:
        if isinstance(part, TextPart):
            texts.append(part.content)
        elif isinstance(part, ToolCallPart):
            tool_calls.append(
                {
                    "id": part.tool_call_id,
                    "type": "function",
                    "function": {
                        "name": part.tool_name,
                        "arguments": part.args_as_json_str(),
                    },
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
    """``ToolDefinition`` → schema ``tools`` della Chat API (stessa forma di OpenAI)."""
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
    usage = payload.get("usage") or {}
    return RequestUsage(
        input_tokens=usage.get("prompt_tokens", 0),
        output_tokens=usage.get("completion_tokens", 0),
    )


@dataclass
class WatsonxStreamedResponse(StreamedResponse):
    """Chunk SSE di ``/ml/v1/text/chat_stream`` → eventi di delta PydanticAI."""

    _model_name: str
    _provider_url: str
    _chunks: AsyncIterator[dict[str, Any]]
    _timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))

    async def _get_event_iterator(self) -> AsyncIterator[ModelResponseStreamEvent]:
        async for chunk in self._chunks:
            if response_id := chunk.get("id"):
                self.provider_response_id = response_id
            if chunk.get("usage"):
                self._usage = _usage(chunk)

            choices = chunk.get("choices") or []
            if not choices:
                continue
            choice = choices[0]

            if reason := choice.get("finish_reason"):
                self.finish_reason = _FINISH_REASONS.get(reason, "stop")

            delta = choice.get("delta") or {}
            if content := delta.get("content"):
                for event in self._parts_manager.handle_text_delta(
                    vendor_part_id="content", content=content
                ):
                    yield event

            for tool_call in delta.get("tool_calls") or []:
                function = tool_call.get("function") or {}
                event = self._parts_manager.handle_tool_call_delta(
                    vendor_part_id=tool_call.get("index", 0),
                    tool_name=function.get("name"),
                    args=function.get("arguments"),
                    tool_call_id=tool_call.get("id"),
                )
                if event is not None:
                    yield event

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


class WatsonxModel(Model):
    """``pydantic_ai.models.Model`` per IBM watsonx.ai, equivalente di ``ChatWatsonx``."""

    def __init__(
        self,
        model_name: str,
        *,
        api_url: str,
        api_key: str,
        project_id: str,
        inference=None,
        settings: Optional[ModelSettings] = None,
        profile=None,
    ) -> None:
        """``inference`` accetta un ``ModelInference`` gia' costruito (usato dai test); quando e'
        ``None`` viene creato alla prima richiesta da :meth:`_get_inference`."""
        super().__init__(settings=settings, profile=profile)
        self._model_name = model_name
        self._api_url = api_url
        self._api_key = api_key
        self._project_id = project_id
        self._inference = inference

    async def _get_inference(self):
        """``ModelInference`` dell'SDK IBM, creato pigramente e fuori dall'event loop.

        Il costruttore dell'SDK fa un handshake IAM **sincrono** (``POST
        iam.cloud.ibm.com/identity/token``). Siccome ``build_model`` gira dentro il request path
        async, costruirlo li' bloccherebbe l'event loop — e con esso tutte le altre richieste in
        volo — per l'intero round-trip verso IBM.
        """
        if self._inference is None:
            self._inference = await asyncio.to_thread(self._build_inference)
        return self._inference

    def _build_inference(self):
        from ibm_watsonx_ai import Credentials
        from ibm_watsonx_ai.foundation_models import ModelInference

        return ModelInference(
            model_id=self._model_name,
            credentials=Credentials(url=self._api_url, api_key=self._api_key),
            project_id=self._project_id,
        )

    @property
    def model_name(self) -> str:
        return self._model_name

    @property
    def system(self) -> str:
        return SYSTEM

    @property
    def base_url(self) -> str:
        return self._api_url

    async def request(
        self,
        messages: list[ModelMessage],
        model_settings: Optional[ModelSettings],
        model_request_parameters: ModelRequestParameters,
    ) -> ModelResponse:
        model_settings, model_request_parameters = self.prepare_request(
            model_settings, model_request_parameters
        )
        inference = await self._get_inference()
        payload = await inference.achat(
            **self._call_kwargs(messages, model_settings, model_request_parameters)
        )

        choice = (payload.get("choices") or [{}])[0]
        message = choice.get("message") or {}
        parts: list[ModelResponsePart] = []

        if content := message.get("content"):
            parts.append(TextPart(content=content))
        for tool_call in message.get("tool_calls") or []:
            function = tool_call.get("function") or {}
            parts.append(
                ToolCallPart(
                    tool_name=function.get("name", ""),
                    args=function.get("arguments"),
                    tool_call_id=tool_call.get("id"),
                )
            )

        return ModelResponse(
            parts=parts,
            usage=_usage(payload),
            model_name=payload.get("model_id") or self._model_name,
            provider_name=SYSTEM,
            provider_url=self._api_url,
            provider_response_id=payload.get("id"),
            finish_reason=_FINISH_REASONS.get(choice.get("finish_reason") or "", None),
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
        inference = await self._get_inference()
        chunks = await inference.achat_stream(
            **self._call_kwargs(messages, model_settings, model_request_parameters)
        )
        yield WatsonxStreamedResponse(
            model_request_parameters=model_request_parameters,
            _model_name=self._model_name,
            _provider_url=self._api_url,
            _chunks=chunks,
        )

    def _call_kwargs(
        self,
        messages: list[ModelMessage],
        model_settings: Optional[ModelSettings],
        model_request_parameters: ModelRequestParameters,
    ) -> dict[str, Any]:
        kwargs: dict[str, Any] = {
            "messages": map_messages(
                messages,
                self._get_instruction_parts(messages, model_request_parameters),
            ),
            "params": self._build_params(model_settings),
        }

        tools = [
            *model_request_parameters.function_tools,
            *model_request_parameters.output_tools,
        ]
        if tools:
            kwargs["tools"] = map_tools(tools)
            kwargs["tool_choice_option"] = (
                "required" if not model_request_parameters.allow_text_output else "auto"
            )

        logger.debug(
            f"[watsonx] chat model_id={self._model_name} "
            f"messages={len(kwargs['messages'])} tools={len(kwargs.get('tools', []))}"
        )
        return kwargs

    @staticmethod
    def _build_params(model_settings: Optional[ModelSettings]) -> dict[str, Any]:
        """``BASE_PARAMS`` + ``temperature`` dalle ModelSettings (omessa se assente)."""
        params = dict(BASE_PARAMS)
        temperature = (model_settings or {}).get("temperature")
        if temperature is not None:
            params["temperature"] = temperature
        return params
