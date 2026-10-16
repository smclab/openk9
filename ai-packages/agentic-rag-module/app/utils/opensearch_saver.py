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

# get_tuple below is adapted from langgraph-opensearch 0.1.5
# (https://github.com/samriddhadev/langgraph-opensearch), released under the
# MIT License:
#
# Copyright (c) 2025 Samriddha Chatterjee
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

from typing import Optional

from langchain_core.runnables import RunnableConfig
from langgraph.checkpoint.base import CheckpointTuple, get_checkpoint_id
from langgraph.checkpoint.opensearch import OpenSearchSaver as _OpenSearchSaver
from langgraph.checkpoint.opensearch.utils import loads_metadata


class OpenSearchSaver(_OpenSearchSaver):
    """OpenSearch checkpointer with the pending writes read correctly.

    The library's ``get_tuple`` iterates over the first hit of the writes
    search instead of the hits themselves, so it raises ``TypeError`` as soon
    as the latest checkpoint has a pending write. That is the normal case while
    a graph runs, because LangGraph saves checkpoints in the background.
    """

    def get_tuple(self, config: RunnableConfig) -> Optional[CheckpointTuple]:
        """Return the requested checkpoint, or the latest one of the thread,
        together with its pending writes; ``None`` when there is none."""
        thread_id = config["configurable"]["thread_id"]
        checkpoint_ns = config["configurable"].get("checkpoint_ns", "")
        parameters = {"thread_id": thread_id, "checkpoint_ns": checkpoint_ns}
        if checkpoint_id := get_checkpoint_id(config):
            parameters["checkpoint_id"] = checkpoint_id

        result = self._search(
            index=self.checkpoint_index_name,
            query=self._build_query_dsl(parameters),
            sort=[{"checkpoint_id": "desc"}],
            size=1,
            scroll=False,
        )

        for item in result:
            doc = item["_source"]
            config_values = {
                "thread_id": thread_id,
                "checkpoint_ns": checkpoint_ns,
                "checkpoint_id": doc["checkpoint_id"],
            }
            writes = self._search(
                index=self.writes_index_name,
                query=self._build_query_dsl(config_values),
                sort=[{"checkpoint_id": "desc"}],
            )
            pending_writes = [
                (
                    write["_source"]["task_id"],
                    write["_source"]["channel"],
                    self.serde.loads(write["_source"]["value"]),
                )
                for write in writes
            ]

            return CheckpointTuple(
                {"configurable": config_values},
                self.serde.loads(doc["checkpoint"]),
                loads_metadata(doc["metadata"]),
                (
                    {
                        "configurable": {
                            "thread_id": thread_id,
                            "checkpoint_ns": checkpoint_ns,
                            "checkpoint_id": doc["parent_checkpoint_id"],
                        }
                    }
                    if doc.get("parent_checkpoint_id")
                    else None
                ),
                pending_writes,
            )

        return None
