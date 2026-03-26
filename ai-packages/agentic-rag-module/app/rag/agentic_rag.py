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

from app.external_services.grpc.grpc_client import (
    get_embedding_model_configuration,
)
from app.models import models
from app.rag.retrievers.domain_documents_retriever import (
    OpenSearchDomainDocumentsRetriever,
)
from app.rag.retrievers.guardrail_documents_retriever import (
    OpenSearchGuardrailDocumentsRetriever,
)
from app.rag.retrievers.retriever import OpenSearchRetriever
from app.rag.retrievers.uploaded_documents_retriever import (
    OpenSearchUploadedDocumentsRetriever,
)
from app.utils.authentication import unauthorized_response
from app.utils.guardrails import GuardrailType, initialize_guardrail
from app.utils.llm import generate_conversation_title
from app.utils.logger import logger
from IPython.display import Image
from langchain.output_parsers import PydanticOutputParser
from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_core.output_parsers.string import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate, PromptTemplate
from langgraph.checkpoint.memory import InMemorySaver
from langgraph.checkpoint.opensearch import OpenSearchSaver
from langgraph.graph import END, START, StateGraph
from langgraph.graph.message import add_messages
from opensearchpy import OpenSearch
from phoenix.evals import (
    TOOL_CALLING_PROMPT_TEMPLATE,
)
from pydantic import BaseModel, Field, field_validator


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
    guardrail_check: Optional[bool] = Field(
        default=False, description="Whether to check guardrail"
    )
    guardrail_category: Optional[str] = Field(
        default=None, description="guardrail check category"
    )
    domain: Optional[List[str]] = Field(default=None, description="Detected domain")
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


