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

from app.utils.logger import logger


def load_messages_from_snapshot(graph, config):
    """Return the chronological conversation messages from the latest checkpoint.

    The ``messages`` channel uses the ``add_messages`` reducer, so the latest
    snapshot already holds the full history in chronological order. Reading it
    directly avoids the ordering issues of reconstructing messages from the
    reverse-ordered state history.
    """
    try:
        snapshot = graph.get_state(config)
        return snapshot.values.get("messages", [])
    except Exception as e:
        logger.error(f"Error loading messages from checkpoints: {e}")
        return []


def load_messages_from_frontend(chat_history):
    """Build chronological messages from the frontend-provided chat history."""
    # Imported lazily so this module stays importable without langchain.
    from langchain_core.messages import AIMessage, HumanMessage

    messages = []

    try:
        for item in chat_history:
            messages.append(HumanMessage(content=item["question"]))
            messages.append(AIMessage(content=item["answer"]))
    except Exception as e:
        logger.error(f"Error loading messages from frontend: {e}")

    return messages
