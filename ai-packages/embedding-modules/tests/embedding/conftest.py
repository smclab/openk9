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

"""Shared doubles for the router tests.

Every capability is faked: chunk splits on '|', fetch reads from an
in-memory {url: (bytes, content_type)} map. The embedders return a
"vector" that echoes their input — ["text", chunk] and ["image", data,
content_type] — so a test sees which bytes, content type and chunk each
piece was actually embedded from. No network, no models.
"""

import pytest

from app.embedding.router import Pipelines


def _echo_image(data, content_type):
    return ["image", data, content_type]


@pytest.fixture
def make_pipelines():
    def _make(storage=None, embed_image=_echo_image):
        store = storage or {}

        return Pipelines(
            embed_texts=lambda texts: [["text", text] for text in texts],
            chunk=lambda text: text.split("|"),
            fetch=lambda url: store[url],
            embed_image=embed_image,
        )

    return _make
