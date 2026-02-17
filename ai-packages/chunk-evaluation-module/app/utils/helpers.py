import logging
from datetime import datetime, timedelta

from chonkie.types import Chunk
from metrics.layout_fidelity import calculate_lf
from metrics.redundancy_bloat import calculate_rb
from metrics.semantic_choerence_ratio import calculate_scr
from phoenix.client import AsyncClient, Client
from phoenix.client.experiments import (
    async_run_experiment,
    run_experiment,
)

client = Client()
async_client = AsyncClient()


def manage_daily_dataset(dataset_name, input_item, output_item):
    try:
        # Verifica se il dataset esiste
        client.datasets._get_dataset_id_by_name(dataset_name=dataset_name)

        return client.datasets.add_examples_to_dataset(
            dataset=dataset_name,
            inputs=input_item,
            outputs=output_item,
            metadata=[{}],
        )

    except ValueError as e:
        # Lo crea altrimenti
        return client.datasets.create_dataset(
            name=dataset_name,
            inputs=input_item,
            outputs=output_item,
            metadata=[{}],
        )


def score(input) -> dict:
    chunks = input.get("chunks")
    text = input.get("text")
    chonkie_chunks = []

    for chunk in chunks:
        current_dict = chunk.get(list(chunk.keys())[0])
        c_text = current_dict.get("text")
        embedding = current_dict.get("embedding")
        chonkie_chunks.append(Chunk(text=c_text, embedding=embedding))

    results = {}
    results["semantic_choerence"] = calculate_scr(chonkie_chunks)
    results["redundancy_bloat"] = calculate_rb(chonkie_chunks, text)
    results["layout_fidelity"] = calculate_lf(chonkie_chunks, text)

    return results


def delete_all_experiments(dataset):
    experiments = client.experiments.list(dataset_id=dataset.id)
    for experiment in experiments:
        print(experiment)
        client.experiments.delete(experiment_id=experiment["id"])


def delete_projects_experiments():
    projects = client.projects.list()
    logging.info(projects)
    for p in projects:
        name = p["name"]
        if name.startswith("Experiment-") and name != "default":
            try:
                client.projects.delete(project_id=name)
                logging.info(f"Deleted project: {name}")
            except Exception as e:
                logging.info(f"Failed to delete {name}: {e}")


async def async_delete_all_experiments(dataset):
    """Delete all experiments for a dataset"""
    experiments = await async_client.experiments.list(dataset_id=dataset.id)
    for exp in experiments:
        await async_client.experiments.delete(experiment_id=exp["id"])


def make_experiment(task, evaluators, dataset=None, experiment_name=None):
    if dataset is None:
        dataset_name = f"dataset-{datetime.today().strftime('%d-%m-%Y')}"
        dataset = client.datasets.get_dataset(dataset=dataset_name)
    if isinstance(dataset, str):
        dataset = client.datasets.get_dataset(dataset=dataset)
    if experiment_name is None:
        experiment_name = f"experiment-{datetime.today().strftime('%d-%m-%Y-%H-%M')}"
    delete_all_experiments(dataset)
    experiment = run_experiment(
        experiment_name=experiment_name,
        dataset=dataset,
        task=task,
        evaluators=evaluators,
        experiment_metadata={"version": "1.0"},
    )
    delete_projects_experiments()
    return experiment


async def make_async_experiment(task, evaluators, dataset=None, experiment_name=None):
    if dataset is None:
        dataset_name = f"dataset-{datetime.today().strftime('%d-%m-%Y')}"
        dataset = await async_client.datasets.get_dataset(dataset=dataset_name)
    if isinstance(dataset, str):
        dataset = await async_client.datasets.get_dataset(dataset=dataset)
    if experiment_name is None:
        experiment_name = f"experiment-{datetime.today().strftime('%d-%m-%Y-%H-%M')}"
    print("STARTING AN EXPERIMENT")
    await async_delete_all_experiments(dataset)
    experiment = await async_run_experiment(
        experiment_name=experiment_name,
        dataset=dataset,
        task=task,
        evaluators=evaluators,
        experiment_metadata={"version": "1.0"},
    )
    delete_projects_experiments()
    return experiment


def create_experiment(dataset=None, experiment_name=None):
    # Risoluzione del dataset
    if dataset is None:
        dataset_name = f"dataset-{datetime.today().strftime('%d-%m-%Y')}"
        dataset = client.datasets.get_dataset(dataset=dataset_name)
    if isinstance(dataset, str):
        dataset = client.datasets.get_dataset(dataset=dataset)

    # Generazione del nome dell'esperimento
    if experiment_name is None:
        experiment_name = f"experiment-{datetime.today().strftime('%d-%m-%Y-%H-%M')}"

    delete_all_experiments(dataset)
    # Creazione dell'esperimento senza esecuzione
    experiment = client.experiments.create(
        dataset_id=dataset.id,
        experiment_name=experiment_name,
        experiment_metadata={"version": "1.0"},
        # project_name="test-chunkeval-module",
    )
    delete_projects_experiments()
    return experiment, dataset


def execute_experiment(experiment, task, evaluators):
    logging.info("STARTING AN EXPERIMENT")

    # Esecuzione dell'esperimento
    client.experiments.resume_experiment(
        experiment_id=experiment["id"],
        task=task,
        evaluators=evaluators,
        print_summary=False,
    )
