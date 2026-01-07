import json
import logging
import os
import random
import time
from datetime import datetime, timedelta
from functools import partial

import numpy as np
import pandas as pd
import pika
from apscheduler.schedulers.background import BackgroundScheduler
from rabbit_manager.structure import setup_rabbitmq
from utils.evaluators import layout_fidelity, redundancy_bloat, semantic_choerence
from utils.helpers import client, make_experiment, manage_daily_dataset, score

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
    handlers=[
        logging.StreamHandler(),  # console
        logging.FileHandler("app.log"),  # file
    ],
)

logger = logging.getLogger(__name__)

task = partial(score)
should_make_experiment = False

rabbit_host = os.getenv("RABBITMQ_HOST", "localhost")
rabbit_user = os.getenv("RABBITMQ_USER", "user")
rabbit_pass = os.getenv("RABBITMQ_PASS", "pass")

MAX_RETRIES = os.getenv("RABBITMQ_MAX_RETRIES", 3)
MIN_TIME_DELAY = int(os.getenv("MIN_TIME_DELAY_MINUTES", 5))
EVALUATORS = [semantic_choerence, redundancy_bloat, layout_fidelity]


def interval_experiment(task, evaluators):
    global should_make_experiment
    if should_make_experiment:
        make_experiment(task, evaluators)
        should_make_experiment = False


scheduler = BackgroundScheduler()
scheduler.add_job(
    interval_experiment,
    trigger="interval",
    minutes=MIN_TIME_DELAY,
    misfire_grace_time=120,
    id="experiment_pending_documents",
    kwargs={
        "task": task,
        "evaluators": EVALUATORS,
    },
)

scheduler.start()
setup_rabbitmq()

credentials = pika.PlainCredentials(rabbit_user, rabbit_pass)

for _ in range(10):
    try:
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=rabbit_host, credentials=credentials)
        )
        break
    except Exception as e:
        print("RabbitMQ non pronto, retry…")
        time.sleep(3)


connection = pika.BlockingConnection(
    pika.ConnectionParameters(host=rabbit_host, credentials=credentials)
)
channel = connection.channel()

channel.queue_declare(queue="chunks")

print("[*] In attesa di messaggi. Premere CTRL+C per uscire.")


def callback(ch, method, properties, body):
    global should_make_experiment
    try:
        data = json.loads(body.decode("utf-8"))
        chunks = data.get("chunks")
        text = data.get("text")
        chonkie_chunks = []
        dataset_name = f"dataset-{datetime.today().strftime('%d-%m-%Y')}"
        experiment_name = f"experiment-{datetime.today().strftime('%d-%m-%Y-%H-%M')}"

        input_item = [{"chunks": chunks, "text": text}]
        output_item = [
            {
                "expected": [
                    {
                        "semantic_choerence": {
                            "forward": [1.0, 1.2],
                            "backward": [1.0, 1.2],
                        },
                        "redundancy_bloat": {"redundancy": [0.0, 0.25]},
                        "layout_fidelity": {"coverage": 1.0},
                    }
                ]
            }
        ]

        dataset = manage_daily_dataset(
            dataset_name=dataset_name, input_item=input_item, output_item=output_item
        )
        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        headers = properties.headers or {}
        retry_count = headers.get("retry-count", 0)

        if retry_count >= int(MAX_RETRIES):
            print(f"[ERROR] Max retries reached ({retry_count}) - invio in error queue")
            ch.basic_publish(
                exchange="error.exchange",
                routing_key="error",
                body=body,
                properties=pika.BasicProperties(delivery_mode=2),
            )
            ch.basic_ack(delivery_tag=method.delivery_tag)
        else:
            # incrementa il contatore e manda alla retry queue
            headers["retry-count"] = retry_count + 1
            ch.basic_publish(
                exchange="retry.exchange",
                routing_key="retry",
                body=body,
                properties=pika.BasicProperties(delivery_mode=2, headers=headers),
            )
            ch.basic_ack(delivery_tag=method.delivery_tag)
            print(
                f"[RETRY] Retry count {retry_count + 1} - messaggio inviato alla retry queue"
            )
    should_make_experiment = True


channel.basic_consume(queue="main.queue", on_message_callback=callback)

channel.start_consuming()
