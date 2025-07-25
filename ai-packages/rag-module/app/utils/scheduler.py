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

from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger

from app.utils.chat_history import delete_documents
from app.utils.quartz_apscheduler import QuartzExpressionParser

scheduler = BackgroundScheduler()
JOB_ID = "document_deletion_job"


def start_document_deletion_scheduler(
    opensearch_host, schedule, cron_expression, interval_in_days
):
    """Configure and control the document deletion scheduler.

    Manages the lifecycle of a background scheduler that periodically deletes
    documents from OpenSearch based on the provided parameters. Implements
    thread-safe job management with a singleton scheduler instance.

    :param str opensearch_host: OpenSearch connection string (host:port)
    :param bool schedule: True to enable scheduling, False to disable
    :param str cron_expression: Cron schedule for deletions (7-part quartz format)
    :param int interval_in_days: Number of days to use as a threshold for deletion.
    **Usage Example**:

    .. code-block:: python

        # Enable daily cleanup at midnight
        start_document_deletion_scheduler(
            "localhost:9200",
            True,
            "0 0 0 ? * * *",
            180
        )

        # Disable cleanup
        start_document_deletion_scheduler(None, False, "", 180)

    **Cron Format**:

    * ``seconds minutes hours day_of_month month day_of_week year``
    * Uses quartz cron syntax with 7 time components
    * Example: "0 20 20 ? * * *" = Daily at 20:20
    * Example: "0 0 10 ? * MON *" = Weekly on Monday at 10:00 UTC
    """
    if schedule and not scheduler.running:

        def delete_documents_cron():
            delete_documents(opensearch_host, interval_in_days)

        parser = QuartzExpressionParser(cron_expression)
        apscheduler_kwargs = parser.to_apscheduler_kwargs()
        trigger = CronTrigger(**apscheduler_kwargs)

        scheduler.add_job(
            delete_documents_cron, trigger=trigger, id=JOB_ID, replace_existing=True
        )
        scheduler.start()

    elif not schedule:
        if scheduler.get_job(JOB_ID):
            scheduler.remove_job(JOB_ID)
        if scheduler.running and not scheduler.get_jobs():
            scheduler.shutdown(wait=False)
