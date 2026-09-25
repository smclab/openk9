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

"""With DO_EVAL=true the v1 GetMessages path publishes the chunks and their
vectors to RabbitMQ for the chunk evaluation; without it, nothing is sent.
A broker failure never costs the response."""

import json
from types import SimpleNamespace

import pytest

from app import server as server_module
from app.external_services.grpc.embedding import embedding_pb2


class _FakeChannel:
    def __init__(self, published):
        self.published = published

    def basic_publish(self, exchange, routing_key, body, properties):
        self.published.append(
            {
                "exchange": exchange,
                "routing_key": routing_key,
                "body": json.loads(body),
                "properties": properties,
            }
        )


class _FakeConnection:
    def __init__(self, published, error):
        self.published = published
        self.error = error
        self.closed = False

    def channel(self):
        if self.error is not None:
            raise self.error
        return _FakeChannel(self.published)

    def close(self):
        self.closed = True


@pytest.fixture
def broker(monkeypatch):
    """Replaces pika with a fake broker recording connections and messages."""
    broker = SimpleNamespace(connections=[], published=[], error=None)

    def blocking_connection(parameters):
        connection = _FakeConnection(broker.published, broker.error)
        broker.connections.append((parameters, connection))
        return connection

    monkeypatch.setattr(
        server_module,
        "pika",
        SimpleNamespace(
            PlainCredentials=lambda user, password: ("credentials", user, password),
            ConnectionParameters=lambda host, credentials: (host, credentials),
            BlockingConnection=blocking_connection,
            BasicProperties=lambda **kwargs: kwargs,
        ),
    )
    monkeypatch.setattr(
        server_module,
        "build_text_embed_texts",
        lambda configuration: lambda texts: [[float(len(text))] for text in texts],
    )
    for variable in ("DO_EVAL", "RABBITMQ_HOST", "RABBITMQ_USER", "RABBITMQ_PASS"):
        monkeypatch.delenv(variable, raising=False)

    return broker


def _get_messages(stub):
    chunk = embedding_pb2.RequestChunk(type=1)
    chunk.jsonConfig.update({"chunk_size": 2048})

    return stub.GetMessages(
        embedding_pb2.EmbeddingRequest(
            chunk=chunk,
            embeddingModel=embedding_pb2.EmbeddingModel(),
            text="<p>pikachu</p>",
        )
    )


def test_without_do_eval_nothing_is_published(stub, broker):
    response = _get_messages(stub)

    assert [chunk.text for chunk in response.chunks] == ["pikachu"]
    assert broker.connections == []


@pytest.mark.parametrize("value", ["true", "True", "TRUE"])
def test_do_eval_publishes_the_chunks_and_the_text(stub, broker, monkeypatch, value):
    monkeypatch.setenv("DO_EVAL", value)
    monkeypatch.setenv("RABBITMQ_HOST", "rabbit")

    _get_messages(stub)

    [(parameters, connection)] = broker.connections
    assert parameters == ("rabbit", ("credentials", "openk9", "openk9"))
    assert broker.published == [
        {
            "exchange": "main.exchange",
            "routing_key": "main",
            "body": {
                "chunks": [{"chunk_0": {"text": "pikachu", "embedding": [7.0]}}],
                "text": "pikachu",
            },
            "properties": {"delivery_mode": 2, "headers": {"retry-count": 0}},
        }
    ]
    assert connection.closed


def test_a_broker_failure_keeps_the_response(stub, broker, monkeypatch):
    monkeypatch.setenv("DO_EVAL", "true")
    broker.error = ConnectionError("broker down")

    response = _get_messages(stub)

    assert [chunk.text for chunk in response.chunks] == ["pikachu"]
    assert broker.published == []
    [(_, connection)] = broker.connections
    assert connection.closed
