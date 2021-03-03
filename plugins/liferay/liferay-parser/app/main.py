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

from flask import Flask, request, jsonify, render_template, make_response
from logging.config import dictConfig
import logging
import threading
import os

from liferay.user.user_extraction import AsyncUserExtraction
from liferay.calendar.calendar_extraction import AsyncCalendarExtraction
from liferay.dml.dml_extraction import AsyncDmlExtraction

app = Flask(__name__)

try:
    app.config.from_json('./static/log/log_config.json')
except FileNotFoundError:
    app.logger.error("Log configuration file is missing")

dictConfig(app.config["LOG_SETTINGS"])

app.config["INGESTION_URL"] = os.environ.get("INGESTION_URL")

status = None


@app.errorhandler(400)
def bad_request_error():
    app.logger.error("Bad request error:" + str())
    return make_response(jsonify({'error': 'Bad request', 'key': str()}), 400)


@app.errorhandler(Exception)
def exception_handler(exception):
    app.logger.error("Exception: " + str(exception))
    logging.exception(str(exception))
    return make_response(
        jsonify({
            'type': str(exception.__class__.__name__),
            'message': str(exception)}),
        500)


@app.route('/')
def get_docs():
    return render_template('swaggerui.html')


@app.route("/execute", methods=["POST"])
def execute():

    class AsyncTask(threading.Thread):

        def __init__(self, domain_, username_, password_, timestamp_, company_id_, datasource_id_, ingestion_url_):
            super(AsyncTask, self).__init__()
            self.user_extraction_task = AsyncUserExtraction(domain_, username_, password_, timestamp_, company_id_,
                                                            datasource_id_, ingestion_url_)

            self.calendar_extraction_task = AsyncCalendarExtraction(domain_, username_, password_, timestamp_,
                                                                    company_id_, datasource_id_, ingestion_url_)

            self.dml_extraction_task = AsyncDmlExtraction(domain_, username_, password_, timestamp_,
                                                          company_id_, datasource_id_, ingestion_url_)

        def run(self):
            global status
            status = self.user_extraction_task.start()
            status = self.calendar_extraction_task.start()
            status = self.dml_extraction_task.start()
            status = self.dml_extraction_task.join()
    
    if request.method == "POST":

        domain = request.json['domain']
        username = request.json['username']
        password = request.json['password']
        try:
            timestamp = request.json['timestamp']
        except KeyError:
            timestamp = 0
        company_id = request.json['companyId']
        datasource_id = request.json['datasourceId']

        async_task = AsyncTask(domain, username, password, timestamp, company_id, datasource_id,
                               app.config["INGESTION_URL"])
        status = async_task.start()
        app.logger.info("Extraction process started")

        return "Extraction process started"


@app.route("/status", methods=["GET"])
def check_status():

    if request.method == "GET":

        global status

        try:
            with open(app.config['LOGGING_FOLDER'] + 'status.log', 'r') as file:
                status_log = file.read().splitlines()
            if len(status_log) > 0:
                response = {
                    "message": status_log[-1],
                    "status": status
                }
                return jsonify(response)
            else:
                response = {
                    "message": "No current status. Probably the process has yet to begin",
                    "status": None
                }
                return jsonify(response)
        except FileNotFoundError:
            response = {
                    "message": "No status log file is present",
                    "status": None
                }
            return jsonify(response)


if __name__ == "__main__":
    app.logger = logging.getLogger("status-logger")
    app.run(host="0.0.0.0", port=80)
