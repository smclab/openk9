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
I due provider custom dentro un ``pydantic_ai.Agent`` vero, non chiamati a mano.

``test_custom_models.py`` verifica il payload che i modelli mettono sul filo. Questi test invece
fanno girare il **loop dell'Agent**, che e' dove i modelli custom si rompono davvero: la
risoluzione del profilo decide la modalita' di structured output, ``prepare_messages`` riscrive i
system prompt non iniziali, e il round-trip di una tool call rimanda al modello messaggi che il
modello stesso ha prodotto — se il mapping non e' simmetrico il secondo giro parte storto.

Nessuna rete: l'endpoint HF e' un ``httpx.MockTransport``, watsonx un finto ``ModelInference``.
"""

import asyncio
import json

import httpx
from pydantic import BaseModel
from pydantic_ai import Agent

from app.rag.agentic.custom_hugging_face_model import CustomHuggingFaceModel
from app.rag.agentic.watsonx_model import WatsonxModel


class Answer(BaseModel):
    """Output strutturato minimo, nella forma di ``OrchestratorOutput`` (campo testuale + stato)."""

    message: str
    status: str


# --------------------------------------------------------------------------------------------
# hugging-face-custom
# --------------------------------------------------------------------------------------------


def test_hugging_face_agent_run_returns_plain_text():
    """Il caso base del RAG: agent con output testuale, risposta dell'endpoint bespoke."""
    prompts = []

    def handler(request):
        prompts.append(json.loads(request.content)["input_str"])
        return httpx.Response(200, text="Il RUP e' Mario Rossi.")

    model = CustomHuggingFaceModel(
        "http://hf.local/generate",
        http_client=httpx.AsyncClient(transport=httpx.MockTransport(handler)),
    )
    agent = Agent(model, instructions="Rispondi in italiano.")

    result = asyncio.run(agent.run("Chi e' il RUP?"))

    assert result.output == "Il RUP e' Mario Rossi."
    # Le istruzioni dell'Agent devono finire nel prompt: in PydanticAI non stanno nel messaggio
    # utente, e l'endpoint riceve una stringa sola.
    assert "Rispondi in italiano." in prompts[0]
    assert "User: Chi e' il RUP?" in prompts[0]


def test_hugging_face_agent_structured_output_goes_through_the_prompt():
    """Con ``output_type`` la run deve passare in modalita' prompted: lo schema JSON finisce nel
    prompt e la risposta dell'endpoint viene validata come ``Answer``.

    L'assert sullo schema e' il punto: col profilo di default PydanticAI userebbe la modalita' tool
    e **non direbbe mai al modello che forma deve avere la risposta**. La run finirebbe comunque
    verde contro un endpoint finto che indovina il JSON, e fallirebbe contro un modello vero."""
    prompts = []

    def handler(request):
        prompts.append(json.loads(request.content)["input_str"])
        return httpx.Response(
            200, text=json.dumps({"message": "Il RUP e' Mario Rossi.", "status": "ok"})
        )

    model = CustomHuggingFaceModel(
        "http://hf.local/generate",
        http_client=httpx.AsyncClient(transport=httpx.MockTransport(handler)),
    )
    agent = Agent(model, output_type=Answer)

    result = asyncio.run(agent.run("Chi e' il RUP?"))

    assert result.output == Answer(message="Il RUP e' Mario Rossi.", status="ok")
    # lo schema arriva al modello via prompt, non via tool definition
    assert "message" in prompts[0] and "status" in prompts[0]


def test_watsonx_merges_model_level_settings():
    """``prepare_request`` fonde le ``settings`` del Model con quelle della richiesta. Saltarlo
    (com'e' successo nella prima stesura) rompe in silenzio: niente merge delle settings, e
    soprattutto l'output mode resta 'auto', quindi la structured output non viene mai configurata."""
    inference = ScriptedInference([_text_payload("ok")])
    model = WatsonxModel(
        "ibm/granite-13b-chat-v2",
        api_url="https://eu-de.ml.cloud.ibm.com",
        api_key="apikey",
        project_id="project-1",
        inference=inference,
        settings={"temperature": 0.3},
    )

    asyncio.run(Agent(model).run("Chi e' il RUP?"))

    assert inference.calls[0]["params"]["temperature"] == 0.3


def test_hugging_face_agent_streams_partial_text():
    """``run_stream``: i pezzi escono mentre l'endpoint genera, non a risposta completa."""

    def handler(request):
        return httpx.Response(
            200, stream=httpx.ByteStream(b"Il RUP " + b"e' Mario " + b"Rossi.")
        )

    async def scenario():
        model = CustomHuggingFaceModel(
            "http://hf.local/generate",
            http_client=httpx.AsyncClient(transport=httpx.MockTransport(handler)),
        )
        agent = Agent(model)
        async with agent.run_stream("Chi e' il RUP?") as stream:
            return [piece async for piece in stream.stream_text(delta=True)]

    pieces = asyncio.run(scenario())

    assert "".join(pieces) == "Il RUP e' Mario Rossi."


