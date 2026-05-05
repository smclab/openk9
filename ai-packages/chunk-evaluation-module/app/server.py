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
from utils.helpers import (
    client,
    get_dataset_index,
    make_experiment,
    manage_daily_dataset,
    score,
)

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
should_make_experiment = True
should_flush_data = False

input_data = []
output_data = []
metadata = []


rabbit_host = os.getenv("RABBITMQ_HOST", "localhost")
rabbit_user = os.getenv("RABBITMQ_USER", "user")
rabbit_pass = os.getenv("RABBITMQ_PASS", "pass")

MAX_RETRIES = os.getenv("RABBITMQ_MAX_RETRIES", 3)
MIN_TIME_DELAY = int(os.getenv("MIN_TIME_DELAY_MINUTES", 5))
BUFFER_TRESHOLD = int(os.getenv("BUFFER_TRESHOLD", 50))
BUFFER_DELAY = int(os.getenv("BUFFER_DELAY", 20))
BUFFER_UPLOAD = os.getenv("BUFFER_UPLOAD", "").lower() == "true"
EVALUATORS = [semantic_choerence, redundancy_bloat, layout_fidelity]
N_MAX_DOCUMENTS = int(os.getenv("N_MAX_DOCUMENTS", 150))


def flush_dataset(dataset_base_name):
    global input_data, output_data, metadata
    global should_make_experiment, should_flush_data
    portion = min(len(input_data), BUFFER_TRESHOLD)

    all_datasets = client.datasets.list()
    dataset_index = len(
        get_dataset_index(
            all_datasets=all_datasets,
            name_contains=dataset_base_name,
            min_examples=N_MAX_DOCUMENTS,
        )
    )
    num_daily_datasets = len(
        get_dataset_index(
            all_datasets=all_datasets,
            name_contains=dataset_base_name,
        )
    )

    dataset_name = dataset_base_name + f"-{dataset_index}"
    logging.info(f"Dataset name: {dataset_name}")

    if not input_data:
        return
    logger.info(f"Flushing {portion}/{len(input_data)} documents")
    manage_daily_dataset(
        dataset_name=dataset_name,
        input_item=input_data[:portion],
        output_item=output_data[:portion],
        metadata=metadata[:portion],
    )

    input_data = input_data[portion:]
    output_data = output_data[portion:]
    metadata = metadata[portion:]
    logger.info(f"Remaining {len(input_data)} documents")
    if len(input_data) > 0:
        should_flush_data = True
        should_make_experiment = False
        scheduler.add_job(**flush_job_params)
    else:
        should_flush_data = False
        should_make_experiment = True
        scheduler.remove_job("flush_pending_documents")


def add_item(dataset_name, input_item, output_item):
    global input_data, output_data, should_flush_data, metadata
    input_data.extend(input_item)
    output_data.extend(output_item)
    metadata.extend([{}])
    logger.info(f"Documents{len(input_data)}")
    if not should_flush_data:
        logger.info("ATTIVATO FLUSHING")
        should_flush_data = True
        scheduler.add_job(**flush_job_params)


def interval_experiment(task, evaluators, dataset_name=None, dataset_base_name=None):
    global should_make_experiment
    if should_make_experiment:
        if dataset_name:
            make_experiment(task, evaluators, dataset=dataset_name)
            return

        all_datasets = client.datasets.list()
        if dataset_base_name:
            daily_dataset = get_dataset_index(
                all_datasets, name_contains=dataset_base_name
            )
            reversing = daily_dataset[::-1]
            for dats in reversing:
                elm = client.experiments.list(dataset_id=dats["id"])
                if not elm or elm[0].get("example_count", 0) < dats.get(
                    "example_count", 0
                ):
                    make_experiment(task, evaluators, dataset=dats["name"])
                    return
        else:
            reversing = all_datasets[::-1]
            for dats in reversing:
                elm = client.experiments.list(dataset_id=dats["id"])
                if not elm or elm[0].get("example_count", 0) < dats.get(
                    "example_count", 0
                ):
                    make_experiment(task, evaluators, dataset=dats["name"])
                    return
            should_make_experiment = False


experiment_job_params = {
    "func": interval_experiment,  # La funzione da eseguire
    "trigger": "interval",  # Il trigger per far partire il job periodicamente
    "minutes": MIN_TIME_DELAY,  # Intervallo in minuti
    "misfire_grace_time": 120,  # Tempo di tolleranza per il job
    "replace_existing": True,
    "id": "experiment_pending_documents",  # ID del job
    "kwargs": {
        "task": task,  # Parametro 'task'
        "evaluators": EVALUATORS,
        # "dataset_base_name": f"dataset-{datetime.today().strftime('%d-%m-%Y')}",  # Parametro 'evaluators'
    },
}

flush_job_params = {
    "func": flush_dataset,  # La funzione da eseguire
    "trigger": "interval",  # Il trigger per far partire il job
    "seconds": BUFFER_DELAY,  # La data in cui il job verrà eseguito
    "replace_existing": True,
    "misfire_grace_time": 120,
    "id": "flush_pending_documents",
    "kwargs": {
        "dataset_base_name": f"dataset-{datetime.today().strftime('%d-%m-%Y')}"  # Parametro della funzione
    },
}

scheduler = BackgroundScheduler()
scheduler.add_job(**experiment_job_params)
scheduler.start()
setup_rabbitmq()

credentials = pika.PlainCredentials(rabbit_user, rabbit_pass)
connection = None

for _ in range(10):
    try:
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=rabbit_host, credentials=credentials)
        )
        logger.info("RabbitMQ pronto")
        break
    except Exception as e:
        logger.info("RabbitMQ non pronto, retry…")
        time.sleep(3)

if not connection:
    raise "Connessione a RabbitMQ fallita"

channel = connection.channel()

channel.basic_qos(prefetch_count=1)

channel.queue_declare(queue="chunks")

logger.info("[*] In attesa di messaggi. Premere CTRL+C per uscire.")


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
        if BUFFER_UPLOAD:
            add_item(
                dataset_name=dataset_name,
                input_item=input_item,
                output_item=output_item,
            )
        else:
            dataset = manage_daily_dataset(
                dataset_name=dataset_name,
                input_item=input_item,
                output_item=output_item,
                metadata=[{}],
            )

        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        headers = properties.headers or {}
        retry_count = headers.get("retry-count", 0)

        if retry_count >= int(MAX_RETRIES):
            logger.info(
                f"[ERROR] Max retries reached ({retry_count}) - invio in error queue"
            )
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
            logger.info(
                f"[RETRY] Retry count {retry_count + 1} - messaggio inviato alla retry queue"
            )
    should_make_experiment = True


channel.basic_consume(queue="main.queue", on_message_callback=callback)

channel.start_consuming()
