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

"""The Ollama embedder speaks /api/embed: plain strings for text, objects
for images and mixed inputs, one vector per input. A mock transport stands
in for the server."""

import base64
import json
import logging

import httpx
import pytest

from app.embedding import ollama
from app.embedding.ollama import OllamaMultimodalEmbedder
from app.embedding.router import EmbedFailed, SkipRef

GEMMA = "embeddinggemma-2:740m"
PNG = b"png-bytes"
PNG_B64 = base64.b64encode(PNG).decode()
# the real /api/show call, before the autouse fixture fakes it
REAL_SHOW = ollama._show


class _Server:
    """Answers /api/embed with one vector per input, or with what the test
    sets; records the bodies."""

    def __init__(self, status=200, answer=None):
        self.status = status
        self.answer = answer
        self.bodies = []

    def __call__(self, request):
        body = json.loads(request.content)
        self.bodies.append((str(request.url), body))
        answer = self.answer
        if answer is None:
            answer = {"embeddings": [[0.1, 0.2, 0.3] for _ in body["input"]]}
        return httpx.Response(self.status, json=answer)


def _embedder(server, model=GEMMA, **kwargs):
    client = httpx.Client(transport=httpx.MockTransport(server))
    return OllamaMultimodalEmbedder(
        model, "http://ollama:11434/", client=client, **kwargs
    )


def test_documents_go_as_plain_strings_without_prefix():
    server = _Server()

    vectors = _embedder(server).embed_texts(["uno", "due"], "search_document")

    assert vectors == [[0.1, 0.2, 0.3]] * 2
    url, body = server.bodies[0]
    assert url == "http://ollama:11434/api/embed"
    # the request langchain sends: an index built with it stays valid
    assert body == {
        "model": GEMMA,
        "input": ["uno", "due"],
        "keep_alive": ollama.DEFAULT_KEEP_ALIVE,
    }


def test_an_embeddinggemma_query_takes_the_search_prefix():
    server = _Server()

    _embedder(server).embed_texts(["sentina"], "search_query")

    assert server.bodies[0][1]["input"] == ["task: search result | query: sentina"]


def test_a_query_of_another_model_goes_as_it_is():
    server = _Server()

    _embedder(server, model="other-vision-embed").embed_texts(["sentina"], "search_query")

    assert server.bodies[0][1]["input"] == ["sentina"]


def test_no_text_no_request():
    server = _Server()

    assert _embedder(server).embed_texts([], "search_document") == []
    assert server.bodies == []


def test_an_image_goes_as_an_image_object():
    server = _Server()

    vector = _embedder(server).embed_image(PNG, "image/png")

    assert vector == [0.1, 0.2, 0.3]
    assert server.bodies[0][1]["input"] == [{"image": PNG_B64}]


def test_a_mixed_query_is_one_object_with_text_and_image():
    server = _Server()

    vector = _embedder(server).embed_mixed("sentina", PNG, "image/png")

    assert vector == [0.1, 0.2, 0.3]
    assert server.bodies[0][1]["input"] == [
        {"text": "task: search result | query: sentina", "image": PNG_B64}
    ]


def test_a_mixed_document_takes_no_prefix():
    server = _Server()

    _embedder(server).embed_mixed(
        "title: Manuale\n", PNG, "image/png", input_type="search_document"
    )

    assert server.bodies[0][1]["input"] == [{"text": "title: Manuale\n", "image": PNG_B64}]


def test_keep_alive_and_num_gpu_reach_ollama_as_integers():
    server = _Server()

    _embedder(server, keep_alive=60.0, num_gpu=0.0).embed_image(PNG, "image/png")

    body = server.bodies[0][1]
    assert body["keep_alive"] == 60 and type(body["keep_alive"]) is int
    assert body["options"] == {"num_gpu": 0}
    assert type(body["options"]["num_gpu"]) is int


@pytest.mark.parametrize(
    "content_type", ["image/jpeg", "image/png", "image/gif", "image/webp", "image/JPEG; q=1"]
)
def test_the_formats_ollama_decodes_are_sent(content_type):
    server = _Server()

    _embedder(server).embed_image(PNG, content_type)

    assert server.bodies[0][1]["input"] == [{"image": PNG_B64}]


@pytest.mark.parametrize("content_type", ["image/bmp", "image/tiff", "image/svg+xml", None])
def test_a_format_ollama_does_not_decode_is_skipped(content_type):
    server = _Server()
    embedder = _embedder(server)

    with pytest.raises(SkipRef, match="PNG, JPEG, GIF and WebP"):
        embedder.embed_image(PNG, content_type)
    with pytest.raises(SkipRef):
        embedder.embed_mixed("title: x\n", PNG, content_type, input_type="search_document")
    assert server.bodies == []


@pytest.mark.parametrize("status", [400, 500])
def test_an_error_of_the_server_fails_the_embedding(status):
    server = _Server(status=status, answer={"error": "failed to process inputs"})

    with pytest.raises(EmbedFailed, match="failed"):
        _embedder(server).embed_image(PNG, "image/png")


