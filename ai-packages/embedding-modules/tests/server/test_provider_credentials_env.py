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

"""The apiKey of the embedding model reaches the environment variable its
provider client reads; Vertex gets the ADC file instead."""

import os

import pytest

from app import server as server_module

PROVIDER_VARIABLES = (
    "OPENAI_API_KEY",
    "WATSONX_APIKEY",
    "AWS_BEARER_TOKEN_BEDROCK",
)


@pytest.fixture(autouse=True)
def _restore_environment(monkeypatch):
    # records the original values, so what the function writes is undone
    for variable in PROVIDER_VARIABLES:
        monkeypatch.setenv(variable, "")


@pytest.mark.parametrize(
    "provider, variable",
    [
        ("openai", "OPENAI_API_KEY"),
        ("watsonx", "WATSONX_APIKEY"),
        ("aws_bedrock", "AWS_BEARER_TOKEN_BEDROCK"),
    ],
)
def test_the_api_key_is_exported_for_the_provider(provider, variable):
    server_module._apply_credentials({"model_type": provider, "api_key": "secret"})

    assert os.environ[variable] == "secret"


def test_the_provider_defaults_to_openai():
    server_module._apply_credentials({"api_key": "secret"})

    assert os.environ["OPENAI_API_KEY"] == "secret"


def test_bedrock_without_a_token_leaves_the_aws_credential_chain():
    server_module._apply_credentials({"model_type": "aws_bedrock", "api_key": ""})

    assert os.environ["AWS_BEARER_TOKEN_BEDROCK"] == ""


def test_vertex_credentials_are_saved_as_the_adc_file(monkeypatch):
    saved = []
    monkeypatch.setattr(
        server_module, "save_google_application_credentials", saved.append
    )
    credentials = {"quota_project_id": "project-id"}

    server_module._apply_credentials(
        {
            "model_type": "chat_vertex_ai",
            "api_key": "",
            "chat_vertex_ai_model_garden": {"credentials": credentials},
        }
    )

    assert saved == [credentials]


def test_vertex_without_credentials_writes_no_file(monkeypatch):
    saved = []
    monkeypatch.setattr(
        server_module, "save_google_application_credentials", saved.append
    )

    server_module._apply_credentials(
        {"model_type": "chat_vertex_ai", "chat_vertex_ai_model_garden": None}
    )

    assert saved == []
