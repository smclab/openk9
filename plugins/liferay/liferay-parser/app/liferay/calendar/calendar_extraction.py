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
import requests
from ..util.utility import call_extraction_api, post_message, map_type
from ..util.log_config import LogConfig

dictConfig(LogConfig().dict())

N_RETRY = 10
RETRY_TIMEOUT = 10
TIMEOUT = 10
N_MAX_ERRORS = 30


class CalendarExtraction(threading.Thread):

    def __init__(self, domain, username, password, timestamp, datasource_id, ingestion_url):

        super(CalendarExtraction, self).__init__()
        self.extraction_url = domain
        self.username = username
        self.password = password
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.ingestion_url = ingestion_url

        self.status = "RUNNING"

        self.status_logger = logging.getLogger("mycoolapp")

        self.basic_auth = HTTPBasicAuth(self.username, self.password)

        self.n_retry = N_RETRY
        self.retry_timeout = RETRY_TIMEOUT
        self.timeout = TIMEOUT
        self.n_max_errors = N_MAX_ERRORS

    def manage_data(self, calendar_bookings):

        calendar_bookings_number = 0
        end_timestamp = datetime.utcnow().timestamp() * 1000

        self.status_logger.info("Posting calendar bookings")

        for calendar_booking in calendar_bookings:
            try:
                if calendar_booking["status"] == 0:

                    start_time = datetime.fromtimestamp(int(calendar_booking['startTime']) / 1000) \
                        .strftime("%d-%m-%Y %H:%M:%S")
                    end_time = datetime.fromtimestamp(int(calendar_booking['endTime']) / 1000) \
                        .strftime("%d-%m-%Y %H:%M:%S")

                    calendar_values = {
                        "calendarBookingId": calendar_booking['calendarBookingId'],
                        "description": calendar_booking['description'],
                        "location": calendar_booking['location'],
                        "title": calendar_booking['title'],
                        "startTime": start_time,
                        "endTime": end_time,
                        "allDay": calendar_booking['allDay']
                    }

                    datasource_payload = {"calendar": calendar_values}

                    raw_content = str(calendar_booking['title']) \
                                  + " " + calendar_booking['description'] + " " \
                                  + str(calendar_booking['location'])

                    payload = {
                        "datasourceId": self.datasource_id,
                        "contentId": str(calendar_booking['calendarBookingId']),
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
                        calendar_bookings_number = calendar_bookings_number + 1
                    except requests.RequestException:
                        self.status_logger.error("Problems during posting of calendar booking with "
                                                 + str(calendar_booking['calendarBookingId']))
                        continue

            except json.decoder.JSONDecodeError:
                continue

        self.status_logger.info("Posting ended")
        self.status_logger.info("Have been posted " + str(calendar_bookings_number) + " calendars bookings")

        return

    def extract_all(self):

        try:
            calendar_bookings = call_extraction_api(self.extraction_url, '/o/dml-exporter/calendarBookings',
                                                    self.basic_auth, self.timeout, self.n_retry, self.retry_timeout)
        except requests.RequestException:
            self.status_logger.error("No calendar bookings extracted. Extraction process aborted.")
            return

        self.status_logger.info("Getting all calendar bookings")

        calendar_bookings = json.loads(calendar_bookings)

        self.status_logger.info("Extraction ended")
        self.status_logger.info("Have been extracted " + str(len(calendar_bookings)) + " calendars bookings")

        self.manage_data(calendar_bookings)
        return

    def extract_recent(self):
        try:
            calendar_bookings = call_extraction_api(self.extraction_url, '/o/dml-exporter/calendarBookings/modified/'
                                                    + str(self.timestamp), self.basic_auth, self.timeout,
                                                    self.n_retry, self.retry_timeout)
        except requests.RequestException:
            self.status_logger.error("No calendar bookings extracted. Extraction process aborted.")
            return

        self.status_logger.info("Getting recent calendar bookings")

        calendar_bookings = json.loads(calendar_bookings)

        self.status_logger.info("Extraction ended")
        self.status_logger.info("Have been extracted " + str(len(calendar_bookings)) + " calendars bookings")

        self.manage_data(calendar_bookings)
        return
