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


import json
from datetime import datetime
from unittest.mock import MagicMock

import pytest

import server

BODY = json.dumps({"chunks": [{"c": {"text": "t"}}], "text": "t"}).encode("utf-8")


class FixedDatetime(datetime):
    @classmethod
    def today(cls):
        return cls(2026, 9, 1, 10, 30)


def message(headers=None):
    channel = MagicMock()
    method = MagicMock(delivery_tag="tag-1")
    properties = MagicMock(headers=headers)
    return channel, method, properties


@pytest.fixture
def manage_daily_dataset(monkeypatch):
    manage_daily_dataset = MagicMock()
    monkeypatch.setattr(server, "manage_daily_dataset", manage_daily_dataset)
    monkeypatch.setattr(server, "BUFFER_UPLOAD", False)
    monkeypatch.setattr(server, "datetime", FixedDatetime)
    return manage_daily_dataset


def test_callback_stores_the_message_in_the_daily_dataset(manage_daily_dataset):
    channel, method, properties = message()

    server.callback(channel, method, properties, BODY)

    kwargs = manage_daily_dataset.call_args.kwargs
    assert kwargs["dataset_name"] == "dataset-01-09-2026"
    assert kwargs["input_item"] == [{"chunks": [{"c": {"text": "t"}}], "text": "t"}]
    assert kwargs["output_item"][0]["expected"][0]["layout_fidelity"] == {
        "coverage": 1.0
    }
    channel.basic_ack.assert_called_once_with(delivery_tag="tag-1")
    channel.basic_publish.assert_not_called()


def test_callback_buffers_the_message_when_buffer_upload_is_on(
    manage_daily_dataset, monkeypatch
):
    add_item = MagicMock()
    monkeypatch.setattr(server, "add_item", add_item)
    monkeypatch.setattr(server, "BUFFER_UPLOAD", True)
    channel, method, properties = message()

    server.callback(channel, method, properties, BODY)

    add_item.assert_called_once()
    manage_daily_dataset.assert_not_called()
    channel.basic_ack.assert_called_once_with(delivery_tag="tag-1")


def test_callback_sends_a_failed_message_to_the_retry_queue(manage_daily_dataset):
    manage_daily_dataset.side_effect = RuntimeError("phoenix down")
    channel, method, properties = message(headers={"retry-count": 1})

    server.callback(channel, method, properties, BODY)

    kwargs = channel.basic_publish.call_args.kwargs
    assert kwargs["exchange"] == "retry.exchange"
    assert kwargs["routing_key"] == "retry"
    assert kwargs["body"] == BODY
    assert kwargs["properties"].headers == {"retry-count": 2}
    channel.basic_ack.assert_called_once_with(delivery_tag="tag-1")


def test_callback_sends_the_message_to_the_error_queue_after_max_retries(
    manage_daily_dataset,
):
    manage_daily_dataset.side_effect = RuntimeError("phoenix down")
    channel, method, properties = message(
        headers={"retry-count": int(server.MAX_RETRIES)}
    )

    server.callback(channel, method, properties, BODY)

    kwargs = channel.basic_publish.call_args.kwargs
    assert kwargs["exchange"] == "error.exchange"
    assert kwargs["routing_key"] == "error"
    assert kwargs["body"] == BODY
    channel.basic_ack.assert_called_once_with(delivery_tag="tag-1")
