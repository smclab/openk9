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

"""Reading a source image from its URL at the source, against a real local
HTTP server: the whitelist on every hop, the 20 MB cap, the timeout and the
check that the bytes are an image."""

import base64
import io
import logging
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

import pytest
from PIL import Image

from app.rag import image_inputs


def _png() -> bytes:
    buffer = io.BytesIO()
    Image.new("RGB", (8, 8), "red").save(buffer, format="PNG")
    return buffer.getvalue()


PNG = _png()


class _Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def do_GET(self):
        path = self.path.split("?")[0]
        if path == "/photo.png":
            self._send(PNG, "application/octet-stream")
        elif path == "/big.png":
            self.send_response(200)
            self.end_headers()
            block = b"\0" * (1024 * 1024)
            try:
                for _ in range(25):
                    self.wfile.write(block)
            except (BrokenPipeError, ConnectionResetError):
                pass
        elif path == "/notes.txt":
            self._send(b"not an image", "text/plain")
        elif path == "/slow.png":
            time.sleep(2)
            self._send(PNG, "image/png")
        elif path == "/redirect-out":
            self.send_response(302)
            self.send_header("Location", "http://elsewhere.example.com/photo.png")
            self.end_headers()
        elif path == "/redirect-in":
            self.send_response(302)
            self.send_header("Location", "/photo.png")
            self.end_headers()
        elif path == "/redirect-slow":
            time.sleep(1.5)
            self.send_response(302)
            self.send_header("Location", "/slow.png")
            self.end_headers()
        elif path == "/drip.png":
            # a byte every half second: no single read waits long
            self.send_response(200)
            self.send_header("Content-Length", "12")
            self.end_headers()
            try:
                for _ in range(12):
                    time.sleep(0.5)
                    self.wfile.write(b"\0")
            except (BrokenPipeError, ConnectionResetError):
                pass
        elif path.startswith("/drip-hop/"):
            # a redirect whose headers come in pieces, 1.2 s in all
            hop = int(path.rsplit("/", 1)[1])
            location = f"/drip-hop/{hop - 1}" if hop else "/photo.png"
            pieces = [
                b"HTTP/1.0 302 Found\r\n",
                f"Location: {location}\r\n".encode(),
                b"Content-Length: 0\r\n",
                b"\r\n",
            ]
            try:
                for piece in pieces:
                    time.sleep(0.3)
                    self.wfile.write(piece)
            except (BrokenPipeError, ConnectionResetError):
                pass
        else:
            self.send_response(404)
            self.end_headers()

    def _send(self, body, content_type):
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


@pytest.fixture(scope="module")
def server():
    httpd = ThreadingHTTPServer(("127.0.0.1", 0), _Handler)
    thread = threading.Thread(target=httpd.serve_forever, daemon=True)
    thread.start()
    yield f"http://127.0.0.1:{httpd.server_address[1]}"
    httpd.shutdown()


def test_an_image_is_read_as_it_is(server):
    image = image_inputs.fetch_source_image(f"{server}/photo.png")

    # the mime type comes from the decoded bytes, not from the header
    assert image.mime_type == "image/png"
    assert base64.b64decode(image.data) == PNG


def test_a_redirect_inside_the_whitelist_is_followed(server, monkeypatch):
    monkeypatch.setattr(image_inputs, "SOURCE_IMAGE_HOST_REGEXES", [r"127\.0\.0\.1"])

    assert image_inputs.fetch_source_image(f"{server}/redirect-in") is not None


def test_an_image_over_20_mb_is_discarded(server, caplog):
    with caplog.at_level(logging.WARNING, logger="app.rag.image_inputs"):
        assert image_inputs.fetch_source_image(f"{server}/big.png") is None

    assert "larger than 20971520 bytes" in caplog.text


