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
import requests
import sys
from datetime import datetime
from typing import Optional
import json

from .imap_client import ImapClient
from .util import parse_email


class AsyncEmailExtraction(threading.Thread):

    def __init__(self, mail_server, port, username, password, timestamp, datasource_id, folder, ingestion_url):
        super(AsyncEmailExtraction, self).__init__()

        self.mail_server = mail_server
        self.port = port
        self.username = username
        self.password = password
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.folder = folder
        self.ingestion_url = ingestion_url
        self.status = "RUNNING"

        self.status_logger = logging.getLogger('status-logger')

        try:
            self.imap = ImapClient(self.mail_server, self.port, self.username, self.password)
        except Exception:
            self.status = "ERROR"
            self.status_logger.error("Connection error: check if mail server and port are correct")
            raise

    def post_message(self, url, payload, timeout):

        try:
            r = requests.post(url, data=payload, timeout=timeout)
            if r.status_code == 200:
                return
            else:
                r.raise_for_status()
        except requests.RequestException as e:
            self.status_logger.error(str(e) + " during request at url: " + str(url))
            raise e

    def extract(self):

        try:
            self.imap.login()
        except Exception:
            self.status = "ERROR"
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
            self.status = "ERROR"
            self.status_logger.error(f"ERROR: Unable to open {self.folder} folder")
            self.imap.logout()
            return

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
                    
                raw_msg, struct_msg, msg_id = parse_email(fetched_msg)

                datasource_payload = {
                    "email": struct_msg
                }

                if struct_msg['date'] > self.timestamp:

                    payload = {
                        "datasourceId": self.datasource_id,
                        "contentId": msg_id,
                        "parsingDate": int(end_timestamp),
                        "rawContent": raw_msg,
                        "datasourcePayload": json.dumps(datasource_payload)
                    }
                    
                    try:
                        self.post_message(self.ingestion_url, payload, 10)
                    except requests.RequestException:
                        self.status_logger.error("Problems during extraction of email with id " + str(num))
                        self.status = "ERROR"
                        continue
                else:
                    self.status_logger.info("Discarded because time is before timestamp") 
                        
        else:
            self.status = "ERROR"
            self.status_logger.error(f"ERROR: Unable to search email in folder: check if query is well done")
        
        self.status = "SUCCESS"
        # when done, you should log out
        self.imap.close()
        self.imap.logout()

        self.status_logger.info("Extraction completed")

    def join(self, timeout: Optional[float] = ...) -> str:
        threading.Thread.join(self)
        return self.status

    def run(self):

        try:
            self.extract()
        except KeyError as error:
            self.status_logger.error("Some problem with key " + str(error) + ". It could be a problem or with some "
                                                                             "config variables badly specified or with"
                                                                             " some key missing in train data file.")
            return

    def start(self) -> str:
        threading.Thread.start(self)
        return self.status
