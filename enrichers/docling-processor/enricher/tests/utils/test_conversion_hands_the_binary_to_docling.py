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

from contextlib import contextmanager
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import pytest
import requests
from docling.datamodel.base_models import InputFormat
from docling_core.types.io import DocumentStream

import app.utils.converter as converter

URL = "http://binaries/r0?X-Amz-Signature=deadbeef"
NAME = "https://minio/bucket/report.pdf"
CONTENT = b"%PDF-1.4 downloaded bytes"
CONFIGS = {"error_strategy": "fail-soft", "pipeline_options.do_ocr": "false"}


@pytest.fixture(autouse=True)
def empty_converter_cache():
    # The cache lives in a thread local shared by every test in this thread.
    converter._converters.cache = {}


# Stub the network, the format detection and docling out of conversion(), and
# yield the mocks so the test can check what each one was handed.
@contextmanager
def _stubbed():
    format_options = object()
    with (
        patch.object(converter, "requests") as requests_mock,
        patch.object(
            converter, "detect_format", return_value=InputFormat.PDF
        ) as detect_format,
        patch.object(
            converter, "stream_name", return_value="report.pdf"
        ) as stream_name,
        patch.object(
            converter, "get_format_options", return_value=format_options
        ) as get_format_options,
        patch.object(converter, "DocumentConverter") as converter_class,
    ):
        requests_mock.get.return_value = MagicMock(content=CONTENT)
        yield SimpleNamespace(
            requests=requests_mock,
            detect_format=detect_format,
            stream_name=stream_name,
            get_format_options=get_format_options,
            format_options=format_options,
            converter_class=converter_class,
        )


def test_the_downloaded_binary_is_what_docling_converts():
    with _stubbed() as stubs:
        result = converter.conversion({"url": URL, "name": NAME}, CONFIGS)

    stubs.requests.get.assert_called_once_with(
        URL, timeout=converter.FETCH_TIMEOUT_SECONDS
    )
    stubs.requests.get.return_value.raise_for_status.assert_called_once_with()
    stubs.detect_format.assert_called_once_with(CONTENT, NAME)
    stubs.stream_name.assert_called_once_with(NAME, InputFormat.PDF)
    stubs.get_format_options.assert_called_once_with(CONFIGS, InputFormat.PDF)
    stubs.converter_class.assert_called_once_with(format_options=stubs.format_options)

    convert = stubs.converter_class.return_value.convert
    convert.assert_called_once()
    (source,) = convert.call_args.args
    assert isinstance(source, DocumentStream)
    assert source.name == "report.pdf"
    assert source.stream.getvalue() == CONTENT
    assert result is convert.return_value


# The name is only a hint: a binary without one is detected from its bytes.
def test_a_binary_without_a_name_is_detected_from_its_bytes():
    with _stubbed() as stubs:
        converter.conversion({"url": URL}, {})

    stubs.detect_format.assert_called_once_with(CONTENT, "")
    stubs.stream_name.assert_called_once_with("", InputFormat.PDF)


# An expired pre-signed URL answers with an error page: converting it would
# index the error page in place of the document.
def test_a_failed_download_is_raised_without_converting():
    with _stubbed() as stubs:
        stubs.requests.get.return_value.raise_for_status.side_effect = (
            requests.HTTPError("403 Forbidden")
        )
        with pytest.raises(requests.HTTPError, match="403 Forbidden"):
            converter.conversion({"url": URL, "name": NAME}, CONFIGS)

    stubs.detect_format.assert_not_called()
    stubs.converter_class.assert_not_called()
