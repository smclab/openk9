from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any, Iterator, Literal

from gitlab import Gitlab
from gitlab.base import RESTObject
from gitlab.exceptions import GitlabListError
from gitlab.v4.objects import Project, ProjectBranch, ProjectCommit, ProjectIssue, ProjectLabel, ProjectMergeRequest, ProjectMilestone

from ..models.util.models_utility import (
    ProjectMinAccessLevelStrings,
    PROJECT_MIN_ACCESS_LEVEL_MAP,
    BoolFilter,
    FilterComposite,
)

from .util.utility import (
    format_raw_content,
    get_object_content_id,
    strftime_datetime_filter,
)

from .base_extractor import BaseExtractor


# GitLab access level for GUEST. Members with at least this level are
# considered authorized on a private project.
GUEST_ACCESS_LEVEL = 10


class ProjectDataExtractor(BaseExtractor):
    def __init__(self, *, 
                domain: str, access_token: str, 
                timestamp: int, datasource_id: int, schedule_id: str, tenant_id: str, 
                
                items_per_page: int,
                acl_enabled: bool,
                project_list: list | None,
                
                archived: BoolFilter, membership: BoolFilter, owned: BoolFilter, starred: BoolFilter, with_issues_enabled: BoolFilter, with_merge_requests_enabled: BoolFilter, active: BoolFilter, 
                min_access_level: ProjectMinAccessLevelStrings, visibility: Literal['NoFilter', 'public', 'internal', 'private'], 
                
                do_extract_issues: bool, 
                issues_confidential: BoolFilter, 
                issue_due_date: Literal['NoFilter', '0', 'any', 'today', 'tomorrow', 'overdue', 'week', 'month', 'next_month_and_previous_two_weeks'],
                issue_type: Literal['issue', 'incident', 'test_case', 'task', 'NoFilter'], 
                issue_scope: Literal['NoFilter', 'created_by_me', 'assigned_to_me', 'all'], 
                issue_state: Literal['NoFilter', 'opened', 'closed', 'all'],
                
                # Commits
                do_extract_commits: bool, 
                commit_first_parent: BoolFilter, commit_with_stats: BoolFilter,
                
                # Branches
                do_extract_branches: bool, 
                
                # Labels
                do_extract_labels: bool, 
                label_with_count: BoolFilter, label_include_ancestor_groups: BoolFilter, label_archived_only: BoolFilter,
                
                # Milestones
                do_extract_milestones: bool, 
                milestone_state: Literal['active', 'closed', 'NoFilter'], milestone_include_ancestors: BoolFilter,
                
                # Merge Requests
                do_extract_merge_requests: bool, 
                merge_request_environment: str | None, merge_request_draft: BoolFilter, 
                merge_request_scope: Literal['NoFilter', 'created_by_me', 'assigned_to_me', 'reviews_for_me', 'all'],
                merge_request_state: Literal['NoFilter', 'all', 'opened', 'closed', 'locked', 'merged'], 
            ) -> None:
        super().__init__(domain, access_token, timestamp, datasource_id, schedule_id, tenant_id, items_per_page, acl_enabled)

        self.project_list = project_list
        
        # Issues
        self.do_extract_issues = do_extract_issues
        self.issue_filters: dict[str, Any] = FilterComposite.compose_params(
            filters={
                'confidential': issues_confidential,
                'due_date': issue_due_date,
                'issue_type': issue_type,
                'scope': issue_scope,
                'state': issue_state,
            })
        
        # Commits
        self.do_extract_commits = do_extract_commits
        self.commit_filters: dict[str, Any] = FilterComposite.compose_params(
            filters={
                'first_parent': commit_first_parent,
                'with_stats': commit_with_stats,
            })

        # Branches
        self.do_extract_branches = do_extract_branches
        
        # Labels
        self.do_extract_labels = do_extract_labels
        self.label_filters: dict[str, Any] = FilterComposite.compose_params(
            filters={
                'with_counts': label_with_count,
                'include_ancestor_groups': label_include_ancestor_groups,
                'archived': label_archived_only,
            })
        
        # Milestones
        self.do_extract_milestones = do_extract_milestones
        self.milestone_filters: dict[str, Any] = FilterComposite.compose_params(
            filters={
                'state': milestone_state,
                'include_ancestors': milestone_include_ancestors,
            })
        
        # Merge Requests
        self.do_extract_merge_requests = do_extract_merge_requests
        self.merge_request_filters: dict[str, Any] = FilterComposite.compose_params(
            filters={
                'environment': merge_request_environment,
                'scope': merge_request_scope,
                'state': merge_request_state,
                'draft': merge_request_draft
            })
        
        self.project_filters: dict[str, Any] = FilterComposite.compose_params(
            filters={
                'archived': archived,
                'membership': membership,
                'min_access_level': PROJECT_MIN_ACCESS_LEVEL_MAP.get(min_access_level, 'NoFilter'),
                'owned': owned,
                'starred': starred,
                'visibility': visibility,
                'with_issues_enabled': with_issues_enabled,
                'with_merge_requests_enabled': with_merge_requests_enabled,
                'active': active,
            })
    
    def extract_data(self, gl: Gitlab, time_stamp_date: datetime) -> Iterator[Project]:
        if time_stamp_date.tzinfo is None:
            time_stamp_date = time_stamp_date.replace(tzinfo=timezone.utc)

        datetime_filter: str = strftime_datetime_filter(dt=time_stamp_date)
        if self.project_list:
            for project_id in self.project_list:
                project = gl.projects.get(id=project_id)
                if last_activity := project.attributes.get('last_activity_at'):
                    if datetime.fromisoformat(last_activity) >= time_stamp_date:
                        yield project
        else:
            yield from gl.projects.list(
                iterator=True,
                created_after=datetime_filter,
                **self.project_filters
            )

    def extract_sub_data(self, data: Project, time_stamp_date: datetime) -> Iterator[ProjectIssue | ProjectCommit | ProjectBranch | ProjectLabel | ProjectMilestone | ProjectMergeRequest]:
        if time_stamp_date.tzinfo is None:
            time_stamp_date = time_stamp_date.replace(tzinfo=timezone.utc)

        datetime_filter: str = strftime_datetime_filter(dt=time_stamp_date)
        project = data

        if self.do_extract_issues:
            self.status_logger.info("Processing issues...")
            try:
                yield from project.issues.list(
                    iterator=True,
                    updated_after=datetime_filter,
                    order_by='updated_at',
                    **self.issue_filters,
                )
            except GitlabListError as e:
                self.status_logger.warning(f"Could not extract issues: {str(e)}")

        if self.do_extract_commits:
            self.status_logger.info("Processing commits...")
            try:
                yield from project.commits.list(
                    iterator=True,
                    since=datetime_filter,
                    **self.commit_filters
                )
            except GitlabListError as e:
                self.status_logger.warning(f"Could not extract commits: {str(e)}")

        if self.do_extract_branches:
            self.status_logger.info("Processing branches...")
            try:
                for branch in project.branches.list(iterator=True,):
                    if committed_date := branch.attributes.get('commit', {}).get('committed_date', None):
                        if datetime.fromisoformat(committed_date) >= time_stamp_date:
                            yield branch
            except GitlabListError as e:
                self.status_logger.warning(f"Could not extract branches: {str(e)}")


        if self.do_extract_labels:
            self.status_logger.info("Processing labels...")
            try:
                yield from project.labels.list(iterator=True, **self.label_filters)
            except GitlabListError as e:
                self.status_logger.warning(f"Could not extract labels: {str(e)}")

        if self.do_extract_milestones:
            self.status_logger.info("Processing milestones...")
            try:
                yield from project.milestones.list(
                    iterator=True,
                    updated_after=datetime_filter,
                    **self.milestone_filters
                )
            except GitlabListError as e:
                self.status_logger.warning(f"Could not extract milestones: {str(e)}")

        if self.do_extract_merge_requests:
            self.status_logger.info("Processing merge requests...")
            try:
                yield from project.mergerequests.list(
                    iterator=True,
                    updated_after=datetime_filter,
                    order_by='updated_at',
                    **self.merge_request_filters
                )
            except GitlabListError as e:
                self.status_logger.warning(f"Could not extract merge requets: {str(e)}")

    def get_acl(self, data: Project) -> dict | None:
        if not self.acl_enabled:
            return None

        visibility = data.attributes.get('visibility')
        if visibility == 'public':
            # Omit acl: the ingestion contract expects Map<String, List<String>>,
            # and the datasource IndexWriter defaults a missing/empty acl to
            # {"public": true}, the only ACL that is world-visible (incl. anonymous).
            return None
        if visibility == 'internal':
            return {"roles": ["internal"]}

        emails: list[str] = []
        try:
            for member in data.members_all.list(iterator=True):
                if member.attributes.get('access_level', 0) >= GUEST_ACCESS_LEVEL:
                    email = self.get_user_email(member.id)
                    if email and email not in emails:
                        emails.append(email)
        except GitlabListError as e:
            self.status_logger.warning(f"Could not extract members for project {data.get_id()}: {str(e)}")

        return {"roles": emails}

    def manage_data(self, data: Project | ProjectIssue | ProjectCommit | ProjectBranch | ProjectLabel | ProjectMilestone | ProjectMergeRequest, end_timestamp: float) -> dict[str, Any]:
        info: dict[str, Any] = data.attributes
        
        content_id: str = get_object_content_id(resource=data)
        
        
        if isinstance(data, Project):
            raw_content_elements = [info['description'] or '', info['name'] or '', *[topic or '' for topic in info['topics']],]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'project'
            
        elif isinstance(data, ProjectIssue):
            raw_content_elements = [info['state'] or '', info['title'] or '', info['description'] or '', *[label or '' for label in info['labels']],]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'issue'
            
        elif isinstance(data, ProjectCommit):
            raw_content_elements = [info['title'] or '', info['author_name'] or '', info['author_email'] or '', info['committer_name'] or '', info['committer_email'] or '', info['message'] or '',]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'commit'
            
        elif isinstance(data, ProjectBranch):
            raw_content_elements = [info['name'] or '', info['web_url'] or '',]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'branch'
        
        elif isinstance(data, ProjectMergeRequest):
            raw_content_elements = [info['title'] or '', info['description'] or '',]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'mergeRequest'
            
        elif isinstance(data, ProjectLabel):
            raw_content_elements = [info['name'] or '', info['color'] or '', info['description'] or '',]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'label'
            
        elif isinstance(data, ProjectMilestone):
            raw_content_elements = [info['title'] or '', info['description'] or '',]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'milestone'
        
        return {
            "datasourceId": self.datasource_id,
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "contentId": content_id,
            "parsingDate": int(end_timestamp),
            "rawContent": raw_content,
            "datasourcePayload": {payload_key: info},
        }