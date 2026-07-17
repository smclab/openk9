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

from docling.datamodel.base_models import FormatToExtensions
from docling.datamodel.document import DocumentStream, _DocumentConversionInput

from app.utils.exceptions import FormatError


def extract_extension(content: bytes) -> str:
    """
    Extract the file's extension from the raw binary content using Docling
    internal functions.

    Args:
        content: the raw file bytes

    Returns:
        File's extension (e.g. 'pdf', 'docx'), or raises FormatError if the
        format could not be detected.
    """
    try:
        stream = DocumentStream(name="unknown", stream=BytesIO(content))
        dci = _DocumentConversionInput(path_or_stream_iterator=[])
        detected_format = dci._guess_format(stream)
        if detected_format and detected_format in FormatToExtensions:
            return FormatToExtensions[detected_format][0]
        raise FormatError("File format could not be detected or file is corrupted")

    except Exception:
        raise FormatError("File format could not be detected or file is corrupted")
