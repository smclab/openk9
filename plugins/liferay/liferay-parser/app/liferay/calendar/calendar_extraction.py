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
import requests

N_RETRY = 10
RETRY_TIMEOUT = 10
TIMEOUT = 10
N_MAX_ERRORS = 30


class AsyncCalendarExtraction(threading.Thread):

    def __init__(self, domain, username, password, timestamp, company_id, datasource_id, ingestion_url):

        super(AsyncCalendarExtraction, self).__init__()
        self.domain = domain
        self.username = username
        self.password = password
        self.timestamp = timestamp
        self.companyId = company_id
        self.datasourceId = datasource_id
        self.ingestion_url = ingestion_url

        self.status = "RUNNING"

        self.status_logger = logging.getLogger("status-logger")

        self.basic_auth = HTTPBasicAuth(self.username, self.password)

        self.n_retry = N_RETRY
        self.retry_timeout = RETRY_TIMEOUT
        self.timeout = TIMEOUT
        self.n_max_errors = N_MAX_ERRORS

    def call_extraction_api(self, url, payload):

        for i in range(self.n_retry):
            try:
                r = requests.post(self.domain + url, auth=self.basic_auth, data=payload, timeout=self.timeout)
                if r.status_code == 200:
                    return r.text
                else:
                    r.raise_for_status()
            except requests.RequestException as e:
                self.status_logger.warning("Retry number " + str(i) + " " + str(e) + " during request at url: "
                                           + str(self.domain + url))
                if i < self.n_retry-1:
                    time.sleep(self.retry_timeout)
                    continue
                else:
                    self.status_logger.error(str(e) + " during request at url: " + str(self.domain + url))
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

    def extract(self):

        try:
            payload = {
                "companyId": self.companyId,
                "keywords": '',
                "calendarResourceIds": '',
                "andOperator": 'true',
                "groupIds": '' 
            }
            calendar_count = self.call_extraction_api("/api/jsonws/calendar.calendar/search-count", payload)
        except requests.RequestException:
            self.status_logger.error("No calendar count extracted. Extraction process aborted.")
            self.status = "ERROR"
            return

        self.status_logger.info(str(calendar_count) + " calendars founded")

        calendar_bookings_number = 0

        start_datetime = datetime.fromtimestamp(self.timestamp/1000)
        start_date = datetime.strftime(start_datetime, "%d-%b-%Y")

        self.status_logger.info("Getting calendar bookings created from " + start_date)

        end_timestamp = datetime.utcnow().timestamp()*1000

        start = 0

        while start < int(calendar_count):

            end = start + 10

            payload = {
                "companyId": self.companyId,
                "start": start,
                "end": end,
                "keywords": '',
                "calendarResourceIds": '',
                "andOperator": 'true',
                "groupIds": '',
                "-orderByComparator": ''
            }

            try:
                calendar_response = self.call_extraction_api('/api/jsonws/calendar.calendar/search', payload)
            except requests.RequestException:
                self.status_logger.error("Error during extraction of calendars from " + str(start)
                                         + " to " + str(end) + ". Extraction process aborted.")
                self.status = "ERROR"
                return

            calendar_list = json.loads(calendar_response)

            for calendar in calendar_list:

                start = start + 1

                self.status_logger.info("Extracting calendar " + str(start) + " of " + str(calendar_count))

                payload = {
                    "calendarId": calendar["calendarId"],
                    "statuses": 0
                }

                try:
                    calendar_bookings_response = \
                        self.call_extraction_api('/api/jsonws/calendar.calendarbooking/get-calendar-bookings', payload)
                except requests.RequestException:
                    self.status_logger.error("Error during extraction of calendars from "
                                             + str(start) + " to " + str(end) + ". Extraction process aborted.")
                    self.status = "ERROR"
                    return
                
                calendar_bookings = json.loads(calendar_bookings_response)

                for calendar_booking in calendar_bookings:

                    try:
                        if calendar_booking["modifiedDate"] > self.timestamp and calendar_booking["status"] == 0:

                            calendar_values = {
                                "calendarBookingId": calendar_booking['calendarBookingId'],
                                "description": calendar_booking['description'],
                                "location": calendar_booking['location'],
                                "title": calendar_booking['title'],
                                "titleCurrentValue": calendar_booking['titleCurrentValue'],
                                "startTime": calendar_booking['startTime'],
                                "endTime": calendar_booking['endTime'],
                                "allDay": calendar_booking['allDay']
                            }

                            datasource_payload = {"calendar": calendar_values}

                            payload = {
                                "datasourceId": self.datasourceId,
                                "contentId": str(calendar_booking['calendarBookingId']),
                                "parsingDate": int(end_timestamp),
                                "rawContent": str(calendar_booking['titleCurrentValue'])
                                                  + " " + str(calendar_booking['description']),
                                "datasourcePayload": json.dumps(datasource_payload)
                            }
                            
                            self.status_logger.info(str(calendar_booking['titleCurrentValue'])
                                                  + " " + str(calendar_booking['description']))
                            try:
                                self.post_message(self.ingestion_url, payload, 10)
                            except requests.RequestException:
                                self.status_logger.error("Problems during extraction of calendar booking with "
                                                         + str(calendar_booking['calendarBookingId']))
                                self.status = "ERROR"
                                continue 
                            
                            calendar_bookings_number = calendar_bookings_number + 1
                            
                    except json.decoder.JSONDecodeError:
                        continue
            
        self.status_logger.info("Extraction ended")
        self.status_logger.info("Have been extracted " + str(calendar_bookings_number) + " calendars bookings")
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
