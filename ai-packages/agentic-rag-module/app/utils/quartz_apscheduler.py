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

from datetime import datetime, timedelta

from apscheduler.triggers.cron import CronTrigger


class QuartzExpressionParser:
    """Parser for converting Quartz CRON expressions to APScheduler kwargs.

    This class takes into account the special characters used in Quartz ('L', 'W', and '#')
    and translates them into the format that APScheduler understands.
    link: https://gist.github.com/mlamina/184c0f1f055ca8b4909022a1094826a5
    """

    def __init__(self, quartz_cron):
        """Initialize the parser with a Quartz CRON expression."""
        self.quartz_cron = quartz_cron.strip()
        self.parts = self.quartz_cron.split()

        if len(self.parts) < 6 or len(self.parts) > 7:
            raise ValueError("Invalid Quartz CRON expression")

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

    def _parse_weekday_nearest(self, day):
        """Calculate the nearest weekday for a given day of the month or last weekday of the month."""
        current_year = datetime.now().year
        current_month = datetime.now().month

        # If year or month are not specified, use current year and month
        year = (
            int(self.parts[6])
            if len(self.parts) == 7 and self.parts[6].isdigit()
            else current_year
        )
        month = int(self.parts[4]) if self.parts[4].isdigit() else current_month

        if "LW" == day:
            # Calculate the last day of the month
            next_month = datetime(year, month % 12 + 1, 1)
            last_day_of_month = (next_month - timedelta(days=1)).day
            target_date = datetime(year, month, last_day_of_month)
        elif "W" in day:
            day_number = int(day.replace("W", ""))
            target_date = datetime(year, month, day_number)
        else:
            return day

        # Adjust if the target date falls on a weekend
        weekday = target_date.weekday()
        if weekday == 5:
            # If it's Saturday, move to Friday
            adjusted_date = target_date - timedelta(days=1)
        elif weekday == 6:
            # If it's Sunday, move to Monday (but check for month boundary)
            adjusted_date = (
                target_date + timedelta(days=1)
                if target_date.day == 1
                else target_date - timedelta(days=2)
            )
        else:
            # It's already a weekday
            adjusted_date = target_date

        return adjusted_date.day

    def _parse_day(self):
        """Parse the day field to handle special Quartz characters."""
        # Handle 'L' and '#' in the day-of-month field
        day_of_month = self.parts[3]
        day_of_week = self.parts[5]

        if "L" in day_of_month and "W" not in day_of_month:
            return "last"
        elif "W" in day_of_month:
            # Handle 'LW' and 'xW' in the day-of-month field
            return str(self._parse_weekday_nearest(day_of_month))
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
