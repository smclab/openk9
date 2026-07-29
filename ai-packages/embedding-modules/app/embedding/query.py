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

"""Query-time embedding dispatch for EmbedQuery.

Unlike EmbedContent, a query returns exactly ONE vector, so there is no
skip: a modality the model cannot handle is an error, not a degraded
result. This maps a query (text and/or one inline image) onto a single
vector, raising typed errors the server turns into gRPC codes:

    QueryInvalid       -> INVALID_ARGUMENT   (malformed request)
    QueryPrecondition  -> FAILED_PRECONDITION (model lacks a capability)

The text+image combination is produced ONLY by a model with native
mixed input (one call, one vector); there is no module-side fusion of
two separate vectors. Capabilities are injected (QueryCapabilities), so
the dispatch is testable without models or network.
"""

from collections.abc import Callable
from dataclasses import dataclass

from app.embedding.router import classify


class QueryInvalid(Exception):
    """Malformed query: empty, or an inline that is not an image."""


class QueryPrecondition(Exception):
    """The model lacks a capability the query needs (image / mixed input)."""


@dataclass
class QueryCapabilities:
    """Injected embedding capabilities for a query.

    embed_image / embed_mixed are absent on models that cannot do them;
    the dispatch turns that absence into FAILED_PRECONDITION.
    """

    embed_text: Callable  # (str) -> list[float], query input_type
    embed_image: Callable = None  # (bytes, content_type) -> list[float]
    embed_mixed: Callable = None  # (str, bytes, content_type) -> list[float]


def query_vector(text, inline, capabilities) -> list:
    """Embeds a query into a single vector.

    ``text`` is the query text or None; ``inline`` is a (data,
    content_type) pair or None. Validation puts malformed requests
    (INVALID_ARGUMENT) before missing model capabilities
    (FAILED_PRECONDITION), so a bad request is not masked by the model.
    """
    has_text = text is not None
    has_inline = inline is not None

    if not has_text and not has_inline:
        raise QueryInvalid("neither text nor inline provided")

    if has_inline:
        data, content_type = inline
        if classify(content_type) != "image":
            raise QueryInvalid(
                f"inline media is not an image (contentType={content_type!r})"
            )

    if has_text and has_inline:
        if capabilities.embed_mixed is None:
            raise QueryPrecondition(
                "the configured model has no native text+image input"
            )
        return capabilities.embed_mixed(text, data, content_type)

    if has_inline:
        if capabilities.embed_image is None:
            raise QueryPrecondition("the configured model has no image input")
        return capabilities.embed_image(data, content_type)

    return capabilities.embed_text(text)
