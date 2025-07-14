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
