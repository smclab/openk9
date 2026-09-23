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
import threading
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

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


def test_start_task_hands_the_work_to_the_pool():
    input = SimpleNamespace(
        payload=_payload(0), enrichItemConfig={}, replyTo="tok"
    )

    with patch.object(server, "EXECUTOR") as executor:
        asyncio.run(server.start_task(input))

    assert executor.submit.call_args.args[0] is server.operation
    assert executor.submit.call_args.kwargs["token"] == "tok"
