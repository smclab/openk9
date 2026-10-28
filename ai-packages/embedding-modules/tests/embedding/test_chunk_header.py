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
"""compose_header writes one "name: value" line per field, values cleaned
like the text; with_header puts it in front of every text to embed and, with
a fusing model, in the vector of images and pages."""

from app.embedding.chunk_header import HeaderStats, compose_header, with_header
from app.embedding.media_options import MediaOptions
from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2


def _fields(*pairs):
    return [
        embedding_pb2.ChunkHeaderField(name=name, value=value) for name, value in pairs
    ]


def _pipelines(calls, embed_mixed=True):
    return Pipelines(
        embed_texts=lambda texts: calls.append(("texts", texts))
        or [[1.0]] * len(texts),
        chunk=lambda text: text.split("|"),
        embed_image=lambda data, content_type: calls.append(("image", data)) or [2.0],
        embed_mixed=(
            (
                lambda text, data, content_type: calls.append(("mixed", text, data))
                or [3.0]
            )
            if embed_mixed
            else None
        ),
    )


def test_compose_header_keeps_the_order_and_cleans_the_values():
    header = compose_header(
        _fields(("web title", "Barche &amp; motori"), ("category", "Barche"))
    )

    assert header.text == "web title: Barche & motori\ncategory: Barche\n"
    assert header.fields == ["web title", "category"]


def test_compose_header_skips_a_value_empty_after_cleaning():
    header = compose_header(_fields(("title", "Manuale"), ("tags", "<br/>")))

    assert header.text == "title: Manuale\n"
    assert header.fields == ["title"]


def test_no_header_leaves_the_pipelines_as_they_are():
    calls = []
    pipelines = _pipelines(calls)

    assert (
        with_header(pipelines, compose_header([]), MediaOptions(), HeaderStats())
        is pipelines
    )


def test_texts_are_embedded_with_the_header():
    calls = []
    header = compose_header(_fields(("title", "Manuale motore")))

    pipelines = with_header(_pipelines(calls), header, MediaOptions(), HeaderStats())
    pipelines.embed_texts(["primo", "secondo"])

    assert calls == [
        ("texts", ["title: Manuale motore\nprimo", "title: Manuale motore\nsecondo"])
    ]


def test_images_are_fused_with_the_header():
    calls = []
    header = compose_header(_fields(("title", "Manuale motore")))
    stats = HeaderStats()

    pipelines = with_header(_pipelines(calls), header, MediaOptions(), stats)
    vector = pipelines.embed_image(b"png", "image/png")

    assert vector == [3.0]
    assert calls == [("mixed", "title: Manuale motore\n", b"png")]
    assert stats.media_without_header == 0


def test_header_on_images_false_embeds_images_as_before():
    calls = []
    header = compose_header(_fields(("title", "Manuale motore")))
    stats = HeaderStats()

    pipelines = with_header(
        _pipelines(calls), header, MediaOptions(header_on_images=False), stats
    )
    pipelines.embed_texts(["primo"])
    vector = pipelines.embed_image(b"png", "image/png")

    assert vector == [2.0]
    assert calls == [("texts", ["title: Manuale motore\nprimo"]), ("image", b"png")]
    assert stats.media_without_header == 0


def test_without_fusion_images_are_counted():
    calls = []
    header = compose_header(_fields(("title", "Manuale motore")))
    stats = HeaderStats()

    pipelines = with_header(
        _pipelines(calls, embed_mixed=False), header, MediaOptions(), stats
    )
    pipelines.embed_image(b"png-1", "image/png")
    pipelines.embed_image(b"png-2", "image/png")

    assert calls == [("image", b"png-1"), ("image", b"png-2")]
    assert stats.media_without_header == 2
