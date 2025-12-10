import json
import os
import random
import time
from functools import partial

import numpy as np
import pandas as pd
import phoenix as px
import pika
from chonkie.types import Chunk
from metrics.layout_fidelity import calculate_lf
from metrics.redundancy_bloat import calculate_rb
from metrics.semantic_choerence_ratio import calculate_scr
from phoenix.experiments import run_experiment
from phoenix.experiments.evaluators import create_evaluator


def semantic_choerence(output: dict):
    results = output["semantic_choerence"]
    forward = results["forward"]
    backward = results["backward"]
    mean = np.mean([forward, backward])
    return mean


def redundancy_bloat(output: dict):
    results = output["redundancy_bloat"]
    redundancy = results["redundancy"]
    return redundancy


def layout_fidelity(output: dict):
    results = output["layout_fidelity"]
    coverage = results["coverage"]
    return coverage


def score(input, chonkie_chunks, text) -> dict:
    results = {}
    results["semantic_choerence"] = calculate_scr(chonkie_chunks)
    results["redundancy_bloat"] = calculate_rb(chonkie_chunks, text)
    results["layout_fidelity"] = calculate_lf(chonkie_chunks, text)

    return results


EVALUATORS = [semantic_choerence, redundancy_bloat, layout_fidelity]
################################################################
rabbit_host = os.getenv("RABBITMQ_HOST", "localhost")
rabbit_user = os.getenv("RABBITMQ_USER", "user")
rabbit_pass = os.getenv("RABBITMQ_PASS", "pass")

print(
    "ENV:",
    {
        k: os.getenv(k)
        for k in [
            "PHOENIX_COLLECTOR_ENDPOINT",
            "PHOENIX_OTEL_EXPORTER_ENDPOINT",
            "PHOENIX_API_KEY",
            "PHOENIX_PROJECT_NAME",
        ]
    },
)

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
    data = json.loads(body.decode("utf-8"))
    chunks = data.get("chunks")
    text = data.get("text")
    chonkie_chunks = []
    rand_name = text[0:25] + str(random.randint(0, 100))
    df_cose = {
        "name": [rand_name],
        "expected": [
            {
                "semantic_choerence": {"forward": [1.0, 1.2], "backward": [1.0, 1.2]},
                "redundancy_bloat": {"redundancy": [0.0, 0.25]},
                "layout_fidelity": {"coverage": 1.0},
            }
        ],
    }

    for chunk in chunks:
        current_dict = chunk.get(list(chunk.keys())[0])
        c_text = current_dict.get("text")
        embedding = current_dict.get("embedding")
        chonkie_chunks.append(Chunk(text=c_text, embedding=embedding))

    task = partial(score, chonkie_chunks=chonkie_chunks, text=text)

    df = pd.DataFrame(df_cose)

    dataset = px.Client().upload_dataset(
        dataset_name=rand_name,
        dataframe=df,
        input_keys=["name"],
        output_keys=["expected"],
    )
    experiment = run_experiment(
        dataset=dataset,
        task=task,
        evaluators=EVALUATORS,
        experiment_metadata={"version": "1.0"},
    )
    # push_chunk_score(affiot["coverage"])

    # with open("/app/dati/results.json", "w") as file:
    #     json.dump(results, file, indent=4, ensure_ascii=False)


channel.basic_consume(queue="chunks", on_message_callback=callback, auto_ack=True)

channel.start_consuming()
