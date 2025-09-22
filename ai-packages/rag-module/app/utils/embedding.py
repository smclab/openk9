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

from app.external_services.grpc.grpc_client import (
    generate_documents_embeddings,
    get_embedding_model_configuration,
)


def documents_embedding(
    grpc_host_datasource, grpc_host_embedding, virtual_host, document
):
    embedding_model_configuration = get_embedding_model_configuration(
        grpc_host=grpc_host_datasource,
        virtual_host=virtual_host,
    )

    api_url = embedding_model_configuration.get("api_url")
    api_key = embedding_model_configuration.get("api_key")
    model_type = embedding_model_configuration.get("model_type")
    model = embedding_model_configuration.get("model")
    json_config = embedding_model_configuration.get("json_config")

    chunk = {"type": 1, "jsonConfig": json_config}
    provider_model = {"provider": model_type, "model": model}
    embedding_model = {
        "apiKey": api_key,
        "providerModel": provider_model,
        "jsonConfig": json_config,
    }

    embedded_documents = generate_documents_embeddings(
        grpc_host_embedding, chunk, embedding_model, document
    )

    return embedded_documents
