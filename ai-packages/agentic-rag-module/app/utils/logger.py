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

import hashlib
import logging
import os

from dotenv import load_dotenv

load_dotenv()

LOGGING_LEVEL = os.getenv("LOGGING_LEVEL", "INFO")

logging.basicConfig(
    level=LOGGING_LEVEL,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
)


def get_logger(name):
    """Return the logger of the calling module.

    Each module logs through its own logger so that ``%(name)s`` in the record
    identifies where the event came from, instead of naming this module for
    every line the service writes.
    """
    return logging.getLogger(name)


def debug_extra(logger, **fields):
    """Render ``fields`` as a trailing fragment, only when DEBUG is enabled.

    An event produces a single record whose level states its importance; the
    fields that cannot be written in production ride on that same record and
    appear only once the level is lowered to DEBUG. Emitting a second, DEBUG
    record for the same event instead would double the volume of a debugging
    session and make the event count twice for whoever aggregates the logs.
    """
    if not logger.isEnabledFor(logging.DEBUG):
        return ""

    return "".join(f" {key}={value!r}" for key, value in fields.items())


def content_fingerprint(label, text):
    """Describe ``text`` without disclosing it, as ``<label>_chars`` and
    ``<label>_hash``.

    The length alone cannot group anything; the truncated digest lets the same
    blocked query be recognised across requests, and joins an INFO record with
    the DEBUG one that carries the text itself.
    """
    value = text if isinstance(text, str) else ""
    digest = hashlib.sha256(value.encode("utf-8")).hexdigest()[:8]

    return f"{label}_chars={len(value)} {label}_hash={digest}"
