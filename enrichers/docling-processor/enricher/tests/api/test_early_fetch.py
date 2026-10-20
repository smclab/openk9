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

import os
import tempfile
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer
from unittest.mock import MagicMock, patch

import pytest
import requests

import app.server as server
import app.utils.converter as converter

CONTENT = b"%PDF-1.4 staged bytes"


class _Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.startswith("/expired"):
            self.send_response(403)
            self.end_headers()
            return
        self.send_response(200)
        self.send_header("Content-Length", str(len(CONTENT)))
        self.end_headers()
        self.wfile.write(CONTENT)

    def log_message(self, *args):
        pass


@pytest.fixture(scope="module")
def storage():
    httpd = HTTPServer(("127.0.0.1", 0), _Handler)
    thread = threading.Thread(target=httpd.serve_forever, daemon=True)
    thread.start()
    yield f"http://127.0.0.1:{httpd.server_address[1]}"
    httpd.shutdown()


def _payload(*urls):
    return {
        "resources": {
            "binaries": [
                {"id": i, "name": f"f{i}.pdf", "stagingUrl": url}
                for i, url in enumerate(urls)
            ]
        },
        "tenantId": "t",
    }


def test_fetch_to_file_writes_the_binary_to_a_temporary_file(storage):
    path = converter.fetch_to_file(f"{storage}/bin?X-Amz-Signature=secret")
    try:
        with open(path, "rb") as file:
            assert file.read() == CONTENT
    finally:
        os.remove(path)


def test_fetch_to_file_does_not_disclose_the_url(storage):
    with pytest.raises(requests.HTTPError) as failure:
        converter.fetch_to_file(f"{storage}/expired?X-Amz-Signature=secret")

    assert "secret" not in str(failure.value)
    assert "403" in str(failure.value)


def test_enqueue_fetches_every_binary_before_queueing_the_conversion(storage):
    payload = _payload(f"{storage}/a", f"{storage}/b")

    with patch.object(server, "EXECUTOR") as executor:
        server.enqueue(payload, {}, token="tok")

    kwargs = executor.submit.call_args.kwargs
    assert executor.submit.call_args.args[0] is server.operation
    assert sorted(kwargs["staged"]) == [0, 1]
    for path in kwargs["staged"].values():
        with open(path, "rb") as file:
            assert file.read() == CONTENT
        os.remove(path)


def test_enqueue_leaves_a_failed_fetch_to_the_conversion(storage):
    payload = _payload(f"{storage}/a", f"{storage}/expired")

    with patch.object(server, "EXECUTOR") as executor:
        server.enqueue(payload, {}, token="tok")

    staged = executor.submit.call_args.kwargs["staged"]
    assert list(staged) == [0]
    os.remove(staged[0])


def test_enqueue_still_queues_a_malformed_payload():
    with patch.object(server, "EXECUTOR") as executor:
        server.enqueue({"tenantId": "t"}, {}, token="tok")

    assert executor.submit.call_args.kwargs["staged"] == {}


def test_operation_converts_the_fetched_file_after_the_url_expired():
    # the URL points to an expired credential: only the fetched file is usable
    with tempfile.NamedTemporaryFile(delete=False) as file:
        file.write(CONTENT)
    payload = _payload("http://storage/expired")
    seen = []

    def conversion(binary, configs, local_path=None):
        with open(local_path, "rb") as staged_file:
            seen.append(staged_file.read())
        result = MagicMock()
        result.document.export_to_markdown.return_value = "md"
        return result

    with (
        patch.object(server, "conversion", side_effect=conversion),
        patch.object(server, "requests") as requests_mock,
    ):
        server.operation(payload, {}, token="tok", staged={0: file.name})

    assert seen == [CONTENT]
    assert requests_mock.post.call_args.kwargs["json"] == {
        "document": {"markdown": "md"}
    }
    assert not os.path.exists(file.name)


def test_operation_removes_the_fetched_files_when_the_conversion_fails():
    with tempfile.NamedTemporaryFile(delete=False) as file:
        file.write(CONTENT)

    def conversion(binary, configs, local_path=None):
        raise ValueError("conversion boom")

    with (
        patch.object(server, "conversion", side_effect=conversion),
        patch.object(server, "requests"),
    ):
        server.operation(
            _payload("http://storage/a"), {}, token="tok", staged={0: file.name}
        )

    assert not os.path.exists(file.name)
