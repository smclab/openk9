"""
Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

import logging
from datetime import datetime
from logging.config import dictConfig

import pandas as pd
import requests
from minio import Minio
from minio.error import MinioException

from .log_config import LogConfig
from .utility import IngestionHandler

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


class ExcelMinioExtractor:

    def __init__(self, host, port, access_key, secret_key, bucket_name, columns, prefix, datasource_payload_key, 
                datasource_id, timestamp, schedule_id, tenant_id, ingestion_url):

        super(ExcelMinioExtractor, self).__init__()
        self.datasource_id = datasource_id
        self.ingestion_url = ingestion_url
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id
        self.access_key = access_key
        self.secret_key = secret_key
        self.bucket_name = bucket_name
        self.columns = columns
        self.prefix = prefix
        self.datasource_payload_key = datasource_payload_key
        self.timestamp = timestamp
        self.url = str(host) + ":" + str(port)

        self.ingestion_handler = IngestionHandler(self.ingestion_url, self.datasource_id, self.schedule_id, self.tenant_id)
        self.status_logger = logging.getLogger("status-logger")

    def extract_data(self):

        try:

            client = Minio(self.url, self.access_key, self.secret_key, secure=False)

            end_timestamp = datetime.utcnow().timestamp() * 1000

            objects = client.list_objects(self.bucket_name)

        except MinioException as e:
            self.ingestion_handler.post_halt(exception=e, end_timestamp=None)
            return

        for obj in objects:

            metadata = client.stat_object(self.bucket_name, obj.object_name)

            binaries = []

            y = client.get_object(self.bucket_name, obj.object_name)

            data = y.data

            df = pd.read_excel(data)

            df = df.drop(columns=df.select_dtypes(include=['datetime64']).columns.tolist())
            df = df.drop(columns=df.select_dtypes(include=['float64']).columns.tolist())

            df = df.astype(str)

            if len(self.columns) > 0:
                df = df[self.columns]

            df_dict = df.to_dict('records')

            self.status_logger.info("Sending " + str(len(df_dict)) + "elements")

            for i, element in enumerate(df_dict):

                datasource_payload = {
                    self.datasource_payload_key: element
                }

                payload = {
                    "datasourceId": self.datasource_id,
                    "contentId": i,
                    "parsingDate": int(end_timestamp),
                    "rawContent": str(element).lower(),
                    "datasourcePayload": datasource_payload,
                    "resources": {
                        "binaries": []
                    },
                    "scheduleId": self.schedule_id,
                    "tenantId": self.tenant_id
                }

                try:
                    # self.status_logger.info(datasource_payload)
                    self.ingestion_handler.post_message(payload=payload)
                    self.status_logger.info("Sent " + str(i + 1) + " element of " + str(len(df_dict)) + " elements")
                except requests.RequestException as e:
                    self.status_logger.error("Something went wrong on payload ingestion")
                    self.status_logger.error(e)
                    self.ingestion_handler.post_halt(exception=e, end_timestamp=end_timestamp)

            self.ingestion_handler.post_last(end_timestamp=end_timestamp)
