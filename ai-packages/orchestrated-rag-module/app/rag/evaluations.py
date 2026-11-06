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
Offline evaluation of the turns traced on Phoenix: the ``/api/rag/evaluate`` route.

Every turn leaves a root span, :data:`TURN_SPAN_NAME`, whose ``output.value`` is
the JSON agentic-rag-module gave its root span: ``current_query``,
``response``, ``context`` (the documents delivered to the orchestrator, as
``metadata.document_id`` and ``page_content``) and ``use_rag``. The route reads
those spans back and annotates them with three judges, as in
agentic-rag-module: the routing, the documents, the clarity of the answer.

The judges run on the model of the tenant, with tracing suppressed, so that
their own calls do not become spans to evaluate.
"""

import json
import time
from enum import Enum

from opentelemetry import context as otel_context
from opentelemetry import trace
from phoenix.client import Client
from phoenix.trace import suppress_tracing
from pydantic import BaseModel, Field
from pydantic_ai import Agent

from app.rag.agentic.evaluation import router_prompt
from app.rag.agentic.models_factory import build_model
from app.utils.logger import get_logger

logger = get_logger(__name__)

TURN_SPAN_NAME = "orchestrated_rag_turn"


def record_turn(*, started_ns: int, question: str, response: str, deps, used_rag: bool) -> None:
    """Leave the root span of a turn, for the offline evaluation. A no-op without tracing."""
    span = trace.get_tracer(__name__).start_span(
        TURN_SPAN_NAME, context=otel_context.Context(), start_time=started_ns
    )
    context = [
        {
            "metadata": {"document_id": document_id},
            "page_content": deps.discovered.get(document_id, {}).get("content", ""),
        }
        for document_id in deps.delivered_ids
    ]
    span.set_attribute("openinference.span.kind", "CHAIN")
    span.set_attribute("input.value", question)
    span.set_attribute(
        "output.value",
        json.dumps(
            {
                "current_query": question,
                "response": response,
                "context": context,
                "use_rag": used_rag,
            }
        ),
    )
    span.set_attribute("output.mime_type", "application/json")
    span.end(end_time=time.time_ns())


class ClassificationEnum(str, Enum):
    clear = "CLEAR"
    unclear = "UNCLEAR"


class ClassificationResponse(BaseModel):
    judgment: ClassificationEnum = Field(
        ClassificationEnum.clear, description="The response is clear or unclear."
    )
    explanation: str = Field(
        ...,
        description="detailed explanation of your reasoning to justify your choice between "
        "clear or unclear",
    )
    vote: int = Field(..., ge=0, le=10, description="rating from 0 to 10 that identifies clarity")


class RetrieverEvaluationEnum(str, Enum):
    relevant = "RELEVANT"
    not_relevant = "NOT_RELEVANT"


class RetrieverEvaluationResponse(BaseModel):
    chunk_id: str = Field(..., description="Unique identifier chunk")
    judgment: RetrieverEvaluationEnum = Field(
        RetrieverEvaluationEnum.relevant,
        description="The reference text contains information relevant or not relevant to "
        "answering the question.",
    )
    explanation: str = Field(
        ...,
        description="detailed explanation of your reasoning to justify your choice between "
        "relevant or not relevant",
    )
    vote: int = Field(..., ge=0, le=10, description="rating from 0 to 10 that identifies relevance")


class RetrieverEvaluationResponseList(BaseModel):
    evaluations: list[RetrieverEvaluationResponse] = Field(
        ..., description="List of evaluations"
    )


CLARITY_PROMPT = """In questa attività, ti verranno presentati una domanda e una risposta. Il tuo obiettivo è valutare la chiarezza della risposta nell'affrontare la domanda. Una risposta chiara è precisa, coerente e affronta direttamente la domanda senza introdurre complessità o ambiguità non necessarie. Una risposta non chiara è vaga, disorganizzata o difficile da capire, anche se potrebbe essere fattualmente corretta.

Dopo aver analizzato la domanda e la risposta, devi fornire il seguente output strutturato in tre parti, in questo ordine e separati da una riga vuota:

1. **judgment:** Una singola parola: "CLEAR" o "UNCLEAR".
2. **vote:** Un punteggio numerico intero da 0 a 10 che quantifica il grado di chiarezza, dove 0 indica assenza totale di chiarezza e 10 indica chiarezza perfetta.
3. **explanation:** Una spiegazione dettagliata del tuo ragionamento. Analizza come la risposta soddisfi o meno i criteri di chiarezza (precisione, coerenza, struttura, linguaggio, pertinenza). La spiegazione deve essere autonoma e non deve ripetere i termini "GIUDIZIO" o "VOTO". Evita di anticipare il giudizio finale all'inizio della spiegazione.

Considera attentamente la domanda e la risposta prima di determinare la tua valutazione.

[BEGIN DATA]
Domanda: {query}
Risposta: {response}
[END DATA]
"""

CHUNKS_PROMPT = """Sei un valutatore che confronta una domanda con una lista di segmenti di testo (chunks) per determinare se ogni chunk contiene informazioni rilevanti per rispondere alla domanda.
Dati da analizzare:

[BEGIN DATA]
[Domanda]: {query}
Testo di riferimento - {chunks}
[END DATA]

