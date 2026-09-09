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

from app.utils.exceptions import FormatError
from app.utils.format_detect import detect_format, stream_name

PDF = (
    b"%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\ntrailer\n"
    b"<< /Root 1 0 R >>\n%%EOF\n"
)
MARKDOWN = b"# Titolo\n\nUn paragrafo.\n"
EMAIL = (
    b"From: a@example.com\nTo: b@example.com\nSubject: Prova\n"
    b'Content-Type: text/plain; charset="utf-8"\n\nCorpo.\n'
)


# Structured formats carry their own magic bytes, so the name is not needed.
def test_binary_format_detected_without_a_name():
    assert detect_format(PDF) is InputFormat.PDF


# Text-based formats are indistinguishable as bytes: docling reads them off the
# extension, so without a name there is nothing to go on.
@pytest.mark.parametrize("content", [MARKDOWN, EMAIL])
def test_text_format_is_undetectable_without_a_name(content):
    with pytest.raises(FormatError):
        detect_format(content)


@pytest.mark.parametrize(
    "content,name,expected",
    [
        (MARKDOWN, "note.md", InputFormat.MD),
        (EMAIL, "messaggio.eml", InputFormat.EMAIL),
    ],
)
def test_text_format_detected_from_the_name(content, name, expected):
    assert detect_format(content, name) is expected


# openk9-crawler puts the document URL in the binary's name, so the hint has to
# survive a path and a query string.
@pytest.mark.parametrize(
    "name",
    [
        "https://minio/bucket/note.md",
        "https://minio/bucket/note.md?X-Amz-Signature=deadbeef",
        "https://minio/bucket/una%20nota.md",
    ],
)
def test_url_shaped_name_is_reduced_to_its_file_name(name):
    assert detect_format(MARKDOWN, name) is InputFormat.MD


def test_undetectable_content_raises_format_error():
    with pytest.raises(FormatError):
        detect_format(bytes(range(256)) * 8, "misterioso.xyz")


# The stream handed to docling must carry a name docling itself resolves to the
# format we detected, otherwise its internal detection could disagree with ours.
def test_stream_name_keeps_the_payload_name_when_it_has_an_extension():
    assert stream_name("https://minio/bucket/note.md", InputFormat.MD) == "note.md"


def test_stream_name_falls_back_to_the_canonical_extension():
    assert stream_name("", InputFormat.PDF) == "doc.pdf"
    assert stream_name("senza-estensione", InputFormat.DOCX) == "doc.docx"
