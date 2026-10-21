"""
Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

import logging
import os
import sys
from types import SimpleNamespace

import pytest
from urllib3 import HTTPHeaderDict

APP_DIR = os.path.join(os.path.dirname(__file__), "..", "app")

sys.path.insert(0, APP_DIR)

from extraction.extractor import MinioExtractor  # noqa: E402


class FakeS3Client:
    """Answers stat_object and get_object for a single image."""

    def __init__(self, object_name: str, headers: dict[str, str]) -> None:
        self.object_name = object_name
        self.headers = HTTPHeaderDict(
            {"Content-Type": "image/png", "ETag": '"abc"', **headers}
        )
        self.stat_calls = 0
        self.get_calls = 0

    def stat_object(self, bucket_name: str, object_name: str) -> SimpleNamespace:
        self.stat_calls += 1
        return SimpleNamespace(
            object_name=object_name,
            content_type="image/png",
            size=4,
            metadata=self.headers,
        )

    def get_object(self, bucket_name: str, object_name: str) -> SimpleNamespace:
        self.get_calls += 1
        return SimpleNamespace(data=b"\x89PNG")


def extract(monkeypatch: pytest.MonkeyPatch, headers: dict[str, str]) -> dict:
    """Runs manage_data on one image and returns the binary sent to ingestion."""
    monkeypatch.chdir(APP_DIR)

    extractor = MinioExtractor(
        "minio", 9000, "key", "secret", "uat-bin", "", {}, 1, 0, "s-1",
        "tenant", "http://ingestion", public_base_url="https://files.example.com",
    )
    payloads = []
    monkeypatch.setattr(
        extractor.ingestion_handler, "post_message",
        lambda payload: payloads.append(payload),
    )

    client = FakeS3Client("manuali/schema 12V.png", headers)
    extractor.manage_data(client, SimpleNamespace(object_name=client.object_name), 0)

    assert client.stat_calls == 1
    assert client.get_calls == 1
    assert len(payloads) == 1

    payload = payloads[0]
    binary = payload["resources"]["binaries"][0]
    binary["documentUrl"] = payload["datasourcePayload"]["document"]["url"]
    return binary


def test_the_binary_carries_the_url_of_the_object(monkeypatch):
    binary = extract(monkeypatch, {})

    assert binary["sourceUrl"] == \
        "https://files.example.com/uat-bin/manuali/schema%2012V.png"
    assert binary["sourceUrl"] == binary["documentUrl"]


def test_the_user_metadata_reach_the_binary(monkeypatch):
    binary = extract(
        monkeypatch,
        {"X-Amz-Meta-Lato": "retro", "X-Amz-Meta-Caption": "schema impianto 12V"},
    )

    assert binary["metadata"] == {"lato": "retro", "caption": "schema impianto 12V"}


def test_a_fixed_key_with_a_value_of_the_right_type(monkeypatch):
    binary = extract(
        monkeypatch,
        {"X-Amz-Meta-Page": "12", "X-Amz-Meta-Generated": "true"},
    )

    assert binary["metadata"] == {"page": 12, "generated": True}


def test_a_fixed_key_with_a_value_of_the_wrong_type(monkeypatch, caplog):
    with caplog.at_level(logging.WARNING, logger="status-logger"):
        binary = extract(
            monkeypatch,
            {
                "X-Amz-Meta-Page": "dodici",
                "X-Amz-Meta-Generated": "True",
                "X-Amz-Meta-Lato": "retro",
            },
        )

    assert binary["metadata"] == {"lato": "retro"}
    assert "page" in caplog.text
    assert "generated" in caplog.text


def test_an_object_without_user_metadata(monkeypatch):
    binary = extract(monkeypatch, {})

    assert "sourceUrl" in binary
    assert "metadata" not in binary


def test_the_keys_are_normalized_and_the_encoded_values_decoded(monkeypatch):
    binary = extract(
        monkeypatch,
        {
            "X-Amz-Meta-Data.Scatto": "2026-10-21",
            "X-Amz-Meta-Caption": "=?utf-8?q?vista_laterale_d=C3=A9cor?=",
        },
    )

    assert binary["metadata"] == {
        "data_scatto": "2026-10-21",
        "caption": "vista laterale décor",
    }


def test_a_value_with_an_unknown_charset_stays_raw(monkeypatch):
    binary = extract(monkeypatch, {"X-Amz-Meta-Caption": "=?x-ignoto?q?abc?="})

    assert binary["metadata"] == {"caption": "=?x-ignoto?q?abc?="}
