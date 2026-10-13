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

    log_file: str = "/tmp/app.log"
    rabbitmq_host: str = "localhost"
    rabbitmq_user: str = "user"
    rabbitmq_pass: str = "pass"
    rabbitmq_retry_delay_ms: int = 5000
    rabbitmq_max_retries: int = 3
    min_time_delay_minutes: int = 5
    buffer_treshold: int = 50
    buffer_delay: int = 20
    buffer_upload: bool = False
    n_max_documents: int = 150
    do_experiments: bool = False
    experiment_mode: int = 1
    phoenix_project_name: str = "test-eval-module"
    phoenix_collector_endpoint: str = "https://phoenix.openk9.io/"
    polling_frequency: float = 2
    dataset_prefix: str = "dataset"
    experiment_prefix: str = "experiment"


settings = Settings()
