import numpy as np


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
