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

"""A Quartz expression has six or seven fields, anything else is refused.

A five-field Unix cron is the likeliest mistake in the retention settings: it
must fail at parse time rather than be read with every field shifted by one.
"""

import pytest

from app.utils.quartz_apscheduler import QuartzExpressionParser


@pytest.mark.parametrize("expression", ["0 10 * * *", "0 0 10 ? * * * *", ""])
def test_wrong_number_of_fields_is_rejected(expression):
    with pytest.raises(ValueError, match="Invalid Quartz CRON expression"):
        QuartzExpressionParser(expression)
