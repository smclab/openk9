"""
Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "app"))

from extraction.source import object_url, text_content  # noqa: E402


def test_object_url_joins_base_bucket_and_object():
    assert object_url("http://minio:9000", "docs", "report.pdf") == \
        "http://minio:9000/docs/report.pdf"


def test_object_url_keeps_slashes_and_encodes_the_rest():
    assert object_url("https://files.example.com/", "docs", "2026/q3 plan.pdf") == \
        "https://files.example.com/docs/2026/q3%20plan.pdf"


def test_text_content_of_a_text_file():
    assert text_content("text/plain; charset=utf-8", "ciao è".encode("utf-8")) == "ciao è"


def test_text_content_is_empty_for_other_formats():
    assert text_content("application/pdf", b"%PDF-1.4") == ""


def test_text_content_is_empty_for_undecodable_text():
    assert text_content("text/plain", b"\xff\xfe\xfa") == ""


def test_text_content_is_empty_without_content_type():
    assert text_content(None, b"anything") == ""
