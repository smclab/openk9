from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger

from app.utils.chat_history import delete_documents


def start_document_deletion_scheduler(opensearch_host):
    """
    Start a background scheduler to delete documents from OpenSearch indices.

    This function sets up a scheduled job that runs at specified times to delete documents
    older than a specified number of days from the OpenSearch instance. The deletion is
    performed by the `delete_documents` function.

    Parameters
    ----------
    opensearch_host : str
        The host URL of the OpenSearch instance (e.g., "http://localhost:9200").

    Returns
    -------
    None
        This function does not return any value. It starts a background scheduler.

    Notes
    -----
    - Ensure that the APScheduler library is installed and properly configured in your environment.
    - The job will run according to the specified cron expression.

    Examples
    --------
    >>> start_document_deletion_scheduler("http://localhost:9200")
    """

    interval_in_days = 30
    cron_expression = "0 00 * * *"
    minute, hour, day, month, day_of_week = cron_expression.split()

    def delete_documents_cron():
        delete_documents(opensearch_host, interval_in_days)

    scheduler = BackgroundScheduler()
    trigger = CronTrigger(
        month=month, day=day, day_of_week=day_of_week, hour=hour, minute=minute
    )
    scheduler.add_job(delete_documents_cron, trigger)
    scheduler.start()
