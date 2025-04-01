from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger

from app.utils.chat_history import delete_documents


scheduler = BackgroundScheduler()
JOB_ID = "document_deletion_job"


def start_document_deletion_scheduler(opensearch_host, schedule, cron_expression):
    """Configure and control the document deletion scheduler.

    Manages the lifecycle of a background scheduler that periodically deletes
    documents from OpenSearch based on the provided parameters. Implements
    thread-safe job management with a singleton scheduler instance.

    :param str opensearch_host: OpenSearch connection string (host:port)
    :param bool schedule: True to enable scheduling, False to disable
    :param str cron_expression: Cron schedule for deletions (5-part format)

    **Usage Example**:

    .. code-block:: python

        # Enable daily cleanup at midnight
        start_document_deletion_scheduler(
            "localhost:9200",
            True,
            "0 0 * * *"
        )

        # Disable cleanup
        start_document_deletion_scheduler(None, False, "")

    **Cron Format**:

    * ``minute hour day month day_of_week``
    * Uses standard cron syntax with 5 time components
    * Example: "0 3 * * 0" = Sundays at 3:00 AM
    """
    if schedule and not scheduler.running:

        def delete_documents_cron():
            delete_documents(opensearch_host)

        trigger = CronTrigger.from_crontab(cron_expression)

        scheduler.add_job(
            delete_documents_cron, trigger=trigger, id=JOB_ID, replace_existing=True
        )
        scheduler.start()

    elif not schedule:
        if scheduler.get_job(JOB_ID):
            scheduler.remove_job(JOB_ID)
        if scheduler.running and not scheduler.get_jobs():
            scheduler.shutdown(wait=False)
