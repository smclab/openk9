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

import json
import os
import threading
from io import BytesIO

import requests
from docling.datamodel.base_models import InputFormat
from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream
from dotenv import load_dotenv

from app.utils.format_detect import detect_format, stream_name
from app.utils.logger import logger
from app.utils.pipeline_options import get_format_options

# app.server is imported before it loads the .env itself, so this module has to
# load it too, or the timeout below would only honour real environment
# variables.
load_dotenv()

FETCH_TIMEOUT_SECONDS = float(os.getenv("FETCH_TIMEOUT_SECONDS", "30"))

# One converter per worker thread, per format and configuration. Building a
# DocumentConverter loads the docling models (OCR, layout, table structure),
# so a converter per request means reloading them every time; sharing a single
# converter across threads is not an option either, since docling does not
# support concurrent conversions on the same pipeline.
_converters = threading.local()

# Formats whose pipeline loads the docling models. Their converters are the
# expensive ones (~1.3 GiB resident each, held until the converter is dropped),
# and two enrich items configured differently would otherwise stack one copy
# per configuration until the container is out of memory. Only the last one is
# kept, so a worker holds a single set of models whatever the configuration.
# The other formats parse with plain backends and cost nothing to keep.
_MODEL_BACKED_FORMATS = frozenset({InputFormat.PDF.value, InputFormat.IMAGE.value})


def _get_converter(file_format, configs):
    cache = getattr(_converters, "cache", None)
    if cache is None:
        cache = _converters.cache = {}

    key = (file_format.value, json.dumps(configs, sort_keys=True, default=str))
    converter = cache.get(key)
    if converter is None:
        logger.info(f"Building a converter for {file_format.value}")
        if file_format.value in _MODEL_BACKED_FORMATS:
            for cached in [k for k in cache if k[0] in _MODEL_BACKED_FORMATS]:
                logger.info(f"Dropping the cached converter for {cached[0]}")
                del cache[cached]
        format_options = get_format_options(configs, file_format)
        converter = cache[key] = DocumentConverter(format_options=format_options)

    return converter


def conversion(bin, configs):
    """
    Converts a binary resource into a document object.

    This function fetches the binary from the pre-signed GET URL carried in the
    payload, determines the document extension from its bytes, and converts it
    into an internal document representation using the configured document
    converter.

    Args:
        bin (dict): A dictionary representing a binary resource. It must contain
            the key `"url"`, a pre-signed GET URL from which the resource is
            fetched, and may carry `"name"`, the binary's file name, used as a
            hint to tell text-based formats apart.
        configs (dict): The enrich item configuration, applied to the docling
            format options.

    Returns:
        Any: The result of the document conversion process. The returned object
        is expected to expose a `document` attribute supporting export operations
        (e.g. `export_to_markdown()`).

    """
    url = bin.get("url")
    response = requests.get(url, timeout=FETCH_TIMEOUT_SECONDS)
    response.raise_for_status()
    content = response.content
    bites = BytesIO(content)
    name = bin.get("name", "")
    file_format = detect_format(content, name)
    source = DocumentStream(name=stream_name(name, file_format), stream=bites)
    converter = _get_converter(file_format, configs)
    result = converter.convert(source)
    return result
