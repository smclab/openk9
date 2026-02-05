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
from io import BytesIO
from logging.config import dictConfig

import pandas as pd
from xlrd import XLRDError

from .base_extractor import BaseMinioExtractor
from .log_config import LogConfig
from .utility import FileExtractor

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


class ExcelMinioExtractor(BaseMinioExtractor):

    def __init__(self, host, port, access_key, secret_key, bucket_name, columns, prefix, datasource_payload_key, 
                datasource_id, timestamp, schedule_id, tenant_id, ingestion_url, do_try_extract_header):

        super(ExcelMinioExtractor, self).__init__(host, port, access_key, secret_key, bucket_name, prefix,
                                                  datasource_id, timestamp, schedule_id, tenant_id, ingestion_url)

        self.columns = columns
        self.datasource_payload_key = datasource_payload_key
        self.do_try_extract_header = do_try_extract_header
        self.file_extractor = FileExtractor(self.do_try_extract_header)

    def manage_data(self, client, obj, end_timestamp):
        y = client.get_object(self.bucket_name, obj.object_name)
        if y.status != 200:
            self.status_logger.warning(f"Could not extract file {obj.object_name}.")
            return

        data = y.data

        df = self.file_extractor.extract_file(obj.object_name, data)
        if df is None:
            return

        df = df.astype(str)

        if len(self.columns) > 0:
            df = df[self.columns]

        df_dict = df.to_dict('records')

        self.status_logger.info("Sending " + str(len(df_dict)) + " elements")

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
            except Exception as e:
                raise e