# --------------------------------------------------------------------------------------------
# watsonx
# --------------------------------------------------------------------------------------------


class ScriptedInference:
    """Finto ``ModelInference``: una risposta per giro, e registra cosa ha ricevuto."""

    def __init__(self, payloads):
        self._payloads = list(payloads)
        self.calls = []

    async def achat(self, **kwargs):
        self.calls.append(kwargs)
        return self._payloads.pop(0)

    async def achat_stream(self, **kwargs):
        self.calls.append(kwargs)
        chunks = self._payloads.pop(0)

        async def generator():
            for chunk in chunks:
                yield chunk

        return generator()


def _watsonx(inference):
    return WatsonxModel(
        "ibm/granite-13b-chat-v2",
        api_url="https://eu-de.ml.cloud.ibm.com",
        api_key="apikey",
        project_id="project-1",
        inference=inference,
    )


def _text_payload(content):
    return {"choices": [{"message": {"content": content}, "finish_reason": "stop"}]}


def test_watsonx_agent_completes_a_tool_round_trip():
    """Il giro che conta per l'orchestratore: il modello chiede un tool, l'Agent lo esegue e
    rimanda l'esito. Il secondo ``achat`` deve ricevere l'assistant con ``tool_calls`` e il
    ``role: tool`` correlato — cioe' il mapping in uscita e quello in entrata devono combaciare."""
    inference = ScriptedInference(
        [
            {
                "choices": [
                    {
                        "message": {
                            "content": None,
                            "tool_calls": [
                                {
                                    "id": "call-1",
                                    "function": {
                                        "name": "cerca_rup",
                                        "arguments": '{"query": "RUP"}',
                                    },
                                }
                            ],
                        },
                        "finish_reason": "tool_calls",
                    }
                ]
            },
            _text_payload("Il RUP e' Mario Rossi."),
        ]
    )
    agent = Agent(_watsonx(inference))

    @agent.tool_plain
    def cerca_rup(query: str) -> str:
        """Cerca il RUP nei documenti."""
        return "Mario Rossi"

    result = asyncio.run(agent.run("Chi e' il RUP?"))

    assert result.output == "Il RUP e' Mario Rossi."

    # il secondo giro riceve la conversazione completa nella forma della Chat API watsonx
    second_turn = inference.calls[1]["messages"]
    assistant = next(m for m in second_turn if m["role"] == "assistant")
    tool_result = next(m for m in second_turn if m["role"] == "tool")
    assert assistant["tool_calls"][0]["function"]["name"] == "cerca_rup"
    assert tool_result["tool_call_id"] == assistant["tool_calls"][0]["id"]
    assert tool_result["content"] == "Mario Rossi"


def test_watsonx_agent_structured_output_uses_tool_mode():
    """A differenza di HF, watsonx fa tool calling: la structured output resta in modalita' tool
    (nessun JSON chiesto nel prompt) e l'output tool arriva fra i ``tools`` della richiesta."""
    inference = ScriptedInference(
        [
            {
                "choices": [
                    {
                        "message": {
                            "content": None,
                            "tool_calls": [
                                {
                                    "id": "call-out",
                                    "function": {
                                        "name": "final_result",
                                        "arguments": json.dumps(
                                            {"message": "Mario Rossi", "status": "ok"}
                                        ),
                                    },
                                }
                            ],
                        },
                        "finish_reason": "tool_calls",
                    }
                ]
            }
        ]
    )
    agent = Agent(_watsonx(inference), output_type=Answer)

    result = asyncio.run(agent.run("Chi e' il RUP?"))

    assert result.output == Answer(message="Mario Rossi", status="ok")
    assert [t["function"]["name"] for t in inference.calls[0]["tools"]] == ["final_result"]


def test_watsonx_agent_streams_partial_text():
    """``run_stream`` sui chunk della Chat API."""
    inference = ScriptedInference(
        [
            [
                {"choices": [{"delta": {"content": "Il RUP "}}]},
                {"choices": [{"delta": {"content": "e' Mario Rossi."}}]},
                {"choices": [{"delta": {}, "finish_reason": "stop"}]},
            ]
        ]
    )

    async def scenario():
        agent = Agent(_watsonx(inference))
        async with agent.run_stream("Chi e' il RUP?") as stream:
            return [piece async for piece in stream.stream_text(delta=True)]

    pieces = asyncio.run(scenario())

    assert "".join(pieces) == "Il RUP e' Mario Rossi."
