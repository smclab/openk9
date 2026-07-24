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

"""In-process EmbedContent server with fake embedders and a fake object
store, so the tests exercise the wire contract (stream, provenance,
quantization) without models or network. The heavy import of app.server
is confined to this folder."""

from concurrent import futures

import grpc
import pytest

from app import server as server_module
from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2_grpc

DIM = 8
# norm 5 -> normalizes to [0.6, 0, 0, 0, 0.8, 0, 0, 0]
TEXT_VECTOR = [3.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0]
IMAGE_VECTOR = [0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]

STORAGE = {
    "https://signed/img-1": (b"png-1", "image/png"),
    "https://signed/img-2": (b"png-2", "image/png"),
    "https://signed/clip-1": (b"wav-1", "audio/wav"),
}


def fake_build_pipelines(configuration, chunker, is_query=False):
    return Pipelines(
        embed_texts=lambda texts: [TEXT_VECTOR for _ in texts],
        # split on whitespace: survives clean_text, unlike punctuation
        chunk=lambda text: text.split(),
        fetch=lambda url: STORAGE[url],
        embed_image=lambda data, content_type: IMAGE_VECTOR,
    )


@pytest.fixture
def make_stub(monkeypatch):
    """Starts an in-process Embedding server and returns a stub. Accepts a
    build_pipelines override (e.g. one whose embedder raises)."""
    channels = []
    servers = []

    def _make(build_pipelines=fake_build_pipelines):
        monkeypatch.setattr(server_module, "build_pipelines", build_pipelines)
        monkeypatch.setattr(
            server_module.chunking, "build_chunker", lambda chunk_type, config: None
        )

        grpc_server = grpc.server(futures.ThreadPoolExecutor(max_workers=2))
        embedding_pb2_grpc.add_EmbeddingServicer_to_server(
            server_module.EmbeddingServicer(), grpc_server
        )
        port = grpc_server.add_insecure_port("localhost:0")
        grpc_server.start()

        channel = grpc.insecure_channel(f"localhost:{port}")
        channels.append(channel)
        servers.append(grpc_server)

        return embedding_pb2_grpc.EmbeddingStub(channel)

    yield _make

    for channel in channels:
        channel.close()
    for grpc_server in servers:
        grpc_server.stop(None)


@pytest.fixture
def stub(make_stub):
    return make_stub()
