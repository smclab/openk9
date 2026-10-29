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

"""A model that cannot call tools gets a clear error, not an answer without documents (#2627).

The documents are found only through tools: the orchestrator delegates with ``discover``, the
Discovery searches with ``retriever``. PydanticAI drops the tools of a model without tool
calling, so with ``hugging-face-custom`` the orchestrator would answer from what the model knows,
without saying so. With ``bypass_rag`` the orchestrator has no tools, and the turn works.
"""

import asyncio
import json
import logging

from app.rag.agentic import entrypoint

HUGGING_FACE = {"model_type": "hugging-face-custom", "api_url": "http://hf.local/generate"}
OPENAI = {"model_type": "openai", "api_url": "", "api_key": "sk-test", "model": "gpt-4o-mini"}
ANSWER = [json.dumps({"chunk": "risposta", "type": "CHUNK"}), json.dumps({"chunk": "", "type": "END"})]


def _events(monkeypatch, *, llm_configuration, rag_configuration=None):
    """Events of ``get_agentic_rag``, and whether the turn started."""
    started = []

    async def answering_turn(**_):
        started.append(True)
        for event in ANSWER:
            yield event

    monkeypatch.setattr(entrypoint, "run_turn", answering_turn)

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.get_agentic_rag(
                rag_type="CHAT_RAG",
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
                rag_configuration=rag_configuration or {},
                llm_configuration=llm_configuration,
                guardrails_configuration={},
                opensearch_host="http://os",
                grpc_host_embedding="em:9000",
                grpc_host_datasource="ds:9000",
            )
        ]

    return asyncio.run(collect()), bool(started)


def test_a_model_without_tools_gets_one_error_and_no_turn(monkeypatch, caplog):
    with caplog.at_level(logging.WARNING):
        events, started = _events(monkeypatch, llm_configuration=HUGGING_FACE)

    assert events == [{"chunk": entrypoint.MODEL_WITHOUT_TOOLS_MESSAGE, "type": "ERROR"}]
    assert not started
    warnings = [r.getMessage() for r in caplog.records if r.levelno == logging.WARNING]
    assert any("hugging-face-custom" in message for message in warnings)


def test_with_bypass_rag_a_model_without_tools_answers(monkeypatch):
    events, started = _events(
        monkeypatch, llm_configuration=HUGGING_FACE, rag_configuration={"bypass_rag": True}
    )

    assert started
    assert events == [json.loads(event) for event in ANSWER]


def test_a_model_with_tools_answers_as_before(monkeypatch):
    events, started = _events(monkeypatch, llm_configuration=OPENAI)

    assert started
    assert events == [json.loads(event) for event in ANSWER]
