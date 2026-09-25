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

"""EmbedContent builds the chunker from the chunk settings of the request
and hands it to the pipelines, and the inline text reaches the chunker
cleaned of its markup."""

from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

CHUNKER = object()


def test_the_chunk_settings_of_the_request_build_the_chunker(make_stub):
    built = []
    handed = []

    def build_chunker(chunk_type, json_config):
        built.append((chunk_type, json_config))
        return CHUNKER

    def build_pipelines(configuration, chunker):
        handed.append(chunker)
        return Pipelines(
            embed_texts=lambda texts: [[1.0] for _ in texts],
            chunk=lambda text: text.split(),
        )

    stub = make_stub(build_pipelines, build_chunker=build_chunker)
    chunk = embedding_pb2.RequestChunk(type=embedding_pb2.CHUNK_TYPE_SENTENCE_SPLITTER)
    chunk.jsonConfig.update({"chunk_size": 100, "delim": "."})

    list(
        stub.EmbedContent(
            embedding_pb2.EmbedContentRequest(tenantId="mew", chunk=chunk, text="uno")
        )
    )

    # a gRPC Struct decodes every number to float
    assert built == [(5, {"chunk_size": 100.0, "delim": "."})]
    assert handed == [CHUNKER]


def test_the_inline_text_is_cleaned_before_chunking(make_stub):
    chunked = []
    embedded = []

    def chunk(text):
        chunked.append(text)
        return text.split()

    def embed_texts(texts):
        embedded.append(list(texts))
        return [[1.0] for _ in texts]

    stub = make_stub(
        lambda configuration, chunker: Pipelines(embed_texts=embed_texts, chunk=chunk)
    )

    chunks = list(
        stub.EmbedContent(
            embedding_pb2.EmbedContentRequest(
                tenantId="mew", text="<p>uno <b>due</b></p>"
            )
        )
    )

    assert chunked == ["uno due"]
    assert embedded == [["uno", "due"]]
    assert [chunk.text for chunk in chunks] == ["uno", "due"]
