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

"""Errori della pipeline verso il client (port di #2253 e di ``error_events`` del modulo).

- Il blocco del filtro del provider (content policy di Azure) e' una violazione di guardrail:
  GUARDRAIL + END, come il guardrail di input, non un ERROR con il dettaglio grezzo.
- Il rate limit ha un messaggio fisso.
- Ogni altro errore porta un testo opaco: il dettaglio dell'eccezione resta nei log. Solo un
  ``HTTPException`` sotto 500, cioe' un rifiuto su cui il chiamante puo' agire, aggiunge il
  motivo nel campo ``message``, l'unico che il frontend mostra.
"""

import asyncio
import json
from unittest.mock import MagicMock

import pytest
from fastapi import HTTPException
from pydantic_ai.exceptions import ModelHTTPError

from app.rag.agentic import entrypoint
from app.utils.error_events import error_event

# Forma del blocco litellm/Azure segnalato dai clienti (stesso testo del test del modulo).
CONTENT_POLICY_ERROR = (
    "Error code: 400 - litellm.BadRequestError: "
    "litellm.ContentPolicyViolationError: The response was filtered due to "
    "the prompt triggering Azure OpenAI's content management policy. "
    "model=gpt-5.4-mini 'innererror': {'code': 'ResponsibleAIPolicyViolation', "
    "'content_filter_result': {'jailbreak': {'detected': True, "
    "'filtered': True}}}"
)


def _events(monkeypatch, exception):
    """Eventi di ``get_agentic_rag`` quando il turno solleva ``exception``."""

    async def failing_run(**_):
        raise exception
        yield  # pragma: no cover - rende la funzione un async generator

    monkeypatch.setattr(entrypoint, "run_turn", failing_run)
    monkeypatch.setattr(entrypoint, "build_model", lambda _: MagicMock())

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.get_agentic_rag(
                rag_type="SIMPLE_GENERATE",
                search_query=None,
                datasource_ids=None,
                after_key=None,
                suggest_keyword=None,
                suggestion_category_id=None,
                jwt=None,
                extra={},
                sort=None,
                sort_after_key=None,
                language=None,
                search_text="domanda",
                chat_id=None,
                user_id=None,
                tenant_id="tenant-1",
                retrieve_from_uploaded_documents=None,
                chat_history=None,
                timestamp=None,
                chat_sequence_number=1,
                rag_configuration={},
                llm_configuration={},
                guardrails_configuration={},
                opensearch_host="http://os",
                grpc_host_embedding="em:9000",
                grpc_host_datasource="ds:9000",
            )
        ]

    return asyncio.run(collect())


@pytest.mark.parametrize(
    "exception",
    [
        Exception(CONTENT_POLICY_ERROR),
        ModelHTTPError(
            status_code=400,
            model_name="gpt-4o",
            body={"error": {"code": "content_filter", "message": "filtered"}},
        ),
    ],
    ids=["litellm_azure", "pydantic_ai_http_error"],
)
def test_content_policy_block_emits_guardrail_and_end(monkeypatch, exception):
    assert _events(monkeypatch, exception) == [
        {"chunk": "Guardrail violation", "type": "GUARDRAIL"},
        {"chunk": "", "type": "END"},
    ]


@pytest.mark.parametrize(
    "exception",
    [
        Exception("litellm.RateLimitError: rate_limit exceeded (429)"),
        ModelHTTPError(status_code=429, model_name="gpt-4o", body={"error": "too many"}),
    ],
    ids=["litellm", "pydantic_ai_http_error"],
)
def test_rate_limit_reported_with_a_fixed_message(monkeypatch, exception):
    assert _events(monkeypatch, exception) == [
        {"chunk": "Rate limit exceeded. Try again later.", "type": "ERROR"},
    ]


def test_internal_error_detail_is_not_streamed(monkeypatch):
    events = _events(monkeypatch, RuntimeError("connection to http://10.0.0.5:9200 refused"))

    assert events == [{"chunk": "Unexpected error", "type": "ERROR"}]


def test_caller_rejection_carries_its_reason(monkeypatch):
    events = _events(
        monkeypatch, HTTPException(status_code=400, detail="Unsupported media type")
    )

    assert events == [
        {"chunk": "Unexpected error", "type": "ERROR", "message": "Unsupported media type"}
    ]


# --- error_event (copia del modulo) ---------------------------------------------


def test_error_event_without_message_for_internal_failures():
    assert error_event(HTTPException(status_code=500, detail="x"), "c") == {
        "chunk": "c",
        "type": "ERROR",
    }
    assert error_event(ValueError("x"), "c") == {"chunk": "c", "type": "ERROR"}


def test_error_event_with_message_for_client_errors():
    assert error_event(HTTPException(status_code=422, detail="bad"), "c") == {
        "chunk": "c",
        "type": "ERROR",
        "message": "bad",
    }
