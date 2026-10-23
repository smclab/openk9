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
Port PydanticAI di ``agentic-rag-module/app/rag/custom_hugging_face_model.py``
(``CustomChatHuggingFaceModel``), per il provider ``hugging-face-custom``.

Il contratto di rete e' **invariato** rispetto ad agentic-rag-module, così le configurazioni
tenant esistenti continuano a funzionare senza modifiche:

- ``POST {api_url}`` con body ``{"input_str": <prompt>}``
- risposta in streaming di testo grezzo (nessun SSE, nessun JSON): ogni frammento ricevuto sul
  socket e' un pezzo di risposta da cedere così com'e'.

Due differenze volute rispetto all'originale:

1. **Il prompt e' l'appiattimento dell'intera conversazione**, non il solo ``messages[-1]``.
   In agentic-rag-module la catena impacchettava contesto + storico + domanda in un unico
   ``HumanMessage``, quindi ``messages[-1].content`` *era* il prompt completo. In PydanticAI le
   istruzioni stanno su ``ModelRequest.instructions`` e i turni sono messaggi separati: prendere
   solo l'ultimo butterebbe via il system prompt. :func:`flatten_messages` ricompone la stessa
   informazione in una stringa.
2. **La richiesta non-streaming funziona.** ``CustomChatHuggingFaceModel._generate`` riferiva un
   attributo ``self.n`` inesistente e sollevava ``AttributeError`` (titolo conversazione,
   guardrail LLM, intent detection). Qui :meth:`CustomHuggingFaceModel.request` aggrega la stessa
   risposta dell'endpoint.

Limite ereditato dall'originale: l'endpoint non fa tool calling, quindi il profilo dichiara
``supports_tools=False`` e la structured output passa in modalita' ``prompted`` (JSON chiesto nel
prompt). I tool di delega dell'orchestratore (``discover``) non sono invocabili con questo provider.
"""

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Optional

import httpx
from pydantic_ai.messages import (
    InstructionPart,
    ModelMessage,
    ModelRequest,
    ModelResponse,
    ModelResponseStreamEvent,
    RetryPromptPart,
    SystemPromptPart,
    TextPart,
    ToolCallPart,
    ToolReturnPart,
    UserPromptPart,
)
from pydantic_ai.models import Model, ModelRequestParameters, StreamedResponse
from pydantic_ai.profiles import ModelProfile
from pydantic_ai.settings import ModelSettings

from app.utils.logger import get_logger

logger = get_logger(__name__)

SYSTEM = "hugging-face-custom"

# L'endpoint bespoke non ha ne' tool calling ne' JSON mode. Le due chiavi vanno insieme:
# `supports_tools=False` da solo, col default_structured_output_mode a 'tool', fa alzare a
# PydanticAI UserError('Tool output is not supported by this model.'); il modo 'prompted' e' quello
# che porta davvero lo schema JSON dentro il prompt (l'unico canale che questo endpoint ha).
HUGGING_FACE_PROFILE = ModelProfile(
    supports_tools=False,
    supports_json_schema_output=False,
    supports_json_object_output=False,
    default_structured_output_mode="prompted",
)

# Generazione lunga su hardware self-hosted: il default di httpx (5s) taglierebbe le risposte.
DEFAULT_TIMEOUT = 300.0


def flatten_messages(
    messages: list[ModelMessage],
    instruction_parts: Optional[list[InstructionPart]] = None,
) -> str:
    """Appiattisce la conversazione PydanticAI nella singola stringa attesa da ``input_str``.

    Istruzioni e system prompt in testa (senza etichetta, com'erano nel prompt template originale),
    poi i turni etichettati ``User:`` / ``Assistant:`` / ``Tool result:`` per non renderli ambigui.

    ``instruction_parts`` arriva da ``Model._get_instruction_parts`` e **non** coincide con
    ``ModelRequest.instructions``: in modalita' prompted output PydanticAI ci appende lo schema
    JSON richiesto, che sta solo li'. Leggendo le istruzioni dai messaggi il modello non saprebbe
    mai che forma deve avere la risposta.
    """
    blocks: list[str] = [part.content for part in instruction_parts or []]

    for message in messages:
        if isinstance(message, ModelRequest):
            blocks.extend(_render_request_part(part) for part in message.parts)
        elif isinstance(message, ModelResponse):
            blocks.extend(_render_response_part(part) for part in message.parts)

    return "\n\n".join(block for block in blocks if block)


def _render_request_part(part) -> str:
    if isinstance(part, SystemPromptPart):
        return part.content
    if isinstance(part, UserPromptPart):
        return f"User: {_user_prompt_text(part)}"
    if isinstance(part, ToolReturnPart):
        return f"Tool result: {part.model_response_str()}"
    if isinstance(part, RetryPromptPart):
        return f"Tool result: {part.model_response()}"
    return ""


def _render_response_part(part) -> str:
    if isinstance(part, TextPart):
        return f"Assistant: {part.content}"
    if isinstance(part, ToolCallPart):
        return f"Assistant: {part.tool_name}({part.args_as_json_str()})"
    return ""


def _user_prompt_text(part: UserPromptPart) -> str:
    """Testo di uno ``UserPromptPart``; le parti non testuali (immagini, file) sono scartate:
    l'endpoint accetta solo ``input_str``."""
    if isinstance(part.content, str):
        return part.content
    return "\n".join(item for item in part.content if isinstance(item, str))


@dataclass
class HuggingFaceStreamedResponse(StreamedResponse):
    """Frammenti di testo grezzo dell'endpoint bespoke → eventi di delta PydanticAI."""

    _model_name: str
    _provider_url: str
    _chunks: AsyncIterator[str]
    _timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))

    async def _get_event_iterator(self) -> AsyncIterator[ModelResponseStreamEvent]:
        async for text in self._chunks:
            if not text:
                continue
            for event in self._parts_manager.handle_text_delta(
                vendor_part_id="content", content=text
            ):
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


