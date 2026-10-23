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

"""An evaluation request defaults to 100 spans and no evaluator enabled.

The time window is optional and accepts dates as well as datetimes, which
the evaluation endpoint uses as inclusive lower and exclusive upper bound.
"""

from datetime import datetime

import pytest
from pydantic import ValidationError

from app.models.models import Evaluation


def test_empty_request_gets_the_defaults():
    evaluation = Evaluation.model_validate({})

    assert evaluation.limit == 100
    assert evaluation.start_time is None
    assert evaluation.end_time is None
    assert evaluation.evaluateRagRouter is False
    assert evaluation.evaluateRetriever is False
    assert evaluation.evaluateResponse is False


def test_time_window_accepts_dates_and_datetimes():
    evaluation = Evaluation.model_validate(
        {"start_time": "2025-12-17", "end_time": "2026-01-14T08:30:00"}
    )

    assert evaluation.start_time == datetime(2025, 12, 17)
    assert evaluation.end_time == datetime(2026, 1, 14, 8, 30)


@pytest.mark.parametrize(
    "payload, field",
    [
        ({"start_time": "yesterday"}, "start_time"),
        ({"limit": "many"}, "limit"),
        ({"evaluateResponse": "maybe"}, "evaluateResponse"),
    ],
)
def test_invalid_values_are_refused(payload, field):
    with pytest.raises(ValidationError) as raised:
        Evaluation.model_validate(payload)

    assert {error["loc"] for error in raised.value.errors()} == {(field,)}
