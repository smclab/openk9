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

import logging
from unittest.mock import MagicMock, patch

import app.server as server

# Neither is the default conftest sets, so a host or a timeout hardcoded in
# place of the configured ones cannot pass.
DATASOURCE_HOST = "http://datasource.test:9999"
CALLBACK_TIMEOUT_SECONDS = 7.5
CALLBACK_URL = f"{DATASOURCE_HOST}/api/datasource/pipeline/callback/tok"


def _binary(id):
    return {"id": id, "resourceId": f"r{id}", "url": f"http://binaries/r{id}"}


# Run operation() with the given payload and conversion behaviour, and return
# the call the enrich callback received.
def _run(payload, conversion=None, post=None):
    conversion = conversion or (lambda binary, configs: MagicMock())
    with patch.object(
        server, "conversion", side_effect=conversion
    ), patch.object(server, "requests") as requests_mock, patch.object(
        server, "DATASOURCE_HOST", DATASOURCE_HOST
    ), patch.object(
        server, "CALLBACK_TIMEOUT_SECONDS", CALLBACK_TIMEOUT_SECONDS
    ):
        if post is not None:
            requests_mock.post.side_effect = post
        server.operation(payload, {}, token="tok")
    return requests_mock.post


def test_malformed_payload_is_reported_to_the_callback(caplog):
    with caplog.at_level(logging.ERROR):
        post = _run({"tenantId": "t"})

    assert post.call_count == 1
    assert post.call_args.args == (CALLBACK_URL,)
    assert post.call_args.kwargs["json"] == {"error": "generic error: 'resources'"}
    assert "generic error: 'resources'" in caplog.text


def test_failed_single_binary_reports_the_error():
    def conversion(binary, configs):
        raise ValueError("conversion boom")

    post = _run(
        {"resources": {"binaries": [_binary(0)]}, "tenantId": "t"}, conversion
    )

    assert post.call_count == 1
    assert post.call_args.args == (CALLBACK_URL,)
    assert post.call_args.kwargs["json"] == {"error": "conversion failed"}


def test_document_without_binaries_is_not_an_error():
    post = _run({"resources": {"binaries": []}, "tenantId": "t"})

    assert post.call_count == 1
    assert post.call_args.args == (CALLBACK_URL,)
    assert post.call_args.kwargs["json"] == {}


def test_unreachable_callback_does_not_kill_the_worker(caplog):
    def post(*args, **kwargs):
        raise ConnectionError("datasource is down")

    # The worker must survive: it is a pool thread, and an exception escaping
    # here would take it out of the pool.
    with caplog.at_level(logging.ERROR):
        _run({"resources": {"binaries": []}, "tenantId": "t"}, post=post)

    assert "Callback failed: generic error: datasource is down" in caplog.text


def test_callback_is_sent_with_a_timeout():
    post = _run({"resources": {"binaries": []}, "tenantId": "t"})

    assert post.call_args.kwargs["timeout"] == CALLBACK_TIMEOUT_SECONDS
