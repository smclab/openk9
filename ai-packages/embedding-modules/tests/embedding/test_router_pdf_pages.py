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

"""A PDF ref is rasterized page by page: every page becomes a Piece with
the image vector of the page, its text and its number, emitted in groups.
The media options and the model decide whether the PDF is embedded at all."""

import io
import logging
import weakref
from concurrent.futures import ThreadPoolExecutor

import pytest
from PIL import Image

from app.embedding import router
from app.embedding.media_options import MediaOptions
from app.embedding.router import EmbedFailed, SkipRef, ref_piece_batches, ref_pieces

URL = "https://signed/manual"


def _ref(content_type="application/pdf"):
    return {"url": URL, "fileId": "manual-1", "contentType": content_type}


def _image(piece):
    # the echo embedder returns ["image", data, content_type]
    return Image.open(io.BytesIO(piece.vector[1]))


def test_every_page_becomes_a_piece_with_its_page_and_text(make_pipelines, pdf_samples):
    data = pdf_samples.build(
        pdf_samples.text_page("Pagina uno"),
        pdf_samples.text_page("Pagina due"),
        pdf_samples.text_page("Pagina tre"),
    )
    pipelines = make_pipelines(storage={URL: (data, "application/pdf")})

    pieces = ref_pieces(_ref(), pipelines)

    assert [piece.metadata for piece in pieces] == [
        {"page": 1},
        {"page": 2},
        {"page": 3},
    ]
    assert [piece.text for piece in pieces] == [
        "Pagina uno",
        "Pagina due",
        "Pagina tre",
    ]
    assert {piece.file_id for piece in pieces} == {"manual-1"}
    assert {piece.vector[2] for piece in pieces} == {"image/png"}


def test_the_long_side_of_a_page_is_page_resolution(make_pipelines, pdf_samples):
    data = pdf_samples.build(pdf_samples.text_page("Pagina uno"))
    pipelines = make_pipelines(storage={URL: (data, "application/pdf")})

    (piece,) = ref_pieces(_ref(), pipelines, MediaOptions(page_resolution=800))

    assert max(_image(piece).size) == 800


def test_a_page_with_only_a_drawing_is_embedded_with_empty_text(
    make_pipelines, pdf_samples
):
    data = pdf_samples.build(pdf_samples.DRAWING)
    pipelines = make_pipelines(storage={URL: (data, "application/pdf")})

    (piece,) = ref_pieces(_ref(), pipelines)

    assert piece.text == ""
    assert piece.metadata == {"page": 1}
    # the lines of the drawing are in the image: not a blank page
    darkest, _ = _image(piece).convert("L").getextrema()
    assert darkest < 128


def test_a_corrupted_pdf_is_skipped(make_pipelines, pdf_samples):
    pipelines = make_pipelines(
        storage={URL: (pdf_samples.CORRUPTED, "application/pdf")}
    )

    with pytest.raises(SkipRef, match="PDF not readable"):
        ref_pieces(_ref(), pipelines)


def test_a_docx_is_skipped_before_fetching(make_pipelines):
    # empty storage: a fetch attempt would KeyError
    docx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    with pytest.raises(SkipRef, match="no handler for modality 'unknown'"):
        ref_pieces(_ref(docx), make_pipelines())


def test_rasterize_pages_false_skips_the_pdf_before_fetching(make_pipelines):
    with pytest.raises(SkipRef, match="rasterize_pages is false"):
        ref_pieces(_ref(), make_pipelines(), MediaOptions(rasterize_pages=False))


@pytest.mark.parametrize("content_type", ["application/pdf", "image/png"])
def test_embed_images_false_skips_images_and_pdfs_before_fetching(
    make_pipelines, content_type
):
    with pytest.raises(SkipRef, match="embed_images is false"):
        ref_pieces(
            _ref(content_type), make_pipelines(), MediaOptions(embed_images=False)
        )


def test_a_text_only_model_skips_the_pdf_before_fetching(make_pipelines):
    with pytest.raises(SkipRef, match="the configured embedding model has no image"):
        ref_pieces(_ref(), make_pipelines(embed_image=None))


def test_max_pages_caps_the_pages_and_logs_the_skipped_ones(
    make_pipelines, pdf_samples, caplog
):
    data = pdf_samples.build(
        *(pdf_samples.text_page(f"Pagina {number}") for number in range(1, 81))
    )
    pipelines = make_pipelines(storage={URL: (data, "application/pdf")})

    with caplog.at_level(logging.WARNING, logger=router.logger.name):
        pieces = ref_pieces(_ref(), pipelines, MediaOptions(max_pages=50))

    assert [piece.metadata["page"] for piece in pieces] == list(range(1, 51))
    assert "has 80 pages: embedding the first 50 (max_pages), skipping 30" in (
        caplog.text
    )


