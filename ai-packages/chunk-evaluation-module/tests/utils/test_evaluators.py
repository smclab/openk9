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


import pytest

from utils.evaluators import layout_fidelity, redundancy_bloat, semantic_choerence

OUTPUT = {
    "semantic_choerence": {"forward": 1.0, "backward": 2.0},
    "redundancy_bloat": {"redundancy": 0.25},
    "layout_fidelity": {"coverage": 0.5},
}


def test_semantic_choerence_is_the_mean_of_forward_and_backward():
    assert semantic_choerence(OUTPUT) == pytest.approx(1.5)


def test_redundancy_bloat_returns_the_redundancy():
    assert redundancy_bloat(OUTPUT) == 0.25


def test_layout_fidelity_returns_the_coverage():
    assert layout_fidelity(OUTPUT) == 0.5
