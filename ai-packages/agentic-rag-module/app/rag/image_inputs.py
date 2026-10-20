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


"""What the generation model can see, and the images it gets to see.

Two concerns live here, both outside the graph so that they can be tested on
their own:

- whether the tenant's chat model accepts images. Nobody declares it: the
  module asks Ollama, then reads the LangChain profile of the model, and when
  neither knows it sends the images and lets the provider answer. A model the
  module could not classify and whose provider rejected the images is then
  treated as text only until the process restarts;
- reading the image of a retrieved source from its URL at the source, with the
  limits that protect the memory and the latency of the module.

The bytes read here never leave the response node: they are not part of the
graph state, so neither the checkpoint nor the chat history ever holds them.
"""

import base64
import io
import os
import re
import time
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from typing import Optional
from urllib.parse import urljoin, urlsplit

import requests
from langchain_ollama import ChatOllama
from PIL import Image

from app.utils.logger import get_logger

logger = get_logger(__name__)

# Fixed on purpose: they protect the module and the chat latency, they are not
# a tuning knob. Whoever needs other sizes or formats prepares the images
# upstream, with an enricher or a middleware.
MAX_SOURCE_IMAGES = 4
MAX_SOURCE_IMAGE_BYTES = 20 * 1024 * 1024
FETCH_TIMEOUT_SECONDS = 10
MAX_REDIRECTS = 5

ALLOWED_SCHEMES = ("http", "https")
DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434"
OLLAMA_VISION_CAPABILITY = "vision"

# Same semantics as the plugin driver and enricher whitelist of the datasource:
# comma-separated regexes, one of which must match the whole host.
SOURCE_IMAGE_HOST_REGEXES = [
    regex.strip()
    for regex in os.getenv("SOURCE_IMAGE_HOST_REGEXES", ".*").split(",")
    if regex.strip()
]

_DOWNLOAD_CHUNK_BYTES = 64 * 1024

# (provider class, base url, model) -> what /api/show answered
_ollama_capabilities: dict[tuple, bool] = {}
# models the module could not classify and whose provider rejected the images
_text_only_models: set[tuple] = set()


@dataclass(frozen=True)
class SourceImage:
    """An image ready for a model message, base64 encoded."""

    data: str
    mime_type: str


class ImageFetchError(Exception):
    """A source image that cannot be given to the model."""


def _model_key(llm) -> tuple:
    model = (
        getattr(llm, "model", None)
        or getattr(llm, "model_name", None)
        or getattr(llm, "model_id", None)
    )
    base_url = (
        getattr(llm, "base_url", None)
        or getattr(llm, "openai_api_base", None)
        or getattr(llm, "url", None)
    )
    return type(llm).__name__, str(base_url), str(model)


