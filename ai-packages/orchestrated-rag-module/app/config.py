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

import json
from pathlib import Path
from typing import Annotated

from pydantic import Field, SecretStr, field_validator
from pydantic_settings import BaseSettings, NoDecode, SettingsConfigDict


def parse_upload_file_extensions(value: str) -> list:
    """
    Read the allowed upload extensions from their setting.

    The value is either a JSON array, as shipped by the charts and the compose
    file, or a comma-separated list. An empty value allows no extension.

    :param value: The raw UPLOAD_FILE_EXTENSIONS setting
    :type value: str

    :return: The allowed extensions, e.g. ['.pdf', '.docx']
    :rtype: list

    :raises json.JSONDecodeError: If the value starts as a JSON array but is not one
    """
    value = value.strip()
    if value.startswith("["):
        return json.loads(value)
    return [extension.strip() for extension in value.split(",") if extension.strip()]


class Settings(BaseSettings):
    """Configuration of the module, read from the environment and from ``.env``.

    Every field maps to the environment variable with the same name in upper
    case. An empty variable falls back to the default, because the charts render
    unset values as empty strings. Variables of the .env that no field declares
    are ignored, as the environment ones already are.
    """

    model_config = SettingsConfigDict(
        env_file=".env", env_ignore_empty=True, extra="ignore"
    )

    origins: Annotated[list[str], NoDecode]
    opensearch_host: str | None = None
    grpc_datasource_host: str | None = None
    grpc_embedding_module_host: str | None = None
    reranker_api_url: str | None = None
    schedule: bool = False
    cron_expression: str = "0 0 0 ? * * *"
    interval_in_days: int = Field(180, gt=0)
    arize_phoenix_enabled: bool = False
    arize_phoenix_project_name: str = "default"
    arize_phoenix_endpoint: str = "http://127.0.0.1:6006/v1/traces"
    upload_dir: Path = Path("uploads")
    upload_file_extensions: Annotated[list[str], NoDecode] = [
        ".pdf",
        ".md",
        ".docx",
        ".xlsx",
        ".pptx",
        ".csv",
    ]
    max_upload_file_size: int = Field(10, gt=0)
    max_upload_files_number: int = Field(5, gt=0)
    openk9_security_admin_password: SecretStr | None = None
    logging_level: str = "INFO"
    dependencies_logging_level: str = "INFO"
    opensearch_verify_certs: bool = True
    opensearch_ca_certs: str | None = None
    opensearch_username: str | None = None
    opensearch_password: SecretStr | None = None

    @field_validator("origins", mode="before")
    @classmethod
    def split_origins(cls, value):
        if isinstance(value, str):
            return value.split(",")
        return value

    @field_validator("upload_file_extensions", mode="before")
    @classmethod
    def parse_extensions(cls, value):
        if isinstance(value, str):
            return parse_upload_file_extensions(value)
        return value


settings = Settings()
