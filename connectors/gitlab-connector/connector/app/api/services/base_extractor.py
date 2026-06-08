from gitlab.v4.objects import exc


from abc import ABC, abstractmethod
from collections.abc import Iterator
from datetime import datetime, timezone
import logging
import os
from typing import Any, Final

from gitlab import Gitlab, gitlab
from gitlab.base import RESTObject

from .util.ingestion import IngestionHandler
from api.util.log_config import LogConfig


ingestion_url = os.environ.get("INGESTION_URL") or "http://ingestion:8080/api/ingestion/v1/ingestion/"

MIN_TIMESTAMP: Final[int] = 100000000


class BaseExtractor(ABC):
    def __init__(self, domain: str, access_token: str, timestamp: int, datasource_id: int, schedule_id: str, tenant_id: str, items_per_page: int) -> None:
        super().__init__()
        self.domain: str = domain
        self.access_token: str = access_token
        self.timestamp: int = timestamp
        self.datasource_id: int = datasource_id
        self.schedule_id: str = schedule_id
        self.tenant_id: str = tenant_id
        self.items_per_page: int = items_per_page
        
        self.ingestion_url: str = ingestion_url
        self.min_timestamp: int = MIN_TIMESTAMP
        
        self.status_logger = LogConfig.get_logger(logger_name="extractor")
        self.ingestion_handler: IngestionHandler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id, verbose=True)
    
    def extract(self):
        end_timestamp = datetime.now(timezone.utc).timestamp() * 1000

        try:
            gl = gitlab.Gitlab(url=self.domain, private_token=self.access_token, pagination="keyset", order_by="id",  per_page=self.items_per_page)
        except Exception as e:
            self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)
            return
        
        try:
            time_stamp_date = datetime.fromtimestamp(self.timestamp/1000)
            
            for data in self.extract_data(gl, time_stamp_date):
                payload = self.manage_data(data, end_timestamp)
                self.ingestion_handler.post_message(payload)

            self.ingestion_handler.post_last(end_timestamp=end_timestamp)
        except Exception as e:
            self.status_logger.error("Something went wrong")
            self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)

        
    @abstractmethod
    def extract_data(self, gl: Gitlab, time_stamp_date: datetime)-> Iterator:
        pass
    
    @abstractmethod
    def manage_data(self, data, end_timestamp: float) -> dict:
        pass