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

import logging

import pytest
from docling.datamodel.base_models import InputFormat
from docling.datamodel.pipeline_options import (
    EasyOcrOptions,
    OcrMode,
    PdfPipelineOptions,
    PictureDescriptionApiOptions,
    PictureDescriptionVlmOptions,
)

from app.utils.pipeline_options import SUPPORTED_FORMATS, get_format_options

# Formats docling knows but this image cannot convert: their backend needs an
# install extra we do not ship -- odfdo (OpenDocument), arelle-release (XBRL),
# whisper/librosa (audio, video) -- or docling can only read them from a file,
# never from the stream this enricher has (XML_USPTO, METS_GBS).
UNSUPPORTED = [
    InputFormat.ODT,
    InputFormat.ODS,
    InputFormat.ODP,
    InputFormat.XML_XBRL,
    InputFormat.AUDIO,
    InputFormat.VIDEO,
    InputFormat.XML_USPTO,
    InputFormat.METS_GBS,
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
@pytest.mark.parametrize("format", ["jpg", "adoc", "eml", "nonsense"])
def test_non_format_string_is_rejected(format):
    with pytest.raises(ValueError, match="Invalid format"):
        get_format_options({}, format)


# docling defaults the PDF pipeline's OCR to OcrAutoOptions, which picks an
# engine by probing the environment and drops `lang` on the way, so the enrich
# item's language would go nowhere.
@pytest.mark.parametrize("format", [InputFormat.PDF, InputFormat.IMAGE])
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


# An option nobody applied is worse than a failed conversion: the enrich item
# looks configured and is not, so the key and the options that rejected it have
# to be in the logs.
def test_unknown_config_key_is_skipped_with_a_warning(caplog):
    configs = {"pipeline_options.non_esiste": "1", "pipeline_options.do_ocr": "false"}

    with caplog.at_level(logging.WARNING):
        pipeline_options = get_format_options(configs, InputFormat.PDF)[
            InputFormat.PDF
        ].pipeline_options

    assert not hasattr(pipeline_options, "non_esiste")
    assert pipeline_options.do_ocr is False
    assert "non_esiste" in caplog.text
    assert "PdfPipelineOptions" in caplog.text


# The same key is unknown to the pipelines that have no OCR at all, and there
# the log is what tells a typo apart from an option the format cannot take.
def test_config_key_of_another_pipeline_names_the_options_that_refused_it(caplog):
    configs = {"pipeline_options.ocr_options.lang": ["it"]}

    with caplog.at_level(logging.WARNING):
        get_format_options(configs, InputFormat.HTML)

    assert "ocr_options" in caplog.text


def test_picture_description_api_options_are_built_from_the_configuration():
    configs = {
        "pipeline_options": {
            "do_picture_description": True,
            "enable_remote_services": True,
            "picture_description_options": {
                "kind": "api",
                "url": "http://endpoint-esterno/v1/chat/completions",
                "headers": {"Authorization": "Bearer TOKEN"},
                "params": {"model": "qualche-modello"},
                "prompt": "Descrivi l'immagine",
                "timeout": 45,
            },
        }
    }

    options = get_format_options(configs, InputFormat.PDF)[
        InputFormat.PDF
    ].pipeline_options.picture_description_options

    assert isinstance(options, PictureDescriptionApiOptions)
    assert str(options.url) == "http://endpoint-esterno/v1/chat/completions"
    assert options.headers == {"Authorization": "Bearer TOKEN"}
    assert options.params == {"model": "qualche-modello"}
    assert options.prompt == "Descrivi l'immagine"
    assert options.timeout == 45


def test_picture_description_vlm_options_are_built_from_the_configuration():
    configs = {
        "pipeline_options.picture_description_options.kind": "vlm",
        "pipeline_options.picture_description_options.repo_id": (
            "ibm-granite/granite-vision-3.3-2b"
        ),
    }

    options = get_format_options(configs, InputFormat.PDF)[
        InputFormat.PDF
    ].pipeline_options.picture_description_options

    assert isinstance(options, PictureDescriptionVlmOptions)
    assert options.repo_id == "ibm-granite/granite-vision-3.3-2b"


# Without a `kind` the configuration still updates whatever docling defaults to,
# which is a local vision model. Only the keys that model does have are applied.
def test_picture_description_without_a_kind_updates_the_default():
    configs = {
        "pipeline_options.picture_description_options.picture_area_threshold": 0.001
    }

    default = get_format_options({}, InputFormat.PDF)[
        InputFormat.PDF
    ].pipeline_options.picture_description_options
    options = get_format_options(configs, InputFormat.PDF)[
        InputFormat.PDF
    ].pipeline_options.picture_description_options

    assert type(options) is type(default)
    assert options.picture_area_threshold == 0.001


def test_unknown_picture_description_kind_keeps_the_default(caplog):
    configs = {"pipeline_options.picture_description_options.kind": "telepatia"}

    with caplog.at_level(logging.WARNING):
        options = get_format_options(configs, InputFormat.PDF)[
            InputFormat.PDF
        ].pipeline_options.picture_description_options

    assert not isinstance(
        options, (PictureDescriptionApiOptions, PictureDescriptionVlmOptions)
    )
    assert "telepatia" in caplog.text


# The docling options do not forbid extra fields, so a key that is not theirs
# would be dropped on construction as silently as it was before the merge.
def test_unknown_key_in_the_picture_description_options_is_skipped_with_a_warning(
    caplog,
):
    configs = {
        "pipeline_options.picture_description_options.kind": "api",
        "pipeline_options.picture_description_options.prompt": "Descrivi l'immagine",
        "pipeline_options.picture_description_options.non_esiste": "1",
    }

    with caplog.at_level(logging.WARNING):
        options = get_format_options(configs, InputFormat.PDF)[
            InputFormat.PDF
        ].pipeline_options.picture_description_options

    assert isinstance(options, PictureDescriptionApiOptions)
    assert options.prompt == "Descrivi l'immagine"
    assert "non_esiste" in caplog.text
    assert "PictureDescriptionApiOptions" in caplog.text


# `repo_id` has no default, so the options cannot be built at all: the enrich
# item keeps converting with the model docling defaults to.
def test_picture_description_options_that_do_not_validate_keep_the_default(caplog):
    configs = {"pipeline_options.picture_description_options.kind": "vlm"}

    with caplog.at_level(logging.WARNING):
        options = get_format_options(configs, InputFormat.PDF)[
            InputFormat.PDF
        ].pipeline_options.picture_description_options

    assert not isinstance(options, PictureDescriptionVlmOptions)
    assert "repo_id" in caplog.text


def test_enable_remote_services_reaches_docling():
    configs = {"pipeline_options.enable_remote_services": "true"}

    pipeline_options = get_format_options(configs, InputFormat.PDF)[
        InputFormat.PDF
    ].pipeline_options

    assert pipeline_options.enable_remote_services is True
