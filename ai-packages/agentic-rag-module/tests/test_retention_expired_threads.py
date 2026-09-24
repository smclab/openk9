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

"""The chat retention deletes every expired thread, each document once.

The fake store answers like OpenSearch does: a terms aggregation without size
returns the first 10 buckets by key, a composite aggregation pages through
every bucket with after_key, and deleting a document twice is a not_found
failure. Against it, a run must remove exactly the threads whose latest
checkpoint is older than the interval, whatever their number and index.
"""

import json
from datetime import datetime, timedelta, timezone
from unittest.mock import MagicMock

import pytest

from app.utils import chat_history

CHECKPOINT_FIELDS = {"thread_id": {}, "checkpoint_id": {}, "checkpoint": {}}
RECENT = datetime.now(timezone.utc) - timedelta(days=1)
EXPIRED = datetime.now(timezone.utc) - timedelta(days=400)


class FakeCheckpointStore:
    """In-memory checkpoint indices behind the OpenSearch calls the job makes."""

    def __init__(self, indices):
        # {index: {thread_id: checkpoint datetime}}, two documents per thread.
        self.docs = {
            index: {
                f"{index}/{thread_id}/{step}": (thread_id, step, when)
                for thread_id, when in threads.items()
                for step in range(2)
            }
            for index, threads in indices.items()
        }
        self.aggregation_bodies = []
        self.bulk_failures = []

        self.client = MagicMock()
        self.client.indices.get.return_value = {index: {} for index in indices}
        self.client.indices.get_mapping.side_effect = lambda index: {
            index: {"mappings": {"properties": CHECKPOINT_FIELDS}}
        }
        self.client.search.side_effect = self.search

    def threads(self, index):
        return sorted({thread_id for thread_id, _, _ in self.docs[index].values()})

    def _latest_hit(self, index, thread_id):
        step, when = max(
            (step, when)
            for tid, step, when in self.docs[index].values()
            if tid == thread_id
        )
        checkpoint = json.dumps({"ts": when.isoformat()})
        return {"_source": {"thread_id": thread_id, "checkpoint": checkpoint}}

    def search(self, index, body, size=None):
        if "aggs" not in body:
            thread_id = body["query"]["match"]["thread_id"]
            hits = [
                {"_id": doc_id}
                for doc_id, (tid, _, _) in self.docs[index].items()
                if tid == thread_id
            ]
            return {"hits": {"hits": hits}}

        self.aggregation_bodies.append(json.loads(json.dumps(body)))
        aggregation = body["aggs"]["threads"]
        threads = self.threads(index)

        if "terms" in aggregation:
            page = threads[: aggregation["terms"].get("size", 10)]
            keys = page
            result = {}
        else:
            composite = aggregation["composite"]
            after = composite.get("after", {}).get("thread_id")
            remaining = [t for t in threads if after is None or t > after]
            page = remaining[: composite.get("size", 10)]
            keys = [{"thread_id": thread_id} for thread_id in page]
            result = {"after_key": keys[-1]} if keys else {}

        buckets = [
            {
                "key": key,
                "max_step_doc": {"hits": {"hits": [self._latest_hit(index, t)]}},
            }
            for key, t in zip(keys, page)
        ]
        return {"aggregations": {"threads": {"buckets": buckets, **result}}}

    def bulk(self, client, actions, **kwargs):
        ok, failed = 0, []
        for action in actions:
            if self.docs[action["_index"]].pop(action["_id"], None) is None:
                failed.append(
                    {"delete": {"_index": action["_index"], "_id": action["_id"]}}
                )
            else:
                ok += 1
        self.bulk_failures.extend(failed)
        return ok, failed


@pytest.fixture
def run_retention(monkeypatch):
    def run(indices, interval_in_days=180):
        store = FakeCheckpointStore(indices)
        monkeypatch.setattr(
            chat_history, "get_opensearch_client", lambda host: store.client
        )
        monkeypatch.setattr(chat_history.helpers, "bulk", store.bulk)
        chat_history.delete_documents("http://localhost:9200", interval_in_days)
        return store

    return run


def test_expired_thread_after_the_first_ten_is_deleted(run_retention):
    threads = {f"t{n:02d}": RECENT for n in range(10)}
    threads["t10-expired"] = EXPIRED

    store = run_retention({"tenant-user": threads})

    assert store.threads("tenant-user") == [f"t{n:02d}" for n in range(10)]


def test_every_expired_thread_is_deleted_in_one_run(run_retention):
    threads = {f"t{n:03d}": EXPIRED for n in range(250)}

    store = run_retention({"tenant-user": threads})

    assert store.threads("tenant-user") == []


def test_pages_follow_the_previous_after_key(run_retention):
    threads = {f"t{n:03d}": EXPIRED for n in range(250)}

    store = run_retention({"tenant-user": threads})

    afters = [
        body["aggs"]["threads"]["composite"].get("after")
        for body in store.aggregation_bodies
    ]
    assert afters[0] is None
    assert all(after is not None for after in afters[1:])
    assert len(afters) > 1


def test_thread_within_the_interval_is_kept(run_retention):
    store = run_retention(
        {"tenant-user": {"active": RECENT, "expired": EXPIRED}},
        interval_in_days=30,
    )

    assert store.threads("tenant-user") == ["active"]


def test_each_document_is_deleted_once_across_indices(run_retention):
    store = run_retention(
        {
            "tenant-user1": {"a-expired": EXPIRED},
            "tenant-user2": {"b-expired": EXPIRED},
        }
    )

    assert store.threads("tenant-user1") == []
    assert store.threads("tenant-user2") == []
    assert store.bulk_failures == []
