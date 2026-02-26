import os
import logging
from abc import ABC, abstractmethod
from logging.config import dictConfig
from ..util.log_config import LogConfig

dictConfig(LogConfig().model_dump())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"


class Data(ABC):
    def __init__(self, elements):
        self.elements = elements

        self.status_logger = logging.getLogger("gitlab_logger")

    @abstractmethod
    def get_info(self, element) -> (int, dict, str):
        raise NotImplementedError("Get Info Not Implemented")
