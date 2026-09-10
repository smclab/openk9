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
import json
import os
import threading
from io import BytesIO

from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream
from dotenv import load_dotenv

from app.utils.fm_helper import FileManagerHelper
from app.utils.format_detect import extract_extension_base64
from app.utils.logger import logger
from app.utils.pipeline_options import get_format_options

# app.server is imported before it loads the .env itself, so this module has to
# load it too, or the hosts below would only honour real environment variables.
load_dotenv()

FILE_MANAGER_HOST = os.getenv("FILE_MANAGER_HOST", default="http://localhost:8000")
DATASOURCE_HOST = os.getenv("DATASOURCE_HOST", default="http://localhost:8001")
FMHelper = FileManagerHelper(FILE_MANAGER_HOST)

# One converter per worker thread, per format and configuration. Building a
# DocumentConverter loads the docling models (OCR, layout, table structure),
# so a converter per request means reloading them every time; sharing a single
# converter across threads is not an option either, since docling does not
# support concurrent conversions on the same pipeline.
_converters = threading.local()


def _get_converter(extension, configs):
    cache = getattr(_converters, "cache", None)
    if cache is None:
        cache = _converters.cache = {}

    key = (extension, json.dumps(configs, sort_keys=True, default=str))
    converter = cache.get(key)
    if converter is None:
        logger.info(f"Building a converter for {extension}")
        format_options = get_format_options(configs, extension)
        converter = cache[key] = DocumentConverter(format_options=format_options)

    return converter


def conversion(bin, tenant, configs):
    """
    Converts a binary resource into a document object using a base64-encoded source.

    This function retrieves a base64-encoded resource associated with the given
    tenant and resource identifier, decodes it into a binary stream, determines
    the document extension, and converts it into an internal document representation
    using the configured document converter.

    Args:
        bin (dict): A dictionary representing a binary resource. It must contain
            the key `"resourceId"` identifying the resource to be retrieved.
        tenant (str): The tenant identifier used to resolve the resource context.

    Returns:
        Any: The result of the document conversion process. The returned object
        is expected to expose a `document` attribute supporting export operations
        (e.g. `export_to_markdown()`).

    """
    resource_id = bin.get("resourceId")
    resource = FMHelper.get_base64(tenant, resource_id)
    bites = BytesIO(base64.b64decode(resource))
    extension = extract_extension_base64(resource)
    source = DocumentStream(name=f"doc.{extension}", stream=bites)
    converter = _get_converter(extension, configs)
    result = converter.convert(source)
    return result
