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

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Configuration of the module, read from the environment and from ``.env``.

    Every field maps to the environment variable with the same name in upper
    case. An empty variable falls back to the default, because the charts render
    unset values as empty strings.
    """

    model_config = SettingsConfigDict(env_file=".env", env_ignore_empty=True)

    logging_level: str = "INFO"
    do_eval: bool = False
    rabbitmq_host: str = "localhost"
    rabbitmq_user: str = "openk9"
    rabbitmq_password: str = "openk9"
    fetch_timeout_seconds: float = 30


settings = Settings()
