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

"""query_vector dispatches a query onto a single vector. Fake capabilities
record which path fired; malformed requests raise QueryInvalid and missing
model capabilities raise QueryPrecondition, with no network or models."""

import pytest

from app.embedding.query import (
    QueryCapabilities,
    QueryInvalid,
    QueryPrecondition,
    query_vector,
)

TEXT_VECTOR = [0.1, 0.2]
IMAGE_VECTOR = [0.3, 0.4]
MIXED_VECTOR = [0.5, 0.6]


def _full_capabilities(calls):
    return QueryCapabilities(
        embed_text=lambda text: calls.append(("text", text)) or TEXT_VECTOR,
        embed_image=lambda data, content_type: calls.append(("image", content_type))
        or IMAGE_VECTOR,
        embed_mixed=lambda text, data, content_type: calls.append(("mixed", text))
        or MIXED_VECTOR,
    )


def test_text_only_uses_text_path():
    calls = []
    vector = query_vector("un gatto", None, _full_capabilities(calls))

    assert vector == TEXT_VECTOR
    assert calls == [("text", "un gatto")]


def test_inline_image_uses_image_path():
    calls = []
    vector = query_vector(None, (b"png", "image/png"), _full_capabilities(calls))

    assert vector == IMAGE_VECTOR
    assert calls == [("image", "image/png")]


def test_text_and_inline_uses_mixed_path_when_supported():
    calls = []
    vector = query_vector("un gatto", (b"png", "image/png"), _full_capabilities(calls))

    # a single vector from the model's native mixed input, not a fusion
    assert vector == MIXED_VECTOR
    assert calls == [("mixed", "un gatto")]


def test_text_and_inline_without_mixed_capability_is_precondition():
    capabilities = QueryCapabilities(
        embed_text=lambda text: TEXT_VECTOR,
        embed_image=lambda data, content_type: IMAGE_VECTOR,
        embed_mixed=None,
    )

    with pytest.raises(QueryPrecondition):
        query_vector("un gatto", (b"png", "image/png"), capabilities)


def test_inline_non_image_is_invalid_argument():
    with pytest.raises(QueryInvalid):
        query_vector(None, (b"wav", "audio/wav"), _full_capabilities([]))


def test_inline_image_on_text_only_model_is_precondition():
    text_only = QueryCapabilities(embed_text=lambda text: TEXT_VECTOR)

    with pytest.raises(QueryPrecondition):
        query_vector(None, (b"png", "image/png"), text_only)


def test_empty_request_is_invalid_argument():
    with pytest.raises(QueryInvalid):
        query_vector(None, None, _full_capabilities([]))


def test_non_image_inline_is_invalid_even_with_text():
    # malformed request (non-image) beats the model-capability check
    with pytest.raises(QueryInvalid):
        query_vector("un gatto", (b"wav", "audio/wav"), _full_capabilities([]))
