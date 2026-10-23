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

import re

from apscheduler.triggers.cron import CronTrigger


class QuartzExpressionParser:
    """Parser for converting Quartz CRON expressions to APScheduler kwargs.

    This class takes into account the special characters used in Quartz ('L' and '#')
    and translates them into the format that APScheduler understands.
    link: https://gist.github.com/mlamina/184c0f1f055ca8b4909022a1094826a5

    Expressions it cannot translate faithfully are rejected with a ValueError
    instead of being scheduled on the wrong dates: 'W' and 'L-n' in the
    day-of-month, numeric lists and steps in the day-of-week, day-of-week
    ranges crossing Sunday and a last day of the week not given as 'nL'.
    """

    # Day-of-week order in APScheduler, where the week ends on Sunday.
    APSCHEDULER_WEEK = ["mon", "tue", "wed", "thu", "fri", "sat", "sun"]

    def __init__(self, quartz_cron):
        """Initialize the parser with a Quartz CRON expression."""
        self.quartz_cron = quartz_cron.strip()
        self.parts = self.quartz_cron.split()

        if len(self.parts) < 6 or len(self.parts) > 7:
            raise ValueError("Invalid Quartz CRON expression")

        self._reject_unsupported()

    def _reject_unsupported(self):
        """Raise ValueError for the Quartz features this parser cannot translate."""
        day_of_month = self.parts[3]
        day_of_week = self.parts[5]

        if "W" in day_of_month:
            self._unsupported("nearest weekday ('W') in the day-of-month")
        if "L" in day_of_month and day_of_month != "L":
            self._unsupported("offset from the last day ('L-n') in the day-of-month")
        if day_of_week.endswith("L") and not re.fullmatch(r"[1-7]L", day_of_week):
            self._unsupported("last day of the week not given as 'nL' with n in 1-7")
        if ("," in day_of_week or "/" in day_of_week) and any(
            char.isdigit() for char in day_of_week
        ):
            self._unsupported("numeric list or step in the day-of-week")
        if "-" in day_of_week:
            start_day, end_day = map(
                self._day_of_week_from_quartz, day_of_week.split("-", 1)
            )
            if (
                start_day in self.APSCHEDULER_WEEK
                and end_day in self.APSCHEDULER_WEEK
                and self.APSCHEDULER_WEEK.index(start_day)
                > self.APSCHEDULER_WEEK.index(end_day)
            ):
                self._unsupported("day-of-week range crossing Sunday")

    def _unsupported(self, reason):
        raise ValueError(
            f"Unsupported Quartz CRON expression '{self.quartz_cron}': {reason}"
        )

    def to_apscheduler_kwargs(self):
        """Convert the Quartz CRON expression to APScheduler kwargs."""
        kwargs = {
            "second": self.parts[0],
            "minute": self.parts[1],
            "hour": self.parts[2],
            "month": self.parts[4],
            "day": self._parse_day(),
            "year": self._parse_year(),
        }

        # Omit 'day_of_week' if 'day' already contains special expressions
        if not kwargs.get("day") or not self._is_special_day_expression(kwargs["day"]):
            kwargs["day_of_week"] = self._parse_day_of_week()

        return kwargs

    def _parse_day(self):
        """Parse the day field to handle special Quartz characters."""
        # Handle 'L' and '#' in the day-of-month field
        day_of_month = self.parts[3]
        day_of_week = self.parts[5]

        if day_of_month == "L":
            return "last"
        elif day_of_week.endswith("L"):
            # The 'L' character is used to specify the last occurrence of a day in a month in Quartz.
            return "last " + self._day_of_week_from_quartz(day_of_week[0])
        elif "#" in day_of_week:
            # The '#' character is used to specify the "nth" occurrence of a particular weekday.
            weekday, nth = day_of_week.split("#")
            if int(nth) == 1:
                nth = "1st"
            elif int(nth) == 2:
                nth = "2nd"
            elif int(nth) == 3:
                nth = "3rd"
            else:
                nth = f"{nth}th"
            return nth + " " + self._day_of_week_from_quartz(weekday)
        elif day_of_month in ("?", "*"):
            return None
        return day_of_month

    def _parse_day_of_week(self):
        """Parse the day_of_week field and return as a comma-separated string."""
        day_of_week = self.parts[5]

        if day_of_week in ("?", "*"):
            return None
        if "-" in day_of_week:
            # Handle ranges
            start_day, end_day = day_of_week.split("-")
            return "-".join(map(self._day_of_week_from_quartz, [start_day, end_day]))

        return self._day_of_week_from_quartz(day_of_week)

    def _parse_year(self):
        """Parse the year field from the Quartz CRON expression."""
        if len(self.parts) == 7 and self.parts[6] != "*":
            return self.parts[6]
        return None

    def _day_of_week_from_quartz(self, quartz_day):
        """Convert Quartz day of the week to APScheduler format."""
        weekdays = {
            "1": "sun",
            "2": "mon",
            "3": "tue",
            "4": "wed",
            "5": "thu",
            "6": "fri",
            "7": "sat",
        }
        return weekdays.get(quartz_day, quartz_day.lower())

    def _is_special_day_expression(self, day_expression):
        """Check if the 'day' field contains a special expression like 'last' or 'nth'."""
        return "last" in day_expression or any(
            char.isdigit() for char in day_expression
        )
