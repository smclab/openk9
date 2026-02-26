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

import base64


class FormatError(Exception):
    pass


def handle_exception(e: Exception):
    if isinstance(e, (base64.binascii.Error, ValueError)):
        return f"base64 error: {e}"

    elif isinstance(e, AttributeError):
        return f"export error: {e}"

    elif isinstance(e, FormatError):
        return f"format error: {e}"

    else:
        return f"generic error: {e}"
