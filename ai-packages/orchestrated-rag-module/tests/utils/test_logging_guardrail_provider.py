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


"""Tests for what a guardrail provider writes when it cannot be used.

A misconfigured provider fails the same way a blocked request does, from
outside: the request gets no answer. Only the record tells them apart.
"""

import logging

import pytest

from app.utils.guardrails import GuardrailType, initialize_guardrail


def _errors(caplog):
    return [
        record.getMessage()
        for record in caplog.records
        if record.levelno == logging.ERROR
    ]


def test_missing_provider_is_reported(caplog):
    with caplog.at_level(logging.INFO), pytest.raises(ValueError):
        initialize_guardrail({})

    assert "guardrail_type is missing" in _errors(caplog)[0]


def test_unknown_provider_is_reported_with_the_valid_ones(caplog):
    with caplog.at_level(logging.INFO), pytest.raises(ValueError):
        initialize_guardrail({"guardrail_type": "azure_content_safety"})

    reported = _errors(caplog)[0]
    assert "azure_content_safety" in reported
    assert GuardrailType.OPENAI_MODERATION.value in reported
