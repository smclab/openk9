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

"""fetch_url does a plain GET on the pre-signed URL and returns the bytes
plus the normalized Content-Type; a non-2xx status raises (skip on the
caller side). An httpx.MockTransport stands in for the network."""

import httpx
import pytest

from app.embedding.fetch import fetch_url


def test_fetch_returns_bytes_and_normalized_content_type():
    def handler(request):
        return httpx.Response(
            200,
            content=b"png-bytes",
            headers={"content-type": "image/png; charset=binary"},
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))

    data, content_type = fetch_url("https://signed/img", client=client)

    assert data == b"png-bytes"
    assert content_type == "image/png"


def test_fetch_raises_on_error_status():
    client = httpx.Client(transport=httpx.MockTransport(lambda request: httpx.Response(404)))

    with pytest.raises(httpx.HTTPError):
        fetch_url("https://signed/missing", client=client)
