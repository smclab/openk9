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

"""Whether the chat model accepts images, and what counts as the provider
rejecting them. Nobody declares it: Ollama is asked, then the LangChain profile
is read, and an unknown model is tried."""

from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import pytest
import requests
from langchain_ollama import ChatOllama

from app.rag import image_inputs


@pytest.fixture(autouse=True)
def _empty_caches():
    image_inputs._ollama_capabilities.clear()
    image_inputs._text_only_models.clear()
    yield
    image_inputs._ollama_capabilities.clear()
    image_inputs._text_only_models.clear()


def _show_response(capabilities):
    response = MagicMock()
    response.json.return_value = {"capabilities": capabilities}
    return response


def test_ollama_model_with_vision_accepts_images():
    llm = ChatOllama(model="moondream", base_url="http://ollama:11434")

    with patch.object(
        image_inputs.requests,
        "post",
        return_value=_show_response(["completion", "vision"]),
    ) as post:
        assert image_inputs.accepts_images(llm) is True

    post.assert_called_once_with(
        "http://ollama:11434/api/show", json={"model": "moondream"}, timeout=10
    )


def test_ollama_model_without_vision_is_text_only():
    llm = ChatOllama(model="smollm2:135m", base_url="http://ollama:11434")

    with patch.object(
        image_inputs.requests, "post", return_value=_show_response(["completion"])
    ):
        assert image_inputs.accepts_images(llm) is False


def test_ollama_capabilities_are_read_once_per_model():
    llm = ChatOllama(model="moondream", base_url="http://ollama:11434")

    with patch.object(
        image_inputs.requests, "post", return_value=_show_response(["vision"])
    ) as post:
        image_inputs.accepts_images(llm)
        image_inputs.accepts_images(llm)

    assert post.call_count == 1


def test_ollama_unreachable_is_unknown_and_asked_again():
    llm = ChatOllama(model="moondream", base_url="http://ollama:11434")

    with patch.object(
        image_inputs.requests,
        "post",
        side_effect=requests.ConnectionError("refused"),
    ) as post:
        assert image_inputs.accepts_images(llm) is None
        assert image_inputs.accepts_images(llm) is None

    assert post.call_count == 2


@pytest.mark.parametrize(
    "profile, expected",
    [
        ({"image_inputs": True}, True),
        ({"image_inputs": False}, False),
        ({}, None),
        (None, None),
    ],
)
def test_other_providers_read_the_langchain_profile(profile, expected):
    llm = SimpleNamespace(model_name="gpt-4o-mini", profile=profile)

    assert image_inputs.accepts_images(llm) is expected


def test_a_model_marked_text_only_stays_so():
    llm = SimpleNamespace(model_name="mistery", profile={})

    image_inputs.mark_text_only(llm)

    assert image_inputs.accepts_images(llm) is False
    other = SimpleNamespace(model_name="other", profile={})
    assert image_inputs.accepts_images(other) is None


class _StatusError(Exception):
    def __init__(self, status_code):
        super().__init__(f"status {status_code}")
        self.status_code = status_code


class APIConnectionError(Exception):
    pass


class ReadTimeoutError(Exception):
    pass


class _BotocoreLike(Exception):
    def __init__(self, status):
        super().__init__("ValidationException")
        self.response = {"ResponseMetadata": {"HTTPStatusCode": status}}


@pytest.mark.parametrize(
    "error, rejected",
    [
        (_StatusError(400), True),
        (_StatusError(422), True),
        (_BotocoreLike(400), True),
        (ValueError("this model does not accept images"), True),
        (_StatusError(429), False),
        (Exception("rate_limit_exceeded"), False),
        (_StatusError(401), False),
        (_StatusError(403), False),
        (_StatusError(404), False),
        (_BotocoreLike(403), False),
        (_StatusError(500), False),
        (_StatusError(503), False),
        (_BotocoreLike(502), False),
        (APIConnectionError("dropped"), False),
        (ReadTimeoutError("slow"), False),
    ],
)
def test_what_counts_as_a_rejection(error, rejected):
    assert image_inputs.is_rejection(error) is rejected
