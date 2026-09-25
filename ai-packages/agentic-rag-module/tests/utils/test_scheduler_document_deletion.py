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

"""The retention scheduler is started once and stopped with its job.

Enabling it registers a single job on the Quartz expression, which runs the
retention with the configured host and interval; enabling it again while it
runs changes nothing. Disabling it removes the job and shuts the scheduler
down once no job is left. The module-level scheduler is replaced by a mock,
so no real thread is started.
"""

from unittest.mock import MagicMock

import pytest
from apscheduler.triggers.cron import CronTrigger

from app.utils import scheduler


@pytest.fixture
def background(monkeypatch):
    background = MagicMock()
    monkeypatch.setattr(scheduler, "scheduler", background)
    return background


def _start(schedule, cron_expression="0 0 0 ? * * *"):
    scheduler.start_document_deletion_scheduler(
        "http://localhost:9200", schedule, cron_expression, 30
    )


def test_enabling_registers_one_job_on_the_expression(background):
    background.running = False

    _start(True, "0 30 2 ? * MON *")

    background.add_job.assert_called_once()
    kwargs = background.add_job.call_args.kwargs
    assert kwargs["id"] == scheduler.JOB_ID
    assert kwargs["replace_existing"] is True
    trigger = kwargs["trigger"]
    assert isinstance(trigger, CronTrigger)
    fields = {field.name: str(field) for field in trigger.fields}
    assert fields["second"] == "0"
    assert fields["minute"] == "30"
    assert fields["hour"] == "2"
    assert fields["day_of_week"] == "mon"
    background.start.assert_called_once()


def test_job_runs_the_retention_with_host_and_interval(background, monkeypatch):
    background.running = False
    delete_documents = MagicMock()
    monkeypatch.setattr(scheduler, "delete_documents", delete_documents)

    _start(True)
    job = background.add_job.call_args.args[0]
    job()

    delete_documents.assert_called_once_with("http://localhost:9200", 30)


def test_enabling_while_running_changes_nothing(background):
    background.running = True

    _start(True)

    background.add_job.assert_not_called()
    background.start.assert_not_called()


def test_unsupported_expression_does_not_start_the_scheduler(background):
    background.running = False

    with pytest.raises(ValueError):
        _start(True, "0 10 * * *")

    background.add_job.assert_not_called()
    background.start.assert_not_called()


def test_disabling_removes_the_job_and_shuts_down(background):
    background.running = True
    background.get_job.return_value = MagicMock()
    background.get_jobs.return_value = []

    _start(False)

    background.get_job.assert_called_once_with(scheduler.JOB_ID)
    background.remove_job.assert_called_once_with(scheduler.JOB_ID)
    background.shutdown.assert_called_once_with(wait=False)


def test_disabling_keeps_running_while_other_jobs_are_left(background):
    background.running = True
    background.get_job.return_value = MagicMock()
    background.get_jobs.return_value = [MagicMock()]

    _start(False)

    background.remove_job.assert_called_once_with(scheduler.JOB_ID)
    background.shutdown.assert_not_called()


def test_disabling_a_stopped_scheduler_is_a_no_op(background):
    background.running = False
    background.get_job.return_value = None
    background.get_jobs.return_value = []

    _start(False)

    background.remove_job.assert_not_called()
    background.shutdown.assert_not_called()
