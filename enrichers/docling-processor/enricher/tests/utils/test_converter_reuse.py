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

from concurrent.futures import ThreadPoolExecutor
from unittest.mock import MagicMock, patch

import pytest
from docling.datamodel.base_models import InputFormat

import app.utils.converter as converter

BINARY = {"url": "http://binaries/r0", "name": "doc.pdf"}


@pytest.fixture(autouse=True)
def empty_converter_cache():
    # The cache lives in a thread local, and pytest runs every test in the
    # same thread: without this the count leaks from one test to the next.
    converter._converters.cache = {}


# Run conversion() with docling and the network stubbed out, and return the
# mocked DocumentConverter class so the caller can count how often it was built.
def _run(configs, file_format=InputFormat.PDF, times=1):
    with patch.object(converter, "requests") as requests_mock, patch.object(
        converter, "detect_format", return_value=file_format
    ), patch.object(converter, "stream_name", return_value="doc.pdf"), patch.object(
        converter, "get_format_options", return_value={}
    ), patch.object(
        converter, "DocumentConverter"
    ) as converter_class:
        requests_mock.get.return_value = MagicMock(content=b"%PDF-1.4")
        for _ in range(times):
            converter.conversion(BINARY, configs)
        return converter_class, requests_mock


def test_the_same_configuration_reuses_one_converter():
    converter_class, _ = _run({"do_ocr": "true"}, times=3)

    assert converter_class.call_count == 1


# The cache key is the configuration's content, not the order its keys came in.
def test_the_same_configuration_in_another_key_order_reuses_the_converter():
    _run({"a": 1, "b": 2})
    converter_class, _ = _run({"b": 2, "a": 1})

    assert converter_class.call_count == 0


# docling does not support concurrent conversions on the same pipeline, so a
# converter built by one worker thread must never be handed to another.
def test_each_worker_thread_builds_its_own_converter():
    def convert_in_a_new_thread():
        # A pool of its own per call: the two conversions cannot share a thread.
        with ThreadPoolExecutor(max_workers=1) as pool:
            pool.submit(converter.conversion, BINARY, {}).result(timeout=30)

    with patch.object(converter, "requests") as requests_mock, patch.object(
        converter, "detect_format", return_value=InputFormat.PDF
    ), patch.object(converter, "stream_name", return_value="doc.pdf"), patch.object(
        converter, "get_format_options", return_value={}
    ), patch.object(
        converter, "DocumentConverter"
    ) as converter_class:
        requests_mock.get.return_value = MagicMock(content=b"%PDF-1.4")
        convert_in_a_new_thread()
        convert_in_a_new_thread()

    assert converter_class.call_count == 2
    # Neither of them touched the cache of the thread running the test.
    assert converter._converters.cache == {}


def test_a_different_format_gets_its_own_converter():
    _run({}, file_format=InputFormat.PDF)
    converter_class, _ = _run({}, file_format=InputFormat.DOCX)

    # The PDF converter built by the first call is still cached, so this one
    # is built on top of it rather than replacing it.
    assert converter_class.call_count == 1
    assert len(converter._converters.cache) == 2


def test_the_binary_is_fetched_with_a_timeout():
    _, requests_mock = _run({})

    assert (
        requests_mock.get.call_args.kwargs["timeout"]
        == converter.FETCH_TIMEOUT_SECONDS
    )


def test_a_new_configuration_replaces_the_model_backed_converter():
    _run({"ocr_options": {"lang": ["it"]}}, file_format=InputFormat.PDF)
    converter_class, _ = _run({"ocr_options": {"lang": ["en"]}}, file_format=InputFormat.PDF)

    # Each model-backed converter keeps its own copy of the docling models, so
    # a second configuration must replace the first instead of stacking on it.
    assert converter_class.call_count == 1
    assert len(converter._converters.cache) == 1


def test_a_plain_format_does_not_evict_the_model_backed_converter():
    _run({}, file_format=InputFormat.PDF)
    _run({}, file_format=InputFormat.DOCX)
    converter_class, _ = _run({}, file_format=InputFormat.PDF)

    # Plain backends cost nothing to keep, and keeping them must not cost the
    # PDF converter a reload.
    assert converter_class.call_count == 0
    assert len(converter._converters.cache) == 2
