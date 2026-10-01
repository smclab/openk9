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

"""A custom provider is an OpenAI-compatible endpoint, reached with the key
of its own model.

The key goes to the client and not to the process environment: without it the
client found no key at all, unless an earlier request had left one behind.
"""

import os

import pytest

from app import server as server_module


class _CapturingOpenAIEmbeddings:
    def __init__(self, seen, **kwargs):
        seen.update(kwargs)


def _initialize(monkeypatch, **configuration):
    seen = {}
    monkeypatch.setattr(
        server_module,
        "OpenAIEmbeddings",
        lambda **kwargs: _CapturingOpenAIEmbeddings(seen, **kwargs),
    )
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)

    server_module.initialize_embedding_model(
        {"model_type": "my-gateway", "model": "my-embedder", **configuration}
    )

    return seen


@pytest.mark.parametrize(
    "api_url",
    [
        pytest.param("http://gateway:8080/v1", id="with endpoint"),
        pytest.param(None, id="without endpoint"),
    ],
)
def test_a_custom_provider_gets_the_key_of_its_model(monkeypatch, api_url):
    seen = _initialize(monkeypatch, api_key="gateway-key", api_url=api_url)

    assert seen["model"] == "my-embedder"
    assert seen["api_key"] == "gateway-key"
    assert seen.get("base_url") == api_url
    # the key stays with the client
    assert "OPENAI_API_KEY" not in os.environ


def test_a_custom_provider_without_a_key_leaves_it_to_the_environment(monkeypatch):
    seen = _initialize(monkeypatch, api_key="", api_url="http://gateway:8080/v1")

    assert seen["api_key"] is None
