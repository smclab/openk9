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

from app.utils.pipeline_options import (
    flatten,
    normalize_dict,
    unflatten_dict,
)

NESTED = {
    "do_ocr": True,
    "ocr_options": {"lang": ["it", "en"], "bitmap_area_threshold": 0.05},
    "table_structure_options": {"mode": "accurate", "cell": {"match": True}},
}

FLAT = {
    "do_ocr": True,
    "ocr_options.lang": ["it", "en"],
    "ocr_options.bitmap_area_threshold": 0.05,
    "table_structure_options.mode": "accurate",
    "table_structure_options.cell.match": True,
}


def test_flatten_joins_the_nested_keys():
    assert flatten(NESTED) == FLAT


def test_unflatten_rebuilds_the_nested_dict():
    assert unflatten_dict(FLAT) == NESTED


def test_flatten_and_unflatten_round_trip():
    assert unflatten_dict(flatten(NESTED)) == NESTED


def test_unflatten_rejects_a_key_nested_under_a_value():
    with pytest.raises(ValueError):
        unflatten_dict({"ocr_options": "easyocr", "ocr_options.lang": ["it"]})


def test_normalize_dict_converts_the_string_values():
    raw = {
        "do_ocr": "True",
        "images_scale": "2",
        "ocr_options": {"bitmap_area_threshold": "0.05", "lang": "it"},
        "artifacts_path": "",
    }

    assert normalize_dict(raw) == {
        "do_ocr": True,
        "images_scale": 2,
        "ocr_options": {"bitmap_area_threshold": 0.05, "lang": "it"},
        "artifacts_path": None,
    }


def test_normalize_dict_unwraps_a_list_nested_in_a_single_list():
    assert normalize_dict({"lang": [["it", "en"]]}) == {"lang": ["it", "en"]}
