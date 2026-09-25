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

import asyncio
import importlib.util
import threading
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import dotenv

import app.server as server

TASKS = 6


def _payload(id):
    return {
        "resources": {
            "binaries": [
                {"id": id, "resourceId": f"r{id}", "url": f"http://binaries/r{id}"}
            ]
        },
        "tenantId": "t",
    }


def test_conversions_never_exceed_the_configured_concurrency():
    lock = threading.Lock()
    running = 0
    peak = 0

    def conversion(binary, configs):
        nonlocal running, peak
        with lock:
            running += 1
            peak = max(peak, running)
        # Hold the worker long enough for the queued tasks to pile up behind it.
        threading.Event().wait(0.05)
        with lock:
            running -= 1
        return MagicMock()

    with patch.object(
        server, "conversion", side_effect=conversion
    ), patch.object(server, "requests") as requests_mock:
        futures = [
            server.EXECUTOR.submit(
                server.operation, payload=_payload(i), configs={}, token=f"tok-{i}"
            )
            for i in range(TASKS)
        ]
        for future in futures:
            future.result(timeout=30)

    assert peak <= server.MAX_CONCURRENT_CONVERSIONS
    assert requests_mock.post.call_count == TASKS
    # Each task answers the callback of its own token.
    assert sorted(c.args[0] for c in requests_mock.post.call_args_list) == sorted(
        f"{server.DATASOURCE_HOST}/api/datasource/pipeline/callback/tok-{i}"
        for i in range(TASKS)
    )


# conftest raises the pool to 2 workers for the whole session, so the default
# is read from a copy of the module loaded on its own, leaving app.server and
# its executor as they are.
def test_conversions_are_serial_by_default(monkeypatch):
    monkeypatch.delenv("MAX_CONCURRENT_CONVERSIONS", raising=False)
    # The module loads the .env on import: a developer's own must not stand in
    # for the default.
    monkeypatch.setattr(dotenv, "load_dotenv", lambda *args, **kwargs: False)
    spec = importlib.util.spec_from_file_location("isolated_server", server.__file__)
    isolated = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(isolated)

    try:
        assert isolated.MAX_CONCURRENT_CONVERSIONS == 1
        assert isolated.EXECUTOR._max_workers == 1
    finally:
        isolated.EXECUTOR.shutdown()


def test_start_task_hands_the_work_to_the_pool():
    input = SimpleNamespace(
        payload=_payload(0),
        enrichItemConfig={"error_strategy": "fail-soft"},
        replyTo="tok",
    )

    with patch.object(server, "EXECUTOR") as executor:
        asyncio.run(server.start_task(input))

    executor.submit.assert_called_once_with(
        server.operation,
        payload=_payload(0),
        configs={"error_strategy": "fail-soft"},
        token="tok",
    )
