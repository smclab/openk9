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

"""On the wire, every page of a PDF ref is a chunk with the fileId of the
PDF, the text of the page and its page in the metadata. The media options
come from the jsonConfig of the chunk and never reach the chunker; the pages
are streamed in groups of the batch size."""

import logging

import grpc
import pytest

from app import server as server_module
from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

TEXT_VECTOR = [3.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0]
IMAGE_VECTOR = [0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]


@pytest.fixture
def storage(pdf_samples):
    return {
        "https://signed/manual": (
            pdf_samples.build(
                pdf_samples.text_page("Pagina uno"),
                pdf_samples.DRAWING,
                pdf_samples.text_page("Pagina tre"),
            ),
            "application/pdf",
        ),
        "https://signed/broken": (pdf_samples.CORRUPTED, "application/pdf"),
        "https://signed/img": (b"png", "image/png"),
    }


def _pipelines(storage, embed_image=lambda data, content_type: IMAGE_VECTOR):
    def build(configuration, chunker):
        return Pipelines(
            embed_texts=lambda texts: [TEXT_VECTOR for _ in texts],
            chunk=lambda text: text.split(),
            fetch=lambda url: storage[url],
            embed_image=embed_image,
        )

    return build


def _ref(name, content_type="application/pdf"):
    return embedding_pb2.MediaRef(
        url=f"https://signed/{name}", fileId=name, contentType=content_type
    )


def _request(refs, text=None, media=None, batch_size=None):
    request = embedding_pb2.EmbedContentRequest(tenantId="mew", refs=refs)
    if text is not None:
        request.text = text
    if media is not None:
        request.chunk.jsonConfig.update(media)
    if batch_size is not None:
        request.embeddingModel.jsonConfig.update({"batch_size": batch_size})

    return request


def test_every_page_is_a_chunk_with_its_page_in_the_metadata(make_stub, storage):
    stub = make_stub(_pipelines(storage))

    chunks = list(stub.EmbedContent(_request([_ref("manual")])))

    assert [chunk.number for chunk in chunks] == [1, 2, 3]
    assert {chunk.fileId for chunk in chunks} == {"manual"}
    assert [chunk.text for chunk in chunks] == ["Pagina uno", "", "Pagina tre"]
    # a Struct carries every number as a double
    assert [dict(chunk.metadata) for chunk in chunks] == [
        {"page": 1.0},
        {"page": 2.0},
        {"page": 3.0},
    ]


def test_a_chunk_without_metadata_has_none_on_the_wire(make_stub, storage):
    stub = make_stub(_pipelines(storage))

    chunks = list(stub.EmbedContent(_request([_ref("img", "image/png")], text="uno")))

    assert [chunk.HasField("metadata") for chunk in chunks] == [False, False]


def test_the_media_options_do_not_reach_the_chunker(make_stub, storage):
    built = []

    def build_chunker(chunk_type, json_config):
        built.append(json_config)
        return object()

    stub = make_stub(_pipelines(storage), build_chunker=build_chunker)
    request = _request(
        [_ref("manual")],
        media={"chunk_size": 100, "max_pages": 2, "page_resolution": 800},
    )

    chunks = list(stub.EmbedContent(request))

    assert built == [{"chunk_size": 100.0}]
    assert [chunk.metadata["page"] for chunk in chunks] == [1.0, 2.0]


def test_a_corrupted_pdf_is_skipped_and_the_text_is_unchanged(
    make_stub, storage, caplog
):
    stub = make_stub(_pipelines(storage))

    with caplog.at_level(logging.WARNING, logger=server_module.logger.name):
        chunks = list(stub.EmbedContent(_request([_ref("broken")], text="uno due")))

    assert [chunk.text for chunk in chunks] == ["uno", "due"]
    assert "fileId=broken" in caplog.text
    assert "reason=PDF not readable" in caplog.text


def test_rasterize_pages_false_leaves_only_the_text(make_stub, storage, caplog):
    stub = make_stub(_pipelines(storage))

    with caplog.at_level(logging.WARNING, logger=server_module.logger.name):
        chunks = list(
            stub.EmbedContent(
                _request(
                    [_ref("manual")], text="uno due", media={"rasterize_pages": False}
                )
            )
        )

    assert [chunk.text for chunk in chunks] == ["uno", "due"]
    assert "reason=rasterize_pages is false" in caplog.text


def test_embed_images_false_skips_the_image_and_the_pdf(make_stub, storage, caplog):
    stub = make_stub(_pipelines(storage))
    request = _request(
        [_ref("img", "image/png"), _ref("manual")],
        text="uno due",
        media={"embed_images": False},
    )

    with caplog.at_level(logging.WARNING, logger=server_module.logger.name):
        chunks = list(stub.EmbedContent(request))

    assert [chunk.text for chunk in chunks] == ["uno", "due"]
    assert not any(chunk.HasField("fileId") for chunk in chunks)
    assert "fileId=img contentType=image/png reason=embed_images is false" in (
        caplog.text
    )
    assert "fileId=manual contentType=application/pdf reason=embed_images is false" in (
        caplog.text
    )


def test_the_model_failing_on_a_later_page_ends_the_rpc_after_the_first_group(
    make_stub, storage
):
    calls = []

    def embed_image(data, content_type):
        calls.append(data)
        if len(calls) == 3:
            raise RuntimeError("image rejected by the provider")
        return IMAGE_VECTOR

    stub = make_stub(_pipelines(storage, embed_image))
    received = []

    with pytest.raises(grpc.RpcError) as error:
        for chunk in stub.EmbedContent(_request([_ref("manual")], batch_size=2)):
            received.append(chunk)

    assert error.value.code() == grpc.StatusCode.INTERNAL
    assert [chunk.metadata["page"] for chunk in received] == [1.0, 2.0]
