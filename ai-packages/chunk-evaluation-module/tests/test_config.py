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
variables, so that the conftest, the developer's environment and .env do not
leak into the assertions.
"""

import pytest
from config import Settings
from pydantic import ValidationError


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

    assert settings.log_file == "/tmp/app.log"
    assert settings.rabbitmq_host == "localhost"
    assert settings.rabbitmq_user == "user"
    assert settings.rabbitmq_pass.get_secret_value() == "pass"
    assert settings.rabbitmq_retry_delay_ms == 5000
    assert settings.rabbitmq_max_retries == 3
    assert settings.min_time_delay_minutes == 5
    assert settings.buffer_treshold == 50
    assert settings.buffer_delay == 20
    assert settings.buffer_upload is False
    assert settings.n_max_documents == 150
    assert settings.do_experiments is False
    assert settings.experiment_mode == 1
    assert settings.phoenix_project_name == "test-eval-module"
    assert settings.phoenix_collector_endpoint == "https://phoenix.openk9.io/"
    assert settings.polling_frequency == 2.0
    assert settings.dataset_prefix == "dataset"
    assert settings.experiment_prefix == "experiment"


def test_rabbitmq_numbers_are_integers_when_set(monkeypatch):
    """They used to stay strings when the variable was set."""
    monkeypatch.setenv("RABBITMQ_MAX_RETRIES", "5")
    monkeypatch.setenv("RABBITMQ_RETRY_DELAY_MS", "10000")

    settings = _settings()

    assert settings.rabbitmq_max_retries == 5
    assert settings.rabbitmq_retry_delay_ms == 10000


@pytest.mark.parametrize(
    "variable",
    [
        "RABBITMQ_MAX_RETRIES",
        "RABBITMQ_RETRY_DELAY_MS",
        "MIN_TIME_DELAY_MINUTES",
        "BUFFER_TRESHOLD",
        "BUFFER_DELAY",
        "N_MAX_DOCUMENTS",
        "EXPERIMENT_MODE",
        "POLLING_FREQUENCY",
    ],
)
def test_non_numeric_value_is_refused_naming_the_variable(monkeypatch, variable):
    monkeypatch.setenv(variable, "abc")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert variable.lower() in _error_fields(error.value)


@pytest.mark.parametrize("variable", ["BUFFER_UPLOAD", "DO_EXPERIMENTS"])
@pytest.mark.parametrize(
    ("value", "expected"),
    [
        ("false", False),
        ("False", False),
        ("0", False),
        ("no", False),
        ("true", True),
        ("True", True),
        ("1", True),
        ("yes", True),
    ],
)
def test_boolean_variables_honour_their_value(monkeypatch, variable, value, expected):
    monkeypatch.setenv(variable, value)

    assert getattr(_settings(), variable.lower()) is expected


@pytest.mark.parametrize("variable", ["BUFFER_UPLOAD", "DO_EXPERIMENTS"])
def test_empty_boolean_variable_falls_back_to_the_default(monkeypatch, variable):
    """The charts render an unset value as an empty string."""
    monkeypatch.setenv(variable, "")

    assert getattr(_settings(), variable.lower()) is False


def test_invalid_boolean_value_is_refused_naming_the_variable(monkeypatch):
    monkeypatch.setenv("DO_EXPERIMENTS", "maybe")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert "do_experiments" in _error_fields(error.value)


def test_variable_defined_only_in_the_env_file_is_loaded(tmp_path):
    env_file = tmp_path / ".env"
    env_file.write_text("RABBITMQ_HOST=rabbitmq\nRABBITMQ_MAX_RETRIES=10\n")

    settings = Settings(_env_file=env_file)

    assert settings.rabbitmq_host == "rabbitmq"
    assert settings.rabbitmq_max_retries == 10


def test_environment_wins_over_the_env_file(monkeypatch, tmp_path):
    monkeypatch.setenv("RABBITMQ_HOST", "from-environment")
    env_file = tmp_path / ".env"
    env_file.write_text("RABBITMQ_HOST=from-env-file\n")

    assert Settings(_env_file=env_file).rabbitmq_host == "from-environment"


def test_variable_of_the_env_file_without_a_field_is_ignored(tmp_path):
    """The .env.example also lists PHOENIX_API_KEY, which the phoenix library
    reads by itself: it must not block the startup."""
    env_file = tmp_path / ".env"
    env_file.write_text("PHOENIX_API_KEY=key\nRABBITMQ_HOST=rabbitmq\n")

    settings = Settings(_env_file=env_file)

    assert settings.rabbitmq_host == "rabbitmq"
    assert not hasattr(settings, "phoenix_api_key")


def test_rabbitmq_password_is_hidden_from_the_representation(monkeypatch):
    monkeypatch.setenv("RABBITMQ_PASS", "s3cret")

    settings = _settings()

    assert "s3cret" not in repr(settings)
    assert settings.rabbitmq_pass.get_secret_value() == "s3cret"


@pytest.mark.parametrize(
    "variable",
    ["MIN_TIME_DELAY_MINUTES", "N_MAX_DOCUMENTS", "POLLING_FREQUENCY"],
)
def test_zero_is_refused_where_a_positive_value_is_needed(monkeypatch, variable):
    monkeypatch.setenv(variable, "0")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert variable.lower() in _error_fields(error.value)


@pytest.mark.parametrize(
    "variable",
    [
        "RABBITMQ_RETRY_DELAY_MS",
        "RABBITMQ_MAX_RETRIES",
        "BUFFER_TRESHOLD",
        "BUFFER_DELAY",
    ],
)
def test_zero_is_accepted_and_negative_values_are_refused(monkeypatch, variable):
    monkeypatch.setenv(variable, "0")
    assert getattr(_settings(), variable.lower()) == 0

    monkeypatch.setenv(variable, "-1")
    with pytest.raises(ValidationError) as error:
        _settings()

    assert variable.lower() in _error_fields(error.value)
