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

"""The chunk header of EmbedContent: fields of the document written in
front of every chunk before embedding.

The header goes into the vector only: the text returned for a chunk is the
original one. Images and PDF pages take it too when the model fuses text and
image into one vector (embed_mixed); otherwise they are embedded as they
are, and the request counts them for a single warning.
"""

from collections.abc import Iterable
from dataclasses import dataclass, replace

from app.embedding.media_options import MediaOptions
from app.embedding.router import Pipelines
from app.utils.text_cleaner import clean_text


@dataclass(frozen=True)
class ChunkHeader:
    """The header text, one "name: value" line per field ending with a line
    break, and the names of the fields it holds."""

    text: str
    fields: list[str]


@dataclass
class HeaderStats:
    """What a request could not put in the header: the images and PDF pages
    embedded without it."""

    media_without_header: int = 0


def compose_header(chunk_header: Iterable) -> ChunkHeader:
    """Builds the header from the ChunkHeaderField messages, in their order.
    Every value goes through clean_text like the text of the document; a
    value empty after cleaning is skipped."""
    lines = []
    fields = []

    for field in chunk_header:
        value = clean_text(field.value)
        if not value:
            continue

        lines.append(f"{field.name}: {value}")
        fields.append(field.name)

    text = "".join(f"{line}\n" for line in lines)

    return ChunkHeader(text=text, fields=fields)


def with_header(
    pipelines: Pipelines,
    header: ChunkHeader,
    options: MediaOptions,
    stats: HeaderStats,
) -> Pipelines:
    """The pipelines of the request with the header in front of every text
    to embed, and in the vector of images and PDF pages when the model fuses
    them and header_on_images allows it."""
    if not header.text:
        return pipelines

    embed_texts = pipelines.embed_texts
    embed_image = pipelines.embed_image
    embed_mixed = pipelines.embed_mixed

    def embed_texts_with_header(chunks):
        return embed_texts([header.text + chunk for chunk in chunks])

    if embed_image is None or not options.header_on_images:
        return replace(pipelines, embed_texts=embed_texts_with_header)

    def embed_image_with_header(data, content_type):
        if embed_mixed is not None:
            return embed_mixed(header.text, data, content_type)

        stats.media_without_header += 1

        return embed_image(data, content_type)

    return replace(
        pipelines,
        embed_texts=embed_texts_with_header,
        embed_image=embed_image_with_header,
    )
