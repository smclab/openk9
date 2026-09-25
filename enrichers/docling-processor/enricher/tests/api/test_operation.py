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

import pytest

import app.server as server

# Not the default conftest sets, so a host hardcoded in place of the configured
# one cannot pass.
DATASOURCE_HOST = "http://datasource.test:9999"


def _binary(id):
    return {"id": id, "resourceId": f"r{id}", "url": f"http://binaries/r{id}"}


def _payload(binaries=None):
    if binaries is None:
        binaries = [_binary(0), _binary(1)]
    return {"resources": {"binaries": binaries}, "tenantId": "t"}


def _result(markdown):
    result = MagicMock()
    result.document.export_to_markdown.return_value = markdown
    return result


# Run operation() over the payload's binaries with the given enrich item
# configs; the binary whose id == failing_id raises during conversion.
# Return the enrich callback mock and the (binary id, configs) pairs the
# conversion was called with.
def _operate(configs, failing_id, payload=None):
    calls = []

    def conversion(binary, configs):
        calls.append((binary["id"], configs))
        if binary["id"] == failing_id:
            raise ValueError("conversion boom")
        return _result(f"md-{binary['id']}")

    with (
        patch.object(server, "conversion", side_effect=conversion),
        patch.object(server, "requests") as requests_mock,
        patch.object(server, "DATASOURCE_HOST", DATASOURCE_HOST),
    ):
        server.operation(payload or _payload(), configs, token="tok")
    return requests_mock.post, calls


# Run operation() over two binaries with the given error strategy;
# the binary whose id == failing_id raises during conversion.
# Return the JSON payload posted to the enrich callback.
def _run(strategy, failing_id):
    post, _ = _operate({"error_strategy": strategy}, failing_id)
    return post.call_args.kwargs["json"]


def test_fail_fast_returns_error():
    posted = _run("fail-fast", failing_id=1)
    assert posted == {"error": "conversion failed"}


def test_all_success_returns_binaries():
    posted = _run("fail-fast", failing_id=-1)
    assert posted == {
        "binaries": [
            {**_binary(0), "markdown": "md-0"},
            {**_binary(1), "markdown": "md-1"},
        ]
    }


def test_fail_soft_isolates_error():
    posted = _run("fail-soft", failing_id=1)
    assert posted == {
        "binaries": [
            {**_binary(0), "markdown": "md-0"},
            {**_binary(1), "error": "value error: conversion boom"},
        ]
    }


# The enrich item may not set a strategy at all, or set one this enricher does
# not know: both invalidate the whole document, as fail-fast does.
@pytest.mark.parametrize(
    "configs", [{}, {"error_strategy": "boh"}], ids=["default", "unknown"]
)
def test_missing_or_unknown_strategy_fails_fast(configs):
    post, calls = _operate(configs, failing_id=0)

    assert post.call_args.kwargs["json"] == {"error": "conversion failed"}
    # Fail-fast stops at the first failure.
    assert [id for id, _ in calls] == [0]


def test_multiple_binaries_answer_the_callback_of_their_token():
    post, _ = _operate({"error_strategy": "fail-soft"}, failing_id=-1)

    assert post.call_count == 1
    assert post.call_args.args == (
        f"{DATASOURCE_HOST}/api/datasource/pipeline/callback/tok",
    )
    assert post.call_args.kwargs["timeout"] == server.CALLBACK_TIMEOUT_SECONDS


def test_single_binary_returns_its_document():
    post, _ = _operate({}, failing_id=-1, payload=_payload([_binary(0)]))

    assert post.call_count == 1
    assert post.call_args.args == (
        f"{DATASOURCE_HOST}/api/datasource/pipeline/callback/tok",
    )
    assert post.call_args.kwargs["json"] == {"document": {"markdown": "md-0"}}


# The whole enrich item configuration is what docling is configured from.
@pytest.mark.parametrize("binaries", [1, 2], ids=["single", "multiple"])
def test_the_enrich_item_configs_reach_the_conversion(binaries):
    configs = {"error_strategy": "fail-soft", "pipeline_options.do_ocr": "false"}
    payload = _payload([_binary(id) for id in range(binaries)])

    _, calls = _operate(configs, failing_id=-1, payload=payload)

    assert calls == [(id, configs) for id in range(binaries)]


# A binary without a url has nothing to fetch. What the callback then carries
# for it is left unasserted on purpose: only that it is never converted.
@pytest.mark.parametrize(
    "ids, converted",
    [([0, None], [0]), ([0, None, 2], [0, 2]), ([None], [])],
    ids=["single", "multiple", "only-without-url"],
)
def test_binary_without_url_is_not_converted(ids, converted):
    binaries = [
        {"id": -2, "resourceId": "r-none"} if id is None else _binary(id)
        for id in ids
    ]

    _, calls = _operate({"error_strategy": "fail-soft"}, -1, _payload(binaries))

    assert [id for id, _ in calls] == converted
