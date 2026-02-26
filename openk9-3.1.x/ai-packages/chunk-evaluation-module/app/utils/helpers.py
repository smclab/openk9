from datetime import datetime, timedelta

from chonkie.types import Chunk
from metrics.layout_fidelity import calculate_lf
from metrics.redundancy_bloat import calculate_rb
from metrics.semantic_choerence_ratio import calculate_scr
from phoenix.client import Client
from phoenix.experiments import run_experiment

client = Client()


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
        client.experiments.delete(experiment_id=experiment["id"])


def make_experiment(task, evaluators, dataset=None, experiment_name=None):
    if dataset is None:
        dataset_name = f"dataset-{datetime.today().strftime('%d-%m-%Y')}"
        dataset = client.datasets.get_dataset(dataset=dataset_name)
    if experiment_name is None:
        experiment_name = f"experiment-{datetime.today().strftime('%d-%m-%Y-%H-%M')}"
    print("STARTING AN EXPERIMENT")
    delete_all_experiments(dataset)
    experiment = run_experiment(
        experiment_name=experiment_name,
        dataset=dataset,
        task=task,
        evaluators=evaluators,
        experiment_metadata={"version": "1.0"},
    )
