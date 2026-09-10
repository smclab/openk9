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