Istruzioni precise:
1. Valuta **OGNI CHUNK IN MODO INDIPENDENTE** dagli altri. Non usare informazioni presenti in altri chunk per giudicare questo chunk.
2. Per ciascun chunk restituisci un oggetto con quattro campi nell'ordine esatto: "chunk_id", "judgment", "vote", "explanation".
- "chunk_id": identificativo del chunk, presente in ogni elemento della lista.
- "judgment": una stringa, **solo** "RELEVANT" o "NOT_RELEVANT".
- "vote": **solo** un intero tra 0 e 10 (0 = per niente rilevante, 10 = risponde completamente e direttamente alla domanda da solo).
- "explanation": una spiegazione autonoma e dettagliata che motiva il voto per **questo singolo chunk**. Deve:
    - riferirsi esplicitamente al contenuto di `chunk_content`;
    - spiegare quali elementi del chunk sono (o non sono) collegati alla domanda;
    - indicare eventuali informazioni mancanti o punti di contatto parziali;
    - **non** iniziare riportando il giudizio o il voto e **non** usare le parole "GIUDIZIO" o "VOTO" dentro l'explanation;
    - essere autosufficiente (chi legge solo questa explanation deve capire perché quel voto è stato dato).
3. Mantieni output **valid JSON** che sia una lista (array) con un oggetto per ogni chunk, nello **stesso ordine** dei chunk in input.
4. Non aggiungere testo, commenti o metadati extra: **OUTPUT SOLO IL JSON**.

Requisiti formali ricapitolati:
- "vote" deve essere un intero 0-10.
- "judgment" deve essere esattamente "RELEVANT" o "NOT_RELEVANT".
- L'array restituito deve contenere esattamente un elemento per chunk, nello stesso ordine.
- Nessun testo fuori dal JSON.

Adesso valuta i chunk forniti usando queste regole.
"""


async def _judge(model, output_type, prompt):
    with suppress_tracing():
        agent = Agent(model, name="evaluation_judge", output_type=output_type)
        return (await agent.run(prompt)).output


async def rag_router_evaluation(model, client, span_id, query, use_rag, rag_tool_description):
    try:
        label = await _judge(model, str, router_prompt(query, use_rag, rag_tool_description))
        client.spans.add_span_annotation(
            annotation_name="evaluate_rag_router",
            annotator_kind="HUMAN",
            span_id=span_id,
            label=label,
        )
        return True
    except Exception as e:  # noqa: BLE001
        logger.error(f"[evaluate] rag router of span {span_id}: {e}")
        return False


async def response_evaluation(model, client, span_id, query, response):
    try:
        verdict = await _judge(
            model, ClassificationResponse, CLARITY_PROMPT.format(query=query, response=response)
        )
        client.spans.add_span_annotation(
            annotation_name="evaluate_response",
            annotator_kind="HUMAN",
            span_id=span_id,
            label=verdict.judgment.value,
            score=verdict.vote,
            explanation=verdict.explanation,
        )
        return True
    except Exception as e:  # noqa: BLE001
        logger.error(f"[evaluate] response of span {span_id}: {e}")
        return False


async def retriever_evaluation(model, client, span_id, query, context):
    try:
        chunks = [
            {
                "chunk_number": number,
                "chunk_id": document.get("metadata", {}).get("document_id"),
                "chunk_content": document.get("page_content"),
            }
            for number, document in enumerate(context, 1)
        ]
        verdicts = await _judge(
            model, RetrieverEvaluationResponseList, CHUNKS_PROMPT.format(query=query, chunks=chunks)
        )
        for verdict in verdicts.evaluations:
            client.spans.add_span_annotation(
                annotation_name=f"evaluation for chunk {verdict.chunk_id}",
                span_id=span_id,
                label=verdict.judgment.value,
                score=verdict.vote,
                explanation=verdict.explanation,
            )
        return True
    except Exception as e:  # noqa: BLE001
        logger.error(f"[evaluate] retriever of span {span_id}: {e}")
        return False


def _turn(span) -> dict:
    """The output of a turn span, or an empty dict for every other span."""
    if span.get("name") != TURN_SPAN_NAME or span.get("parent_id"):
        return {}
    try:
        return json.loads((span.get("attributes") or {}).get("output.value") or "")
    except ValueError:
        return {}


async def evaluations(
    rag_configuration,
    llm_configuration,
    arize_phoenix_project_name,
    arize_phoenix_base_url,
    limit,
    start_time,
    end_time,
    evaluate_rag_router,
    evaluate_retriever,
    evaluate_response,
):
    """Annotate the turn spans of the period with the judges asked for; return their ids."""
    model = build_model(llm_configuration)
    client = Client(base_url=arize_phoenix_base_url)
    spans = client.spans.get_spans(
        project_identifier=arize_phoenix_project_name,
        limit=limit,
        start_time=start_time,
        end_time=end_time,
    )
    evaluated_spans = set()
    for span in spans:
        turn = _turn(span)
        if not turn:
            continue
        span_id = span.get("context", {}).get("span_id")
        query = turn.get("current_query", "")
        response = turn.get("response", "")
        context = turn.get("context") or []
        # a span counts as evaluated only when one of its annotations reached Phoenix
        if evaluate_response and query and response:
            if await response_evaluation(model, client, span_id, query, response):
                evaluated_spans.add(span_id)
        if evaluate_rag_router:
            if await rag_router_evaluation(
                model,
                client,
                span_id,
                query,
                turn.get("use_rag", False),
                rag_configuration.get("rag_tool_description"),
            ):
                evaluated_spans.add(span_id)
        if evaluate_retriever and context:
            if await retriever_evaluation(model, client, span_id, query, context):
                evaluated_spans.add(span_id)
    return evaluated_spans
