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


"""Tests for the two helpers that decide what a record may carry.

``debug_extra`` is what keeps an event on a single record: the fields that
cannot be written in production are appended to the record already emitted,
not to a second one. ``content_fingerprint`` is what is left of the content
when it cannot be written at all.
"""

import hashlib
import logging

from app.utils.logger import content_fingerprint, debug_extra, get_logger

QUERY = "Quali corsi avete in catalogo?"


def test_each_module_gets_its_own_logger():
    assert get_logger("app.rag.agentic_rag").name == "app.rag.agentic_rag"
    assert get_logger("app.server") is not get_logger("app.rag.agentic_rag")


def test_debug_extra_is_empty_above_debug(caplog):
    logger = get_logger("app.test.debug_extra")

    with caplog.at_level(logging.INFO):
        assert debug_extra(logger, query=QUERY) == ""


def test_debug_extra_renders_the_fields_at_debug(caplog):
    logger = get_logger("app.test.debug_extra")

    with caplog.at_level(logging.DEBUG, logger="app"):
        rendered = debug_extra(logger, query=QUERY, score=0.82)

    assert rendered == f" query={QUERY!r} score=0.82"


def test_fingerprint_describes_without_disclosing():
    digest = hashlib.sha256(QUERY.encode("utf-8")).hexdigest()[:8]

    assert content_fingerprint("query", QUERY) == (
        f"query_chars={len(QUERY)} query_hash={digest}"
    )
    assert QUERY not in content_fingerprint("query", QUERY)


def test_the_same_content_keeps_the_same_fingerprint():
    # What makes a blocked query countable across requests.
    assert content_fingerprint("query", QUERY) == content_fingerprint("query", QUERY)
    assert content_fingerprint("query", QUERY) != content_fingerprint("query", "altro")


def test_fingerprint_tolerates_a_missing_content():
    assert content_fingerprint("query", None).startswith("query_chars=0")
