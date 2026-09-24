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

"""UPLOAD_FILE_EXTENSIONS is read as a JSON array or as a comma-separated list.

The charts and the compose file ship a JSON array, while a comma-separated
value is what other list settings of the module use: both must yield the same
list, so no existing deployment has to change its configuration.
"""

import json

import pytest

from app.utils.file_upload import parse_upload_file_extensions

EXPECTED = [".pdf", ".md", ".docx"]


@pytest.mark.parametrize(
    "value",
    [
        '[".pdf",".md",".docx"]',
        ' [".pdf", ".md", ".docx"] ',
        ".pdf,.md,.docx",
        " .pdf , .md , .docx ",
        ".pdf,.md,.docx,",
    ],
)
def test_json_array_and_comma_separated_list_are_equivalent(value):
    assert parse_upload_file_extensions(value) == EXPECTED


@pytest.mark.parametrize("value", ["", "   ", "[]"])
def test_empty_setting_allows_no_extension(value):
    assert parse_upload_file_extensions(value) == []


def test_malformed_json_array_is_refused():
    with pytest.raises(json.JSONDecodeError):
        parse_upload_file_extensions('[".pdf", ".md"')
