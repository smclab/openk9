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
import os

from app.external_services.grpc.grpc_client import (
    get_llm_configuration,
    get_rag_configuration,
)


def save_google_application_credentials(credentials):
    """
    Save Google Application credentials to a JSON file and configure environment variables.

    Serializes credentials to a JSON file and sets the GOOGLE_APPLICATION_CREDENTIALS environment
    variable to enable automatic credential discovery by Google Cloud client libraries.

    .. note::
        The environment variable modification only affects the current process and child processes.

    :param dict credentials: Dictionary containing Google Application credentials data.
        Expected to contain service account or user credential fields.
        Must be JSON-serializable (typically contains key/values with primitive types).

    :raises json.JSONEncodeError: If credentials contain non-serializable data types
    :raises OSError: If file writing operations fail (e.g., permission issues)

    Example::

        "credentials": {
            "account": "",
            "client_id": "client_id",
            "client_secret": "client_secret",
            "quota_project_id": "quota_project_id",
            "refresh_token": "refresh_token",
            "type": "type",
            "universe_domain": "universe_domain"
            }
        save_google_cloud_credentials(credentials)
    """
    json_credentials = json.dumps(credentials, indent=2, sort_keys=True)
    credential_file_path = "application_default_credentials.json"

    with open(credential_file_path, "w", encoding="utf-8") as outfile:
        outfile.write(json_credentials)

    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = credential_file_path


def get_configurations(
    rag_type,
    grpc_host,
    tenant_id,
):
    rag_configuration = get_rag_configuration(grpc_host, tenant_id, rag_type)
    llm_configuration = get_llm_configuration(grpc_host, tenant_id)

    configurations = {
        "rag_configuration": rag_configuration,
        "llm_configuration": llm_configuration,
    }

    return configurations
