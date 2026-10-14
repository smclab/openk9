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
variables, so that the developer's environment and .env do not leak into the
assertions.
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

    assert settings.logging_level == "INFO"
    assert settings.do_eval is False
    assert settings.rabbitmq_host == "localhost"
    assert settings.rabbitmq_user == "openk9"
    assert settings.rabbitmq_password.get_secret_value() == "openk9"
    assert settings.fetch_timeout_seconds == 30.0


@pytest.mark.parametrize(
    ("value", "expected"),
    [
        ("false", False),
        ("0", False),
        ("no", False),
        ("off", False),
        ("true", True),
        ("True", True),
        ("TRUE", True),
        ("1", True),
        ("yes", True),
        ("on", True),
    ],
)
def test_do_eval_honours_its_value(monkeypatch, value, expected):
    monkeypatch.setenv("DO_EVAL", value)

    assert _settings().do_eval is expected


def test_empty_do_eval_falls_back_to_the_default(monkeypatch):
    """The charts render an unset value as an empty string."""
    monkeypatch.setenv("DO_EVAL", "")

    assert _settings().do_eval is False


def test_invalid_do_eval_is_refused_naming_the_variable(monkeypatch):
    monkeypatch.setenv("DO_EVAL", "maybe")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert "do_eval" in _error_fields(error.value)


def test_fetch_timeout_is_a_number(monkeypatch):
    monkeypatch.setenv("FETCH_TIMEOUT_SECONDS", "12.5")

    assert _settings().fetch_timeout_seconds == 12.5


def test_non_numeric_fetch_timeout_is_refused_naming_the_variable(monkeypatch):
    monkeypatch.setenv("FETCH_TIMEOUT_SECONDS", "abc")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert "fetch_timeout_seconds" in _error_fields(error.value)


def test_rabbitmq_variables_are_read(monkeypatch):
    monkeypatch.setenv("RABBITMQ_HOST", "rabbitmq")
    monkeypatch.setenv("RABBITMQ_USER", "user")
    monkeypatch.setenv("RABBITMQ_PASSWORD", "secret")

    settings = _settings()

    assert settings.rabbitmq_host == "rabbitmq"
    assert settings.rabbitmq_user == "user"
    assert settings.rabbitmq_password.get_secret_value() == "secret"


def test_the_old_rabbitmq_pass_name_is_no_longer_read(monkeypatch):
    """The charts pass the password as RABBITMQ_PASSWORD, like the other
    OpenK9 services do."""
    monkeypatch.setenv("RABBITMQ_PASS", "secret")

    assert _settings().rabbitmq_password.get_secret_value() == "openk9"


def test_variable_defined_only_in_the_env_file_is_loaded(tmp_path):
    env_file = tmp_path / ".env"
    env_file.write_text("LOGGING_LEVEL=DEBUG\nDO_EVAL=true\n")

    settings = Settings(_env_file=env_file)

    assert settings.logging_level == "DEBUG"
    assert settings.do_eval is True


def test_environment_wins_over_the_env_file(monkeypatch, tmp_path):
    monkeypatch.setenv("LOGGING_LEVEL", "WARNING")
    env_file = tmp_path / ".env"
    env_file.write_text("LOGGING_LEVEL=DEBUG\n")

    assert Settings(_env_file=env_file).logging_level == "WARNING"


def test_variable_of_the_env_file_without_a_field_is_ignored(tmp_path):
    """A developer's .env also holds the provider keys used by hand: they must
    not block the startup."""
    env_file = tmp_path / ".env"
    env_file.write_text("OPENAI_API_KEY=sk-test\nDO_EVAL=true\n")

    settings = Settings(_env_file=env_file)

    assert settings.do_eval is True
    assert not hasattr(settings, "openai_api_key")


def test_rabbitmq_password_is_hidden_from_the_representation(monkeypatch):
    monkeypatch.setenv("RABBITMQ_PASSWORD", "s3cret")

    settings = _settings()

    assert "s3cret" not in repr(settings)
    assert settings.rabbitmq_password.get_secret_value() == "s3cret"


@pytest.mark.parametrize("value", ["0", "-1"])
def test_non_positive_fetch_timeout_is_refused_naming_the_variable(monkeypatch, value):
    monkeypatch.setenv("FETCH_TIMEOUT_SECONDS", value)

    with pytest.raises(ValidationError) as error:
        _settings()

    assert "fetch_timeout_seconds" in _error_fields(error.value)
