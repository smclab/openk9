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

import json
from enum import Enum
from typing import Annotated, Any, Dict, Iterator, List, Literal, Optional

from IPython.display import Image
from langchain.prompts import ChatPromptTemplate, PromptTemplate
from langchain.schema import Document
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_core.output_parsers.string import StrOutputParser
from langgraph.checkpoint.memory import InMemorySaver
from langgraph.checkpoint.opensearch import OpenSearchSaver
from langgraph.graph import END, START, StateGraph
from langgraph.graph.message import add_messages
from opensearchpy import OpenSearch
from phoenix.evals import (
    TOOL_CALLING_PROMPT_TEMPLATE,
)
from pydantic import BaseModel, Field

from app.rag.retrievers.retriever import OpenSearchRetriever
from app.utils.llm import generate_conversation_title
from app.utils.logger import logger


class GraphState(BaseModel):
    current_query: Annotated[str, "current_query"] = Field(
        "", description="The current user query"
    )
    chat_sequence_number: Annotated[int, "chat_sequence_number"] = Field(
        1, description="Sequence number of the chat"
    )
    conversation_title: Annotated[str, "conversation_title"] = Field(
        "", description="Title of the conversation"
    )
    use_rag: Optional[bool] = Field(
        default=None, description="Whether to use RAG or not"
    )
    do_retrieve: Optional[bool] = Field(
        default=True, description="Whether to retrieve or not informations"
    )
    context: Optional[List[Document]] = Field(
        default=None, description="Retrieved documents for RAG"
    )
    response: Optional[str] = Field(default=None, description="LLM response")
    messages: Annotated[list, add_messages] = Field(
        default=[], description="messages history"
    )
    rag_router_evaluation: Optional[str] = Field(
        default=None, description="RAG Router evaluation"
    )
    response_evaluation: Optional[str] = Field(
        default=None, description="Response evaluation"
    )
    retriever_evaluation: Optional[str] = Field(
        default=None, description="Retriever evaluation"
    )
    retriever_chunks_evaluation: Optional[list[dict]] = Field(
        default=None, description="Retriever chunks evaluation"
    )
    # conversation_summary: Annotated[str, "conversation_summary"] = Field(
    #     "", description="Summary of the conversation"
    # )


class RouterResponseEnum(str, Enum):
    rag = "RAG"
    direct = "DIRECT"


class AnalyzeQuestionEnum(str, Enum):
    follow_up = "FOLLOW_UP"
    new_question = "NEW_QUESTION"


class ClassificationEnum(str, Enum):
    clear = "CLEAR"
    unclear = "UNCLEAR"


class RetrieverEvaluationEnum(str, Enum):
    relevant = "RELEVANT"
    not_relevant = "NOT_RELEVANT"


class RouterResponse(BaseModel):
    response: RouterResponseEnum = Field(
        RouterResponseEnum.direct, description="The response of the RAG router."
    )


class AnalyzeQuestion(BaseModel):
    response: AnalyzeQuestionEnum = Field(
        AnalyzeQuestionEnum.follow_up,
        description="The response of the question analysis.",
    )


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


class RetrieverEvaluationResponse(BaseModel):
    chunk_id: str = Field(
        ...,
        description="Unique identifier chunk",
        example="9460035979983399",
    )
    judgment: RetrieverEvaluationEnum = Field(
        RetrieverEvaluationEnum.relevant,
        description="The eference text contains information relevant or not relevant to answering the question.",
    )
    explanation: str = Field(
        ...,
        description="detailed explanation of your reasoning to justify your choice between relevant or not relevant",
    )
    vote: int = Field(
        ...,
        ge=0,
        le=10,
        description="rating from 0 to 10 that identifies relevance",
    )


class RetrieverEvaluationResponseList(BaseModel):
    evaluations: list[RetrieverEvaluationResponse] = Field(
        ..., description="List of evaluations"
    )