def test_the_pages_come_in_groups_rasterized_one_group_at_a_time(
    make_pipelines, pdf_samples
):
    data = pdf_samples.build(
        *(pdf_samples.text_page(f"Pagina {number}") for number in range(1, 6))
    )
    embedded = []

    def embed_image(image, content_type):
        embedded.append(image)
        return [1.0]

    pipelines = make_pipelines(
        storage={URL: (data, "application/pdf")}, embed_image=embed_image
    )

    groups = ref_piece_batches(_ref(), pipelines, MediaOptions(), batch_size=2)

    first = next(groups)
    # the next group is rasterized only when the caller asks for it
    assert len(embedded) == 2
    rest = list(groups)

    assert [len(group) for group in [first, *rest]] == [2, 2, 1]


def test_the_model_failing_on_a_page_fails_after_the_earlier_groups(
    make_pipelines, pdf_samples
):
    data = pdf_samples.build(
        *(pdf_samples.text_page(f"Pagina {number}") for number in range(1, 5))
    )
    calls = []

    def embed_image(image, content_type):
        calls.append(image)
        if len(calls) == 3:
            raise RuntimeError("image rejected by the provider")
        return [1.0]

    pipelines = make_pipelines(
        storage={URL: (data, "application/pdf")}, embed_image=embed_image
    )
    groups = ref_piece_batches(_ref(), pipelines, MediaOptions(), batch_size=2)

    assert len(next(groups)) == 2
    with pytest.raises(EmbedFailed, match="image rejected by the provider"):
        next(groups)


def test_a_page_that_cannot_be_rasterized_is_skipped(
    make_pipelines, pdf_samples, monkeypatch, caplog
):
    data = pdf_samples.build(
        pdf_samples.text_page("Pagina uno"),
        pdf_samples.text_page("Pagina due"),
    )
    render = router._render_page
    rendered = []

    def failing_second_page(pdf, index, resolution):
        rendered.append(index)
        if len(rendered) == 2:
            raise ValueError("broken page")
        return render(pdf, index, resolution)

    monkeypatch.setattr(router, "_render_page", failing_second_page)
    pipelines = make_pipelines(storage={URL: (data, "application/pdf")})

    with caplog.at_level(logging.WARNING, logger=router.logger.name):
        pieces = ref_pieces(_ref(), pipelines)

    assert [piece.metadata for piece in pieces] == [{"page": 1}]
    assert "Skipping page 2 of PDF fileId=manual-1: broken page" in caplog.text


def test_an_object_orphaned_by_a_failing_render_is_finalized_under_the_lock(
    make_pipelines, pdf_samples, monkeypatch, caplog
):
    # stands for a native object pypdfium2 allocates and loses when it
    # raises: only the traceback references it, so it is finalized when the
    # exception dies, which must happen under the lock
    data = pdf_samples.build(pdf_samples.text_page("Pagina uno"))
    finalized_under_lock = []

    class Orphan:
        pass

    def failing_render(page, **kwargs):
        orphan = Orphan()
        weakref.finalize(
            orphan,
            lambda: finalized_under_lock.append(router._PDFIUM_LOCK._is_owned()),
        )
        raise RuntimeError("render failed")

    monkeypatch.setattr(router.pdfium.PdfPage, "render", failing_render)
    pipelines = make_pipelines(storage={URL: (data, "application/pdf")})

    with caplog.at_level(logging.WARNING, logger=router.logger.name):
        pieces = ref_pieces(_ref(), pipelines)

    assert pieces == []
    assert finalized_under_lock == [True]
    assert "Skipping page 1 of PDF fileId=manual-1: render failed" in caplog.text


def test_pdfs_embedded_on_many_threads_at_once_keep_their_pages(
    make_pipelines, pdf_samples
):
    # pdfium is not thread-safe: without the lock this crashes the process
    urls = [f"https://signed/manual-{number}" for number in range(10)]
    storage = {
        url: (
            pdf_samples.build(
                *(
                    pdf_samples.text_page(f"Manuale {number} pagina {page}")
                    for page in range(1, 11)
                )
            ),
            "application/pdf",
        )
        for number, url in enumerate(urls)
    }
    pipelines = make_pipelines(storage=storage)

    def embed(url):
        return [
            piece
            for group in ref_piece_batches(
                {"url": url, "fileId": url, "contentType": "application/pdf"},
                pipelines,
                MediaOptions(),
                batch_size=3,
            )
            for piece in group
        ]

    with ThreadPoolExecutor(max_workers=10) as executor:
        results = list(executor.map(embed, urls))

    for number, pieces in enumerate(results):
        assert [piece.text for piece in pieces] == [
            f"Manuale {number} pagina {page}" for page in range(1, 11)
        ]


def test_the_response_content_type_routes_a_pdf_ref_without_one(
    make_pipelines, pdf_samples
):
    data = pdf_samples.build(pdf_samples.text_page("Pagina uno"))
    pipelines = make_pipelines(storage={URL: (data, "application/pdf")})

    pieces = ref_pieces({"url": URL, "fileId": "manual-1"}, pipelines)

    assert [piece.metadata for piece in pieces] == [{"page": 1}]
