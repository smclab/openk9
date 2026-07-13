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


def is_int_like(value):
    """Checks if an input can be casted to int."""
    try:
        int(value)
        return True
    except (ValueError, TypeError):
        return False


def coerce_argument(value, expected):
    """Return (accepted, coerced_value) for a chunk argument.

    Keeps the value as-is when it already matches the expected type
    (so Union[int, float] params like chunk_overlap stay float),
    otherwise casts int-like float/str to int.
    """
    if isinstance(value, expected):
        return True, value
    if isinstance(value, (float, str)) and is_int_like(value):
        coerced = int(value)
        if isinstance(coerced, expected):
            return True, coerced
    return False, None


def build_chunk_arguments(chunk_json_config, signature):
    """Build the keyword arguments passed to a chonkie chunker.

    Only keeps config entries whose name is in the chunker signature and
    whose value is (or can be coerced to) the expected type. Int-like
    float/str values are coerced to int so chunkers that use them as a
    ``range`` step do not raise ``TypeError``.
    """
    arguments = {}
    for name in chunk_json_config:
        if name not in signature:
            continue
        accepted, coerced = coerce_argument(chunk_json_config[name], signature[name])
        if accepted:
            arguments[name] = coerced
    return arguments
