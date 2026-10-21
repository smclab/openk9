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

"""Options of the media refs of EmbedContent.

They live in the embedding jsonConfig of the data index (RequestChunk), next
to the keys of the chunker, which never sees them. An invalid value is
ignored with a warning and the default applies, so a configuration mistake
does not stop the indexing.
"""

import logging
from dataclasses import dataclass, fields

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class MediaOptions:
    """embed_images false skips images and PDF pages, rasterize_pages false
    skips PDF pages only; max_pages caps the pages of one PDF and
    page_resolution is the long side of a rasterized page, in pixels."""

    embed_images: bool = True
    rasterize_pages: bool = True
    max_pages: int = 300
    page_resolution: int = 1568


def _valid(value, default):
    """The option a jsonConfig value stands for, or None: a bool for a bool
    option, a whole number of at least 1 for a number one. The gRPC Struct
    decodes every number as a float, so 50.0 is 50."""
    if isinstance(default, bool):
        return value if isinstance(value, bool) else None
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return None
    if value != int(value) or value < 1:
        return None

    return int(value)


def split_media_options(json_config: dict) -> tuple[MediaOptions, dict]:
    """Splits the jsonConfig of the chunk into the media options and the
    rest, which is the configuration of the chunker."""
    rest = dict(json_config)
    values = {}

    for field in fields(MediaOptions):
        if field.name not in rest:
            continue

        configured = rest.pop(field.name)
        value = _valid(configured, field.default)

        if value is None:
            logger.warning(
                "Ignoring %s %r: using the default %r",
                field.name,
                configured,
                field.default,
            )
        else:
            values[field.name] = value

    return MediaOptions(**values), rest
