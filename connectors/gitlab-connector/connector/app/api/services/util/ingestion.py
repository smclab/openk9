from datetime import datetime, timezone

import requests

from api.util.log_config import LogConfig


def get_end_timestamp() -> float:
    return datetime.now(timezone.utc).timestamp() * 1000


class IngestionHandler:
    def __init__(self, ingestion_url, datasource_id, schedule_id, tenant_id, verbose: bool = True):
        self.ingestion_url = ingestion_url
        self.datasource_id = datasource_id
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id

        self.status_logger = LogConfig.get_logger("ingetsion-handler")
        self.verbose_logger = self.status_logger.getChild("verbose")
        self.verbose_logger.disabled = not verbose

    def post_message(self, payload):
        self.status_logger.info("[post_message]: POSTING")
        self.verbose_logger.info(f"[post_message][payload]: {payload}")
        r = requests.post(self.ingestion_url, json=payload, timeout=20)
        r.raise_for_status()
        self.status_logger.info("[post_message]: COMPLETED")

    def post_halt(self, exception: Exception, end_timestamp: float | None = None):
        self.status_logger.info("[post_halt]: POSTING")
        self.verbose_logger.error("[post_halt]: Exception info:", exc_info=exception)

        end_timestamp = end_timestamp if end_timestamp else get_end_timestamp()

        payload = {
            "datasourceId": self.datasource_id,
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "contentId": -1,
            "parsingDate": int(end_timestamp),
            "rawContent": str(exception),
            "datasourcePayload": {},
            "resources": {"binaries": []},
            "type": "HALT"
        }
        try:
            self.post_message(payload)
        except requests.exceptions.RequestException as e:
            self.status_logger.error(f"[post_halt]: Error on [post_message]: {e}")
            self.verbose_logger.exception("[post_halt]: Exception info:")

            self.status_logger.warning("[post_halt]: Could not send HALT message.")

        self.status_logger.info("[post_halt]: COMPLETED")

    def post_last(self, end_timestamp: float | None = None):
        self.status_logger.info("[post_last]: POSTING")
        end_timestamp = end_timestamp if end_timestamp else get_end_timestamp()

        payload = {
            "datasourceId": self.datasource_id,
            "parsingDate": int(end_timestamp),
            "contentId": None,
            "rawContent": None,
            "datasourcePayload": {},
            "resources": {
                "binaries": []
            },
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "last": True
        }
        self.post_message(payload)

        self.status_logger.info("[post_last]: COMPLETED")

