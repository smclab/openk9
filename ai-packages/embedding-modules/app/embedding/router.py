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

"""Media-aware routing for EmbedContent.

Turns a content source (inline text, or a media ref pulled from its
pre-signed URL) into embedded Pieces, one per chunk. The module is the
sole authority on embeddability: a ref whose modality has no handler in
phase 1 (audio, video, unknown), or that the configured model cannot
embed (image on a text-only model, binary not decodable), raises SkipRef
so the caller skips it — no chunk for that fileId, structured log, the
stream continues. New modalities plug in by registering a handler, with
no change to the control flow. All capabilities (chunking, embedding,
HTTP fetch) are injected via Pipelines, so the routing is testable
without network or models.
"""

from dataclasses import dataclass
from typing import Callable, List, Optional


class SkipRef(Exception):
    """A ref that cannot be embedded; the message is the skip reason."""


@dataclass
class Piece:
    """One embedded chunk: its vector, its text (empty for images) and the
    source fileId (None for inline-text chunks)."""

    vector: list
    text: str = ""
    file_id: Optional[str] = None


@dataclass
class Pipelines:
    """Injected capabilities used by the router."""

    embed_texts: Callable  # (list[str]) -> list[list[float]]
    chunk: Callable  # (str) -> list[str]
    fetch: Callable = None  # (url) -> (bytes, content_type | None)
    embed_image: Callable = None  # (bytes, content_type) -> list[float]


def classify(content_type) -> str:
    """Maps a content type onto a modality: text | image | audio | video |
    unknown. Subtype parameters (charset, ...) are ignored."""
    if not content_type:
        return "unknown"

    main = content_type.split(";")[0].strip().lower()

    if main.startswith("text/"):
        return "text"
    if main.startswith("image/"):
        return "image"
    if main.startswith("audio/"):
        return "audio"
    if main.startswith("video/"):
        return "video"

    return "unknown"


def text_pieces(text, pipelines, file_id=None) -> List[Piece]:
    """Chunks text and embeds every chunk on the text path."""
    chunks = pipelines.chunk(text)
    vectors = pipelines.embed_texts(chunks) if chunks else []

    return [
        Piece(vector=vector, text=chunk, file_id=file_id)
        for chunk, vector in zip(chunks, vectors)
    ]


def _text_ref_pieces(data, content_type, pipelines, file_id):
    try:
        text = data.decode("utf-8")
    except UnicodeDecodeError as error:
        raise SkipRef(f"binary not decodable as utf-8 text: {error}") from error

    return text_pieces(text, pipelines, file_id=file_id)


def _image_ref_pieces(data, content_type, pipelines, file_id):
    if pipelines.embed_image is None:
        raise SkipRef("the configured embedding model has no image input")

    vector = pipelines.embed_image(data, content_type)

    return [Piece(vector=vector, file_id=file_id)]


_HANDLERS = {
    "text": _text_ref_pieces,
    "image": _image_ref_pieces,
}


def ref_pieces(ref, pipelines) -> List[Piece]:
    """Embeds one binary ref pulled from its pre-signed URL.

    The authoritative content type comes from the ref (connector
    metadata); the HTTP response Content-Type is only a fallback when the
    ref carries none. Raises SkipRef when the modality has no handler or
    the model cannot embed it.
    """
    content_type = ref.get("contentType")
    modality = classify(content_type)

    # authoritative content type already excludes it: skip before fetching
    if content_type and modality not in _HANDLERS:
        raise SkipRef(f"no handler for modality {modality!r} ({content_type})")

    data, response_content_type = pipelines.fetch(ref["url"])

    if not content_type:
        content_type = response_content_type
        modality = classify(content_type)

    handler = _HANDLERS.get(modality)
    if handler is None:
        raise SkipRef(f"no handler for modality {modality!r} ({content_type})")

    return handler(data, content_type, pipelines, ref.get("fileId"))
