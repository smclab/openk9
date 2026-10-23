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

"""Quartz expressions the parser supports become valid APScheduler kwargs.

The expressions come from the demo of the gist the parser is based on, which
is the set its author declares working, plus the defaults the service ships
with. Every result must also be accepted by a real CronTrigger: kwargs that
only look right would still stop the service at startup.
"""

import pytest
from apscheduler.triggers.cron import CronTrigger

from app.utils.quartz_apscheduler import QuartzExpressionParser


def _kwargs(hour="10", minute="0", month="*", day=None, year=None, **day_of_week):
    kwargs = {
        "second": "0",
        "minute": minute,
        "hour": hour,
        "month": month,
        "day": day,
        "year": year,
    }
    kwargs.update(day_of_week)
    return kwargs


SUPPORTED_EXPRESSIONS = [
    # Defaults of the service (server.py) and of the Helm chart.
    ("0 0 0 ? * * *", _kwargs(hour="0", day_of_week=None)),
    ("0 0 * ? * *", _kwargs(hour="*", day_of_week=None)),
    # Demo of the gist.
    ("0 20 20 ? * * *", _kwargs(hour="20", minute="20", day_of_week=None)),
    ("0 0 10 ? * MON-FRI *", _kwargs(day_of_week="mon-fri")),
    ("0 0 10 ? * MON *", _kwargs(day_of_week="mon")),
    ("0 0 10 ? * MON,TUE *", _kwargs(day_of_week="mon,tue")),
    ("0 0 10 ? * MON-WED *", _kwargs(day_of_week="mon-wed")),
    ("0 0 10 ? * SAT,SUN *", _kwargs(day_of_week="sat,sun")),
    ("0 0 10 ? * 2#2 *", _kwargs(day="2nd mon")),
    ("0 0 10 ? * 3L *", _kwargs(day="last tue")),
    ("0 0 10 ? * 3#3 *", _kwargs(day="3rd tue")),
    ("0 0 10 ? */2 4#4 *", _kwargs(month="*/2", day="4th wed")),
    ("0 20 20 */2 * ? *", _kwargs(hour="20", minute="20", day="*/2")),
    ("0 0 10 1 * ?", _kwargs(day="1")),
    ("0 0 10 L * ?", _kwargs(day="last")),
    # Numeric days of the week follow Quartz, where 1 is Sunday.
    ("0 0 10 ? * 1", _kwargs(day_of_week="sun")),
    ("0 0 10 ? * 7", _kwargs(day_of_week="sat")),
    ("0 0 10 ? * 2-6", _kwargs(day_of_week="mon-fri")),
    ("0 0 10 ? * MON#1", _kwargs(day="1st mon")),
    # Lists, steps, month names and years pass through unchanged.
    ("0 0 10 1,15 * ?", _kwargs(day="1,15")),
    ("0 0 10 1/10 * ?", _kwargs(day="1/10")),
    ("0 0 10 1 JAN,JUN ?", _kwargs(month="JAN,JUN", day="1")),
    ("0 0 10 1 1 ? 2027", _kwargs(month="1", day="1", year="2027")),
    ("0 0 10 1 1 ? 2027-2028", _kwargs(month="1", day="1", year="2027-2028")),
]


@pytest.mark.parametrize(
    "expression, expected",
    SUPPORTED_EXPRESSIONS,
    ids=[expression for expression, _ in SUPPORTED_EXPRESSIONS],
)
def test_supported_expression_becomes_apscheduler_kwargs(expression, expected):
    kwargs = QuartzExpressionParser(expression).to_apscheduler_kwargs()

    assert kwargs == expected
    CronTrigger(**kwargs)


def test_surrounding_whitespace_is_ignored():
    kwargs = QuartzExpressionParser("  0 0 10 ? * MON *\n").to_apscheduler_kwargs()

    assert kwargs == _kwargs(day_of_week="mon")
