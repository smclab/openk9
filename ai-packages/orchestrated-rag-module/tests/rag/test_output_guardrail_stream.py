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
"""The output guardrail on the stream of the answer, for the three types of agentic-rag-module.

- type 1 holds the chunks back ``chunk_interval`` at a time and sends them only after the check;
- type 2 sends the chunks as they come and checks the last window every ``chunk_interval``;
- type 3, the scope gate, holds the first characters back for a single check.

A blocked answer ends with CANCEL: no END, and ``post_stream`` (title and save) is not called.
The checks are stubs here: what is tested is which text is checked, and what leaves before.
"""

import asyncio
import json

import pytest

from app.rag.agentic.streaming import (
    CHUNK_SIZE,
    OUTPUT_GUARDRAIL_CANCEL_MESSAGE,
    deltas_from_message,
    stream_answer,
)

#: Five pieces of CHUNK_SIZE: enough to tell "held back" from "already sent".
MESSAGE = "".join(str(i) * CHUNK_SIZE for i in range(5))
CHUNK_INTERVAL = 3


def _stream(*, guardrail_type, outcome="NONE", scope="VALID", prefix_chars=250, message=MESSAGE):
    checks, posted = [], []

    async def output_check(answer, stage):
        checks.append((answer, stage))
        return outcome

    async def scope_check(prefix):
        checks.append((prefix, "scope"))
        return scope

    async def post_stream():
        posted.append(True)
        return None, False, []

    async def collect():
        return [
            json.loads(event)
            async for event in stream_answer(
                deltas=deltas_from_message(message),
                rag_type="CHAT_RAG",
                post_stream=post_stream,
                output_guardrail_type=guardrail_type,
                chunk_interval=CHUNK_INTERVAL,
                output_check=output_check,
                scope_check=scope_check,
                scope_gate_prefix_chars=prefix_chars,
                scope_gate_redirect_message="Solo temi del dominio.",
            )
        ]

    return asyncio.run(collect()), checks, posted


def _types(events):
    return [event["type"] for event in events]


def _text(events):
    return "".join(event["chunk"] for event in events if event["type"] == "CHUNK")


# --------------------------------------------------------------------------- #
# No guardrail                                                                 #
# --------------------------------------------------------------------------- #
def test_without_output_guardrail_nothing_is_checked():
    events, checks, posted = _stream(guardrail_type=None, outcome="HATE")

    assert _text(events) == MESSAGE
    assert _types(events)[-1] == "END"
    assert checks == []
    assert posted == [True]


# --------------------------------------------------------------------------- #
# Type 1                                                                       #
# --------------------------------------------------------------------------- #
def test_type_1_checks_the_answer_so_far_before_sending_it():
    events, checks, posted = _stream(guardrail_type=1)

    assert _text(events) == MESSAGE
    assert checks == [(MESSAGE[: CHUNK_SIZE * 3], "chunk_interval"), (MESSAGE, "final_tail")]
    assert _types(events)[-1] == "END"
    assert posted == [True]


def test_type_1_blocked_answer_sends_no_text():
    events, _, posted = _stream(guardrail_type=1, outcome="HATE")

    assert _types(events) == ["STATUS", "START", "CANCEL"]
    assert events[-1]["chunk"] == OUTPUT_GUARDRAIL_CANCEL_MESSAGE
    assert posted == []


@pytest.mark.parametrize("unknown", [0, 0.0, 7])
def test_an_unknown_type_is_checked_as_type_1(unknown):
    """A jsonConfig with ``output_guardrail_type`` 0 (a float, from the jsonConfig) must not send
    the text before checking it, as type 2 would."""
    events, _, _ = _stream(guardrail_type=unknown, outcome="HATE")

    assert "CHUNK" not in _types(events)
    assert _types(events)[-1] == "CANCEL"


# --------------------------------------------------------------------------- #
# Type 2                                                                       #
# --------------------------------------------------------------------------- #
def test_type_2_checks_windows_of_text():
    events, checks, _ = _stream(guardrail_type=2)

    assert _text(events) == MESSAGE
    assert checks == [
        (MESSAGE[: CHUNK_SIZE * 3], "chunk_interval"),
        (MESSAGE[CHUNK_SIZE * 3 :], "final_tail"),
    ]


def test_type_2_sends_the_text_before_the_check_that_blocks_it():
    events, _, posted = _stream(guardrail_type=2, outcome="HATE")

    assert _types(events).count("CHUNK") == CHUNK_INTERVAL - 1
    assert _types(events)[-1] == "CANCEL"
    assert posted == []


# --------------------------------------------------------------------------- #
# Type 3: the scope gate                                                       #
# --------------------------------------------------------------------------- #
def test_scope_gate_checks_the_prefix_once_then_lets_the_answer_flow():
    events, checks, posted = _stream(guardrail_type=3, prefix_chars=CHUNK_SIZE * 2)

    assert checks == [(MESSAGE[: CHUNK_SIZE * 2], "scope")]
    assert _text(events) == MESSAGE
    assert posted == [True]


def test_float_type_3_is_the_scope_gate():
    _, checks, _ = _stream(guardrail_type=3.0, prefix_chars=CHUNK_SIZE * 2)

    assert checks[0][1] == "scope"


def test_off_scope_prefix_is_replaced_by_the_redirect_message():
    events, _, posted = _stream(guardrail_type=3, scope="OFF_SCOPE", prefix_chars=CHUNK_SIZE * 2)

    assert _types(events) == ["STATUS", "START", "CANCEL"]
    assert events[-1]["chunk"] == "Solo temi del dominio."
    assert posted == []


def test_answer_shorter_than_the_prefix_is_checked_whole():
    events, checks, _ = _stream(guardrail_type=3, message="Breve.", prefix_chars=250)

    assert checks == [("Breve.", "scope")]
    assert _text(events) == "Breve."


def test_short_off_scope_answer_is_cancelled():
    events, _, _ = _stream(guardrail_type=3, scope="OFF_SCOPE", message="Breve.")

    assert _types(events)[-1] == "CANCEL"
    assert "CHUNK" not in _types(events)
