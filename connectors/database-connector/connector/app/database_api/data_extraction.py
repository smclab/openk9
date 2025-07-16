import threading
import logging
import datetime
from logging.config import dictConfig
import json
import requests
from sqlalchemy import create_engine, Table, MetaData, text
from sqlalchemy.orm import Session
from sqlalchemy.inspection import inspect
from .util.utility import post_message, validate_model
from .util.log_config import LogConfig

dictConfig(LogConfig().dict())


class DataExtraction(threading.Thread):

    def __init__(self, dialect, driver, username, password, host,
                 port, db, table, columns, where, timestamp, datasource_id, schedule_id, tenant_id, ingestion_url):

        super(DataExtraction, self).__init__()
        self.dialect = dialect
        self.driver = driver
        self.username = username
        self.password = password
        self.host = host
        self.port = port
        self.db = db
        self.table = table
        self.columns = columns
        self.where = where
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id
        self.ingestion_url = ingestion_url

        self.url_extract = self.dialect + "+" + self.driver + "://" + self.username + ":" + self.password + "@" + self.host + ":" + self.port + "/" + self.db

        self.status_logger = logging.getLogger("database-parser")

    def manage_data(self, results, primary_keys):
        row_numbers = 0
        end_timestamp = datetime.datetime.now(datetime.UTC).timestamp() * 1000

        self.status_logger.info("Posting rows")

        for row in results:
            try:
                model = validate_model(dict(row))
                row_values = json.dumps(model)

                datasource_payload = {"row": row_values}

                raw_content = self.dialect + " " + self.username + " " + self.db + " " + self.table + " " + " ".join([str(row[primary_key]) for primary_key in primary_keys])

                payload = {
                    "datasourceId": self.datasource_id,
                    "scheduleId": self.schedule_id,
                    "tenantId": self.tenant_id,
                    "contentId": " ".join([str(row[primary_key]) for primary_key in primary_keys]),
                    "parsingDate": int(end_timestamp),
                    "rawContent": raw_content,
                    "datasourcePayload": datasource_payload,
                    "resources": {
                        "binaries": []
                    }
                }

                try:
                    self.status_logger.info(datasource_payload)
                    # post_message(self.ingestion_url, payload, self.timeout)
                    row_numbers = row_numbers + 1
                except requests.RequestException:
                    self.status_logger.error("Problems during posting of row with id: ")
                    continue

            except json.decoder.JSONDecodeError:
                continue

        self.status_logger.info("Posting ended")
        self.status_logger.info("Have been posted " + str(row_numbers) + " rows")

        return row_numbers

    def extract_recent(self):
        try:
            engine = create_engine(self.url_extract)
            with Session(engine) as session:
                metadata_obj = MetaData()
                table = Table(self.table, metadata_obj, autoload_with=engine)

                query = session.query()

                if self.columns:
                    query = query.add_columns(*[table.c[e] for e in self.columns])
                else:
                    query = query.add_columns(*table.c)

                if self.where:
                    query = query.where(text(self.where))

                results = session.execute(query)
                primary_keys = [key.name for key in inspect(table).primary_key()]
                self.manage_data(results, primary_keys)

        except requests.RequestException:
            self.status_logger.error("No row extracted. Extraction process aborted.")
            return

        self.status_logger.info("Extraction ended")

        return
