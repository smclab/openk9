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
import json
import threading
import logging
import requests
import sys
import os
from datetime import datetime
from typing import Optional
from logging.config import dictConfig
from .util.log_config import LogConfig

dictConfig(LogConfig().dict())

from .imap_client import ImapClient
from imap.util.utility import parse_email, post_message

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"


class AsyncEmailExtraction(threading.Thread):

    def __init__(self, mail_server, port, username, password, timestamp, datasource_id, folder, schedule_id, tenant_id,
                 index_acl, get_attachments, additional_metadata):
        super(AsyncEmailExtraction, self).__init__()

        self.mail_server = mail_server
        self.port = port
        self.username = username
        self.password = password
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.folder = folder
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id
        self.index_acl = index_acl
        self.get_attachments = get_attachments
        self.additional_metadata = dict(additional_metadata)

        self.status_logger = logging.getLogger('email-logger')

        try:
            self.imap = ImapClient(self.mail_server, self.port, self.username, self.password)
        except Exception:
            self.status_logger.error("Connection error: check if mail server and port are correct")
            raise

    def post_last(self, end_timestamp):

        payload = {
            "datasourceId": self.datasource_id,
            "parsingDate": int(end_timestamp),
            "contentId": None,
            "rawContent": None,
            "datasourcePayload": {},
            "resources": {
                "binaries": []
            },
            "acl": {
                "type": ["deleted"]
            },
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "last": True
        }

        post_message(ingestion_url, payload)

    def extract(self):

        try:
            self.imap.login()
        except Exception:
            self.status_logger.error("Problem during login: check credentials")
            return

        try:
            start_datetime = datetime.fromtimestamp(self.timestamp/1000)
        except ValueError as error:
            self.status_logger.error("Valuer error:  " + str(error) + " at line " + str(sys.exc_info()[-1].tb_lineno))
            return
            
        start_date = datetime.strftime(start_datetime, "%d-%b-%Y")
        # retrieve messages from a given sender

        end_timestamp = datetime.utcnow().timestamp()*1000
        
        resp = self.imap.select_folder(self.folder)
        if resp != 'OK':
            self.status_logger.error(f"ERROR: Unable to open {self.folder} folder")
            self.imap.logout()
            return

        email_posted = 0

        mbox_response, msg_nums = self.imap.search('(SINCE "' + start_date + '")')

        if mbox_response == 'OK':
            email_count = len(msg_nums[0].split())
            self.status_logger.info("There are " + str(email_count) + " new messages from " + start_date)

            for i, num in enumerate(msg_nums[0].split()):

                self.status_logger.info("Extracting and parsing email " + str(i+1) + " of " + str(email_count)
                                        + " from " + str(self.username))
                ret_val, fetched_msg = self.imap.get_message(num)

                if ret_val != 'OK':
                    self.status_logger.error('ERROR getting message', num)
                    continue

                raw_msg, struct_msg, msg_id, binaries, acl_list = parse_email(fetched_msg)

                datasource_payload = {
                    "email": struct_msg
                }

                self.status_logger.info(self.additional_metadata)

                for key, value in self.additional_metadata.items():
                    datasource_payload[key] = value

                body = struct_msg['body']

                if struct_msg['date'] > self.timestamp:

                    payload = {
                        "datasourceId": self.datasource_id,
                        "contentId": str(msg_id).replace("<", "").replace(">", ""),
                        "parsingDate": int(end_timestamp),
                        "rawContent": body,
                        "datasourcePayload": datasource_payload,
                        "scheduleId": self.schedule_id,
                        "tenantId": self.tenant_id,
                    }

                    if self.index_acl:
                        payload["acl"] = {
                                "email": acl_list
                        }
                    else:
                        payload["acl"] = {}

                    if self.get_attachments:
                        payload["resources"] = {
                            "binaries": binaries,
                            "splitBinaries": True
                        }
                    else:
                        payload["resources"] = {
                            "binaries": [],
                            "splitBinaries": True
                        }

                    try:
                        post_message(ingestion_url, payload, 10)
                        # self.status_logger.info(payload)
                        email_posted += 1
                    except requests.RequestException:
                        self.status_logger.error("Problems during extraction of email with id " + str(num))
                        continue
                else:
                    self.status_logger.info("Discarded because time is before timestamp")
                        
        else:
            self.status_logger.error(f"ERROR: Unable to search email in folder: check if query is well done")
        
        # when done, you should log out
        self.imap.close()
        self.imap.logout()

        self.post_last(end_timestamp)

        self.status_logger.info("Extraction completed")
        self.status_logger.info(str(email_posted) + " emails have been posted")
