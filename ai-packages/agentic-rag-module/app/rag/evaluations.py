import json
from enum import Enum

from langchain.prompts import PromptTemplate
from langchain_core.output_parsers.string import StrOutputParser
from phoenix.client import Client
from phoenix.evals import (
    TOOL_CALLING_PROMPT_TEMPLATE,
)
from phoenix.trace import suppress_tracing
from pydantic import BaseModel, Field

from app.utils.llm import initialize_language_model
from app.utils.logger import logger


class ClassificationEnum(str, Enum):
    clear = "CLEAR"
    unclear = "UNCLEAR"


class ClassificationResponse(BaseModel):
    judgment: ClassificationEnum = Field(
        ClassificationEnum.clear, description="The response is clear or unclear."
    )
    explanation: str = Field(
        ...,
        description="detailed explanation of your reasoning to justify your choice between clear or unclear",
    )
    vote: int = Field(
        ...,
        ge=0,
        le=10,
        description="rating from 0 to 10 that identifies clarity",
    )


def rag_router_evaluation(llm, client, span_id, query, use_rag, rag_tool_description):
    try:
        tool_call = "rag_tool" if use_rag else None

        rag_router_evaluation_prompt_template = PromptTemplate.from_template(
            str(TOOL_CALLING_PROMPT_TEMPLATE)
        )

        with suppress_tracing():
            parser = StrOutputParser()

            rag_router_evaluation_chain = (
                rag_router_evaluation_prompt_template | llm | parser
            )

            rag_router_evaluation_response = rag_router_evaluation_chain.invoke(
                {
                    "question": query,
                    "tool_call": tool_call,
                    "tool_definitions": rag_tool_description,
                }
            )

            annotation = client.annotations.add_span_annotation(
                annotation_name="evaluate_rag_router",
                annotator_kind="HUMAN",
                span_id=span_id,
                label=rag_router_evaluation_response,
            )

    except Exception as e:
        logger.error(f"{e}")


def response_evaluation(
    llm,
    client,
    span_id,
    query,
    response,
):
    try:
        clarity_llm_judge_prompt = """
            In questa attività, ti verranno presentati una domanda e una risposta. Il tuo obiettivo è valutare la chiarezza della risposta nell'affrontare la domanda. Una risposta chiara è precisa, coerente e affronta direttamente la domanda senza introdurre complessità o ambiguità non necessarie. Una risposta non chiara è vaga, disorganizzata o difficile da capire, anche se potrebbe essere fattualmente corretta.

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

        classification_prompt_template = PromptTemplate.from_template(
            clarity_llm_judge_prompt
        )

        with suppress_tracing():
            classification_chain = (
                classification_prompt_template
                | llm.with_structured_output(
                    schema=ClassificationResponse,
                    include_raw=False,
                    method="function_calling",
                )
            )

            classification_response = classification_chain.invoke(
                {"query": query, "response": response}
            )

            judgment = classification_response.judgment.value
            explanation = classification_response.explanation
            vote = classification_response.vote

            classification = {
                "judgment": judgment,
                "explanation": explanation,
                "vote": vote,
            }

            annotation = client.annotations.add_span_annotation(
                annotation_name="evaluate_response",
                annotator_kind="HUMAN",
                span_id=span_id,
                label=judgment,
                score=vote,
                explanation=explanation,
            )

    except Exception as e:
        logger.error(f"{e}")


def evaluations(
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
    prompt_template = rag_configuration.get("prompt")
    rephrase_prompt_template = rag_configuration.get("rephrase_prompt")
    rerank = rag_configuration.get("rerank")
    chunk_window = rag_configuration.get("chunk_window")
    metadata = rag_configuration.get("metadata")
    rag_tool_description = rag_configuration.get("rag_tool_description")

    api_url = llm_configuration.get("api_url")
    api_key = llm_configuration.get("api_key")
    model_type = llm_configuration.get("model_type")
    model = llm_configuration.get("model")
    context_window = llm_configuration.get("context_window")
    retrieve_citations = llm_configuration.get("retrieve_citations")
    retrieve_type = llm_configuration.get("retrieve_type")
    watsonx_project_id = llm_configuration.get("watsonx_project_id")
    chat_vertex_ai_credentials = llm_configuration.get("chat_vertex_ai_credentials")
    chat_vertex_ai_model_garden = llm_configuration.get("chat_vertex_ai_model_garden")

    llm_configuration = {
        "api_url": api_url,
        "api_key": api_key,
        "model_type": model_type,
        "model": model,
        "prompt_template": prompt_template,
        "rephrase_prompt_template": rephrase_prompt_template,
        "context_window": context_window,
        "retrieve_citations": retrieve_citations,
        "rerank": rerank,
        "chunk_window": chunk_window,
        "metadata": metadata,
        "retrieve_type": retrieve_type,
        "watsonx_project_id": watsonx_project_id,
        "chat_vertex_ai_credentials": chat_vertex_ai_credentials,
        "chat_vertex_ai_model_garden": chat_vertex_ai_model_garden,
    }

    llm = initialize_language_model(llm_configuration)

    client = Client(
        base_url=arize_phoenix_base_url,
    )

    spans = client.spans.get_spans(
        project_identifier=arize_phoenix_project_name,
        limit=limit,
        start_time=start_time,
        end_time=end_time,
    )

    evaluated_spans = set()

    for span in spans:
        span_id = span.get("context").get("span_id")
        parent_id = span.get("parent_id")
        attributes = span.get("attributes")
        output_value = attributes.get("output.value")
        if output_value != "incorrect" and not parent_id:
            output_value_dict = json.loads(output_value)
            query = output_value_dict.get("current_query", "")
            context = output_value_dict.get("context", "")
            response = output_value_dict.get("response", "")
            use_rag = output_value_dict.get("use_rag", False)

            if evaluate_response and query and response:
                response_evaluation(llm, client, span_id, query, response)
                evaluated_spans.add(span_id)

            if evaluate_rag_router:
                rag_router_evaluation(
                    llm, client, span_id, query, use_rag, rag_tool_description
                )
                if span_id not in evaluated_spans:
                    evaluated_spans.add(span_id)

    return evaluated_spans
