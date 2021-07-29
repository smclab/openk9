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
import json
import time
import base64
import requests
from PIL import Image
from io import BytesIO

N_RETRY = 10
RETRY_TIMEOUT = 10
TIMEOUT = 10
N_MAX_ERRORS = 30


class AsyncDmlExtraction(threading.Thread):

    def __init__(self, domain, username, password, timestamp, company_id, datasource_id, ingestion_url):

        super(AsyncDmlExtraction, self).__init__()
        self.extraction_url = domain
        self.username = username
        self.password = password
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.company_id = company_id
        self.ingestion_url = ingestion_url
        self.status = "RUNNING"

        self.status_logger = logging.getLogger("status-logger")

        self.basic_auth = HTTPBasicAuth(self.username, self.password)

        self.n_retry = N_RETRY
        self.retry_timeout = RETRY_TIMEOUT
        self.timeout = TIMEOUT
        self.n_max_errors = N_MAX_ERRORS

        try:
            with open("./liferay/dml/mapping_config.json") as config_file:
                self.config = json.load(config_file)
        except (FileNotFoundError, json.decoder.JSONDecodeError):
            self.status = "ERROR"
            self.status_logger.error("Ingestion configuration file is missing or there is some error in it.")
            return

        self.type_mapping = self.config["TYPE_MAPPING"]

    def call_extraction_api(self, url):

        for i in range(self.n_retry):
            try:
                r = requests.get(self.extraction_url + url, auth=self.basic_auth, timeout=self.timeout)
                if r.status_code == 200:
                    return r.text
                else:
                    r.raise_for_status()
            except requests.RequestException as e:
                self.status_logger.warning("Retry number " + str(i) + " " + str(e) + " during request at url: "
                                           + str(self.extraction_url + url))
                if i < self.n_retry-1:
                    time.sleep(self.retry_timeout)
                    continue
                else:
                    self.status_logger.error(str(e) + " during request at url: " + str(self.extraction_url + url))
                    raise e

    def post_message(self, url, payload, timeout):

        try:
            r = requests.post(url, json=payload, timeout=timeout)
            if r.status_code == 200:
                return
            else:
                r.raise_for_status()
        except requests.RequestException as e:
            self.status_logger.error(str(e) + " during request at url: " + str(url))
            raise e

    def get_as_base64(self, url):
        
        if url.startswith("http"):
            url = url.replace("http://localhost:8085", "http://liferay-portal:8080")
            url = url.replace("http://localhost:8080", "http://liferay-portal:8080")
        response = requests.get(url, auth=self.basic_auth, stream=True)
        try:
            im = Image.open(response.raw)
            size = 128, 128
            im.thumbnail(size, Image.ANTIALIAS)
            buffered = BytesIO()
            im.save(buffered, "PNG")
        except IOError:
            self.status_logger.error("cannot create thumbnail")
            return
        uri = ("data:" + response.headers['Content-Type'] + ";" + "base64,"
               + base64.b64encode(buffered.getvalue()).decode("utf-8"))
        uri = uri.replace("http://localhost:8085", "http://liferay-portal:8080")
        return uri

    def map_type(self, content_type):

        try:
            document_type = self.type_mapping[content_type]
            return document_type
        except KeyError:
            return None

    def extract(self):

        errors_number = 0

        try:
            sites_response = self.call_extraction_api("/o/dml-exporter/sites")
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
                documents_response = self.call_extraction_api('/o/dml-exporter/site/' + site['siteId']
                                                              + '/documents/modified?timestamp=' + str(self.timestamp))
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
                        text_response = self.call_extraction_api('/o/dml-exporter/document/' + str(doc_id))
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

                        if len(preview_urls) > 0:
                            base64_preview_url = self.get_as_base64(preview_urls[0])
                        else:
                            base64_preview_url = ''

                        preview_urls = [element.replace("http://localhost:8080", "http://dev-projectq.smc.it/liferay") for element in preview_urls]

                        file_values = {
                            "lastModifiedDate": document['modifiedDate'],
                            "path": document['path']
                        }

                        document_values = {
                            "title": document['title'],
                            "contentType": document['mimeType'],
                            "content": text['content'],
                            "previewURLs": preview_urls,
                            "previewUrl": base64_preview_url,
                            "URL": document['URL'].replace("http://localhost:8080", "http://dev-projectq.smc.it/liferay")
                        }

                        acl_values = {
                            "allow": {	
                                "roles": document['roles']
                            }
                        }

                        type_values = {}

                        datasource_payload = {"file": file_values, "document": document_values, "acl": acl_values}

                        extension = document['extension']
                        if self.map_type(extension) is not None:
                            datasource_payload[self.map_type(extension)] = type_values

                        payload = {
                            "datasourceId": self.datasource_id,
                            "contentId": str(doc_id),
                            "parsingDate": int(end_timestamp),
                            "rawContent": raw_msg,
                            "datasourcePayload": datasource_payload
                        }
                                                
                        try:
                            self.post_message(self.ingestion_url, payload, 10)
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

    def join(self, timeout: Optional[float] = ...) -> str:
        threading.Thread.join(self)
        return self.status

    def run(self):
        try:
            self.extract()
        except KeyError as error:
            self.status = "ERROR"
            self.status_logger.error("Some problem with key " + str(error) + ". It could be a problem or with some "
                                                                             "config variables badly specified or with"
                                                                             " some key missing in train data file or "
                                                                             "in api response.")
            return

    def start(self) -> str:
        threading.Thread.start(self)
        return self.status
