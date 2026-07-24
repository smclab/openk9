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

"""HTTP fetch of a binary from its pre-signed URL.

Storage is out of the contract: the module receives an
already-signed GET URL and does nothing more than a plain GET — no
storage credentials, no bucket/key knowledge. Returns the raw bytes and
the response Content-Type, used as a fallback modality hint when the
MediaRef carries none.
"""

import os

import httpx

FETCH_TIMEOUT_SECONDS = float(os.getenv("FETCH_TIMEOUT_SECONDS", "30"))


def fetch_url(url, client=None):
    """GETs the pre-signed URL. Returns (content_bytes, content_type | None).

    Raises httpx.HTTPError on a network failure or a non-2xx status; the
    caller treats it as an operational error on that ref (skip + log). An
    httpx.Client can be injected for tests.
    """
    if client is None:
        response = httpx.get(
            url, timeout=FETCH_TIMEOUT_SECONDS, follow_redirects=True
        )
    else:
        response = client.get(url)

    response.raise_for_status()

    content_type = response.headers.get("content-type")
    if content_type:
        content_type = content_type.split(";")[0].strip() or None

    return response.content, content_type
