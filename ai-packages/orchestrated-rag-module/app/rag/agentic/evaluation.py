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
Real-time evaluation of a turn, as the evaluation graph of agentic-rag-module does it.

With ``enable_real_time_evaluation`` three judges look at the turn once the
answer is complete, before it is streamed:

- the routing: was searching the documents, or not, the right call for the
  question (the tool calling template of Phoenix, with the tool description of
  the tenant);
- every document delivered to the orchestrator: is it relevant to the question;
- the answer: is it clear.

The verdicts are not SSE events: they go into the saved turn, in the fields of
agentic-rag-module. Every judge is best effort: a failure is logged and leaves
its field empty, it never costs the answer.
"""

import asyncio
from enum import Enum
from typing import Optional

from phoenix.evals import TOOL_CALLING_PROMPT_TEMPLATE
from pydantic import BaseModel, Field
from pydantic_ai import Agent

from app.rag.agentic.contracts import OrchestratorOutput
from app.rag.agentic.deps import AgenticDeps
from app.utils.logger import get_logger

logger = get_logger(__name__)


class ClarityJudgment(str, Enum):
    clear = "CLEAR"
    unclear = "UNCLEAR"


class RelevanceJudgment(str, Enum):
    relevant = "RELEVANT"
    not_relevant = "NOT_RELEVANT"


class ResponseEvaluation(BaseModel):
    judgment: ClarityJudgment = Field(
        ClarityJudgment.clear, description="The response is clear or unclear."
    )
    explanation: str = Field(..., description="The reasoning behind the judgment")
    vote: int = Field(..., ge=0, le=10, description="Rating from 0 to 10 of the clarity")


class ChunkEvaluation(BaseModel):
    chunk_id: str = Field(..., description="The identifier of the chunk")
    judgment: RelevanceJudgment = Field(
        RelevanceJudgment.relevant, description="The chunk is relevant or not relevant."
    )
    explanation: str = Field(..., description="The reasoning behind the judgment")
    vote: int = Field(..., ge=0, le=10, description="Rating from 0 to 10 of the relevance")


CHUNK_PROMPT = """You are comparing a reference text with a question to determine if the reference text contains information relevant to answering the question. **Each segment (chunk) of the reference text must be evaluated INDEPENDENTLY from the others.** Here is the data:
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

RESPONSE_PROMPT = """In this task, you will be presented with a question and an answer. Your goal is to evaluate the clarity of the answer in addressing the question. A clear answer is precise, coherent, and directly addresses the question without introducing unnecessary complexity or ambiguity. An unclear answer is vague, disorganized, or difficult to understand, even if it might be factually correct.
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


def router_prompt(question: str, used_rag: bool, tool_description: Optional[str]) -> str:
    """The tool calling template of Phoenix: the search is the ``rag_tool`` the turn called,
    or did not call."""
    return str(TOOL_CALLING_PROMPT_TEMPLATE).format(
        question=question,
        tool_call="rag_tool" if used_rag else None,
        tool_definitions=tool_description,
    )


async def _best_effort(name: str, judge):
    try:
        return await judge
    except Exception as e:  # noqa: BLE001
        logger.warning(f"[real_time_evaluation] {name} failed: {e!r}")
        return None


async def run_evaluations(
    *, question: str, output: OrchestratorOutput, deps: AgenticDeps, used_rag: bool
) -> dict:
    """
    Judge the turn and return the fields of the saved turn:
    ``rag_router_evaluation``, ``retriever_chunks_evaluation`` and
    ``response_evaluation``, each ``None`` when its judge did not run or failed.
    """
    model = deps.orchestrator_model

    async def judge(output_type, prompt):
        agent = Agent(model, name="evaluation_judge", output_type=output_type)
        return (await agent.run(prompt, usage=deps.usage)).output

    documents = [
        (document_id, deps.discovered.get(document_id, {}).get("content", ""))
        for document_id in (deps.delivered_ids if used_rag else [])
    ]
    router, response, *chunks = await asyncio.gather(
        _best_effort(
            "router",
            judge(
                str,
                router_prompt(
                    question, used_rag, deps.rag_configuration.get("rag_tool_description")
                ),
            ),
        ),
        _best_effort(
            "response",
            judge(
                ResponseEvaluation,
                RESPONSE_PROMPT.format(query=question, response=output.message),
            ),
        ),
        *[
            _best_effort(
                f"chunk {document_id}",
                judge(
                    ChunkEvaluation,
                    CHUNK_PROMPT.format(
                        query=question,
                        chunk_number=number,
                        total_chunks=len(documents),
                        context_chunk=content,
                        chunk_id=document_id,
                    ),
                ),
            )
            for number, (document_id, content) in enumerate(documents, 1)
        ],
    )
    chunks = [chunk.model_dump(mode="json") for chunk in chunks if chunk is not None]
    fields = {
        "rag_router_evaluation": router,
        "retriever_chunks_evaluation": chunks or None,
        "response_evaluation": response.judgment.value if response else None,
    }
    logger.debug(
        f"[real_time_evaluation] router={router!r} chunks={len(chunks)} "
        f"response={fields['response_evaluation']}"
    )
    return fields
