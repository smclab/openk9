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
Intent detection: the domains of the tenant a question belongs to, as in agentic-rag-module.

The detected domains become the domain filter of the Discovery searches. Two levels:

1. the question is searched among the domain documents of the tenant
   (``<tenant_id>-domain-documents-index``) with a pure KNN query, and every
   domain with a document at ``domain_threshold`` or more is kept;
2. if none reaches the threshold, the model classifies the question among all
   the domains of the tenant.

A tenant without domain documents has nothing to detect: neither the
embedding nor the model is paid for.
"""

from typing import List, Optional

from pydantic import BaseModel, Field, field_validator

from app.external_services.grpc.grpc_client import get_embedding_model_configuration
from app.rag.agentic import retrieval
from app.rag.agentic.deps import AgenticDeps
from app.utils.logger import get_logger, log_context

logger = get_logger(__name__)

DOMAIN_INSTRUCTION = """You are a user intent classification system.

Your task is to:
1. Analyze the user's query
2. Identify the main intents
3. Assign the query to one or more of the available domains
4. If no domain seems appropriate, return an empty string

---

AVAILABLE DOMAINS:
{possible_domains}

---

RULES:
- You must choose EXACTLY one domain from the provided ones
- Do not invent new domains
- If the query is ambiguous, choose the most likely domain
- If the query cannot be clearly classified, return an empty string
- Do not add any extra text outside the required format
"""


class Domain(BaseModel):
    domain: Optional[List[str]] = Field(default=None, description="Detected domain")

    @field_validator("domain", mode="before")
    @classmethod
    def force_list(cls, v):
        if v is None:
            return None
        if isinstance(v, str):
            return None if v.strip() == "" else [v]
        return v


def intent_detection_enabled(rag_type: str, rag_configuration: dict) -> bool:
    """Intent detection runs for the chat RAG types, unless the tenant turns it off with
    ``enable_intent_detection``."""
    if rag_type == "SIMPLE_GENERATE":
        return False
    return bool(rag_configuration.get("enable_intent_detection", True))


async def classify_domain_with_llm(model, query: str, possible_domains) -> List[str]:
    """The domains the model gives to ``query`` among ``possible_domains``."""
    from pydantic_ai import Agent

    agent = Agent(
        model,
        output_type=Domain,
        system_prompt=DOMAIN_INSTRUCTION.format(possible_domains=list(possible_domains)),
    )
    result = await agent.run(query)
    return result.output.domain or []


async def detect_domains(*, query: str, deps: AgenticDeps) -> tuple[list[str], list[str]]:
    """
    Detect the domains of ``query``.

    :return: ``(configured, detected)``: the domains of the tenant, which the
        Discovery may choose from, and the ones detected for ``query``, the
        domain filter of the searches. Empty detected domains mean no filter.
    """
    context = log_context(tenant_id=deps.tenant_id, user_id=deps.user_id, chat_id=deps.chat_id)
    index = f"{deps.tenant_id}-domain-documents-index"
    configured = retrieval.get_domains(opensearch_host=deps.opensearch_host, index=index)
    if not configured:
        logger.info(f"[input_domain] domain=None source=not_configured {context}")
        return [], []

    threshold = deps.rag_configuration.get("domain_threshold")
    documents = retrieval.retrieve_domain_documents(
        opensearch_host=deps.opensearch_host,
        grpc_host_embedding=deps.grpc_host_embedding,
        embedding_model_configuration=get_embedding_model_configuration(
            grpc_host=deps.grpc_host_datasource, tenant_id=deps.tenant_id
        ),
        index=index,
        search_text=query,
    )
    logger.debug(f"[input_domain] retrieved {len(documents)} docs threshold={threshold}")
    for document in documents:
        logger.debug(f"[input_domain] doc domain={document['domain']} score={document['score']}")

    detected = list(
        dict.fromkeys(d["domain"] for d in documents if d["score"] >= threshold and d["domain"])
    )
    source = "threshold"
    if not detected:
        logger.debug(f"[input_domain] no domain above threshold, model among {configured}")
        detected = await classify_domain_with_llm(deps.orchestrator_model, query, configured)
        source = "llm"
    logger.info(f"[input_domain] domain={detected or None} source={source} {context}")
    return configured, detected
