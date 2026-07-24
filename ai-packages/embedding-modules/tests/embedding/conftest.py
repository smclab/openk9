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

Every capability is faked: chunk splits on '|', text/image embedders
return fixed vectors, fetch reads from an in-memory {url: (bytes,
content_type)} map. No network, no models.
"""

import pytest

from app.embedding.router import Pipelines

TEXT_VECTOR = [1.0, 0.0, 0.0, 0.0]
IMAGE_VECTOR = [0.0, 1.0, 0.0, 0.0]


@pytest.fixture
def make_pipelines():
    def _make(storage=None, embed_image=lambda data, content_type: IMAGE_VECTOR):
        store = storage or {}

        return Pipelines(
            embed_texts=lambda texts: [TEXT_VECTOR for _ in texts],
            chunk=lambda text: text.split("|"),
            fetch=lambda url: store[url],
            embed_image=embed_image,
        )

    return _make
