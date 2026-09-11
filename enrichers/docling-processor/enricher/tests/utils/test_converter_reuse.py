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

import base64
from unittest.mock import patch

import pytest

import app.utils.converter as converter

BINARY = {"resourceId": "r0"}
RESOURCE = base64.b64encode(b"%PDF-1.4").decode()


@pytest.fixture(autouse=True)
def empty_converter_cache():
    # The cache lives in a thread local, and pytest runs every test in the
    # same thread: without this the count leaks from one test to the next.
    converter._converters.cache = {}


# Run conversion() with docling and the file manager stubbed out, and return
# the mocked DocumentConverter class so the caller can count how often it was
# built.
def _run(configs, extension="pdf", times=1):
    with patch.object(
        converter.FMHelper, "get_base64", return_value=RESOURCE
    ) as get_base64, patch.object(
        converter, "extract_extension_base64", return_value=extension
    ), patch.object(
        converter, "get_format_options", return_value={}
    ), patch.object(
        converter, "DocumentConverter"
    ) as converter_class:
        for _ in range(times):
            converter.conversion(BINARY, "tenant", configs)
        return converter_class, get_base64


def test_the_same_configuration_reuses_one_converter():
    converter_class, _ = _run({"do_ocr": "true"}, times=3)

    assert converter_class.call_count == 1


def test_a_different_format_gets_its_own_converter():
    _run({}, extension="pdf")
    converter_class, _ = _run({}, extension="docx")

    # The PDF converter built by the first call is still cached, so this one
    # is built on top of it rather than replacing it.
    assert converter_class.call_count == 1
    assert len(converter._converters.cache) == 2


def test_the_binary_is_fetched_from_the_file_manager():
    _, get_base64 = _run({})

    assert get_base64.call_args.args == ("tenant", "r0")


def test_a_new_configuration_replaces_the_model_backed_converter():
    _run({"ocr_options": {"lang": ["it"]}}, extension="pdf")
    converter_class, _ = _run({"ocr_options": {"lang": ["en"]}}, extension="pdf")

    # Each model-backed converter keeps its own copy of the docling models, so
    # a second configuration must replace the first instead of stacking on it.
    assert converter_class.call_count == 1
    assert len(converter._converters.cache) == 1


def test_a_plain_format_does_not_evict_the_model_backed_converter():
    _run({}, extension="pdf")
    _run({}, extension="docx")
    converter_class, _ = _run({}, extension="pdf")

    # Plain backends cost nothing to keep, and keeping them must not cost the
    # PDF converter a reload.
    assert converter_class.call_count == 0
    assert len(converter._converters.cache) == 2
