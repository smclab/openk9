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

"""Quartz expressions the parser cannot translate are rejected up front.

Before, some of them were silently scheduled on the wrong dates and others
produced kwargs that CronTrigger refused with a message unrelated to the
Quartz expression. The parser now names the expression it does not support,
so a misconfigured retention job fails at startup with a clear reason.
"""

import pytest

from app.utils.quartz_apscheduler import QuartzExpressionParser

UNSUPPORTED_EXPRESSIONS = [
    # Nearest weekday: computed once on the current month, then reused for
    # every month, and moved to the wrong side of weekends.
    ("nearest_weekday", "0 0 10 15W * ?"),
    ("nearest_weekday_first", "0 0 10 1W * ?"),
    ("last_weekday", "0 0 10 LW * ?"),
    # The offset from the last day of the month was dropped.
    ("last_day_offset", "0 0 10 L-3 * ?"),
    # Numeric lists and steps reached APScheduler untranslated, where 0 is
    # Monday instead of Quartz's 1 as Sunday.
    ("numeric_day_of_week_list", "0 0 10 ? * 2,4"),
    ("numeric_day_of_week_step", "0 0 10 ? * 2/2"),
    ("day_of_week_wildcard_step", "0 0 10 ? * */2"),
    # Ranges crossing Sunday: in APScheduler the week ends on Sunday.
    ("numeric_range_from_sunday", "0 0 10 ? * 1-5"),
    ("named_range_from_sunday", "0 0 10 ? * SUN-SAT"),
    ("wrapping_range", "0 0 10 ? * FRI-MON"),
    # Last day of the week given by name, or bare L (Saturday).
    ("named_last_day_of_week", "0 0 10 ? * FRIL"),
    ("bare_last_day_of_week", "0 0 10 ? * L"),
]


@pytest.mark.parametrize(
    "label, expression",
    UNSUPPORTED_EXPRESSIONS,
    ids=[label for label, _ in UNSUPPORTED_EXPRESSIONS],
)
def test_unsupported_expression_is_rejected(label, expression):
    with pytest.raises(ValueError, match="Unsupported Quartz CRON expression"):
        QuartzExpressionParser(expression)
