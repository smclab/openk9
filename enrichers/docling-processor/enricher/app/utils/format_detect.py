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

from io import BytesIO
from urllib.parse import unquote, urlparse

from docling.datamodel.base_models import FormatToExtensions, InputFormat
from docling.datamodel.document import DocumentStream, _DocumentConversionInput

from app.utils.exceptions import FormatError


def _name_hint(name: str) -> str:
    """
    Reduce a binary's `name` to the bare file name Docling can read an
    extension from. The payload carries whatever the connector put there,
    which may be a full URL with a query string (openk9-crawler sets the
    document URL), so keep only the last path segment.
    """
    return unquote(urlparse(name).path).rsplit("/", 1)[-1] or "unknown"


def detect_format(content: bytes, name: str = "") -> InputFormat:
    """
    Detect the document's format from the raw binary content using Docling
    internal functions.

    Text-based formats (md, eml, tex, vtt) are indistinguishable as bytes:
    Docling tells them apart by the file extension, so the binary's name is
    passed through as a hint when the payload carries one.

    Args:
        content: the raw file bytes
        name: the binary's name from the payload, used only as a hint

    Returns:
        The detected `InputFormat`, or raises FormatError if the format could
        not be detected.
    """
    try:
        stream = DocumentStream(name=_name_hint(name), stream=BytesIO(content))
        dci = _DocumentConversionInput(path_or_stream_iterator=[])
        detected_format = dci._guess_format(stream)
        if detected_format and detected_format in FormatToExtensions:
            return detected_format
        raise FormatError("File format could not be detected or file is corrupted")

    except Exception:
        raise FormatError("File format could not be detected or file is corrupted")


def stream_name(name: str, format: InputFormat) -> str:
    """
    Name for the stream handed to Docling. Reuse the binary's own file name
    when it carries an extension, so Docling's internal format detection sees
    the same input ours did and cannot disagree with it; otherwise fall back
    to the format's canonical extension.
    """
    hint = _name_hint(name)
    return hint if "." in hint else f"doc.{FormatToExtensions[format][0]}"