class RagGraph:
    def __init__(self, llm, configuration):
        self.llm = llm
        self.configuration = configuration
        self.rag_type = configuration.get("rag_type")
        self.tenant_id = configuration.get("tenant_id")
        self.user_id = configuration.get("user_id")
        self.chat_id = configuration.get("chat_id")
        self.chat_sequence_number = configuration.get("chat_sequence_number")
        self.chat_history = configuration.get("chat_history")
        self.opensearch_host = configuration.get("opensearch_host")
        self.open_search_client = OpenSearch(
            hosts=[self.opensearch_host],
        )
        self.config = {
            "configurable": {
                "thread_id": self.chat_id if self.chat_id else "not_logged_user"
            }
        }
        self.checkpointer = None
        self.graph = self.build_graph()

        # Image(self.graph.get_graph().draw_mermaid_png(output_file_path="./graph.png"))

    def _load_messages_from_checkpoints(self) -> List[Any]:
        """Load messages from OpenSearch checkpoints"""
        messages = {}

        try:
            states = list(self.graph.get_state_history(self.config))

            for state in states:
                query = state.values.get("current_query")
                response = state.values.get("response")
                if query and query not in messages:
                    messages[query] = HumanMessage(content=query)
                if response and response not in messages:
                    messages[response] = AIMessage(content=response)

        except Exception as e:
            logger.error(f"Error loading messages from checkpoints: {e}")

        messages.popitem()

        return list(messages.values())

    def _load_messages_from_frontend(self) -> List[Any]:
        """Load messages from frontend"""
        messages = []

        try:
            for item in self.chat_history:
                question = item["question"]
                answer = item["answer"]
                messages.append(HumanMessage(content=question))
                messages.append(AIMessage(content=answer))

        except Exception as e:
            logger.error(f"Error loading messages from frontend: {e}")

        return messages

    def _get_conversation_context(self, messages: List[Any]) -> str:
        """Extract conversation context from messages"""
        context_text = []

        for message in messages:
            if isinstance(message, HumanMessage):
                context_text.append(f"User: {message.content}")
            elif isinstance(message, AIMessage):
                context_text.append(f"Assistant: {message.content}")

        return "\n".join(context_text) if context_text else "No previous context"

    def _format_conversation_history(self, messages: List[Any]) -> str:
        """Format conversation history for LLM context"""
        formatted_history = []

        for message in messages:
            if isinstance(message, HumanMessage):
                formatted_history.append(f"User: {message.content}")
            elif isinstance(message, AIMessage):
                formatted_history.append(f"Assistant: {message.content}")

        return (
            "\n".join(formatted_history)
            if formatted_history
            else "No previous conversation"
        )

    def _summarize_chat(self, messages):
        """
        Summarizes context.
        """

        if len(messages) < 4:
            return {"conversation_summary": ""}

        conversation_context = self._get_conversation_context(messages)
        summary_prompt = """Summarize the key topics and context from this conversation concisely (2-3 sentences max). Discard irrelevant information, such as misunderstandings or off-topic queries/responses. If there are no key topics, return an empty string:\n\n"""
        summary_prompt += conversation_context

        summary_prompt += "\nBrief Summary:"
        summary_response = self.llm.invoke([SystemMessage(content=summary_prompt)])
        return summary_response.content

    def _rewrite_query(self, query, previous_query, previous_response):
        """
        Rewrites user query, using previous messages.
        """

        rephrase_prompt_template = (
            self.configuration.get("rephrase_prompt_template")
            if self.configuration.get("rephrase_prompt_template")
            else """
        Analizza la query corrente in relazione alla query precedente e alla risposta precedente, e riscrivila in una forma ottimizzata per il retrieval informativo.

        **PROTOCOLLO DI RISCITTURA:**

        ANALISI INIZIALE:
        - Determina se la query corrente è un follow-up o una nuova domanda indipendente.
        - Identifica riferimenti anaforici, termini vaghi e dipendenze dal contesto precedente.

        Se è un FOLLOW-UP:
        - Risolvi tutti i riferimenti anaforici (“questo”, “quello”, “lui”, “lei”, “esso”, “ciò”, ecc.).
        - Sostituisci termini vaghi con entità specifiche ricavate dal contesto.
        - Aggiungi le informazioni mancanti per rendere la query completamente autosufficiente.
        - Mantieni la continuità tematica con la query precedente.

        Se è una NUOVA DOMANDA:
        - Mantieni intatto il nucleo semantico della query.
        - Aumenta la specificità e la densità di parole chiave rilevanti.

        LINEE GUIDA TRANSVERSALI:
        - Ottimizza per chiarezza, precisione e recupero efficace di informazioni.
        - Usa terminologia tecnica quando pertinente.
        - Mantieni la forma interrogativa.
        - Preserva l’intento informativo primario.
        """
        )

        rewrite_query_prompt = (
            rephrase_prompt_template
            + """
        **QUERY ORIGINALE:**
        "{query}"

        **QUERY PRECEDENTE:**
        "{previous_query}"

        **RISPOSTA PRECEDENTE:**
        {previous_response}

        Rispondi ESCLUSIVAMENTE con la query riscritta.
        """
        )

        rewrite_query_prompt_template = PromptTemplate.from_template(
            rewrite_query_prompt
        )

        parser = StrOutputParser()

        rewrite_query_chain = rewrite_query_prompt_template | self.llm | parser

        response = rewrite_query_chain.invoke(
            {
                "query": query,
                "previous_query": previous_query,
                "previous_response": previous_response,
            }
        )

        return response

    def history_handler_node(self, state: GraphState) -> GraphState:
        if self.rag_type != "SIMPLE_GENERATE" and self.chat_sequence_number > 1:
            state.messages = (
                self._load_messages_from_checkpoints()
                if all([self.tenant_id, self.user_id, self.chat_id])
                else self._load_messages_from_frontend()
            )
        else:
            state.messages = []

        return state

    def analyze_and_rewrite_query_node(self, state: GraphState) -> GraphState:
        if self.rag_type != "SIMPLE_GENERATE" and self.chat_sequence_number > 1:
            query = state.current_query
            messages = state.messages
            context = state.context

            conversation_context = self._get_conversation_context(messages)

            analyze_query_prompt_template = (
                self.configuration.get("analyze_query_prompt_template")
                if self.configuration.get("analyze_query_prompt_template")
                else """
            Analizza la relazione tra la domanda corrente e la conversazione precedente.

            **Criteri di classificazione:**

            1. Rispondi "FOLLOW_UP" se la domanda corrente:
            -Si riferisce esplicitamente o implicitamente a informazioni presenti nella conversazione precedente
            -Chiede chiarimenti, approfondimenti, estensioni o applicazioni di concetti già discussi

            2. Rispondi "NEW_QUESTION" se la domanda corrente:
            - Introduce un argomento nuovo, non collegato alla conversazione precedente
            - Non contiene riferimenti diretti o indiretti a ciò che è stato detto in precedenza
            - Rappresenta un cambio di tema chiaro e netto
            """
            )

            analyze_query_prompt = (
                analyze_query_prompt_template
                + """
            **CONVERSAZIONE PRECEDENTE:**
            {context}

            **DOMANDA CORRENTE:**
            {query}
            """
            )

            analyze_query_prompt_template = PromptTemplate.from_template(
                analyze_query_prompt
            )

            analyze_query_chain = (
                analyze_query_prompt_template
                | self.llm.with_structured_output(
                    schema=AnalyzeQuestion, include_raw=False, method="function_calling"
                )
            )

            decision = analyze_query_chain.invoke(
                {"query": query, "context": conversation_context}
            )

            if decision.response.value == "FOLLOW_UP":
                queries = set()
                responses = set()

                for message in messages:
                    if isinstance(message, HumanMessage):
                        queries.add(message.content)
                    elif isinstance(message, AIMessage):
                        responses.add(message.content)

                previous_query = queries.pop()
                previous_response = responses.pop()

                rewrited_query = self._rewrite_query(
                    query, previous_query, previous_response
                )
                state.current_query = rewrited_query

        return state

    def rag_router_node(self, state: GraphState) -> GraphState:
        """Node to determine if RAG is necessary or not"""
        query = state.current_query
        messages = state.messages

        conversation_context = self._get_conversation_context(messages)

        rag_tool_description = self.configuration.get("rag_tool_description")

        rag_router_prompt = (
            rag_tool_description
            + """
        Contesto della conversazione: {context}
        Domanda corrente: {query}

        Rispondi SOLAMENTE con "RAG" or "DIRECT" senza altro testo.
        """
        )

        no_rag = self.configuration.get("no_rag")

        if no_rag:
            state.use_rag = False
        else:
            rag_router_prompt_template = PromptTemplate.from_template(rag_router_prompt)

            rag_router_chain = (
                rag_router_prompt_template
                | self.llm.with_structured_output(
                    schema=RouterResponse, include_raw=False, method="function_calling"
                )
            )

            decision = rag_router_chain.invoke(
                {"query": query, "context": conversation_context}
            )

            state.use_rag = "RAG" in decision.response.value

        return state

    def rag_router_evaluation_node(self, state: GraphState) -> GraphState:
        query = state.current_query
        use_rag = state.use_rag
        rag_tool_description = self.configuration.get("rag_tool_description")
        tool_call = "rag_tool" if use_rag else None

        rag_router_evaluation_prompt_template = PromptTemplate.from_template(
            str(TOOL_CALLING_PROMPT_TEMPLATE)
        )

        parser = StrOutputParser()

        rag_router_evaluation_chain = (
            rag_router_evaluation_prompt_template | self.llm | parser
        )

        rag_router_evaluation_response = rag_router_evaluation_chain.invoke(
            {
                "question": query,
                "tool_call": tool_call,
                "tool_definitions": rag_tool_description,
            }
        )

        state.rag_router_evaluation = rag_router_evaluation_response

        return state

    def route_decision(
        self, state: GraphState
    ) -> Literal["opensearch_retriever", "llm_response"]:
        """Separate function for conditional routing decision"""
        if state.use_rag:
            return "opensearch_retriever"
        else:
            return "llm_response"

    def opensearch_retriever_node(self, state: GraphState) -> GraphState:
        """Opensearch RAG processing"""
        if not state.use_rag:
            state.context = []
            return state

        query = state.current_query

        retriever = OpenSearchRetriever(
            search_query=self.configuration.get("search_query"),
            search_text=query,
            rerank=self.configuration.get("rerank"),
            chunk_window=self.configuration.get("chunk_window"),
            range_values=self.configuration.get("range_values"),
            after_key=self.configuration.get("after_key"),
            suggest_keyword=self.configuration.get("suggest_keyword"),
            suggestion_category_id=self.configuration.get("suggestion_category_id"),
            virtual_host=self.configuration.get("virtual_host"),
            jwt=self.configuration.get("jwt"),
            extra=self.configuration.get("extra"),
            sort=self.configuration.get("sort"),
            sort_after_key=self.configuration.get("sort_after_key"),
            language=self.configuration.get("language"),
            context_window=self.configuration.get("context_window"),
            metadata=self.configuration.get("metadata"),
            retrieve_type=self.configuration.get("retrieve_type"),
            opensearch_host=self.configuration.get("opensearch_host"),
            grpc_host=self.configuration.get("grpc_host"),
        )

        retrieved_docs = retriever.invoke(query)
        state.context = retrieved_docs

        return state

    def opensearch_retriever_evaluation_node(self, state: GraphState) -> GraphState:
        if state.do_retrieve:
            retriever_evaluation_prompt = """
                    Stai confrontando un testo di riferimento con una domanda per determinare se il testo di riferimento contiene informazioni rilevanti per rispondere alla domanda. Ecco i dati:

                    [BEGIN DATA]
                    ************
                    [Domanda]: {query}
                    ************
                    [Testo di riferimento]: {context}
                    [END DATA]

                    Dopo la tua analisi, fornisci il seguente output strutturato in tre parti, in questo ordine e separate da una riga vuota:

                    1.  **judgment:** Una singola parola: "RELEVANT" o "NOT_RELEVANT".
                    2.  **vote:** Un punteggio numerico intero da 0 a 10 che quantifica il grado di rilevanza, dove 0 indica assenza totale di rilevanza e 10 indica rilevanza perfetta (ovvero il testo di riferimento risponde completamente e direttamente alla domanda).
                    3.  **explanation:** Una spiegazione dettagliata del tuo ragionamento. Analizza se il testo di riferimento tratta l'argomento o i concetti chiave sollevati dalla domanda. Spiega perché le informazioni sono considerate pertinenti o meno, indicando eventuali punti di contatto parziali o informazioni mancanti. La spiegazione deve essere autonoma e non deve ripetere i termini "GIUDIZIO" o "VOTO". Evita di anticipare il giudizio finale all'inizio della spiegazione.
                    """

            query = state.current_query
            context = state.context

            context_text = "\n\n".join([doc.page_content for doc in context])

            retriever_evaluation_prompt_template = PromptTemplate.from_template(
                retriever_evaluation_prompt
            )

            retriever_evaluation_chain = (
                retriever_evaluation_prompt_template
                | self.llm.with_structured_output(
                    schema=RetrieverEvaluationResponse,
                    include_raw=False,
                    method="function_calling",
                )
            )

            classification_response = retriever_evaluation_chain.invoke(
                {"query": query, "context": context_text}
            )

            state.retriever_evaluation = classification_response.judgment.value

        return state

    def opensearch_retriever_chunks_evaluation_node(
        self, state: GraphState
    ) -> GraphState:
        if state.do_retrieve:
            query = state.current_query
            context = state.context

            chunks = []
            evaluations = []

            for chunk_number, document in enumerate(context, start=1):
                chunk = {
                    "chunk_number": chunk_number,
                    "chunk_id": document.metadata["document_id"],
                    "chunk_content": document.page_content,
                }

                chunks.append(chunk)

            retriever_evaluation_prompt = """
                        Sei un valutatore che confronta una domanda con una lista di segmenti di testo (chunks) per determinare se ogni chunk contiene informazioni rilevanti per rispondere alla domanda.
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

            retriever_evaluation_prompt_template = PromptTemplate.from_template(
                retriever_evaluation_prompt
            )

            retriever_evaluation_chain = (
                retriever_evaluation_prompt_template
                | self.llm.with_structured_output(
                    schema=RetrieverEvaluationResponseList,
                    include_raw=False,
                    method="function_calling",
                )
            )

            classification_response = retriever_evaluation_chain.invoke(
                {
                    "query": query,
                    "chunks": chunks,
                }
            )

            for response in classification_response.evaluations:
                evaluation = {
                    "chunk_id": response.chunk_id,
                    "judgment": response.judgment.value,
                    "explanation": response.explanation,
                    "vote": response.vote,
                }

                evaluations.append(evaluation)

            state.retriever_chunks_evaluation = evaluations

        return state

    def opensearch_retriever_chunks_evaluation_for_node(
        self, state: GraphState
    ) -> GraphState:
        if state.do_retrieve:
            query = state.current_query
            context = state.context

            chunks = []
            total_chunks = len(chunks)

            evaluations = []
            for chunk_number, document in enumerate(context, start=1):
                retriever_evaluation_prompt = """
                        Stai confrontando un testo di riferimento con una domanda per determinare se il testo di riferimento contiene informazioni rilevanti per rispondere alla domanda. **Ogni segmento (chunk) del testo di riferimento deve essere valutato INDIPENDENTEMENTE dagli altri.** Ecco i dati:

                        [BEGIN DATA]
                        ************
                        [Domanda]: {query}
                        ************
                        Testo di riferimento - Chunk {chunk_number} di {total_chunks}]: {context_chunk} con chunk_id {chunk_id}
                        [END DATA]

                        Dopo la tua analisi, fornisci il seguente output strutturato in tre parti, in questo ordine e separate da una riga vuota:

                        1.  **chunk_id**: identificativo del chunk, presente in ogni chunk.
                        2.  **judgment:** Una singola parola: "RELEVANT" o "NOT_RELEVANT".
                        3.  **vote:** Un punteggio numerico intero da 0 a 10 che quantifica il grado di rilevanza di QUESTO SINGOLO CHUNK, dove 0 indica assenza totale di rilevanza e 10 indica che questo chunk, da solo, contiene informazioni che rispondono completamente e direttamente alla domanda.
                        4.  **explanation:** Una spiegazione dettagliata del tuo ragionamento. Analizza se QUESTO SPECIFICO CHUNK tratta l'argomento o i concetti chiave sollevati dalla domanda. Spiega perché le informazioni in questo segmento sono considerate pertinenti o meno, indicando eventuali punti di contatto parziali o informazioni mancanti. La spiegazione deve essere autonoma, riferirsi esplicitamente al contenuto di questo chunk e non deve ripetere i termini "GIUDIZIO" o "VOTO". Evita di anticipare il giudizio finale all'inizio della spiegazione.
                        """

                retriever_evaluation_prompt_template = PromptTemplate.from_template(
                    retriever_evaluation_prompt
                )

                retriever_evaluation_chain = (
                    retriever_evaluation_prompt_template
                    | self.llm.with_structured_output(
                        schema=RetrieverEvaluationResponse,
                        include_raw=False,
                        method="function_calling",
                    )
                )

                classification_response = retriever_evaluation_chain.invoke(
                    {
                        "query": query,
                        "chunk_number": chunk_number,
                        "total_chunks": total_chunks,
                        "context_chunk": document.page_content,
                        "chunk_id": document.metadata["document_id"],
                    }
                )

                evaluation = {
                    "chunk_id": classification_response.chunk_id,
                    "judgment": classification_response.judgment.value,
                    "explanation": classification_response.explanation,
                    "vote": classification_response.vote,
                }

                evaluations.append(evaluation)

            state.retriever_chunks_evaluation = evaluations

        return state

    def llm_response_node(self, state: GraphState) -> GraphState:
        """LLM response node"""
        query = state.current_query
        context = state.context or []
        messages = state.messages

        conversation_history = self._format_conversation_history(messages)

        if state.use_rag and context:
            prompt = self.configuration.get("prompt_template")
            context_text = "\n\n".join([doc.page_content for doc in context])

            rag_prompt = ChatPromptTemplate.from_template(
                prompt
                + """
                Use the following context to answer the question. If you don't know the answer based on the context, just say so.
                
                Previous conversation: {history}
                
                Context: {context}
                
                Question: {query}
                
                Answer:
                """
            )

            rag_chain = rag_prompt | self.llm
            response = rag_chain.invoke(
                {
                    "query": query,
                    "context": context_text,
                    "history": conversation_history,
                }
            )
        else:
            prompt_no_rag = self.configuration.get("prompt_no_rag")

            direct_prompt = ChatPromptTemplate.from_template(
                prompt_no_rag
                + """
                Previous conversation: {history}
                
                Current question: {query}
                
                Answer:
                """
            )
            direct_chain = direct_prompt | self.llm
            response = direct_chain.invoke(
                {"query": query, "history": conversation_history}
            )

        state.response = response.content
        return state

    def history_saver_node(self, state: GraphState) -> GraphState:
        """Update messages with the latest query and response"""
        if self.rag_type != "SIMPLE_GENERATE":
            state.messages.append(HumanMessage(content=state.current_query))
            state.messages.append(AIMessage(content=state.response))

            if all(
                [
                    self.chat_sequence_number == 1,
                    self.tenant_id,
                    self.user_id,
                    self.chat_id,
                ]
            ):
                conversation_title = generate_conversation_title(
                    self.llm, state.current_query, state.response
                )
                state.conversation_title = conversation_title.strip('"')

        return state

    def response_evaluation_node(self, state: GraphState) -> GraphState:
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

        query = state.current_query
        response = state.response

        classification_prompt_template = PromptTemplate.from_template(
            clarity_llm_judge_prompt
        )

        classification_chain = (
            classification_prompt_template
            | self.llm.with_structured_output(
                schema=ClassificationResponse,
                include_raw=False,
                method="function_calling",
            )
        )

        classification_response = classification_chain.invoke(
            {"query": query, "response": response}
        )

        state.response_evaluation = classification_response.judgment.value

        return state

    def build_graph(self):
        workflow = StateGraph(GraphState)

        if self.configuration.get("enable_real_time_evaluation"):
            workflow.add_node("history_handler", self.history_handler_node)
            workflow.add_node(
                "analyze_and_rewrite_query", self.analyze_and_rewrite_query_node
            )
            workflow.add_node("rag_router", self.rag_router_node)
            workflow.add_node("rag_router_evaluation", self.rag_router_evaluation_node)
            workflow.add_node("opensearch_retriever", self.opensearch_retriever_node)
            workflow.add_node(
                "opensearch_retriever_evaluation",
                self.opensearch_retriever_chunks_evaluation_for_node,
            )
            workflow.add_node("llm_response", self.llm_response_node)
            workflow.add_node("history_saver", self.history_saver_node)
            workflow.add_node("response_evaluation", self.response_evaluation_node)

            workflow.set_entry_point("history_handler")

            workflow.add_conditional_edges(
                "rag_router",
                self.route_decision,
                {
                    "opensearch_retriever": "opensearch_retriever",
                    "llm_response": "llm_response",
                },
            )

            workflow.add_edge("history_handler", "analyze_and_rewrite_query")
            workflow.add_edge("analyze_and_rewrite_query", "rag_router")
            workflow.add_edge("opensearch_retriever", "opensearch_retriever_evaluation")
            workflow.add_edge("opensearch_retriever_evaluation", "llm_response")
            workflow.add_edge("llm_response", "rag_router_evaluation")
            workflow.add_edge("rag_router_evaluation", "history_saver")
            workflow.add_edge("history_saver", "response_evaluation")
            workflow.add_edge("response_evaluation", END)
        else:
            workflow.add_node("history_handler", self.history_handler_node)
            workflow.add_node(
                "analyze_and_rewrite_query", self.analyze_and_rewrite_query_node
            )
            workflow.add_node("rag_router", self.rag_router_node)
            workflow.add_node("opensearch_retriever", self.opensearch_retriever_node)
            workflow.add_node("llm_response", self.llm_response_node)
            workflow.add_node("history_saver", self.history_saver_node)

            workflow.set_entry_point("history_handler")

            workflow.add_conditional_edges(
                "rag_router",
                self.route_decision,
                {
                    "opensearch_retriever": "opensearch_retriever",
                    "llm_response": "llm_response",
                },
            )

            workflow.add_edge("history_handler", "analyze_and_rewrite_query")
            workflow.add_edge("analyze_and_rewrite_query", "rag_router")
            workflow.add_edge("opensearch_retriever", "llm_response")
            workflow.add_edge("llm_response", "history_saver")
            workflow.add_edge("history_saver", END)

        if self.tenant_id and self.user_id and self.chat_id:
            checkpoint_index_name = f"{self.tenant_id}-{self.user_id}"

            self.checkpointer = OpenSearchSaver(
                client=self.open_search_client,
                checkpoint_index_name=checkpoint_index_name,
                writes_index_name=self.chat_id,
            )
        else:
            self.checkpointer = InMemorySaver()

        return workflow.compile(checkpointer=self.checkpointer)

    def invoke(self, query: str):
        """Invoke the graph with a query"""
        return self.graph.invoke(
            {
                "current_query": query,
                "chat_sequence_number": self.chat_sequence_number,
            },
            config=self.config,
        )

    def _stream_title(self, title: str) -> Iterator[Dict[str, Any]]:
        yield json.dumps(
            {
                "chunk": title,
                "type": "TITLE",
            }
        )

    def _stream_documents(self, documents: List[Document]) -> Iterator[Dict[str, Any]]:
        documents_id = set()
        for document in documents:
            metadata = document.metadata
            if (
                document_id := metadata.get("document_id")
            ) and document_id not in documents_id:
                documents_id.add(document_id)
                score = metadata.get("score")
                title = metadata.get("title")
                url = metadata.get("url")

                yield json.dumps(
                    {
                        "chunk": {
                            "score": score,
                            "title": title,
                            "url": url,
                            "citations": [],
                        },
                        "type": "DOCUMENT",
                    }
                )

    def stream(self, query: str) -> Iterator[Dict[str, Any]]:
        result_answer = ""

        for chunk, metadata in self.graph.stream(
            {
                "current_query": query,
                "chat_sequence_number": self.chat_sequence_number,
            },
            config=self.config,
            stream_mode="messages",
        ):
            if metadata["langgraph_node"] == "llm_response" and chunk.content:
                if result_answer == "":
                    yield json.dumps({"chunk": "", "type": "START"})
                result_answer += chunk.content
                yield json.dumps({"chunk": chunk.content, "type": "CHUNK"})

        if last_state := self.graph.get_state(self.config):
            if all(
                [
                    self.chat_sequence_number == 1,
                    self.tenant_id,
                    self.user_id,
                    self.chat_id,
                    conversation_title := last_state.values.get("conversation_title"),
                ]
            ):
                yield from self._stream_title(conversation_title)

            if self.rag_type != "SIMPLE_GENERATE" and (
                documents := last_state.values.get("context")
            ):
                yield from self._stream_documents(documents)

        info = {
            "chain": "agentic_rag",
            "user_id": self.user_id,
            "chat_id": self.chat_id,
            "answer": result_answer[:200] + "...",
        }
        logger.info(json.dumps(info))

        yield json.dumps({"chunk": "", "type": "END"})