def test_a_host_outside_the_whitelist_is_not_read(server, monkeypatch, caplog):
    monkeypatch.setattr(image_inputs, "SOURCE_IMAGE_HOST_REGEXES", [r"photos\..*"])

    with caplog.at_level(logging.WARNING, logger="app.rag.image_inputs"):
        assert image_inputs.fetch_source_image(f"{server}/photo.png") is None

    assert "not allowed by the whitelist" in caplog.text


def test_a_redirect_outside_the_whitelist_is_not_followed(server, monkeypatch):
    monkeypatch.setattr(image_inputs, "SOURCE_IMAGE_HOST_REGEXES", [r"127\.0\.0\.1"])

    assert image_inputs.fetch_source_image(f"{server}/redirect-out") is None


def test_the_whitelist_matches_the_whole_host(server, monkeypatch):
    monkeypatch.setattr(image_inputs, "SOURCE_IMAGE_HOST_REGEXES", [r"127\.0"])

    assert image_inputs.fetch_source_image(f"{server}/photo.png") is None


@pytest.mark.parametrize("url", ["file:///etc/passwd", "ftp://host/photo.png"])
def test_only_http_and_https_are_read(url):
    assert image_inputs.fetch_source_image(url) is None


def test_bytes_that_are_not_an_image_are_discarded(server):
    assert image_inputs.fetch_source_image(f"{server}/notes.txt") is None


def test_a_url_that_does_not_answer_is_logged(server, monkeypatch, caplog):
    monkeypatch.setattr(image_inputs, "FETCH_TIMEOUT_SECONDS", 0.5)

    with caplog.at_level(logging.WARNING, logger="app.rag.image_inputs"):
        started = time.monotonic()
        assert image_inputs.fetch_source_image(f"{server}/slow.png?sig=s3cr3t") is None

    assert time.monotonic() - started < 1.5
    assert "/slow.png not read" in caplog.text
    # a pre-signed query is a credential: it never reaches the log
    assert "s3cr3t" not in caplog.text


def test_redirects_share_the_deadline(server, monkeypatch):
    monkeypatch.setattr(image_inputs, "FETCH_TIMEOUT_SECONDS", 2)

    started = time.monotonic()
    # 1.5 s for the redirect: the image, 2 s more, has only the 0.5 s left
    assert image_inputs.fetch_source_image(f"{server}/redirect-slow") is None

    assert time.monotonic() - started < 3


def test_the_deadline_is_checked_before_every_hop(server, monkeypatch, caplog):
    monkeypatch.setattr(image_inputs, "FETCH_TIMEOUT_SECONDS", 2)

    with caplog.at_level(logging.WARNING, logger="app.rag.image_inputs"):
        started = time.monotonic()
        # the second hop ends past the deadline, the third is never asked
        assert image_inputs.fetch_source_image(f"{server}/drip-hop/3") is None

    assert time.monotonic() - started < 3
    assert "/drip-hop/3 not read: not read within 2 seconds" in caplog.text


def test_a_server_that_drips_bytes_is_cut_at_the_deadline(server, monkeypatch, caplog):
    monkeypatch.setattr(image_inputs, "FETCH_TIMEOUT_SECONDS", 2)

    with caplog.at_level(logging.WARNING, logger="app.rag.image_inputs"):
        started = time.monotonic()
        # the drip lasts 6 s: the turn does not wait for it
        images = image_inputs.fetch_source_images([f"{server}/drip.png"])

    assert time.monotonic() - started < 4
    assert images == [None]
    assert "/drip.png not read: not read within 2 seconds" in caplog.text


def test_images_are_read_together_in_their_order(server, monkeypatch):
    monkeypatch.setattr(image_inputs, "FETCH_TIMEOUT_SECONDS", 3)

    started = time.monotonic()
    images = image_inputs.fetch_source_images(
        [f"{server}/slow.png", f"{server}/notes.txt", f"{server}/slow.png"]
    )

    # two slow reads of 2 s each, done in parallel
    assert time.monotonic() - started < 3.5
    assert [image is not None for image in images] == [True, False, True]
