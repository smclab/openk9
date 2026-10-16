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


from unittest.mock import MagicMock

from langgraph.checkpoint.base import empty_checkpoint
from langgraph.checkpoint.opensearch.jsonplus_ext_opensearch import (
    JSONPlusExtOSSerializer,
)
from langgraph.checkpoint.opensearch.utils import dumps_metadata

from app.utils.opensearch_saver import OpenSearchSaver

CHECKPOINT_INDEX = "tenant-1-user-1"
WRITES_INDEX = "tenant-1-user-1-writes"
CONFIG = {"configurable": {"thread_id": "chat-1"}}
SERDE = JSONPlusExtOSSerializer()


def _saver(checkpoint_hits, write_hits):
    """A saver on a fake client that answers each index with the given hits.

    The writes search scrolls with ``search_after`` until a page comes back
    empty, so only its first page carries hits.
    """
    client = MagicMock()

    def search(index, body):
        if index == CHECKPOINT_INDEX:
            hits = checkpoint_hits
        else:
            hits = [] if "search_after" in body else write_hits
        return {"hits": {"hits": hits}}

    client.search.side_effect = search

    saver = OpenSearchSaver(
        client=client,
        checkpoint_index_name=CHECKPOINT_INDEX,
        writes_index_name=WRITES_INDEX,
    )
    return saver, client


def _checkpoint_hit(checkpoint, parent_checkpoint_id=None):
    return {
        "_source": {
            "thread_id": "chat-1",
            "checkpoint_ns": "",
            "checkpoint_id": checkpoint["id"],
            "parent_checkpoint_id": parent_checkpoint_id,
            "checkpoint": SERDE.dumps(checkpoint),
            "metadata": dumps_metadata({"source": "loop", "step": 3}),
        }
    }


def _write_hit(checkpoint_id, task_id, channel, value, idx):
    return {
        "_source": {
            "thread_id": "chat-1",
            "checkpoint_ns": "",
            "checkpoint_id": checkpoint_id,
            "task_id": task_id,
            "channel": channel,
            "value": SERDE.dumps(value),
            "idx": idx,
        },
        "sort": [checkpoint_id],
    }


def _searched_indices(client):
    return [call.kwargs["index"] for call in client.search.call_args_list]


def test_get_tuple_returns_the_pending_writes_of_the_latest_checkpoint():
    """While a graph runs, the latest saved checkpoint usually has pending
    writes: every one of them comes back deserialized."""
    checkpoint = empty_checkpoint()
    saver, _ = _saver(
        [_checkpoint_hit(checkpoint)],
        [
            _write_hit(
                checkpoint["id"],
                "task-1",
                "branch:to:history_handler",
                None,
                0,
            ),
            _write_hit(checkpoint["id"], "task-2", "context", ["d"], 1),
        ],
    )

    checkpoint_tuple = saver.get_tuple(CONFIG)

    assert checkpoint_tuple.pending_writes == [
        ("task-1", "branch:to:history_handler", None),
        ("task-2", "context", ["d"]),
    ]


def test_get_tuple_without_pending_writes():
    checkpoint = empty_checkpoint()
    saver, client = _saver(
        [_checkpoint_hit(checkpoint, parent_checkpoint_id="parent-1")],
        [],
    )

    checkpoint_tuple = saver.get_tuple(CONFIG)

    assert checkpoint_tuple.pending_writes == []
    assert checkpoint_tuple.checkpoint["id"] == checkpoint["id"]
    assert checkpoint_tuple.metadata == {"source": "loop", "step": 3}
    assert checkpoint_tuple.config == {
        "configurable": {
            "thread_id": "chat-1",
            "checkpoint_ns": "",
            "checkpoint_id": checkpoint["id"],
        }
    }
    assert checkpoint_tuple.parent_config == {
        "configurable": {
            "thread_id": "chat-1",
            "checkpoint_ns": "",
            "checkpoint_id": "parent-1",
        }
    }
    assert _searched_indices(client) == [CHECKPOINT_INDEX, WRITES_INDEX]


def test_get_tuple_of_the_first_checkpoint_has_no_parent():
    checkpoint = empty_checkpoint()
    saver, _ = _saver([_checkpoint_hit(checkpoint)], [])

    assert saver.get_tuple(CONFIG).parent_config is None


def test_get_tuple_of_an_unknown_thread_returns_none():
    saver, client = _saver([], [])

    assert saver.get_tuple(CONFIG) is None
    assert _searched_indices(client) == [CHECKPOINT_INDEX]


def test_get_tuple_looks_up_the_requested_checkpoint():
    saver, client = _saver([], [])

    saver.get_tuple({"configurable": {"thread_id": "chat-1", "checkpoint_id": "c-7"}})

    query = client.search.call_args.kwargs["body"]["query"]
    assert {"match": {"checkpoint_id": "c-7"}} in query["bool"]["must"]
