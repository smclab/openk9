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

import threading
import logging
from datetime import datetime
from typing import Optional
from requests.auth import HTTPBasicAuth
from logging.config import dictConfig
import json
import time
import base64
import requests
from ..util.utility import call_extraction_api, post_message, map_type
from ..util.log_config import LogConfig

dictConfig(LogConfig().dict())

N_RETRY = 10
RETRY_TIMEOUT = 10
TIMEOUT = 10
N_MAX_ERRORS = 30


class UserExtraction:

    def __init__(self, domain, username, password, timestamp, datasource_id, ingestion_url):

        super(UserExtraction, self).__init__()
        self.extraction_url = domain
        self.username = username
        self.password = password
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.ingestion_url = ingestion_url

        self.status_logger = logging.getLogger("mycoolapp")

        self.url_extract_all = "/o/dml-exporter/users"
        self.url_extract_recent = "/o/dml-exporter/users/modified/" + str(self.timestamp)

        self.basic_auth = HTTPBasicAuth(self.username, self.password)

        self.n_retry = N_RETRY
        self.retry_timeout = RETRY_TIMEOUT
        self.timeout = TIMEOUT
        self.n_max_errors = N_MAX_ERRORS

    def manage_data(self, users):

        users_number = 0
        end_timestamp = datetime.utcnow().timestamp() * 1000

        self.status_logger.info("Posting users")

        for user in users:
            try:
                if user["status"] == 0:

                    user_values = {
                        "userId": user['userId'],
                        "screenName": user['screenName'],
                        "emailAddress": user['emailAddress'],
                        "jobTitle": user['jobTitle'],
                        "male": user['male'],
                        "twitterSn": user['twitterSn'],
                        "skypeSn": user['skypeSn'],
                        "facebookSn": user['facebookSn'],
                        "firstName": user['firstName'],
                        "middleName": user['middleName'],
                        "lastName": user['lastName'],
                        "fullName": user['firstName'] + " " + user['lastName'],
                        "birthday": user['birthday'],
                        "portrait_preview": user["portraitContent"]
                    }

                    addresses_list = user["addresses"]

                    raw_content = user['firstName'] + " " + user['lastName'] + " <" + user[
                        'emailAddress'] + ">"

                    if len(addresses_list) > 0:
                        address_info = addresses_list[0]
                        user_values["street"] = address_info["street"]
                        user_values["zip"] = address_info["zip"]
                        user_values["city"] = address_info["city"]
                        user_values["country"] = address_info["country"]

                        raw_content = raw_content + " " + address_info["city"] + " " + address_info["country"]

                    phones_list = user["phoneNumbers"]
                    if len(phones_list) > 0:
                        user_values["phoneNumber"] = phones_list[0]

                    datasource_payload = {"user": user_values}

                    payload = {
                        "datasourceId": self.datasource_id,
                        "contentId": str(user['userId']),
                        "parsingDate": int(end_timestamp),
                        "rawContent": raw_content,
                        "datasourcePayload": datasource_payload,
                        "resources": {
                            "binaries": []
                        }
                    }

                    try:
                        # post_message(self.ingestion_url, payload, self.timeout)
                        self.status_logger.info(payload)
                        users_number = users_number + 1
                    except requests.RequestException:
                        self.status_logger.error("Problems during posting of users with "
                                                 + str(user['userId']))
                        continue

            except json.decoder.JSONDecodeError:
                continue

        self.status_logger.info("Posting ended")
        self.status_logger.info("Have been posted " + str(users_number) + " users")

        return

    def extract_all(self):

        try:
            users = call_extraction_api(self.extraction_url, self.url_extract_all, self.basic_auth,
                                        self.timeout, self.n_retry, self.retry_timeout)
        except requests.RequestException:
            self.status_logger.error("No users extracted. Extraction process aborted.")
            return

        self.status_logger.info("Getting all users")

        users = json.loads(users)

        self.status_logger.info("Extraction ended")
        self.status_logger.info("Have been extracted " + str(len(users)) + " users")

        self.manage_data(users)
        return

    def extract_recent(self):
        try:
            users = call_extraction_api(self.extraction_url, self.url_extract_recent, self.basic_auth,
                                        self.timeout, self.n_retry, self.retry_timeout)
        except requests.RequestException:
            self.status_logger.error("No users extracted. Extraction process aborted.")
            return

        self.status_logger.info("Getting recent users")

        users = json.loads(users)

        self.status_logger.info("Extraction ended")
        self.status_logger.info("Have been extracted " + str(len(users)) + " users")

        self.manage_data(users)
        return
