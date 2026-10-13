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

Each case builds its own Settings from an environment holding only ORIGINS,
the one required variable, so that neither the session-wide variables of the
conftest nor the developer's .env leak into the assertions.
"""

from pathlib import Path

import pytest
from pydantic import ValidationError

from app.config import Settings

CHART_UPLOAD_FILE_EXTENSIONS = [".pdf", ".md", ".docx", ".xlsx", ".pptx", ".csv"]


@pytest.fixture(autouse=True)
def isolated_environment(monkeypatch):
    for field in Settings.model_fields:
        monkeypatch.delenv(field.upper(), raising=False)
    monkeypatch.setenv("ORIGINS", "http://localhost")


def _settings():
    return Settings(_env_file=None)


def _error_fields(error):
    return {location for item in error.errors() for location in item["loc"]}


def test_optional_variables_fall_back_to_their_defaults():
    settings = _settings()

    assert settings.opensearch_host is None
    assert settings.grpc_datasource_host is None
    assert settings.grpc_embedding_module_host is None
    assert settings.reranker_api_url is None
    assert settings.schedule is False
    assert settings.cron_expression == "0 0 0 ? * * *"
    assert settings.interval_in_days == 180
    assert settings.arize_phoenix_enabled is False
    assert settings.arize_phoenix_project_name == "default"
    assert settings.arize_phoenix_endpoint == "http://127.0.0.1:6006/v1/traces"
    assert settings.openk9_security_admin_password is None
    assert settings.logging_level == "INFO"
    assert settings.dependencies_logging_level == "INFO"
    assert settings.opensearch_verify_certs is True
    assert settings.opensearch_ca_certs is None
    assert settings.opensearch_username is None
    assert settings.opensearch_password is None


def test_upload_variables_default_to_the_chart_values():
    settings = _settings()

    assert settings.upload_dir == Path("uploads")
    assert settings.upload_file_extensions == CHART_UPLOAD_FILE_EXTENSIONS
    assert settings.max_upload_file_size == 10
    assert settings.max_upload_files_number == 5


@pytest.mark.parametrize(
    "variable",
    ["SCHEDULE", "ARIZE_PHOENIX_ENABLED", "OPENSEARCH_VERIFY_CERTS"],
)
@pytest.mark.parametrize(
    ("value", "expected"),
    [
        ("false", False),
        ("0", False),
        ("no", False),
        ("off", False),
        ("true", True),
        ("1", True),
        ("yes", True),
        ("on", True),
    ],
)
def test_boolean_variables_honour_their_value(monkeypatch, variable, value, expected):
    monkeypatch.setenv(variable, value)

    assert getattr(_settings(), variable.lower()) is expected


@pytest.mark.parametrize("variable", ["SCHEDULE", "ARIZE_PHOENIX_ENABLED"])
def test_empty_boolean_variable_falls_back_to_the_default(monkeypatch, variable):
    """The charts render an unset value as an empty string."""
    monkeypatch.setenv(variable, "")

    assert getattr(_settings(), variable.lower()) is False


def test_numeric_variable_is_an_integer(monkeypatch):
    monkeypatch.setenv("MAX_UPLOAD_FILES_NUMBER", "7")
    monkeypatch.setenv("INTERVAL_IN_DAYS", "30")

    settings = _settings()

    assert settings.max_upload_files_number == 7
    assert settings.interval_in_days == 30


@pytest.mark.parametrize(
    "variable",
    ["MAX_UPLOAD_FILE_SIZE", "MAX_UPLOAD_FILES_NUMBER", "INTERVAL_IN_DAYS"],
)
def test_non_numeric_value_is_refused_naming_the_variable(monkeypatch, variable):
    monkeypatch.setenv(variable, "abc")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert variable.lower() in _error_fields(error.value)


def test_invalid_boolean_value_is_refused_naming_the_variable(monkeypatch):
    monkeypatch.setenv("SCHEDULE", "maybe")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert "schedule" in _error_fields(error.value)


def test_missing_origins_is_refused_naming_the_variable(monkeypatch):
    monkeypatch.delenv("ORIGINS")

    with pytest.raises(ValidationError) as error:
        _settings()

    assert "origins" in _error_fields(error.value)


def test_origins_is_a_comma_separated_list(monkeypatch):
    monkeypatch.setenv("ORIGINS", "http://localhost,https://openk9.local")

    assert _settings().origins == ["http://localhost", "https://openk9.local"]


@pytest.mark.parametrize(
    "value",
    ['[".pdf",".md",".docx"]', ".pdf,.md,.docx"],
)
def test_upload_file_extensions_accept_json_and_comma_separated(monkeypatch, value):
    monkeypatch.setenv("UPLOAD_FILE_EXTENSIONS", value)

    assert _settings().upload_file_extensions == [".pdf", ".md", ".docx"]


def test_malformed_upload_file_extensions_are_refused(monkeypatch):
    monkeypatch.setenv("UPLOAD_FILE_EXTENSIONS", '[".pdf", ".md"')

    with pytest.raises(ValidationError) as error:
        _settings()

    assert "upload_file_extensions" in _error_fields(error.value)


def test_hosts_are_kept_as_configured(monkeypatch):
    """Hosts are passed through untouched: gRPC targets are host:port, and the
    OpenSearch host can be a comma-separated list of nodes."""
    monkeypatch.setenv("GRPC_DATASOURCE_HOST", "openk9-datasource:9000")
    monkeypatch.setenv("OPENSEARCH_HOST", "http://node-1:9200,https://node-2:9200")

    settings = _settings()

    assert settings.grpc_datasource_host == "openk9-datasource:9000"
    assert settings.opensearch_host == "http://node-1:9200,https://node-2:9200"


def test_variable_defined_only_in_the_env_file_is_loaded(monkeypatch, tmp_path):
    monkeypatch.delenv("ORIGINS")
    env_file = tmp_path / ".env"
    env_file.write_text("ORIGINS=http://from-env-file\nMAX_UPLOAD_FILE_SIZE=20\n")

    settings = Settings(_env_file=env_file)

    assert settings.origins == ["http://from-env-file"]
    assert settings.max_upload_file_size == 20


def test_environment_wins_over_the_env_file(monkeypatch, tmp_path):
    monkeypatch.setenv("MAX_UPLOAD_FILE_SIZE", "30")
    env_file = tmp_path / ".env"
    env_file.write_text("MAX_UPLOAD_FILE_SIZE=20\n")

    assert Settings(_env_file=env_file).max_upload_file_size == 30
