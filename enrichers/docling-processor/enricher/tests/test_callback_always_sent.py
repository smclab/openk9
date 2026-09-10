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

from unittest.mock import MagicMock, patch

import app.server as server


def _binary(id):
    return {"id": id, "resourceId": f"r{id}"}


# Run operation() with the given payload and conversion behaviour, and return
# the call the enrich callback received.
def _run(payload, conversion=None, post=None):
    conversion = conversion or (lambda binary, tenant, configs: MagicMock())
    with patch.object(
        server, "conversion", side_effect=conversion
    ), patch.object(server, "requests") as requests_mock:
        if post is not None:
            requests_mock.post.side_effect = post
        server.operation(payload, {}, token="tok")
    return requests_mock.post


def test_malformed_payload_is_reported_to_the_callback():
    post = _run({"tenantId": "t"})

    assert post.call_count == 1
    assert "error" in post.call_args.kwargs["json"]


def test_failed_single_binary_reports_the_error():
    def conversion(binary, tenant, configs):
        raise ValueError("conversion boom")

    post = _run(
        {"resources": {"binaries": [_binary(0)]}, "tenantId": "t"}, conversion
    )

    assert post.call_args.kwargs["json"] == {"error": "conversion failed"}


def test_document_without_binaries_is_not_an_error():
    post = _run({"resources": {"binaries": []}, "tenantId": "t"})

    assert post.call_args.kwargs["json"] == {}


def test_unreachable_callback_does_not_kill_the_worker():
    def post(*args, **kwargs):
        raise ConnectionError("datasource is down")

    # The worker must survive: it is a pool thread, and an exception escaping
    # here would take it out of the pool.
    _run({"resources": {"binaries": []}, "tenantId": "t"}, post=post)


def test_callback_is_sent_with_a_timeout():
    post = _run({"resources": {"binaries": []}, "tenantId": "t"})

    assert post.call_args.kwargs["timeout"] == server.CALLBACK_TIMEOUT_SECONDS