class Domain(BaseModel):
    domain: Optional[List[str]] = Field(default=None, description="Detected domain")

    @field_validator("domain", mode="before")
    @classmethod
    def force_list(cls, v):
        if v is None:
            return ["nullo"]
        if isinstance(v, str):
            return [v]
        return v


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
        self.retrieve_from_uploaded_documents = configuration.get(
            "retrieve_from_uploaded_documents"
        )
        self.opensearch_host = configuration.get("opensearch_host")
        self.open_search_client = OpenSearch(
            hosts=[self.opensearch_host],
        )
        self.guardrail_configuration = self.configuration.get("guardrail_configuration")
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

        if self.configuration.get("rephrase_prompt_template"):
            rephrase_prompt_template = self.configuration.get(
                "rephrase_prompt_template"
            )

            rewrite_query_prompt = rephrase_prompt_template + (
                """
                **QUERY ORIGINALE:**
                "{query}"

                **QUERY PRECEDENTE:**
                "{previous_query}"

                **RISPOSTA PRECEDENTE:**
                {previous_response}

                Rispondi ESCLUSIVAMENTE con la query riscritta.
                """
            )
        else:
            rephrase_prompt_template = """
                Rewrite the current query as a self-contained, context-resolved version optimized for information retrieval, given that it is a follow-up to a previous query and response.

                **FOLLOW-UP REWRITING GUIDELINES:**

                1. **Resolve all references**: Replace pronouns (e.g., "it", "they", "this"), demonstratives (e.g., "these", "that"), and vague terms with the specific entities, concepts, or examples mentioned in the prior exchange.
                2. **Inject missing context**: Include any relevant background, assumptions, or details from the previous query and response that are necessary to make the query fully understandable on its own.
                3. **Enhance clarity and specificity**: Use precise language, domain-relevant terminology, and well-defined phrases to improve searchability and reduce ambiguity.
                4. **Preserve interrogative structure**: Keep the rewritten query in question form, reflecting the user's original intent and information need.
                5. **Maintain thematic continuity**: Ensure the rewritten query logically follows from the prior context while standing alone as an independent, retrievable query.
            """

            rewrite_query_prompt = rephrase_prompt_template + (
                """
                **ORIGINAL QUERY:**
                "{query}"

                **PREVIOUS QUERY:**
                "{previous_query}"

                **PREVIOUS RESPONSE:**
                {previous_response}

                Reply ONLY with the rewritten query.
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

    def _llm_input_guardrail(self, query):
        guardrail_prompt = """
            You are a guardrail system specialized in text classification.
            Your task is to analyze the sentence provided by the user and classify it
            exclusively into one of the following categories.

            CATEGORIES:
            1. SELF-HARM/SUICIDE - Content that describes, encourages, instructs, or refers to acts of self-harm, cutting, eating disorders, suicidal thoughts, methods of suicide, incitement to suicide, or implicit/explicit requests for help related to these issues.
            2. VIOLENCE/WEAPONS - Content that describes, glorifies, instructs, or threatens acts of physical violence against people or animals, misuse of firearms, bladed weapons, blunt objects, fights, torture, murder, assault, or instructions for building/obtaining weapons.
            3. EXPLOSIVES - Content that describes, instructs, or provides detailed information on the production, assembly, detonation, or use of explosive devices, bombs, explosive materials, incendiaries, or components for manufacturing explosives.
            4. HATE/HARASSMENT - Content that expresses incitement to hatred, discrimination, harassment, bullying, or derogatory language based on race, ethnicity, nationality, religion, gender, gender identity, sexual orientation, disability, age, or socioeconomic status; includes targeted insults, negative stereotypes, dehumanization, advocacy of genocide, racial superiority, repeated personal attacks, doxxing, verbal sexual harassment, or calls for exclusion/harm against protected individuals or groups.
            5. SEXUAL_CONTENT - Explicit sexual content, detailed descriptions of sexual acts, obscene or vulgar language with sexual connotations, pornographic material, requests for sexual interactions, sexting, or content that sexualizes minors (with maximum priority).

            RULES:
            - If the sentence DOES NOT FALL INTO any of the categories listed above, return exactly: NONE
            - If the sentence falls into one of the categories, return ONLY the category name in UPPERCASE
            - If the sentence falls into multiple categories, choose the predominant or most severe one
            - Do not add explanations, comments, or punctuation

            SENTENCE TO CLASSIFY:
            {query}

            CATEGORY:
            """

        guardrail_prompt_template = PromptTemplate.from_template(guardrail_prompt)

        if (
            self.guardrail_configuration.get("guardrail_type")
            == GuardrailType.AWS_BEDROCK.value
        ):
            llm_guardrail = initialize_guardrail(self.guardrail_configuration)
            guardrail_chain = guardrail_prompt_template | llm_guardrail
            guardrail_response = guardrail_chain.invoke({"query": query})

            return guardrail_response.content[0].get("text")
        elif (
            self.guardrail_configuration.get("guardrail_type")
            == GuardrailType.GOOGLE_MODEL_ARMOR.value
        ):
            llm_guardrail = initialize_guardrail(self.guardrail_configuration)
            try:
                guardrail_response = llm_guardrail.invoke({"query": query})
                return "NONE"
            except Exception as e:
                if "flagged as unsafe" in str(e):
                    return "UNSAFE"
                else:
                    raise e
        elif (
            self.guardrail_configuration.get("guardrail_type")
            == GuardrailType.OPENAI_MODERATION.value
        ):
            llm_guardrail = initialize_guardrail(self.guardrail_configuration)
            guardrail_response = llm_guardrail.invoke({"input": query})
            if guardrail_response.get("input") == guardrail_response.get("output"):
                return "NONE"
            else:
                return "UNSAFE"
        else:
            guardrail_chain = guardrail_prompt_template | self.llm
            guardrail_response = guardrail_chain.invoke({"query": query})

            return guardrail_response.content

    def input_guardrail_node(self, state: GraphState) -> GraphState:
        query = state.current_query
        embedding_model_configuration = get_embedding_model_configuration(
            grpc_host=self.configuration.get("grpc_host_datasource"),
            virtual_host=self.configuration.get("virtual_host"),
        )

        retriever = OpenSearchGuardrailDocumentsRetriever(
            opensearch_host=self.opensearch_host,
            grpc_host_embedding=self.configuration.get("grpc_host_embedding"),
            embedding_model_configuration=embedding_model_configuration,
            uploaded_documents_index="guardrails-documents-index",
            retrieve_type="HYBRID",
            search_text=query,
        )

        retrieved_docs = retriever.invoke(query)

        for doc in retrieved_docs:
            document_id = doc.metadata["document_id"]
            score = doc.metadata["score"]
            if score >= 0.5:
                llm_guardrail = self._llm_input_guardrail(query)
                if llm_guardrail != "NONE":
                    state.guardrail_check = True
                    state.guardrail_category = llm_guardrail
                break

        return state

    def _llm_input_domain(self, query, possible_domains):
        parser = PydanticOutputParser(pydantic_object=Domain)
        format_instructions = parser.get_format_instructions()
        domain_prompt = """
        Sei un sistema di classificazione degli intenti utente.

        Il tuo compito è:
        1. Analizzare la query dell'utente
        2. Identificare gli intenti principali
        3. Assegnare la query a uno o più dei domini disponibili
        4. Se nessun dominio sembra corretto rispondi con una stringa vuota

        ---

        DOMINI DISPONIBILI:
        {possible_domains}

        ---

        REGOLE:
        - Devi scegliere ESATTAMENTE un dominio tra quelli forniti
        - Non inventare nuovi domini
        - Se la query è ambigua, scegli il dominio più probabile
        - Se la query non è chiaramente classificabile rispondi con una stringa vuota
        - Non aggiungere testo extra fuori dal formato richiesto

        ---

        QUERY UTENTE:
        {query}

        ---

        {format_instructions}
        """

        domain_prompt_template = PromptTemplate.from_template(domain_prompt)
        chain = domain_prompt_template | self.llm
        raw_output = chain.invoke(
            {
                "query": query,
                "possible_domains": possible_domains,
                "format_instructions": format_instructions,
            }
        )

        domain_response = parser.parse(raw_output.content)

        return domain_response

    def input_domain_node(self, state: GraphState) -> GraphState:
        query = state.current_query
        print(f"Si lavora sulla query '{query}'")
        embedding_model_configuration = get_embedding_model_configuration(
            grpc_host=self.configuration.get("grpc_host_datasource"),
            virtual_host=self.configuration.get("virtual_host"),
        )

        retriever = OpenSearchDomainDocumentsRetriever(
            opensearch_host=self.opensearch_host,
            grpc_host_embedding=self.configuration.get("grpc_host_embedding"),
            embedding_model_configuration=embedding_model_configuration,
            uploaded_documents_index="domain-documents-index",
            retrieve_type="HYBRID",
            search_text=query,
        )
        print("Retrieving ...")
        retrieved_docs = retriever.invoke(query)

        high_score_docs = []
        found_domains = set()
        for doc in retrieved_docs:
            score = doc.metadata["score"]
            if score >= 0.7:
                high_score_docs.append(doc)
                found_domains.add(doc.metadata["domain"])
                print(doc)

        if len(found_domains) > 0:
            print("OpenSearch judge")
            print(list(found_domains))
            state.domain = list(found_domains)
        else:
            print("LLM judge")
            INTENTS = [
                "troubleshooting",
                "how-to",
                "billing",
                "feature-information",
                "account-management",
                "''",
            ]
            found_domains = set(INTENTS)

            llm_domain = self._llm_input_domain(query, found_domains)
            print(llm_domain.domain)
            state.domain = llm_domain.domain

        return state

    def input_guardrail_route_decision(
        self, state: GraphState
    ) -> Literal["guardrail_violation_response", "history_handler"]:
        """Separate function for conditional routing decision"""
        if state.guardrail_check:
            return "guardrail_violation_response"
        else:
            return "history_handler"

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

            if self.configuration.get("analyze_query_prompt_template"):
                analyze_query_prompt_template = self.configuration.get(
                    "analyze_query_prompt_template"
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
            else:
                analyze_query_prompt_template = """
                    Analyze the relationship between the current question and the previous conversation.

                    **Classification Criteria:**

                    1.  Respond "FOLLOW_UP" if the current question:
                        - Explicitly or implicitly refers to information present in the previous conversation.
                        - Asks for clarification, further details, extensions, or applications of concepts already discussed.

                    2.  Respond "NEW_QUESTION" if the current question:
                        - Introduces a new topic, unrelated to the previous conversation.
                        - Contains no direct or indirect references to what was previously said.
                        - Represents a clear and distinct change of subject.
                    """

                analyze_query_prompt = (
                    analyze_query_prompt_template
                    + """
                        **PREVIOUS CONVERSATION:**
                        {context}

                        **CURRENT QUESTION:**
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
        bypass_rag = self.configuration.get("bypass_rag")

        if bypass_rag:
            state.use_rag = False
        else:
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

            rag_router_prompt_template = PromptTemplate.from_template(rag_router_prompt)

        #     rag_router_chain = (
        #         rag_router_prompt_template
        #         | self.llm.with_structured_output(
        #             schema=RouterResponse, include_raw=False, method="function_calling"
        #         )
        #     )

        #     decision = rag_router_chain.invoke(
        #         {"query": query, "context": conversation_context}
        #     )
        #     print(decision.response.value)
        state.use_rag = True  # "RAG" in decision.response.value

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

        if self.retrieve_from_uploaded_documents and self.user_id and self.tenant_id:
            retriever = OpenSearchUploadedDocumentsRetriever(
                opensearch_host=self.configuration.get("opensearch_host"),
                grpc_host_embedding=self.configuration.get("grpc_host_embedding"),
                embedding_model_configuration=self.configuration.get(
                    "embedding_model_configuration"
                ),
                uploaded_documents_index=f"{self.tenant_id}-uploaded-documents-index",
                retrieve_type=self.configuration.get("retrieve_type"),
                user_id=self.user_id,
                chat_id=self.chat_id,
                search_text=query,
            )
        elif self.retrieve_from_uploaded_documents and (
            not self.user_id or not self.tenant_id
        ):
            unauthorized_response()
        else:
            search_query = self.configuration.get("search_query")

            domain_filter = models.SearchToken(
                tokenType="TEXT",
                keywordKey="domain",
                values=state.domain,
                filter=True,
                entityType="",
                entityName="",
                extra={},
            )

            if self.configuration.get("search_query"):
                search_query.append(domain_filter)
            else:
                search_query = [domain_filter]

                retriever = OpenSearchRetriever(
                    search_query=search_query,
                    search_text=query,
                    rerank=self.configuration.get("rerank"),
                    chunk_window=self.configuration.get("chunk_window"),
                    range_values=self.configuration.get("range_values"),
                    after_key=self.configuration.get("after_key"),
                    suggest_keyword=self.configuration.get("suggest_keyword"),
                    suggestion_category_id=self.configuration.get(
                        "suggestion_category_id"
                    ),
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
                    grpc_host=self.configuration.get("grpc_host_datasource"),
                )

        retrieved_docs = retriever.invoke(query)
        state.context = retrieved_docs

        return state

    def opensearch_retriever_evaluation_node(self, state: GraphState) -> GraphState:
        if state.do_retrieve:
            retriever_evaluation_prompt = """
                    You are comparing a reference text with a question to determine if the reference text contains information relevant to answering the question. Here is the data:

                    [BEGIN DATA]
                    ************
                    [Question]: {query}
                    ************
                    [Reference text]: {context}
                    [END DATA]

                    After your analysis, provide the following structured output in three parts, in this order and separated by a blank line:

                    1. judgment: A single word: "RELEVANT" or "NOT_RELEVANT".

                    2. vote: An integer score from 0 to 10 that quantifies the degree of relevance, where 0 indicates a total lack of relevance and 10 indicates perfect relevance (i.e., the reference text completely and directly answers the question).

                    3. explanation: A detailed explanation of your reasoning. Analyze whether the reference text addresses the topic or key concepts raised by the question. Explain why the information is considered relevant or not, indicating any partial points of contact or missing information. The explanation must be self-contained and must not repeat the terms "JUDGMENT" or "VOTE". Avoid anticipating the final judgment at the beginning of the explanation.
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
                        You are an evaluator comparing a question with a list of text segments (chunks) to determine if each chunk contains information relevant to answering the question.  
                        Data to be analyzed:

                        [BEGIN DATA]  
                        [Question]: {query}  
                        Reference text - {chunks}  
                        [END DATA]

                        Precise instructions:
                        1. Evaluate **EACH CHUNK INDEPENDENTLY** from the others. Do not use information present in other chunks to judge this chunk.
                        2. For each chunk, return an object with four fields in the exact order: "chunk_id", "judgment", "vote", "explanation".
                        - "chunk_id": the chunk identifier, present in each element of the list.
                        - "judgment": a string, **only** "RELEVANT" or "NOT_RELEVANT".
                        - "vote": **only** an integer between 0 and 10 (0 = not relevant at all, 10 = completely and directly answers the question on its own).
                        - "explanation": a self-contained and detailed explanation that justifies the vote for **this single chunk**. It must:
                            - explicitly refer to the content of `chunk_content`;
                            - explain which elements of the chunk are (or are not) related to the question;
                            - indicate any missing information or partial points of contact;
                            - **not** begin by stating the judgment or the vote and **not** use the words "JUDGMENT" or "VOTE" within the explanation;
                            - be self-sufficient (someone reading only this explanation must understand why that vote was given).
                        3. Maintain **valid JSON** output that is a list (array) with one object for each chunk, in the **same order** as the input chunks.
                        4. Do not add extra text, comments, or metadata: **OUTPUT ONLY THE JSON**.

                        Recap of formal requirements:
                        - "vote" must be an integer 0-10.
                        - "judgment" must be exactly "RELEVANT" or "NOT_RELEVANT".
                        - The returned array must contain exactly one element per chunk, in the same order.
                        - No text outside the JSON.

                        Now evaluate the provided chunks using these rules.

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
                        You are comparing a reference text with a question to determine if the reference text contains information relevant to answering the question. **Each segment (chunk) of the reference text must be evaluated INDEPENDENTLY from the others.** Here is the data:

                        [BEGIN DATA]
                        ************
                        [Question]: {query}
                        ************
                        Reference text - [Chunk {chunk_number} of {total_chunks}]: {context_chunk} with chunk_id {chunk_id}
                        [END DATA]

                        After your analysis, provide the following structured output in three parts, in this order and separated by a blank line:

                        1.  **chunk_id**: chunk identifier, present in each chunk.
                        2.  **judgment:** A single word: "RELEVANT" or "NOT_RELEVANT".
                        3.  **vote:** An integer score from 0 to 10 that quantifies the degree of relevance of THIS SINGLE CHUNK, where 0 indicates a total lack of relevance and 10 indicates that this chunk, on its own, contains information that completely and directly answers the question.
                        4.  **explanation:** A detailed explanation of your reasoning. Analyze whether THIS SPECIFIC CHUNK addresses the topic or key concepts raised by the question. Explain why the information in this segment is considered relevant or not, indicating any partial points of contact or missing information. The explanation must be self-contained, explicitly refer to the content of this chunk, and must not repeat the terms "JUDGMENT" or "VOTE". Avoid anticipating the final judgment at the beginning of the explanation.
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

    def guardrail_violation_response_node(self, state: GraphState) -> GraphState:
        """Guardrail response node"""
        state.response = "Guardrail violation"

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

            conversation_title = ""

            if all(
                [
                    self.configuration.get("enable_conversation_title"),
                    self.chat_sequence_number == 1,
                    self.tenant_id,
                    self.user_id,
                    self.chat_id,
                ]
            ):
                conversation_title = generate_conversation_title(
                    self.llm, state.current_query, state.response
                )

            elif self.chat_sequence_number == 1 and self.tenant_id and self.user_id:
                conversation_title = state.current_query

            state.conversation_title = conversation_title.strip('"')

        return state

    def response_evaluation_node(self, state: GraphState) -> GraphState:
        clarity_llm_judge_prompt = """
                In this task, you will be presented with a question and an answer. Your goal is to evaluate the clarity of the answer in addressing the question. A clear answer is precise, coherent, and directly addresses the question without introducing unnecessary complexity or ambiguity. An unclear answer is vague, disorganized, or difficult to understand, even if it might be factually correct.
                After analyzing the question and the answer, you must provide the following structured output in three parts, in this order and separated by a blank line:
                - judgment: A single word: "CLEAR" or "UNCLEAR".
                - vote: An integer score from 0 to 10 that quantifies the degree of clarity, where 0 indicates a total lack of clarity and 10 indicates perfect clarity.
                - explanation: A detailed explanation of your reasoning. Analyze how the answer does or does not meet the criteria for clarity (precision, coherence, structure, language, relevance). The explanation must be self-contained and must not repeat the terms "JUDGMENT" or "VOTE". Avoid anticipating the final judgment at the beginning of the explanation.

                Carefully consider the question and the answer before determining your evaluation.

                [BEGIN DATA]
                Question: {query}
                Answer: {response}
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
            workflow.add_node("input_guardrail", self.input_guardrail_node)
            workflow.add_node("input_domain_pre", self.input_domain_node)
            # workflow.add_node("input_domain_post", self.input_domain_node)

            workflow.add_node(
                "guardrail_violation_response", self.guardrail_violation_response_node
            )
            workflow.add_node("history_handler", self.history_handler_node)
            workflow.add_node(
                "analyze_and_rewrite_query", self.analyze_and_rewrite_query_node
            )
            workflow.add_node("rag_router", self.rag_router_node)
            workflow.add_node("opensearch_retriever", self.opensearch_retriever_node)
            workflow.add_node("llm_response", self.llm_response_node)
            workflow.add_node("history_saver", self.history_saver_node)

            workflow.set_entry_point(
                "input_domain_pre"
            )  # ATTENZIONE, non sta iniziando da input_guardrail al momento
            workflow.add_edge("input_domain_pre", "history_handler")
            # workflow.add_edge("analyze_and_rewrite_query", "input_domain_post")

            workflow.add_conditional_edges(
                "input_guardrail",
                self.input_guardrail_route_decision,
                {
                    "guardrail_violation_response": "guardrail_violation_response",
                    "history_handler": "history_handler",
                },
            )

            workflow.add_conditional_edges(
                "rag_router",
                self.route_decision,
                {
                    "opensearch_retriever": "opensearch_retriever",
                    "llm_response": "llm_response",
                },
            )

            workflow.add_edge("guardrail_violation_response", END)
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

        if last_state.values.get("guardrail_check"):
            yield json.dumps(
                {
                    "chunk": f"{last_state.values.get('response')} - ({last_state.values.get('guardrail_category')})",
                    "type": "GUARDRAIL",
                }
            )

        yield json.dumps({"chunk": "", "type": "END"})
