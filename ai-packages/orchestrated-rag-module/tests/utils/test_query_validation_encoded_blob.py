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

"""The structural base64 / hex checks and the stream sent for a blocked input.

A run of base64 characters counts as a blob only when it carries base64-only
signals or mixes upper case, lower case and digits; a run of hex characters
only when it has a letter, so a long decimal number is not a blob. When the
input is blocked, the client receives a GUARDRAIL event followed by END,
the same contract the guardrail node emits from inside the graph.
"""

import json

import pytest

from app.utils.query_validation import (
    GUARDRAIL_VIOLATION_MESSAGE,
    _looks_base64,
    _looks_hex,
    encoded_blob_type,
    guardrail_violation_stream,
)


@pytest.mark.parametrize(
    "token",
    [
        "aGVsbG8gd29ybGQgdGhpcyBpcyBi+w",  # base64-only "+"
        "aGVsbG8gd29ybGQgdGhpcyBpcyBi/w",  # base64-only "/"
        "aGVsbG8gd29ybGQgdGhpcyBpcyBiYQ==",  # "=" padding
        "UXVlc3RvIGUnIHVuIG1lc3NhZ2dpbw",  # upper, lower and digits
    ],
)
def test_base64_signals_are_recognised(token):
    assert _looks_base64(token) is True


@pytest.mark.parametrize(
    "token",
    [
        # Each signal is enough on its own, even on a single-case run that
        # would otherwise be taken for a word.
        "abcdefghijkl+mnopqrstuvwx",  # "+" alone
        "abcdefghijkl/mnopqrstuvwx",  # "/" alone, no padding
        "abcdefghijklmnopqrstuvwx=",  # "=" padding alone
    ],
)
def test_a_single_base64_signal_is_enough(token):
    assert _looks_base64(token) is True


@pytest.mark.parametrize(
    "token, expected",
    [
        ("aB3" * 8, True),  # 24 characters, the minimum length
        ("aB3" * 7 + "aB", False),  # 23 characters
        ("aB3" * 8 + "==", True),  # 24 characters plus padding
        ("aB3" * 7 + "aB=", False),  # 23 characters plus padding
        ("aB3" * 7 + "aB==", False),
    ],
)
def test_base64_minimum_length_excludes_the_padding(token, expected):
    assert _looks_base64(token) is expected


@pytest.mark.parametrize(
    "token",
    [
        "questaeunastringamoltolungadaverosenzacifre",  # single-case word
        "QUESTAEUNASTRINGAMOLTOLUNGA",  # upper case only
        "abcdefghijklmnop12345678",  # no upper case
        "aB3" * 7,  # 21 characters, below the minimum length
        "aB3" * 7 + "==",  # padding does not count towards the length
    ],
)
def test_base64_look_alikes_are_not_blobs(token):
    assert _looks_base64(token) is False


def test_hex_needs_a_letter():
    assert _looks_hex("a94a8fe5ccb19ba61c4c0873d391e987") is True
    assert _looks_hex("12345678901234567890123456789012") is False


def test_lowercase_hex_digest_is_classified_as_hex():
    assert encoded_blob_type("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3") == "hex"


@pytest.mark.parametrize(
    "text, expected",
    [
        ("a94a8fe5ccb19ba61c4c0873d391e987", "hex"),  # 32 characters
        ("a94a8fe5ccb19ba61c4c0873d391e98", None),  # 31 characters
    ],
)
def test_hex_minimum_length(text, expected):
    assert encoded_blob_type(text) == expected


def test_base64_takes_precedence_over_hex():
    text = (
        "UXVlc3RvIGUnIHVuIG1lc3NhZ2dpbyBkaSBwcm92YQ== "
        "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"
    )

    assert encoded_blob_type(text) == "base64"


def test_plain_text_has_no_blob_type():
    assert encoded_blob_type("Qual è il perché del caffè?") is None


def test_guardrail_violation_stream_is_guardrail_then_end():
    events = [json.loads(event) for event in guardrail_violation_stream()]

    assert events == [
        {"chunk": GUARDRAIL_VIOLATION_MESSAGE, "type": "GUARDRAIL"},
        {"chunk": "", "type": "END"},
    ]
