import os
import logging
import threading
from datetime import datetime
from enum import Enum
from itertools import tee

import gitlab
import requests

from .data.branch.branch import BranchData
from .data.commit.commit import CommitData
from .data.issue.issue import IssueData
from .data.project.project import ProjectData
from .data.merge_request.merge_request import MergeRequestData
from .data.milestone.milestone import MilestoneData
from .data.label.label import LabelData
from .data.user.user import UserData
from logging.config import dictConfig
from .data.util.log_config import LogConfig
from .data.util.utility import logger, post_message

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://ingestion:8080/api/ingestion/v1/ingestion/"

dictConfig(LogConfig().model_dump())


MIN_TIMESTAMP = 100000000


class DataExtractionType(str, Enum):
    USER = 'User'
    PROJECT = 'Project'
    PROJECT_ISSUE = 'Project Issue'
    PROJECT_COMMIT = 'Project Commit'
    PROJECT_BRANCH = 'Project Branch'
    PROJECT_LABELS = 'Project Labels'
    PROJECT_MILESTONE = 'Project Milestone'
    PROJECT_MERGE_REQUEST = 'Project Merge Request'


class DataExtraction(threading.Thread):
    def __init__(self, domain, access_token, types, items_per_page, project_list, timestamp, datasource_id, schedule_id, tenant_id):

        super(DataExtraction, self).__init__()
        self.domain = domain
        self.access_token = access_token
        self.types = types
        self.project_list = project_list
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id

        self.status_logger = logging.getLogger("gitlab_logger")

        self.items_per_page = items_per_page
        self.min_timestamp = MIN_TIMESTAMP

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

    def manage_data(self, data, end_timestamp):

        self.status_logger.info("Posting " + data.__class__.__name__)

        count = 0

        for element in data.elements:

            content_id, datasource_payload, raw_content = data.get_info(element)

            payload = {
                "datasourceId": self.datasource_id,
                "scheduleId": self.schedule_id,
                "tenantId": self.tenant_id,
                "contentId": content_id,
                "parsingDate": int(end_timestamp),
                "rawContent": raw_content,
                "datasourcePayload": datasource_payload,
            }

            try:

                # self.status_logger.info(datasource_payload)
                post_message(ingestion_url, payload, 10)
                count = count + 1

            except requests.RequestException:

                self.status_logger.error("Problems during posting of + " + str(type(data)) + " with "
                                         + str(content_id))

                continue

        self.status_logger.info("Posting " + data.__class__.__name__ + " ended")

        return count

    def extract_recent(self):

        end_timestamp = datetime.utcnow().timestamp() * 1000

        gl = gitlab.Gitlab(url=self.domain, private_token=self.access_token, pagination="keyset", order_by="id",  per_page=self.items_per_page)

        time_stamp_date = datetime.fromtimestamp(self.timestamp/1000)

        if DataExtractionType.USER in self.types:
            users = gl.users.list(iterator=True)
            user_count = self.manage_data(UserData(users), end_timestamp)

            self.status_logger.info(str(user_count) + " users have been posted")

        if DataExtractionType.PROJECT in self.types:
            projects, projects_list = tee(gl.projects.list(owned=True, iterator=True, last_activity_after=time_stamp_date))
            project_count = self.manage_data(ProjectData(projects), end_timestamp)

            self.status_logger.info(str(project_count) + " projects have been posted")

            if len(self.project_list) > 0:
                projects_list = [project for project in projects_list if project.id in self.project_list]

            for project in projects_list:

                project_name = project.name

                if DataExtractionType.PROJECT_ISSUE in self.types:
                    try:
                        issues = project.issues.list(iterator=True, updated_after=time_stamp_date)
                        issues_count = self.manage_data(IssueData(issues), end_timestamp)

                        self.status_logger.info(str(issues_count) + " issues have been posted for project: " + project_name)
                    except gitlab.GitlabListError as e:
                        self.status_logger.error(e)
                        self.status_logger.error("Problems with project " + str(project.id) + " in issues")

                if DataExtractionType.PROJECT_COMMIT in self.types:
                    try:
                        since = datetime.fromtimestamp(self.min_timestamp).isoformat() if self.timestamp < self.min_timestamp else time_stamp_date.isoformat() + 'Z'
                        commits = project.commits.list(iterator=True, since=since)
                        commits_count = self.manage_data(CommitData(commits), end_timestamp)

                        self.status_logger.info(str(commits_count) + " commits have been posted for project: " + project_name)
                    except gitlab.GitlabListError:
                        self.status_logger.error("Problems with project " + str(project.id) + " in commits")

                if DataExtractionType.PROJECT_BRANCH in self.types:
                    try:
                        branches = project.branches.list(iterator=True)
                        branches_count = self.manage_data(BranchData(branches), end_timestamp)

                        self.status_logger.info(str(branches_count) + " branches have been posted for project: " + project_name)
                    except gitlab.GitlabListError:
                        self.status_logger.error("Problems with project " + str(project.id) + " in branches")

                if DataExtractionType.PROJECT_LABELS in self.types:
                    try:
                        labels = project.labels.list(iterator=True)
                        labels_count = self.manage_data(LabelData(labels), end_timestamp)

                        self.status_logger.info(str(labels_count) + " labels have been posted for project: " + project_name)
                    except gitlab.GitlabListError:
                        self.status_logger.error("Problems with project " + str(project.id) + " in labels")

                if DataExtractionType.PROJECT_MILESTONE in self.types:
                    try:
                        milestones = project.milestones.list(iterator=True)
                        milestones_count = self.manage_data(MilestoneData(milestones), end_timestamp)

                        self.status_logger.info(str(milestones_count) + " milestones have been posted for project: " + project_name)
                    except gitlab.GitlabListError:
                        self.status_logger.error("Problems with project " + str(project.id) + " in milestones")

                if DataExtractionType.PROJECT_MERGE_REQUEST in self.types:
                    try:
                        merge_requests = project.mergerequests.list(iterator=True, updated_after=time_stamp_date)
                        merge_requests_count = self.manage_data(MergeRequestData(merge_requests), end_timestamp)

                        self.status_logger.info(str(merge_requests_count) + " merge requests have been posted for project: " + project_name)
                    except gitlab.GitlabListError:
                        self.status_logger.error("Problems with project " + str(project.id) + " in merge requests")

        self.post_last(end_timestamp)