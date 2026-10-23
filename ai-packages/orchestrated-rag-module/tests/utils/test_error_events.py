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


import pytest
from fastapi import HTTPException

from app.utils.error_events import error_event


def test_client_error_detail_is_forwarded():
    # A refusal the caller can act on: the frontend already reads `message` and
    # falls back to a hardwired string when it is absent, which is why every
    # error used to look the same to the user.
    event = error_event(
        HTTPException(status_code=400, detail="media.data must not be empty"),
        "Unexpected error",
    )

    assert event == {
        "chunk": "Unexpected error",
        "type": "ERROR",
        "message": "media.data must not be empty",
    }


def test_internal_error_detail_is_withheld():
    event = error_event(
        HTTPException(status_code=500, detail="Unexpected error"), "Unexpected error"
    )

    assert event == {"chunk": "Unexpected error", "type": "ERROR"}


def test_plain_exception_carries_no_message():
    event = error_event(RuntimeError("connection refused"), "boom")

    assert event == {"chunk": "boom", "type": "ERROR"}


@pytest.mark.parametrize("status_code", [400, 404, 499])
def test_every_client_error_forwards_its_detail(status_code):
    # The event, its type and its position in the stream do not change:
    # `message` is an extra field, ignored by clients that do not read it.
    event = error_event(
        HTTPException(status_code=status_code, detail="nope"), "chunk text"
    )

    assert event == {"chunk": "chunk text", "type": "ERROR", "message": "nope"}


@pytest.mark.parametrize("status_code", [500, 502, 503])
def test_every_server_error_withholds_its_detail(status_code):
    event = error_event(
        HTTPException(status_code=status_code, detail="nope"), "chunk text"
    )

    assert event == {"chunk": "chunk text", "type": "ERROR"}
