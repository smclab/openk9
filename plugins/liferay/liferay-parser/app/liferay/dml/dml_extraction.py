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
import requests
from ..util.utility import call_extraction_api, post_message, map_type
from ..util.log_config import LogConfig

dictConfig(LogConfig().dict())

N_RETRY = 10
RETRY_TIMEOUT = 10
TIMEOUT = 10
N_MAX_ERRORS = 30


class DmlExtraction:

    def __init__(self, domain, username, password, timestamp, datasource_id, ingestion_url):

        super(DmlExtraction, self).__init__()
        self.extraction_url = domain
        self.username = username
        self.password = password
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.ingestion_url = ingestion_url

        self.status_logger = logging.getLogger("mycoolapp")

        self.basic_auth = HTTPBasicAuth(self.username, self.password)

        self.n_retry = N_RETRY
        self.retry_timeout = RETRY_TIMEOUT
        self.timeout = TIMEOUT
        self.n_max_errors = N_MAX_ERRORS

    def extract(self):

        errors_number = 0

        try:
            sites_response = call_extraction_api(self.extraction_url, "/o/dml-exporter/sites", self.basic_auth,
                                                      self.timeout, self.n_retry, self.retry_timeout)
        except requests.RequestException:
            self.status_logger.error("No list of sites extracted. Extraction process aborted.")
            self.status = "ERROR"
            return

        site_list = json.loads(sites_response)

        if len(site_list) > 0:
            self.status_logger.info(str(len(site_list)) + " sites identified")
        else:
            self.status = "ERROR"
            self.status_logger.error("No sites to read documents. Check error log for errors in get "
                                     "request or check options field if contains wrong keys for sites")
            return

        documents_number = 0

        start_datetime = datetime.fromtimestamp(self.timestamp/1000)
        start_date = datetime.strftime(start_datetime, "%d-%b-%Y")

        self.status_logger.info("Getting documents from " + start_date)

        end_timestamp = datetime.utcnow().timestamp()*1000

        for i, site in enumerate(site_list):

            try:
                documents_response = call_extraction_api(self.extraction_url, '/o/dml-exporter/site/' + site['siteId']
                                                              + '/documents/modified?timestamp=' + str(self.timestamp),
                                                              self.basic_auth,
                                                              self.timeout, self.n_retry, self.retry_timeout
                                                              )
            except requests.RequestException:
                self.status_logger.error("Error during extraction of document's list for site with id: "
                                         + str(site['siteId']) + ". Extraction process aborted.")
                self.status = "ERROR"
                return

            documents_response = json.loads(documents_response)

            documents = documents_response['documents']
            if len(documents) > 0:
                for j, document in enumerate(documents):
                    doc_id = document['id']
                    try:
                        self.status_logger.info("Extracting document " + str(j + 1) + "/" + str(len(documents))
                                                + " of site " + str(i + 1) + "/" + str(len(site_list)) + " with id "
                                                + str(site['siteId']))
                        text_response = call_extraction_api(self.extraction_url,
                                                                 '/o/dml-exporter/document/' + str(doc_id),
                                                                 self.basic_auth,
                                                                 self.timeout, self.n_retry, self.retry_timeout
                                                                 )
                    except requests.RequestException:
                        errors_number += 1
                        if errors_number > self.n_max_errors:
                            self.status_logger.error("Maximum number of errors reached. Extraction process aborted.")
                            self.status = "ERROR"
                            return
                        else:
                            self.status_logger.error("Error during extraction of document with id: " + str(doc_id))
                            continue

                    try:
                        text = json.loads(text_response)
                        raw_msg = text['content'].replace('\t', ' ').replace("\n", " ").replace("\\", " ")\
                            .replace("..", "").replace("__", "").replace(";", "").replace(",", "")
                        raw_msg = raw_msg

                        preview_urls = document['previewURLs']
                        try:
                            base_preview_url = preview_urls[0]
                        except IndexError:
                            base_preview_url = None


                        file_values = {
                            "lastModifiedDate": document['modifiedDate'],
                            "path": document['path']
                        }

                        document_values = {
                            "title": document['title'],
                            "contentType": document['mimeType'],
                            "content": text['content'],
                            "previewURLs": preview_urls,
                            "previewUrl": base_preview_url,
                            "URL": document['URL']
                        }

                        acl_values = {
                            "allow": {	
                                "roles": document['roles']
                            }
                        }

                        type_values = {}

                        datasource_payload = {"file": file_values, "document": document_values, "acl": acl_values}

                        extension = document['extension']
                        if map_type(extension) is not None:
                            datasource_payload[map_type(extension)] = type_values

                        payload = {
                            "datasourceId": self.datasource_id,
                            "contentId": str(doc_id),
                            "parsingDate": int(end_timestamp),
                            "rawContent": raw_msg,
                            "datasourcePayload": datasource_payload,
                            "resources": {
                                "binaries": []
                            }
                        }
                                                
                        try:
                            self.status_logger.info(payload)
                            # post_message(self.ingestion_url, payload, 10)
                        except requests.RequestException:
                            self.status_logger.error("Problems during extraction of document with id " + str(doc_id))
                            self.status = "ERROR"
                            continue 
                        
                        documents_number = documents_number + 1
                    except json.decoder.JSONDecodeError:
                        continue
            else:
                self.status_logger.info("No documents founded for site of site " + str(i + 1) + "/"
                                        + str(len(site_list)) + " with id " + str(site['siteId']) + ". Next site.")

        self.status_logger.info("Extraction ended")
        self.status_logger.info("Have been extracted " + str(documents_number) + " documents")
        return
