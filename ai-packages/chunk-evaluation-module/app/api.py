import asyncio
import json
import logging
import os
import threading
import time
from datetime import datetime
from functools import partial
from queue import Queue
from typing import Any, List, Optional

from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from phoenix.otel import register
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse
from utils.evaluators import layout_fidelity, redundancy_bloat, semantic_choerence
from utils.helpers import (
    async_client,
    client,
    create_experiment,
    execute_experiment,
    make_experiment,
    manage_daily_dataset,
    score,
)

PHOENIX_PROJECT_NAME = os.getenv("PHOENIX_PROJECT_NAME", "test-eval-module")
PHOENIX_COLLECTOR_ENDPOINT = os.getenv(
    "PHOENIX_COLLECTOR_ENDPOINT", "https://phoenix.openk9.io/"
)
POLLING_FREQUENCY = float(os.getenv("POLLING_FREQUENCY", 2))

tracer_provider = register(
    project_name=PHOENIX_PROJECT_NAME,
    auto_instrument=True,
)

logging.basicConfig(level=logging.INFO)

task = partial(score)

METRICS = {
    "semantic_choerence": semantic_choerence,
    "redundancy_bloat": redundancy_bloat,
    "layout_fidelity": layout_fidelity,
}


class InputPayload(BaseModel):
    chunks: List[Any]
    text: str


class InputDataset(BaseModel):
    db_name: Optional[str]
    metrics: Optional[List[str]]


app = FastAPI(
    title="Base FastAPI",
)


@app.get("/")
def health():
    return {"status": "ok"}


@app.post("/add_dataset")
def add_dataset(payload: InputPayload):
    dataset_prefix = os.getenv("DATASET_PREFIX", "dataset")
    experiment_prefix = os.getenv("EXPERIMENT_PREFIX", "experiment")
    chunks = payload.chunks
    text = payload.text
    chonkie_chunks = []
    dataset_name = f"{dataset_prefix}-{datetime.today().strftime('%d-%m-%Y')}"
    experiment_name = (
        f"{experiment_prefix}-{datetime.today().strftime('%d-%m-%Y-%H-%M')}"
    )

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


@app.post("/evaluate")
def evaluate(payload: InputDataset):
    if payload.metrics is None:
        selected_metrics = list(METRICS.values())
    else:
        selected_metrics = []
        for name in payload.metrics:
            if name not in METRICS:
                logging.error(f"Evaluator '{name}' not supported")
            else:
                selected_metrics.append(METRICS[name])

    if payload.db_name:
        logging.info("DB NAME")
        make_experiment(task, selected_metrics, dataset=payload.db_name)
    else:
        logging.info("No DB NAME")
        make_experiment(task, selected_metrics)

    return {"status": "ok", "message": "Process ended"}


@app.post("/evaluate/start")
async def evaluate_part(payload: InputDataset):
    if payload.metrics is None:
        selected_metrics = list(METRICS.values())
    else:
        selected_metrics = []
        for name in payload.metrics:
            if name not in METRICS:
                logging.error(f"Evaluator '{name}' not supported")
            else:
                selected_metrics.append(METRICS[name])

    # Creazione dell'esperimento
    if payload.db_name:
        logging.info("DB NAME")
        experiment = create_experiment(experiment_name="Test", dataset=payload.db_name)
    else:
        logging.info("No DB NAME")
        experiment = create_experiment(experiment_name="Test")

    logging.info(experiment)

    thread = threading.Thread(
        target=execute_experiment,
        args=(experiment[0], task, selected_metrics),
        daemon=True,  # muore con il processo
    )
    thread.start()

    return {"experiment_id": experiment[0]["id"]}


@app.get("/evaluate/stream/{experiment_id}")
async def stream_process(experiment_id: str):
    async def stream_response():
        while True:
            exp = await async_client.experiments.get(experiment_id=experiment_id)
            total = exp["example_count"] * exp["repetitions"]
            completed = exp["successful_run_count"] + exp["failed_run_count"]
            progress = (completed / total * 100) if total else 0

            yield (
                json.dumps(
                    {
                        "event": "progress",
                        "data": {
                            "progress": progress,
                            "completed": completed,
                            "total": total,
                        },
                    }
                )
                + "\n\n"
            )

            if exp["missing_run_count"] == 0:
                yield (
                    json.dumps({"event": "done", "data": {"status": "completed"}})
                    + "\n\n"
                )

                break
            await asyncio.sleep(POLLING_FREQUENCY)

    return EventSourceResponse(stream_response())


@app.post("/db_list")
def get_db_list():
    datasets = client.datasets.list()
    return [dts["name"] for dts in datasets]


@app.post("/get_metrics")
def get_evaluators():
    return {"available_metrics": list(METRICS.keys())}
