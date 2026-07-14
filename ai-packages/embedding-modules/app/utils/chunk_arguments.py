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


def _matches(value, expected):
    """isinstance() that tolerates non-class type hints.

    Chunker __init__ hints include subscripted generics, typing.Any and
    non-runtime-checkable Protocols (e.g. Union[str, TokenizerProtocol],
    Dict[str, Any], Optional[Literal[...]]), which raise TypeError when used
    as the second isinstance() argument. Returns None when the check cannot
    be evaluated, so the caller can pass the value through unverified.
    """
    try:
        return isinstance(value, expected)
    except TypeError:
        return None


def coerce_argument(value, expected):
    """Return (accepted, coerced_value) for a chunk argument.

    Keeps the value as-is when it already matches the expected type
    (so Union[int, float] params like chunk_overlap stay float),
    otherwise casts int-like float/str to int. When the expected type is
    not a plain class (subscripted generic / Any / Protocol), the value is
    accepted unchanged and left for the chunker to validate.
    """
    matched = _matches(value, expected)
    if matched:
        return True, value
    if matched is None:
        return True, value
    if isinstance(value, (float, str)) and is_int_like(value):
        coerced = int(value)
        if _matches(coerced, expected) is True:
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