class CustomHuggingFaceModel(Model):
    """``pydantic_ai.models.Model`` per un endpoint HuggingFace custom ``{"input_str": ...}``."""

    def __init__(
        self,
        base_url: str,
        *,
        model_name: str = SYSTEM,
        timeout: float = DEFAULT_TIMEOUT,
        http_client: Optional[httpx.AsyncClient] = None,
        settings: Optional[ModelSettings] = None,
        profile=None,
    ) -> None:
        # `is None` e non `or`: un ModelProfile vuoto e' un dict falsy, e verrebbe scartato.
        super().__init__(
            settings=settings,
            profile=HUGGING_FACE_PROFILE if profile is None else profile,
        )
        self._base_url = base_url
        self._model_name = model_name
        self._timeout = timeout
        self._http_client = http_client

    @property
    def base_url(self) -> str:
        return self._base_url

    @property
    def model_name(self) -> str:
        return self._model_name

    @property
    def system(self) -> str:
        return SYSTEM

    async def request(
        self,
        messages: list[ModelMessage],
        model_settings: Optional[ModelSettings],
        model_request_parameters: ModelRequestParameters,
    ) -> ModelResponse:
        """Risposta completa: stessa POST dello streaming, frammenti concatenati."""
        _, model_request_parameters = self.prepare_request(
            model_settings, model_request_parameters
        )
        stream = self._post(messages, model_request_parameters)
        text = "".join([chunk async for chunk in stream])
        return ModelResponse(
            parts=[TextPart(content=text)],
            model_name=self._model_name,
            provider_name=SYSTEM,
            provider_url=self.base_url,
        )

    @asynccontextmanager
    async def request_stream(
        self,
        messages: list[ModelMessage],
        model_settings: Optional[ModelSettings],
        model_request_parameters: ModelRequestParameters,
        run_context=None,
    ) -> AsyncIterator[StreamedResponse]:
        _, model_request_parameters = self.prepare_request(
            model_settings, model_request_parameters
        )
        yield HuggingFaceStreamedResponse(
            model_request_parameters=model_request_parameters,
            _model_name=self._model_name,
            _provider_url=self.base_url,
            _chunks=self._post(messages, model_request_parameters),
        )

    async def _post(
        self,
        messages: list[ModelMessage],
        model_request_parameters: ModelRequestParameters,
    ) -> AsyncIterator[str]:
        """``POST {base_url}`` con ``{"input_str": ...}``; cede i frammenti di testo grezzo."""
        input_str = flatten_messages(
            messages,
            self._get_instruction_parts(messages, model_request_parameters),
        )
        logger.debug(f"[hugging-face-custom] POST {self.base_url} input_str={len(input_str)} char")

        client = self._http_client
        async with _client_scope(client, self._timeout) as active_client:
            async with active_client.stream(
                "POST",
                self._base_url,
                json={"input_str": input_str},
            ) as response:
                response.raise_for_status()
                async for text in response.aiter_text():
                    yield text


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