def _ollama_accepts_images(llm: ChatOllama) -> Optional[bool]:
    key = _model_key(llm)
    if key in _ollama_capabilities:
        return _ollama_capabilities[key]

    base_url = (llm.base_url or DEFAULT_OLLAMA_BASE_URL).rstrip("/")
    try:
        response = requests.post(
            f"{base_url}/api/show",
            json={"model": llm.model},
            timeout=FETCH_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        capabilities = response.json().get("capabilities") or []
    except (requests.RequestException, ValueError) as error:
        # Not cached: the next turn asks again.
        logger.warning(
            f"[image_inputs] Ollama capabilities of {llm.model} unknown: {error}"
        )
        return None

    accepts = OLLAMA_VISION_CAPABILITY in capabilities
    _ollama_capabilities[key] = accepts
    logger.info(f"[image_inputs] Ollama model {llm.model} vision={accepts}")
    return accepts


def accepts_images(llm) -> Optional[bool]:
    """Tell whether the chat model accepts images.

    :param llm: The chat model of the tenant
    :type llm: BaseChatModel

    :return: True or False when Ollama or the LangChain profile know it, None
        when nobody does and the module has to try
    :rtype: Optional[bool]
    """
    if _model_key(llm) in _text_only_models:
        return False

    if isinstance(llm, ChatOllama):
        return _ollama_accepts_images(llm)

    profile = getattr(llm, "profile", None)
    if isinstance(profile, dict) and "image_inputs" in profile:
        return bool(profile["image_inputs"])

    return None


def mark_text_only(llm) -> None:
    """Treat the model as text only until the process restarts."""
    _text_only_models.add(_model_key(llm))


def _status_code(error: BaseException) -> Optional[int]:
    for candidate in (
        getattr(error, "status_code", None),
        getattr(getattr(error, "response", None), "status_code", None),
        getattr(error, "code", None),
    ):
        if isinstance(candidate, int):
            return candidate

    # botocore keeps the HTTP status in the parsed error response
    response = getattr(error, "response", None)
    if isinstance(response, dict):
        status = response.get("ResponseMetadata", {}).get("HTTPStatusCode")
        if isinstance(status, int):
            return status

    return None


def is_rejection(error: BaseException) -> bool:
    """Tell whether the provider rejected a request because of its images.

    Rate limits, server errors, timeouts and dropped connections say nothing
    about the images: they keep today's handling. Any other error of a request
    that carried images is taken as a rejection, whatever the provider calls
    it.
    """
    status = _status_code(error)
    if status == 429 or "rate_limit" in str(error).lower():
        return False
    if status is not None and status >= 500:
        return False

    names = [cls.__name__ for cls in type(error).__mro__]
    if any("Timeout" in name or "Connection" in name for name in names):
        return False

    return True


def _without_query(url: str) -> str:
    """The URL as it goes to the log: a pre-signed query is a credential."""
    parts = urlsplit(url)
    return f"{parts.scheme}://{parts.netloc}{parts.path}"


def _check_url(url: str) -> None:
    parts = urlsplit(url)
    if parts.scheme not in ALLOWED_SCHEMES:
        raise ImageFetchError(f"scheme {parts.scheme!r} not allowed")

    host = parts.hostname or ""
    if not any(re.fullmatch(regex, host) for regex in SOURCE_IMAGE_HOST_REGEXES):
        raise ImageFetchError(f"host {host!r} not allowed by the whitelist")


def _download(url: str) -> bytes:
    deadline = time.monotonic() + FETCH_TIMEOUT_SECONDS

    for _ in range(MAX_REDIRECTS + 1):
        _check_url(url)
        with requests.get(
            url,
            stream=True,
            timeout=FETCH_TIMEOUT_SECONDS,
            allow_redirects=False,
        ) as response:
            if response.is_redirect:
                # Followed by hand so that every hop passes the whitelist.
                url = urljoin(url, response.headers["location"])
                continue

            response.raise_for_status()

            declared = response.headers.get("content-length")
            if (
                declared
                and declared.isdigit()
                and int(declared) > MAX_SOURCE_IMAGE_BYTES
            ):
                raise ImageFetchError(f"larger than {MAX_SOURCE_IMAGE_BYTES} bytes")

            content = bytearray()
            for chunk in response.iter_content(_DOWNLOAD_CHUNK_BYTES):
                content.extend(chunk)
                if len(content) > MAX_SOURCE_IMAGE_BYTES:
                    raise ImageFetchError(f"larger than {MAX_SOURCE_IMAGE_BYTES} bytes")
                if time.monotonic() > deadline:
                    raise ImageFetchError(
                        f"not read within {FETCH_TIMEOUT_SECONDS} seconds"
                    )
            return bytes(content)

    raise ImageFetchError(f"more than {MAX_REDIRECTS} redirects")


def _image_mime_type(content: bytes) -> str:
    try:
        with Image.open(io.BytesIO(content)) as image:
            image_format = image.format
            image.verify()
    except Exception as error:
        raise ImageFetchError(f"not an image: {error}") from error

    mime_type = Image.MIME.get(image_format or "")
    if not mime_type or not mime_type.startswith("image/"):
        raise ImageFetchError(f"not an image: format {image_format}")
    return mime_type


def fetch_source_image(url: str) -> Optional[SourceImage]:
    """Read the image of a source from its URL at the source.

    :param url: ``binary.sourceUrl`` of the chunk
    :type url: str

    :return: The image as it is, or None when it cannot be given to the model;
        the reason goes to the log
    :rtype: Optional[SourceImage]
    """
    try:
        content = _download(url)
        mime_type = _image_mime_type(content)
    except (ImageFetchError, requests.RequestException) as error:
        logger.warning(
            f"[image_inputs] source image {_without_query(url)} not read: {error}"
        )
        return None

    return SourceImage(base64.b64encode(content).decode("ascii"), mime_type)


def fetch_source_images(urls: list[str]) -> list[Optional[SourceImage]]:
    """Read the source images together, in the order of ``urls``."""
    if not urls:
        return []
    with ThreadPoolExecutor(max_workers=MAX_SOURCE_IMAGES) as executor:
        return list(executor.map(fetch_source_image, urls))
