from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger

from app.utils.chat_history import delete_documents


def start_document_deletion_scheduler(opensearch_host):
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
