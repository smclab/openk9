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
import json
import requests
import os

app = Flask(__name__)

try:
    app.config.from_json('./static/log/log_config.json')
except FileNotFoundError:
    app.logger.error("Log configuration file is missing")

dictConfig(app.config["LOG_SETTINGS"])

app.config["INGESTION_URL"] = os.environ.get("INGESTION_URL")


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


def post_message(url, payload, timeout):
    try:
        r = requests.post(url, data=payload, timeout=timeout)
        if r.status_code == 200:
            return r.json()
        else:
            r.raise_for_status()
    except requests.RequestException as e:
        app.logger.error(str(e) + " during request at url: " + str(url))
        raise e


@app.route('/')
def get_docs():
    return render_template('swaggerui.html')


@app.route("/execute-web-crawler", methods=["POST"])    
def execute_web():
    
    if request.method == "POST":
        
        try:
            start_urls = request.json["startUrls"]
        except KeyError:
            app.logger.error("No url from start crawling")
            return "No url from start crawling"

        try:
            allowed_domains = request.json["allowedDomains"]
        except KeyError:
            allowed_domains = []

        try:
            allowed_paths = request.json["allowedPaths"]
        except KeyError:
            allowed_paths = []
        
        try:
            excluded_paths = request.json["excludedPaths"]
        except KeyError:
            excluded_paths = []
        
        try:
            depth = request.json["depth"]
        except KeyError:
            depth = 10
        timestamp = request.json["timestamp"]
        datasource_id = request.json["datasourceId"]
        try:
            follow = request.json["follow"]
        except KeyError:
            follow = True
        try:
            max_length = request.json["max_length"]
        except KeyError:
            max_length = 10000
        try:
            page_count = request.json["page_count"]
        except KeyError:
            page_count = 1000

        payload = {
            "project": "web_crawler",
            "spider": "SimpleWebCrawler",
            "start_urls": json.dumps(start_urls),
            "allowed_domains": json.dumps(allowed_domains),
            "allowed_paths": json.dumps(allowed_paths),
            "excluded_paths": json.dumps(excluded_paths),
            "timestamp": timestamp,
            "datasource_id": datasource_id,
            "ingestion_url": app.config["INGESTION_URL"],
            "setting": "DEPTH_LIMIT=" + str(depth),
            "setting": "CLOSESPIDER_PAGECOUNT=" + str(page_count),
            "follow": follow,
            "max_length": max_length
        }

        if timestamp == 0:
            response = post_message("http://localhost:6800/schedule.json", payload, 10)
            
            if response["status"] == 'ok':
                app.logger.info("Crawling process started with job " + str(response["jobid"]))
                return "Crawling process started with job " + str(response["jobid"])
            else:
                app.logger.error(response)
                return response
        else:
            app.logger.error("Timestamp bigger than 0")
            return "Timestamp bigger than 0"


@app.route("/execute-sitemap-crawler", methods=["POST"])    
def execute_sitemap():
    
    if request.method == "POST":
        
        try:
            sitemap_urls = request.json["sitemapUrls"]
        except KeyError:
            app.logger.error("No sitemap founded")
            return "No url from start crawling"

        try:
            allowed_domains = request.json["allowedDomains"]
        except KeyError:
            allowed_domains = []

        try:
            sitemap_rules = request.json["sitemapRules"]
        except KeyError:
            sitemap_rules = []
        
        try:
            depth = request.json["depth"]
        except KeyError:
            depth = 10
        timestamp = int(request.json["timestamp"])
        datasource_id = request.json["datasourceId"]
        try:
            follow = request.json["follow"]
        except KeyError:
            follow = False
        try:
            max_length = request.json["max_length"]
        except KeyError:
            max_length = 10000
        try:
            page_count = request.json["page_count"]
        except KeyError:
            page_count = 1000

        payload = {
            "project": "web_crawler",
            "spider": "SitemapWebCrawler",
            "sitemap_urls": json.dumps(sitemap_urls),
            "allowed_domains": json.dumps(allowed_domains),
            "sitemap_rules": json.dumps(sitemap_rules),
            "timestamp": timestamp,
            "datasource_id": datasource_id,
            "ingestion_url": app.config["INGESTION_URL"],
            "setting": "DEPTH_LIMIT=" + str(depth),
            "setting": "CLOSESPIDER_PAGECOUNT=" + str(page_count),
            "follow": follow,
            "max_length": max_length
        }

        if timestamp == 0:
            response = post_message("http://localhost:6800/schedule.json", payload, 10)

            if response["status"] == 'ok':
                app.logger.info("Crawling process started with job " + str(response["jobid"]))
                return "Crawling process started with job " + str(response["jobid"])
            else:
                app.logger.error(response)
                return response
        else:
            app.logger.error("Timestamp bigger than 0")
            return "Timestamp bigger than 0"


@app.route("/cancel-job", methods=["POST"])    
def cancel_job():

    if request.method == "POST":
        
        try:
            job = request.json["job"]
        except KeyError:
            app.logger.error("No job with this id founded")
            return "No job with this id founded"

        payload = {
                "project": "web_crawler",
                "job": str(job)
            }

        response = post_message("http://localhost:6800/cancel.json", payload, 10)

        if response["status"] == 'ok':
            app.logger.info("Cancelled job with id " + str(job))
            return "Cancelled job with id " + str(job)
        else:
            app.logger.error(response)
            return response


if __name__ == "__main__":
    app.logger = logging.getLogger("status-logger")
    app.run(host="0.0.0.0", port=80)
