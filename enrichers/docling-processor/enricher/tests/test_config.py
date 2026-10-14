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

"""The settings of the module, read from the environment and from .env.

Each case builds its own Settings from an environment cleared of the module
variables, so that neither the session-wide variables of the conftest nor the
developer's .env leak into the assertions.
"""

import pytest
from pydantic import ValidationError

from app.config import Settings


@pytest.fixture(autouse=True)
def isolated_environment(monkeypatch):
    for field in Settings.model_fields:
        monkeypatch.delenv(field.upper(), raising=False)


def _settings():
    return Settings(_env_file=None)


def _error_fields(error):
    return {location for item in error.errors() for location in item["loc"]}


def test_variables_fall_back_to_their_defaults():
    settings = _settings()

    assert settings.datasource_host == "http://localhost:8001"
    assert settings.max_concurrent_conversions == 1
    assert settings.callback_timeout_seconds == 30.0
    assert settings.fetch_timeout_seconds == 30.0


def test_numeric_variables_are_numbers(monkeypatch):
    monkeypatch.setenv("MAX_CONCURRENT_CONVERSIONS", "3")
    monkeypatch.setenv("CALLBACK_TIMEOUT_SECONDS", "7.5")
    monkeypatch.setenv("FETCH_TIMEOUT_SECONDS", "12")

    settings = _settings()

    assert settings.max_concurrent_conversions == 3
    assert settings.callback_timeout_seconds == 7.5
    assert settings.fetch_timeout_seconds == 12.0


@pytest.mark.parametrize(
    "variable",
    [
        "MAX_CONCURRENT_CONVERSIONS",
        "CALLBACK_TIMEOUT_SECONDS",
        "FETCH_TIMEOUT_SECONDS",
    ],
)
def test_non_numeric_value_is_refused_naming_the_variable(monkeypatch, variable):
    monkeypatch.setenv(variable, "abc")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert variable.lower() in _error_fields(error.value)


def test_empty_variable_falls_back_to_the_default(monkeypatch):
    """The charts render an unset value as an empty string."""
    monkeypatch.setenv("MAX_CONCURRENT_CONVERSIONS", "")

    assert _settings().max_concurrent_conversions == 1


def test_datasource_host_is_kept_as_configured(monkeypatch):
    monkeypatch.setenv("DATASOURCE_HOST", "http://openk9-datasource:8080")

    assert _settings().datasource_host == "http://openk9-datasource:8080"


def test_variable_defined_only_in_the_env_file_is_loaded(tmp_path):
    env_file = tmp_path / ".env"
    env_file.write_text(
        "DATASOURCE_HOST=http://from-env-file\nFETCH_TIMEOUT_SECONDS=5\n"
    )

    settings = Settings(_env_file=env_file)

    assert settings.datasource_host == "http://from-env-file"
    assert settings.fetch_timeout_seconds == 5.0


def test_environment_wins_over_the_env_file(monkeypatch, tmp_path):
    monkeypatch.setenv("DATASOURCE_HOST", "http://from-environment")
    env_file = tmp_path / ".env"
    env_file.write_text("DATASOURCE_HOST=http://from-env-file\n")

    assert Settings(_env_file=env_file).datasource_host == "http://from-environment"


def test_variable_of_the_env_file_without_a_field_is_ignored(tmp_path):
    """A developer's .env may hold variables of other tools: they must not
    block the startup."""
    env_file = tmp_path / ".env"
    env_file.write_text("HF_HUB_OFFLINE=1\nMAX_CONCURRENT_CONVERSIONS=2\n")

    settings = Settings(_env_file=env_file)

    assert settings.max_concurrent_conversions == 2
    assert not hasattr(settings, "hf_hub_offline")


@pytest.mark.parametrize(
    "variable",
    [
        "MAX_CONCURRENT_CONVERSIONS",
        "CALLBACK_TIMEOUT_SECONDS",
        "FETCH_TIMEOUT_SECONDS",
    ],
)
@pytest.mark.parametrize("value", ["0", "-1"])
def test_non_positive_value_is_refused_naming_the_variable(
    monkeypatch, variable, value
):
    monkeypatch.setenv(variable, value)

    with pytest.raises(ValidationError) as error:
        _settings()

    assert variable.lower() in _error_fields(error.value)
