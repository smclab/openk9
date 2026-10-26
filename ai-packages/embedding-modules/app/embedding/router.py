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
pre-signed URL) into embedded Pieces, one per chunk; a PDF gives one Piece
per page, rasterized, in groups. The module is the sole authority on
embeddability: a ref whose modality has no handler (audio, video, unknown),
that the configured model cannot embed (image or PDF on a text-only model,
binary not decodable) or that the media options turn off, raises SkipRef
so the caller skips it — no chunk for that fileId, structured log, the
stream continues. A model failing on content it can embed is not a skip:
it raises EmbedFailed and the caller fails the request. New modalities
plug in by registering a handler, with no change to the control flow. All capabilities (chunking, embedding,
HTTP fetch) are injected via Pipelines, so the routing is testable
without network or models.
"""

import io
import logging
import threading
from collections.abc import Callable, Iterator
from dataclasses import dataclass

import pypdfium2 as pdfium

from app.embedding.media_options import MediaOptions
from app.utils.text_cleaner import clean_text

logger = logging.getLogger(__name__)

PDF_CONTENT_TYPE = "application/pdf"
# the content type of a rasterized page, as the image embedders receive it
PAGE_CONTENT_TYPE = "image/png"


class SkipRef(Exception):
    """A ref that cannot be embedded; the message is the skip reason."""


class EmbedFailed(Exception):
    """The model failed on the content of a ref. Unlike SkipRef it says
    nothing about the ref, so the caller fails the request instead of
    skipping it."""


@dataclass
class Piece:
    """One embedded chunk: its vector, its text (empty for images), the
    source fileId (None for inline-text chunks) and its metadata (the page
    of a PDF page, None when there is nothing to add)."""

    vector: list
    text: str = ""
    file_id: str | None = None
    metadata: dict | None = None


@dataclass
class Pipelines:
    """Injected capabilities used by the router."""

    embed_texts: Callable  # (list[str]) -> list[list[float]]
    chunk: Callable  # (str) -> list[str]
    fetch: Callable = None  # (url) -> (bytes, content_type | None)
    embed_image: Callable = None  # (bytes, content_type) -> list[float]


def classify(content_type) -> str:
    """Maps a content type onto a modality: text | image | pdf | audio |
    video | unknown. Subtype parameters (charset, ...) are ignored."""
    if not content_type:
        return "unknown"

    main = content_type.split(";")[0].strip().lower()

    if main.startswith("text/"):
        return "text"
    if main.startswith("image/"):
        return "image"
    if main == PDF_CONTENT_TYPE:
        return "pdf"
    if main.startswith("audio/"):
        return "audio"
    if main.startswith("video/"):
        return "video"

    return "unknown"


def text_pieces(text, pipelines, file_id=None) -> list[Piece]:
    """Chunks text and embeds every chunk on the text path."""
    chunks = pipelines.chunk(text)
    vectors = pipelines.embed_texts(chunks) if chunks else []

    return [
        Piece(vector=vector, text=chunk, file_id=file_id)
        for chunk, vector in zip(chunks, vectors)
    ]


def text_piece_batches(chunks, pipelines, batch_size, file_id=None):
    """Embeds the chunks batch_size at a time, yielding the Pieces of each
    batch in order, so the caller can emit a batch before the next is
    embedded."""
    for start in range(0, len(chunks), batch_size):
        batch = chunks[start : start + batch_size]
        vectors = pipelines.embed_texts(batch)

        yield [
            Piece(vector=vector, text=chunk, file_id=file_id)
            for chunk, vector in zip(batch, vectors)
        ]


def _text_ref_pieces(data, content_type, pipelines, file_id):
    # Textual refs are embedded as-is: unlike the inline text, they are NOT run
    # through clean_text (which strips markup), to preserve the
    # connector-extracted content verbatim. Deliberate choice.
    try:
        text = data.decode("utf-8")
    except UnicodeDecodeError as error:
        raise SkipRef(f"binary not decodable as utf-8 text: {error}") from error

    try:
        return text_pieces(text, pipelines, file_id=file_id)
    except Exception as error:
        raise EmbedFailed(str(error)) from error


def _image_ref_pieces(data, content_type, pipelines, file_id):
    try:
        vector = pipelines.embed_image(data, content_type)
    except Exception as error:
        raise EmbedFailed(str(error)) from error

    return [Piece(vector=vector, file_id=file_id)]


# pdfium is not thread-safe and the server embeds up to 10 contents at once:
# every pdfium call goes through this lock, the embedding of a page does not.
# No pdfium object outlives a locked section, so the GC never finalizes one
# outside it. Reentrant all the same: code run by the GC on a thread that
# holds the lock can take it again instead of hanging the thread forever
_PDFIUM_LOCK = threading.RLock()


class _PdfiumFailed(Exception):
    """A failure under the pdfium lock, raised again outside it."""

    def __init__(self, message: str, unreadable: bool):
        super().__init__(message)
        self.unreadable = unreadable


def _with_pdfium(work):
    """Runs work under the pdfium lock. An exception dies inside the lock,
    with its traceback: that may hold the last reference to an object
    pypdfium2 created but did not return, which is then finalized under the
    lock too. A _PdfiumFailed with the same message comes out instead."""
    failure = None
    with _PDFIUM_LOCK:
        try:
            return work()
        except Exception as error:
            failure = _PdfiumFailed(
                str(error), isinstance(error, pdfium.PdfiumError)
            )

    raise failure


def _page_count(data) -> int:
    pdf = pdfium.PdfDocument(data)
    try:
        return len(pdf)
    finally:
        pdf.close()


def _render_page(pdf, index: int, resolution: int):
    """Rasterizes the page at index with its long side at resolution pixels
    and reads its text. The caller holds the pdfium lock."""
    page = pdf[index]
    try:
        width, height = page.get_size()
        bitmap = page.render(scale=resolution / max(width, height))
        try:
            image = bitmap.to_pil()
        finally:
            bitmap.close()

        textpage = page.get_textpage()
        try:
            text = textpage.get_text_bounded()
        finally:
            textpage.close()
    finally:
        page.close()

    return image, text


def _open_and_render_page(data, index: int, resolution: int):
    pdf = pdfium.PdfDocument(data)
    try:
        return _render_page(pdf, index, resolution)
    finally:
        pdf.close()


def _rasterize_page(data, index: int, resolution: int) -> tuple[bytes, str]:
    """Opens the PDF, rasterizes the page at index and closes the PDF under
    the pdfium lock, then encodes the page as PNG and cleans its text like
    the inline text."""
    image, text = _with_pdfium(
        lambda: _open_and_render_page(data, index, resolution)
    )

    buffer = io.BytesIO()
    image.save(buffer, format="PNG")

    return buffer.getvalue(), clean_text(text)


def _page_piece(data, index: int, pipelines, file_id, options) -> Piece | None:
    """Embeds the page at index (from 0) as an image, or returns None when the
    page cannot be rasterized: the other pages go on."""
    try:
        image, text = _rasterize_page(data, index, options.page_resolution)
    except Exception as error:
        logger.warning(
            "Skipping page %d of PDF fileId=%s: %s", index + 1, file_id, error
        )
        return None

    try:
        vector = pipelines.embed_image(image, PAGE_CONTENT_TYPE)
    except Exception as error:
        raise EmbedFailed(str(error)) from error

    return Piece(
        vector=vector, text=text, file_id=file_id, metadata={"page": index + 1}
    )


def _pdf_page_batches(
    data, content_type, pipelines, file_id, options, batch_size
) -> Iterator[list[Piece]]:
    """Embeds the pages of a PDF, up to max_pages, batch_size at a time,
    yielding the Pieces of each batch so the caller can emit them before the
    next is rasterized: the PDF bytes stay in memory, the page images do not.
    Every page opens the PDF again, in milliseconds, so no pdfium object is
    left open across a yield."""
    try:
        total = _with_pdfium(lambda: _page_count(data))
    except _PdfiumFailed as failure:
        if failure.unreadable:
            raise SkipRef(f"PDF not readable: {failure}") from None
        raise

    pages = min(total, options.max_pages)

    if pages < total:
        logger.warning(
            "PDF fileId=%s has %d pages: embedding the first %d (max_pages), "
            "skipping %d",
            file_id,
            total,
            pages,
            total - pages,
        )

    for start in range(0, pages, batch_size):
        group = []
        for index in range(start, min(start + batch_size, pages)):
            piece = _page_piece(data, index, pipelines, file_id, options)
            if piece is not None:
                group.append(piece)

        if group:
            yield group


def _one_group(handler):
    """A handler that embeds the whole ref at once, as a handler of groups."""

    def batches(data, content_type, pipelines, file_id, options, batch_size):
        yield handler(data, content_type, pipelines, file_id)

    return batches


# modality -> (data, content_type, pipelines, file_id, options, batch_size),
# yielding the Pieces of the ref in groups
_HANDLERS = {
    "text": _one_group(_text_ref_pieces),
    "image": _one_group(_image_ref_pieces),
    "pdf": _pdf_page_batches,
}


def _check_embeddable(modality, content_type, pipelines, options) -> None:
    """Raises SkipRef when the modality has no handler, the media options
    turn it off or the model has no image input for it."""
    if modality not in _HANDLERS:
        raise SkipRef(f"no handler for modality {modality!r} ({content_type})")

    if modality in ("image", "pdf"):
        if not options.embed_images:
            raise SkipRef("embed_images is false")
        if modality == "pdf" and not options.rasterize_pages:
            raise SkipRef("rasterize_pages is false")
        if pipelines.embed_image is None:
            raise SkipRef("the configured embedding model has no image input")


def ref_piece_batches(
    ref, pipelines, options: MediaOptions, batch_size: int
) -> Iterator[list[Piece]]:
    """Embeds one binary ref pulled from its pre-signed URL, yielding its
    Pieces in groups: one group for text and images, batch_size pages at a
    time for a PDF.

    The authoritative content type comes from the ref (connector
    metadata); the HTTP response Content-Type is only a fallback when the
    ref carries none. Raises SkipRef when the ref cannot be embedded,
    before any group for a PDF, EmbedFailed when the model fails on it.
    """
    content_type = ref.get("contentType")
    modality = classify(content_type)

    # authoritative content type already excludes it: skip before fetching
    if content_type:
        _check_embeddable(modality, content_type, pipelines, options)

    data, response_content_type = pipelines.fetch(ref["url"])

    if not content_type:
        content_type = response_content_type
        modality = classify(content_type)
        _check_embeddable(modality, content_type, pipelines, options)

    yield from _HANDLERS[modality](
        data, content_type, pipelines, ref.get("fileId"), options, batch_size
    )


def ref_pieces(ref, pipelines, options: MediaOptions = MediaOptions()) -> list[Piece]:
    """All the Pieces of one binary ref at once (see ref_piece_batches)."""
    return [
        piece
        for group in ref_piece_batches(ref, pipelines, options, options.max_pages)
        for piece in group
    ]
