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

import pytest
from docling.datamodel.base_models import InputFormat
from docling.datamodel.pipeline_options import (
    EasyOcrOptions,
    OcrMode,
    PdfPipelineOptions,
)

from app.utils.pipeline_options import SUPPORTED_FORMATS, get_format_options

# Formats docling knows but whose backend needs an install extra the image does
# not ship: odfdo (OpenDocument), arelle-release (XBRL), whisper/librosa
# (audio, video).
UNSUPPORTED = [
    InputFormat.ODT,
    InputFormat.ODS,
    InputFormat.ODP,
    InputFormat.XML_XBRL,
    InputFormat.AUDIO,
    InputFormat.VIDEO,
]


def test_supported_formats_is_everything_docling_can_convert_here():
    assert SUPPORTED_FORMATS == set(InputFormat) - set(UNSUPPORTED)


@pytest.mark.parametrize("format", sorted(SUPPORTED_FORMATS, key=lambda f: f.value))
def test_every_supported_format_yields_its_options(format):
    assert list(get_format_options({}, format)) == [format]


@pytest.mark.parametrize("format", UNSUPPORTED)
def test_format_without_its_extra_is_rejected(format):
    with pytest.raises(ValueError, match="Unsupported format"):
        get_format_options({}, format)


# A file extension is not an InputFormat value: 'jpg' is the extension of
# InputFormat.IMAGE, whose value is 'image'.
@pytest.mark.parametrize("format", ["jpg", "adoc", "tar.gz", "nonsense"])
def test_non_format_string_is_rejected(format):
    with pytest.raises(ValueError, match="Invalid format"):
        get_format_options({}, format)


# docling defaults the PDF pipeline's OCR to OcrAutoOptions, which picks an
# engine by probing the environment and drops `lang` on the way, so the enrich
# item's language would go nowhere.
@pytest.mark.parametrize(
    "format", [InputFormat.PDF, InputFormat.IMAGE, InputFormat.METS_GBS]
)
def test_pdf_pipeline_ocr_is_pinned_to_easyocr(format):
    options = get_format_options({}, format)[format]

    assert isinstance(options.pipeline_options, PdfPipelineOptions)
    assert isinstance(options.pipeline_options.ocr_options, EasyOcrOptions)


def test_ocr_language_from_the_enrich_item_reaches_the_engine():
    configs = {"pipeline_options.ocr_options.lang": ["it", "en"]}

    options = get_format_options(configs, InputFormat.PDF)[InputFormat.PDF]

    assert options.pipeline_options.ocr_options.lang == ["it", "en"]


# Configs are merged with setattr and docling's models do not validate on
# assignment, so `mode` stays the raw string. OcrMode is a str enum and docling
# only ever compares it with ==, so the string is equivalent at runtime.
def test_ocr_mode_from_the_enrich_item_is_applied():
    configs = {"pipeline_options.ocr_options.mode": "full_page"}

    options = get_format_options(configs, InputFormat.PDF)[InputFormat.PDF]

    assert options.pipeline_options.ocr_options.mode == OcrMode.FULL_PAGE


# `force_full_page_ocr` was superseded by `mode` but docling keeps it as a
# deprecated view over it, so enrich items configured on the old name still work.
def test_deprecated_force_full_page_ocr_still_sets_the_mode():
    configs = {"pipeline_options.ocr_options.force_full_page_ocr": "true"}

    options = get_format_options(configs, InputFormat.PDF)[InputFormat.PDF]

    # This one goes through the deprecated property's setter, which assigns the
    # enum itself rather than the string.
    assert options.pipeline_options.ocr_options.mode is OcrMode.FULL_PAGE


def test_nested_pipeline_options_are_normalized_and_applied():
    configs = {
        "pipeline_options.do_ocr": "false",
        "pipeline_options.images_scale": "2",
        "pipeline_options.accelerator_options.num_threads": "8",
        "pipeline_options.document_timeout": "",
    }

    pipeline_options = get_format_options(configs, InputFormat.PDF)[
        InputFormat.PDF
    ].pipeline_options

    assert pipeline_options.do_ocr is False
    assert pipeline_options.images_scale == 2
    assert pipeline_options.accelerator_options.num_threads == 8
    assert pipeline_options.document_timeout is None


def test_unknown_config_key_is_ignored():
    configs = {"pipeline_options.non_esiste": "1", "pipeline_options.do_ocr": "false"}

    pipeline_options = get_format_options(configs, InputFormat.PDF)[
        InputFormat.PDF
    ].pipeline_options

    assert not hasattr(pipeline_options, "non_esiste")
    assert pipeline_options.do_ocr is False
