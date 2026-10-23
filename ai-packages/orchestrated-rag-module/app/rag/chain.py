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

from app.utils.logger import get_logger

logger = get_logger(__name__)

PIPELINE_NOT_AVAILABLE_MESSAGE = "RAG pipeline not available yet"


def get_agentic_rag(
    rag_type,
    search_query,
    datasource_ids,
    after_key,
    suggest_keyword,
    suggestion_category_id,
    jwt,
    extra,
    sort,
    sort_after_key,
    language,
    search_text,
    chat_id,
    user_id,
    tenant_id,
    retrieve_from_uploaded_documents,
    chat_history,
    timestamp,
    chat_sequence_number,
    rag_configuration,
    llm_configuration,
    guardrails_configuration,
    opensearch_host,
    grpc_host_embedding,
    grpc_host_datasource,
    media=None,
):
    """
    Stream the answer to a RAG request.

    The orchestrated pipeline is not wired yet: every request gets the ERROR
    event that agentic-rag-module sends on an unexpected failure, with a fixed
    chunk, so that the routes and their contract already work.
    """
    logger.warning(f"{PIPELINE_NOT_AVAILABLE_MESSAGE}: rag_type={rag_type}")
    yield json.dumps({"chunk": PIPELINE_NOT_AVAILABLE_MESSAGE, "type": "ERROR"})