@pytest.mark.parametrize(
    "answer",
    [
        pytest.param({"embeddings": [[0.1]]}, id="one vector for two"),
        pytest.param({"embeddings": [[0.1], []]}, id="an empty vector"),
        pytest.param({}, id="no embeddings"),
    ],
)
def test_a_short_answer_fails_the_embedding(answer):
    server = _Server(answer=answer)

    with pytest.raises(EmbedFailed, match="for 2 inputs"):
        _embedder(server).embed_texts(["uno", "due"], "search_document")


@pytest.mark.parametrize(
    ("capabilities", "multimodal"),
    [
        pytest.param(["embedding", "vision", "audio"], True, id="embedding and vision"),
        pytest.param(["embedding"], False, id="text only"),
        pytest.param(["completion", "vision"], False, id="not an embedding model"),
    ],
)
def test_the_capabilities_decide(ollama_server, capabilities, multimodal):
    ollama_server.show = {"capabilities": capabilities}

    assert ollama.is_multimodal("http://ollama:11434", GEMMA) is multimodal


def test_the_answer_is_kept_per_server_and_model(ollama_server):
    ollama_server.show = {"capabilities": ["embedding", "vision"]}

    for _ in range(3):
        ollama.is_multimodal("http://ollama:11434", GEMMA)
    ollama.is_multimodal("http://other:11434", GEMMA)

    assert ollama_server.asked == [
        ("http://ollama:11434", GEMMA),
        ("http://other:11434", GEMMA),
    ]


def test_a_server_that_does_not_answer_leaves_the_model_text_only(
    ollama_server, caplog, monkeypatch
):
    ollama_server.show = httpx.ConnectError("connection refused")
    clock = [1000.0]
    monkeypatch.setattr(ollama, "_now", lambda: clock[0])

    with caplog.at_level(logging.WARNING, logger=ollama.logger.name):
        assert ollama.is_multimodal("http://ollama:11434", GEMMA) is False

    assert "treated as text only: /api/show failed" in caplog.text
    # the rest of the request does not ask again
    clock[0] += ollama.PROBE_RETRY_SECONDS - 1
    assert ollama.is_multimodal("http://ollama:11434", GEMMA) is False
    assert len(ollama_server.asked) == 1
    # a later request does, and finds the server back
    clock[0] += 2
    ollama_server.show = {"capabilities": ["embedding", "vision"]}
    assert ollama.is_multimodal("http://ollama:11434", GEMMA) is True
    assert len(ollama_server.asked) == 2


def test_an_api_url_without_scheme_takes_http(ollama_server):
    ollama_server.show = {"capabilities": ["embedding", "vision"]}

    ollama.is_multimodal("ollama:11434/", GEMMA)

    assert ollama_server.asked == [("http://ollama:11434", GEMMA)]
    assert OllamaMultimodalEmbedder(GEMMA, "ollama:11434").url == (
        "http://ollama:11434/api/embed"
    )


def test_a_server_without_capabilities_leaves_the_model_text_only(
    ollama_server, caplog
):
    ollama_server.show = {"modelfile": "FROM x"}

    with caplog.at_level(logging.WARNING, logger=ollama.logger.name):
        assert ollama.is_multimodal("http://ollama:11434", GEMMA) is False

    assert "lists no capabilities" in caplog.text


def test_show_asks_the_model_and_the_version(monkeypatch):
    seen = []

    def handler(request):
        seen.append((request.method, request.url.path, request.content))
        if request.url.path == "/api/version":
            return httpx.Response(200, json={"version": "0.40.16"})
        return httpx.Response(200, json={"capabilities": ["embedding", "vision"]})

    client = httpx.Client(transport=httpx.MockTransport(handler))
    monkeypatch.setattr(ollama.httpx, "post", client.post)
    monkeypatch.setattr(ollama.httpx, "get", client.get)

    show, version = REAL_SHOW("http://ollama:11434", GEMMA)

    assert show == {"capabilities": ["embedding", "vision"]}
    assert version == "0.40.16"
    assert seen[0][:2] == ("POST", "/api/show")
    assert json.loads(seen[0][2]) == {"model": GEMMA}


def test_a_server_without_version_is_still_recognized(monkeypatch):
    def handler(request):
        if request.url.path == "/api/version":
            return httpx.Response(404, text="404 page not found")
        return httpx.Response(200, json={"capabilities": ["embedding", "vision"]})

    client = httpx.Client(transport=httpx.MockTransport(handler))
    monkeypatch.setattr(ollama.httpx, "post", client.post)
    monkeypatch.setattr(ollama.httpx, "get", client.get)

    assert REAL_SHOW("http://ollama:11434", GEMMA) == (
        {"capabilities": ["embedding", "vision"]},
        None,
    )


def test_a_version_that_is_not_json_is_none(monkeypatch):
    def handler(request):
        if request.url.path == "/api/version":
            return httpx.Response(200, text="<html>proxy</html>")
        return httpx.Response(200, json={"capabilities": ["embedding", "vision"]})

    client = httpx.Client(transport=httpx.MockTransport(handler))
    monkeypatch.setattr(ollama.httpx, "post", client.post)
    monkeypatch.setattr(ollama.httpx, "get", client.get)

    assert REAL_SHOW("http://ollama:11434", GEMMA)[1] is None
