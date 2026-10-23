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

from fastapi import HTTPException


def error_event(exception, chunk):
    """Build the SSE ERROR event payload for ``exception``.

    The event type and the chunk are unchanged: ``message`` is an extra,
    optional field carrying the reason of the refusal, and it is set only when
    the failure belongs to the caller. An ``HTTPException`` below 500 is a
    rejection the caller can act on (an unacceptable query media, for
    instance), so its detail is worth forwarding; an internal failure keeps the
    opaque chunk alone and leaks nothing about the backend.

    The rule lives here because two independent ``except`` blocks apply it, and
    a divergence between them would show up as an error message that appears on
    one path and vanishes on the other.
    """
    event = {"chunk": chunk, "type": "ERROR"}

    if isinstance(exception, HTTPException) and exception.status_code < 500:
        event["message"] = exception.detail

    return event
