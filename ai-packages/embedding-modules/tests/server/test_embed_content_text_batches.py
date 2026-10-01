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

"""EmbedContent streams the inline text batch by batch: the first chunks
reach the caller while the next batch is still being embedded, numbering
and total span the whole text, and an error on a later batch ends the RPC
with INTERNAL after the chunks already emitted."""

import threading

import grpc
import pytest

from app import server as server_module
from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

VECTOR = [3.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0]


def _pipelines(embed_texts):
    def build_pipelines(configuration, chunker):
        return Pipelines(
            embed_texts=embed_texts,
            chunk=lambda text: text.split(),
            fetch=lambda url: (b"", None),
            embed_image=None,
        )

    return build_pipelines


@pytest.fixture(autouse=True)
def batches_of_two(monkeypatch):
    monkeypatch.setattr(server_module, "EMBED_CONTENT_BATCH_SIZE", 2)


def test_the_first_batch_is_emitted_before_the_next_is_embedded(make_stub):
    second_batch_started = threading.Event()
    release = threading.Event()
    calls = []

    def embed_texts(texts):
        calls.append(list(texts))
        if len(calls) == 2:
            second_batch_started.set()
            assert release.wait(timeout=10), "the test never released the batch"
        return [VECTOR for _ in texts]

    stub = make_stub(_pipelines(embed_texts))
    stream = stub.EmbedContent(
        embedding_pb2.EmbedContentRequest(tenantId="mew", text="uno due tre")
    )

    first = [next(stream), next(stream)]

    # both chunks of the first batch are in hand, the second batch is held
    assert second_batch_started.wait(timeout=10)
    assert [chunk.text for chunk in first] == ["uno", "due"]
    assert calls == [["uno", "due"], ["tre"]]

    release.set()
    rest = list(stream)

    assert [chunk.text for chunk in rest] == ["tre"]


def test_numbering_and_total_span_all_the_batches(make_stub):
    stub = make_stub(_pipelines(lambda texts: [VECTOR for _ in texts]))
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew", text="uno due tre quattro cinque"
    )

    chunks = list(stub.EmbedContent(request))

    assert [chunk.number for chunk in chunks] == [1, 2, 3, 4, 5]
    assert all(chunk.total == 5 for chunk in chunks)
    assert [chunk.text for chunk in chunks] == [
        "uno",
        "due",
        "tre",
        "quattro",
        "cinque",
    ]


def test_an_error_on_a_later_batch_ends_with_internal(make_stub):
    calls = []

    def embed_texts(texts):
        calls.append(list(texts))
        if len(calls) == 2:
            raise RuntimeError("embedding backend down")
        return [VECTOR for _ in texts]

    stub = make_stub(_pipelines(embed_texts))
    stream = stub.EmbedContent(
        embedding_pb2.EmbedContentRequest(tenantId="mew", text="uno due tre")
    )

    received = []
    with pytest.raises(grpc.RpcError) as error:
        for chunk in stream:
            received.append(chunk.text)

    assert received == ["uno", "due"]
    assert error.value.code() == grpc.StatusCode.INTERNAL
