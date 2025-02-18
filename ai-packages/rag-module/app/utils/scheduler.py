import os

from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
from dotenv import load_dotenv

from app.utils.chat_history import delete_documents

load_dotenv()

OPENSEARCH_HOST = os.getenv("OPENSEARCH_HOST")

interval_in_days = 30
cron_expression = "0 00 * * *"
cron_expression = cron_expression.split()
minute = cron_expression[0]
hour = cron_expression[1]
day = cron_expression[2]
month = cron_expression[3]
day_of_week = cron_expression[4]


def delete_documents_cron():
    delete_documents(OPENSEARCH_HOST, interval_in_days)


scheduler = BackgroundScheduler()
trigger = CronTrigger(
    month=month, day=day, day_of_week=day_of_week, hour=hour, minute=minute
)
scheduler.add_job(delete_documents_cron, trigger)
scheduler.start()
