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

"""The Ollama keys of the embedding model jsonConfig reach OllamaEmbeddings:
keep_alive keeps the embedder resident between two runs of the indexing, and
num_gpu=0 runs it on CPU so it stops fighting the generator for the VRAM of a
single Ollama."""

from app import server as server_module
from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

OLLAMA_CONFIGURATION = {
    "model_type": "ollama",
    "model": "qwen3-embedding:0.6b",
    "api_url": "http://localhost:11434",
    "api_key": "",
}


def _capturing_pipelines(seen):
    def build_pipelines(configuration, chunker):
        seen.update(configuration)

        return Pipelines(
            embed_texts=lambda texts: [[1.0, 0, 0, 0, 0, 0, 0, 0] for _ in texts],
            chunk=lambda text: text.split(),
            fetch=lambda url: (b"", None),
            embed_image=None,
        )

    return build_pipelines


def _embed_content(make_stub, json_config):
    """Run one EmbedContent through the wire and return the configuration the
    jsonConfig was decoded into."""
    seen = {}
    stub = make_stub(_capturing_pipelines(seen))
    model = embedding_pb2.EmbeddingModel(
        apiUrl="http://localhost:11434",
        providerModel=embedding_pb2.ProviderModel(
            provider="ollama", model="qwen3-embedding:0.6b"
        ),
    )
    model.jsonConfig.update(json_config)

    list(
        stub.EmbedContent(
            embedding_pb2.EmbedContentRequest(
                tenantId="mew", embeddingModel=model, text="uno"
            )
        )
    )

    return seen


def _initialize(**overrides):
    return server_module.initialize_embedding_model(
        {**OLLAMA_CONFIGURATION, **overrides}
    )


def test_ollama_keys_read_from_json_config(make_stub):
    configuration = _embed_content(make_stub, {"keep_alive": 60, "num_gpu": 0})

    assert configuration["keep_alive"] == 60
    assert configuration["num_gpu"] == 0


def test_ollama_keys_absent_from_json_config(make_stub):
    configuration = _embed_content(make_stub, {})

    assert configuration["keep_alive"] is None
    assert configuration["num_gpu"] is None


def test_ollama_parameters_from_configuration():
    embeddings = _initialize(keep_alive=60, num_gpu=0)

    assert embeddings.keep_alive == 60
    assert embeddings.num_gpu == 0


def test_ollama_defaults_without_configuration():
    embeddings = _initialize()

    assert embeddings.keep_alive == server_module.DEFAULT_KEEP_ALIVE
    # no cap sent: the split between VRAM and RAM stays with Ollama
    assert embeddings.num_gpu is None


def test_keep_alive_zero_is_not_replaced_by_the_default():
    embeddings = _initialize(keep_alive=0)

    assert embeddings.keep_alive == 0


def test_json_config_numbers_reach_ollama_as_integers():
    # a gRPC Struct decodes numbers to float, and Ollama rejects a float where
    # it expects a count of seconds or of layers
    embeddings = _initialize(keep_alive=60.0, num_gpu=0.0)

    assert type(embeddings.keep_alive) is int
    assert type(embeddings.num_gpu) is int


def test_other_providers_ignore_the_ollama_keys():
    embeddings = server_module.initialize_embedding_model(
        {
            "model_type": "openai",
            "model": "text-embedding-3-small",
            "api_url": "",
            "api_key": "key",
            "keep_alive": 60,
            "num_gpu": 0,
        }
    )

    assert not hasattr(embeddings, "keep_alive")
    assert not hasattr(embeddings, "num_gpu")
