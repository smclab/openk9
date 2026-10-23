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

"""The detected domain is always a list, or None when nothing was detected.

The LLM's structured output may answer a single domain as a bare string, or
an empty one when it found none: both are normalised before the domain is
used as a retrieval filter.
"""

import pytest

from app.rag.agentic_rag import Domain


@pytest.mark.parametrize(
    "value, expected",
    [
        ("insurance", ["insurance"]),
        (["insurance", "motor"], ["insurance", "motor"]),
        ([], []),
        ("", None),
        ("   ", None),
        (None, None),
    ],
)
def test_domain_is_normalised_to_a_list(value, expected):
    assert Domain(domain=value).domain == expected


def test_missing_domain_is_none():
    assert Domain().domain is None
