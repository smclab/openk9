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

import os
from io import BytesIO

import requests
from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream

from app.utils.format_detect import extract_extension
from app.utils.logger import logger
from app.utils.pipeline_options import get_format_options

DATASOURCE_HOST = os.getenv("DATASOURCE_HOST", default="http://localhost:8001")


def conversion(bin, tenant, configs):
    """
    Converts a binary resource into a document object using a base64-encoded source.

    This function retrieves a base64-encoded resource associated with the given
    tenant and resource identifier, decodes it into a binary stream, determines
    the document extension, and converts it into an internal document representation
    using the configured document converter.

    Args:
        bin (dict): A dictionary representing a binary resource. It must contain
            the key `"url"`, a pre-signed GET URL from which the resource is
            fetched.
        tenant (str): The tenant identifier used to resolve the resource context.

    Returns:
        Any: The result of the document conversion process. The returned object
        is expected to expose a `document` attribute supporting export operations
        (e.g. `export_to_markdown()`).

    """
    url = bin.get("url")
    response = requests.get(url)
    response.raise_for_status()
    content = response.content
    bites = BytesIO(content)
    extension = extract_extension(content)
    source = DocumentStream(name=f"doc.{extension}", stream=bites)
    format_options = get_format_options(configs, extension)
    converter = DocumentConverter(format_options=format_options)
    result = converter.convert(source)
    return result
