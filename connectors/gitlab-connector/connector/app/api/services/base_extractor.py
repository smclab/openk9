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
    def __init__(self, domain: str, access_token: str, timestamp: int, datasource_id: int, schedule_id: str, tenant_id: str, items_per_page: int, acl_enabled: bool = True) -> None:
        super().__init__()
        self.domain: str = domain
        self.access_token: str = access_token
        self.timestamp: int = timestamp
        self.datasource_id: int = datasource_id
        self.schedule_id: str = schedule_id
        self.tenant_id: str = tenant_id
        self.items_per_page: int = items_per_page
        self.acl_enabled: bool = acl_enabled

        self.ingestion_url: str = ingestion_url
        self.min_timestamp: int = MIN_TIMESTAMP

        self.gl: Gitlab | None = None
        self.email_cache: dict[int, str | None] = {}

        self.status_logger = LogConfig.get_logger(logger_name="extractor")
        self.ingestion_handler: IngestionHandler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id, verbose=True)

    def get_user_email(self, user_id: int) -> str | None:
        """Resolve a GitLab user id to an email, memoized for the whole run.

        The members endpoints do not return emails, so each user requires a
        ``GET /users/:id`` call. Prefers ``email`` (visible to admin tokens)
        and falls back to ``public_email``; returns None when no email is
        retrievable.
        """
        if user_id in self.email_cache:
            return self.email_cache[user_id]

        email: str | None = None
        try:
            user = self.gl.users.get(user_id)
            email = user.attributes.get("email") or user.attributes.get("public_email") or None
        except Exception as e:
            self.status_logger.warning(f"Could not retrieve email for user {user_id}: {str(e)}")

        self.email_cache[user_id] = email
        return email

    def extract(self):
        end_timestamp = datetime.now(timezone.utc).timestamp() * 1000

        try:
            gl = gitlab.Gitlab(url=self.domain, private_token=self.access_token, pagination="keyset", order_by="id",  per_page=self.items_per_page)
            gl.auth()
            self.gl = gl
        except Exception as e:
            self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)
            return

        try:
            time_stamp_date = datetime.fromtimestamp(self.timestamp/1000)

            for data in self.extract_data(gl, time_stamp_date):
                acl = self.get_acl(data)
                self._send(data, acl, end_timestamp)

                for sub_data in self.extract_sub_data(data, time_stamp_date):
                    self._send(sub_data, acl, end_timestamp)

            self.ingestion_handler.post_last(end_timestamp=end_timestamp)
        except Exception as e:
            self.status_logger.error(f"Something went wrong {str(e)}")
            self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)

    def _send(self, data, acl: dict | None, end_timestamp: float) -> None:
        payload = self.manage_data(data, end_timestamp)
        if acl is not None:
            payload["acl"] = acl
        self.ingestion_handler.post_message(payload)

    @abstractmethod
    def extract_data(self, gl: Gitlab, time_stamp_date: datetime) -> Iterator:
        pass

    @abstractmethod
    def extract_sub_data(self, data, time_stamp_date: datetime) -> Iterator:
        pass

    @abstractmethod
    def get_acl(self, data) -> dict | None:
        pass

    @abstractmethod
    def manage_data(self, data, end_timestamp: float) -> dict:
        pass