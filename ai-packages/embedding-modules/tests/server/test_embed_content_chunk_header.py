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
"""EmbedContent with a chunk header: the header goes into every embedded
text, the chunk text on the wire stays the original; a header too long for
chunk_size is INVALID_ARGUMENT; images are fused with it on a fusing model,
counted in one warning otherwise."""

import logging

import grpc
import pytest

from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

TEXT_VECTOR = [1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
IMAGE_VECTOR = [0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
MIXED_VECTOR = [0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0]

STORAGE = {
    "https://signed/img-1": (b"png-1", "image/png"),
    "https://signed/img-2": (b"png-2", "image/png"),
    "https://signed/notes-1": (b"riga uno|riga due", "text/plain"),
}


def _recording_pipelines(calls, fusing=True):
    def build(configuration, chunker):
        return Pipelines(
            embed_texts=lambda texts: calls.append(("texts", list(texts)))
            or [TEXT_VECTOR for _ in texts],
            chunk=lambda text: text.split("|"),
            fetch=lambda url: STORAGE[url],
            embed_image=lambda data, content_type: calls.append(("image", data))
            or IMAGE_VECTOR,
            embed_mixed=(
                (
                    lambda text, data, content_type: calls.append(("mixed", text, data))
                    or MIXED_VECTOR
                )
                if fusing
                else None
            ),
        )

    return build


def _header(*pairs):
    return [
        embedding_pb2.ChunkHeaderField(name=name, value=value) for name, value in pairs
    ]


def _request(text=None, refs=(), chunk_header=(), json_config=None):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        chunk=embedding_pb2.RequestChunk(type=1),
        refs=list(refs),
        chunkHeader=list(chunk_header),
    )
    if text is not None:
        request.text = text
    if json_config:
        request.chunk.jsonConfig.update(json_config)

    return request


def test_without_header_the_texts_are_the_ones_of_today(make_stub):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls))

    chunks = list(stub.EmbedContent(_request(text="primo|secondo")))

    assert calls == [("texts", ["primo", "secondo"])]
    assert [chunk.text for chunk in chunks] == ["primo", "secondo"]


def test_the_header_is_embedded_and_the_chunk_text_is_the_original(make_stub):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls))

    chunks = list(
        stub.EmbedContent(
            _request(
                text="primo|secondo",
                chunk_header=_header(
                    ("title", "Manuale motore"), ("category", "Barche")
                ),
            )
        )
    )

    header = "title: Manuale motore\ncategory: Barche\n"
    assert calls == [("texts", [header + "primo", header + "secondo"])]
    assert [chunk.text for chunk in chunks] == ["primo", "secondo"]


def test_a_text_ref_takes_the_same_header(make_stub):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls))

    ref = embedding_pb2.MediaRef(
        url="https://signed/notes-1", fileId="notes-1", contentType="text/plain"
    )
    chunks = list(
        stub.EmbedContent(
            _request(refs=[ref], chunk_header=_header(("title", "Manuale")))
        )
    )

    assert calls == [
        ("texts", ["title: Manuale\nriga uno", "title: Manuale\nriga due"])
    ]
    assert [chunk.text for chunk in chunks] == ["riga uno", "riga due"]


def test_a_header_too_long_is_invalid_argument(make_stub):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls))

    request = _request(
        text="primo",
        chunk_header=_header(("title", "x" * 120)),
        json_config={"chunk_size": 400},
    )

    with pytest.raises(grpc.RpcError) as error:
        list(stub.EmbedContent(request))

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT
    details = error.value.details()
    assert "128 characters" in details
    assert "100 characters" in details
    assert "title" in details
    assert calls == []


def test_an_overlap_left_without_room_is_invalid_argument(make_stub):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls))

    request = _request(
        text="primo",
        chunk_header=_header(("title", "x" * 13)),
        json_config={"chunk_size": 100, "chunk_overlap": 90},
    )

    with pytest.raises(grpc.RpcError) as error:
        list(stub.EmbedContent(request))

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT
    details = error.value.details()
    assert "chunk_overlap 90" in details
    assert "title" in details
    assert calls == []


def test_a_chunker_error_stays_internal(make_stub):
    def broken_chunker(chunk_type, config):
        raise RuntimeError("chunker broken")

    stub = make_stub(build_chunker=broken_chunker)

    with pytest.raises(grpc.RpcError) as error:
        list(stub.EmbedContent(_request(text="primo")))

    assert error.value.code() == grpc.StatusCode.INTERNAL


def test_images_are_fused_with_the_header(make_stub):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls))

    ref = embedding_pb2.MediaRef(
        url="https://signed/img-1", fileId="img-1", contentType="image/png"
    )
    chunks = list(
        stub.EmbedContent(
            _request(refs=[ref], chunk_header=_header(("title", "Manuale")))
        )
    )

    assert calls == [("mixed", "title: Manuale\n", b"png-1")]
    assert len(chunks) == 1


def test_header_on_images_false_embeds_the_image_as_before(make_stub):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls))

    ref = embedding_pb2.MediaRef(
        url="https://signed/img-1", fileId="img-1", contentType="image/png"
    )
    list(
        stub.EmbedContent(
            _request(
                text="primo",
                refs=[ref],
                chunk_header=_header(("title", "Manuale")),
                json_config={"header_on_images": False},
            )
        )
    )

    assert calls == [("texts", ["title: Manuale\nprimo"]), ("image", b"png-1")]


def test_without_fusion_one_warning_counts_the_images(make_stub, caplog):
    calls = []
    stub = make_stub(build_pipelines=_recording_pipelines(calls, fusing=False))

    refs = [
        embedding_pb2.MediaRef(
            url=f"https://signed/img-{index}",
            fileId=f"img-{index}",
            contentType="image/png",
        )
        for index in (1, 2)
    ]
    request = _request(refs=refs, chunk_header=_header(("title", "Manuale")))
    request.embeddingModel.providerModel.model = "text-only-model"

    with caplog.at_level(logging.WARNING):
        list(stub.EmbedContent(request))

    assert calls == [("image", b"png-1"), ("image", b"png-2")]
    warnings = [
        record.getMessage()
        for record in caplog.records
        if "without the chunk header" in record.getMessage()
    ]
    assert len(warnings) == 1
    assert "2 images" in warnings[0]
    assert "text-only-model" in warnings[0]
