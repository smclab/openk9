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


"""The application lifespan starts the chat retention scheduler and stops it."""

import asyncio
from unittest.mock import MagicMock, call

import pytest

import app.server as server


@pytest.fixture
def scheduler(monkeypatch):
    scheduler = MagicMock()
    monkeypatch.setattr(server, "start_document_deletion_scheduler", scheduler)
    monkeypatch.setattr(server, "OPENSEARCH_HOST", "http://opensearch:9200")
    monkeypatch.setattr(server, "SCHEDULE", True)
    monkeypatch.setattr(server, "CRON_EXPRESSION", "0 0 3 ? * * *")
    monkeypatch.setattr(server, "INTERVAL_IN_DAYS", 30)
    return scheduler


def _settings(schedule):
    return call(
        opensearch_host="http://opensearch:9200",
        schedule=schedule,
        cron_expression="0 0 3 ? * * *",
        interval_in_days=30,
    )


def test_lifespan_starts_the_scheduler_on_startup_and_stops_it_on_shutdown(
    scheduler,
):
    calls_while_running = []

    async def run():
        async with server.lifespan(server.app):
            calls_while_running.extend(scheduler.call_args_list)

    asyncio.run(run())

    assert calls_while_running == [_settings(schedule=True)]
    assert scheduler.call_args_list == [
        _settings(schedule=True),
        _settings(schedule=False),
    ]


def test_lifespan_with_scheduling_disabled_never_schedules(scheduler, monkeypatch):
    monkeypatch.setattr(server, "SCHEDULE", False)

    async def run():
        async with server.lifespan(server.app):
            pass

    asyncio.run(run())

    assert scheduler.call_args_list == [
        _settings(schedule=False),
        _settings(schedule=False),
    ]
