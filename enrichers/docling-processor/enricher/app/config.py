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

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


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

    datasource_host: str = "http://localhost:8001"
    max_concurrent_conversions: int = Field(1, gt=0)
    callback_timeout_seconds: float = Field(30, gt=0)
    fetch_timeout_seconds: float = Field(30, gt=0)


settings = Settings()
