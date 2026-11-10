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

"""Ollama multimodal embedder.

Whether an Ollama model embeds images is asked to the server, not read from
the model name: /api/show lists the capabilities of the model, and a model
with both `embedding` and `vision` is served here for every modality, so its
text and its images share one vector space.

/api/embed takes images as objects in `input` ({"image": <base64>}, and
{"text": ..., "image": ...} for one fused vector): a shape the Ollama
documentation does not describe, so _embed is the only place that knows it.
Text goes as plain strings, the request langchain OllamaEmbeddings sends:
for the same text the two give the same vector.
"""

import base64
import logging
import time

import httpx

from app.embedding.router import EmbedFailed, SkipRef

logger = logging.getLogger(__name__)

# Ollama unloads a model after five minutes of inactivity, so a run of the
# indexing that follows a pause pays the reload; keep_alive in the jsonConfig
# of the embedding model overrides this, in seconds (0 unloads at once, -1
# keeps it resident).
DEFAULT_KEEP_ALIVE = 1800
# the questions on the model are short; the embedding of a long batch on CPU
# is not, so it waits for the answer as langchain does
PROBE_TIMEOUT = httpx.Timeout(10.0)
EMBED_TIMEOUT = httpx.Timeout(None, connect=10.0)
# A request asks the registry more than once (batch size, client, log): a
# server that did not answer is not asked again for this many seconds.
PROBE_RETRY_SECONDS = 30
# The prefix a query takes, by model family: Ollama sends the input to the
# model as it is (template {{ .Prompt }}). The documents go without one, as
# langchain sends them, so an index built with langchain stays valid.
QUERY_PREFIXES = {"embeddinggemma": "task: search result | query: "}
# the image formats Ollama decodes; it answers 400 to any other
IMAGE_TYPES = {"image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp"}

# (api_url, model) -> whether the model embeds images, once the server answered
_multimodal = {}
# (api_url, model) -> when /api/show last failed
_failed = {}
_now = time.monotonic


def base_url(api_url):
    """The api_url without the trailing slash, with http:// when it has no
    scheme, as the Ollama client of langchain reads it."""
    url = (api_url or "").rstrip("/")

    return url if not url or "://" in url else f"http://{url}"


def _show(api_url, model):
    """The /api/show answer of the model and the version of the server, None
    when /api/version does not tell it."""
    response = httpx.post(
        f"{api_url}/api/show", json={"model": model}, timeout=PROBE_TIMEOUT
    )
    response.raise_for_status()

    try:
        version = httpx.get(f"{api_url}/api/version", timeout=PROBE_TIMEOUT)
        version = version.json().get("version") if version.is_success else None
    except (httpx.HTTPError, ValueError, AttributeError):
        version = None

    return response.json(), version


def is_multimodal(api_url, model):
    """True when the Ollama model lists the `embedding` and `vision`
    capabilities. The answer of the server is kept for the process; a server
    that does not answer leaves the model text-only, with a warning, and is
    asked again after PROBE_RETRY_SECONDS."""
    api_url = base_url(api_url)
    key = (api_url, model)

    if key in _multimodal:
        return _multimodal[key]

    if key in _failed and _now() - _failed[key] < PROBE_RETRY_SECONDS:
        return False

    try:
        show, version = _show(api_url, model)
    except (httpx.HTTPError, ValueError) as error:
        _failed[key] = _now()
        logger.warning(
            "Ollama model %s at %s treated as text only: /api/show failed: %s",
            model,
            api_url,
            error,
        )
        return False

    _failed.pop(key, None)
    capabilities = show.get("capabilities") if isinstance(show, dict) else None

    if not capabilities:
        logger.warning(
            "Ollama model %s at %s treated as text only: /api/show lists no "
            "capabilities (Ollama %s)",
            model,
            api_url,
            version,
        )
        _multimodal[key] = False
        return False

    _multimodal[key] = "embedding" in capabilities and "vision" in capabilities
    logger.info(
        "Ollama model %s at %s: capabilities=%s multimodal=%s (Ollama %s)",
        model,
        api_url,
        capabilities,
        _multimodal[key],
        version,
    )

    return _multimodal[key]


def _image(data, content_type):
    """The image as /api/embed takes it, base64; SkipRef for a format Ollama
    does not decode, so the ref is skipped as on a text-only model."""
    if (content_type or "").split(";")[0].strip().lower() not in IMAGE_TYPES:
        raise SkipRef(
            f"Ollama embeds PNG, JPEG, GIF and WebP images only, not {content_type}"
        )

    # the declared type can be wrong: Ollama reads the bytes
    if not (
        data.startswith((b"\x89PNG", b"\xff\xd8\xff", b"GIF87a", b"GIF89a"))
        or (data[:4] == b"RIFF" and data[8:12] == b"WEBP")
    ):
        raise SkipRef(
            f"Ollama embeds PNG, JPEG, GIF and WebP images only: the bytes of "
            f"this {content_type} are none of them"
        )

    return base64.b64encode(data).decode("ascii")


class OllamaMultimodalEmbedder:
    def __init__(self, model, api_url, keep_alive=None, num_gpu=None, client=None):
        self.model = model
        self.url = f"{base_url(api_url)}/api/embed"
        # jsonConfig numbers reach the module as floats, Ollama wants integers
        self.keep_alive = int(DEFAULT_KEEP_ALIVE if keep_alive is None else keep_alive)
        self.num_gpu = int(num_gpu) if num_gpu is not None else None
        self.query_prefix = next(
            (
                prefix
                for family, prefix in QUERY_PREFIXES.items()
                if model.startswith(family)
            ),
            "",
        )
        self.client = client if client is not None else httpx

    def _embed(self, inputs):
        """One /api/embed request, one vector per input, in order. Raises
        EmbedFailed on an error of the server or a short answer."""
        body = {"model": self.model, "input": inputs, "keep_alive": self.keep_alive}
        if self.num_gpu is not None:
            body["options"] = {"num_gpu": self.num_gpu}

        try:
            response = self.client.post(self.url, json=body, timeout=EMBED_TIMEOUT)
            response.raise_for_status()
            vectors = response.json().get("embeddings") or []
        except (httpx.HTTPError, ValueError, AttributeError) as error:
            raise EmbedFailed(f"Ollama /api/embed failed: {error}") from error

        if len(vectors) != len(inputs) or not all(vectors):
            raise EmbedFailed(
                f"Ollama /api/embed returned {len(vectors)} vectors for "
                f"{len(inputs)} inputs"
            )

        return vectors

    def _text(self, text, input_type):
        return self.query_prefix + text if input_type == "search_query" else text

    def embed_texts(self, texts, input_type="search_document"):
        """Embeds a batch of texts in one request; one vector per text."""
        texts = [self._text(text, input_type) for text in texts]

        return self._embed(texts) if texts else []

    def embed_image(self, data, content_type):
        """Embeds a single image (raw bytes, in the format they come)."""
        return self._embed([{"image": _image(data, content_type)}])[0]

    def embed_mixed(self, text, data, content_type, input_type="search_query"):
        """Embeds text and an image together: the model fuses them into one
        vector."""
        return self._embed(
            [
                {
                    "text": self._text(text, input_type),
                    "image": _image(data, content_type),
                }
            ]
        )[0]
