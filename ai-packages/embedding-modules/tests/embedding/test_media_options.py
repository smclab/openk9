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

"""The media options are split from the jsonConfig of the chunk: the
chunker gets the rest, an invalid value falls back to the default."""

import logging

import pytest

from app.embedding import media_options
from app.embedding.media_options import MediaOptions, split_media_options


def test_without_media_keys_the_defaults_apply_and_the_config_is_unchanged():
    options, rest = split_media_options({"chunk_size": 100.0})

    assert options == MediaOptions(
        embed_images=True, rasterize_pages=True, max_pages=200, page_resolution=1568
    )
    assert rest == {"chunk_size": 100.0}


def test_the_media_keys_are_read_and_taken_out_of_the_chunker_config():
    options, rest = split_media_options(
        {
            "chunk_size": 100.0,
            "embed_images": False,
            "rasterize_pages": False,
            # the gRPC Struct decodes every number to float
            "max_pages": 50.0,
            "page_resolution": 1024.0,
        }
    )

    assert options == MediaOptions(
        embed_images=False, rasterize_pages=False, max_pages=50, page_resolution=1024
    )
    assert rest == {"chunk_size": 100.0}


@pytest.mark.parametrize(
    "key, value",
    [
        ("embed_images", "false"),
        ("rasterize_pages", 0.0),
        ("max_pages", 0.0),
        ("max_pages", 2.5),
        ("page_resolution", True),
        ("page_resolution", "1568"),
        ("page_resolution", 4097.0),
    ],
)
def test_an_invalid_value_falls_back_to_the_default_with_a_warning(key, value, caplog):
    with caplog.at_level(logging.WARNING, logger=media_options.logger.name):
        options, rest = split_media_options({key: value})

    assert options == MediaOptions()
    assert rest == {}
    assert f"Ignoring {key} {value!r}" in caplog.text


def test_page_resolution_is_accepted_up_to_its_maximum():
    options, _ = split_media_options({"page_resolution": 4096.0})

    assert options.page_resolution == 4096
