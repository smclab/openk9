from dataclasses import dataclass
from datetime import datetime
from typing import Any, Iterator, Literal
from uuid import UUID

from gitlab import Gitlab
from gitlab.base import RESTObject
from gitlab.v4.objects import Project, ProjectBranch, ProjectCommit, ProjectIssue, ProjectLabel, ProjectMergeRequest, ProjectMilestone

from ..models.util.models_utility import (
    ProjectMinAccessLevelStrings,
    PROJECT_MIN_ACCESS_LEVEL_MAP
)

from .util.utility import (
    format_raw_content,
    get_object_content_id,
    strftime_datetime_filter,
)

from .base_extractor import BaseExtractor


@dataclass
class ProjectData:
    project: Project
    issues: Iterator[ProjectIssue] | None = None
    commits: Iterator[ProjectCommit] | None = None
    branches: Iterator[ProjectBranch] | None = None
    labels: Iterator[ProjectLabel] | None = None
    milestones: Iterator[ProjectMilestone] | None = None
    merge_requests: Iterator[ProjectMergeRequest] | None = None


class ProjectDataExtractor(BaseExtractor):
    def __init__(self, *, 
                domain: str, access_token: str, timestamp: int, datasource_id: int, schedule_id: str, tenant_id: str, items_per_page: int,
                project_list: list | None,
                archived: bool, membership: bool, owned: bool, starred: bool, with_issues_enabled: bool, with_merge_requests_enabled: bool, active: bool, 
                min_access_level: ProjectMinAccessLevelStrings, visibility: Literal['NoFilter', 'public', 'internal', 'private'], 
                
                do_extract_issues: bool, 
                issues_confidential: Literal['confidential', 'public', 'NoFilter'], issue_due_date: Literal['0', 'any', 'today', 'tomorrow', 'overdue', 'week', 'month', 'next_month_and_previous_two_weeks'],
                issue_type: Literal['issue', 'incident', 'test_case', 'task', 'NoFilter'], issue_scope: Literal['created_by_me', 'assigned_to_me', 'all'], issue_state: Literal['opened', 'closed', 'all'],
                
                # Commits
                do_extract_commits: bool, 
                commit_first_parent: bool, commit_with_stats: bool,
                
                # Branches
                do_extract_branches: bool, 
                
                # Labels
                do_extract_labels: bool, 
                label_with_count: bool, label_include_ancestor_groups: bool, label_archived_only: bool,
                
                # Milestones
                do_extract_milestones: bool, 
                milestone_state: Literal['active', 'closed', 'NoFilter'], milestone_include_ancestors: bool,
                
                # Merge Requests
                do_extract_merge_requests: bool, 
                merge_request_environment: str | None, merge_request_scope: Literal['created_by_me', 'assigned_to_me', 'reviews_for_me', 'all'],
                merge_request_state: Literal['all', 'opened', 'closed', 'locked', 'merged'], merge_request_draft: Literal['NoFilter', 'draft', 'non-draft']
            ) -> None:
        super().__init__(domain, access_token, timestamp, datasource_id, schedule_id, tenant_id, items_per_page)
        
        self.project_list = project_list
        
        # Issues
        self.do_extract_issues = do_extract_issues
        self.issue_filters: dict[str, Any] = {
            **({'confidential': issues_confidential == 'confidential'} if issues_confidential != 'NoFilter' else {}),
            'due_date': issue_due_date,
            **({'issue_type': issue_type} if issue_type != 'NoFilter' else {}),
            'scope': issue_scope,
            'state': issue_state,
        }
        
        # Commits
        self.do_extract_commits = do_extract_commits
        self.commit_filters: dict[str, Any] = {
            'first_parent': commit_first_parent,
            'with_stats': commit_with_stats,
        }
        
        # Branches
        self.do_extract_branches = do_extract_branches
        
        # Labels
        self.do_extract_labels = do_extract_labels
        self.label_filters: dict[str, Any] = {
            'with_counts': label_with_count,
            'include_ancestor_groups': label_include_ancestor_groups,
            'archived': label_archived_only,
        }
        
        # Milestones
        self.do_extract_milestones = do_extract_milestones
        self.milestone_filters: dict[str, Any] = {
            **({'state': milestone_state} if milestone_state != 'NoFilter' else {}),
            'include_ancestors': milestone_include_ancestors,
        }
        
        # Merge Requests
        self.do_extract_merge_requests = do_extract_merge_requests
        self.merge_request_filters: dict[str, Any] = {
            **({'environment': merge_request_environment} if merge_request_environment else {}),
            'scope': merge_request_scope,
            'state': merge_request_state,
            **({'draft': merge_request_draft == 'draft'} if merge_request_draft != 'NoFilter' else {})
        }
        
        self.project_filters: dict[str, Any] = {
            'archived': archived,
            'membership': membership,
            **({'min_access_level': PROJECT_MIN_ACCESS_LEVEL_MAP[min_access_level]} if min_access_level != 'NoFilter' else {}),
            'owned': owned,
            'starred': starred,
            **({'visibility': visibility} if visibility != 'NoFilter' else {}),
            'with_issues_enabled': with_issues_enabled,
            'with_merge_requests_enabled': with_merge_requests_enabled,
            'active': active,
        }
    
    def extract_data(self, gl: Gitlab, time_stamp_date: datetime) -> Iterator[Project | ProjectIssue | ProjectCommit | ProjectBranch | ProjectLabel | ProjectMilestone | ProjectMergeRequest]:
        if self.project_list:
            for project_id in self.project_list:
                project = gl.projects.get(id=project_id)
                last_activity: datetime | None = project.attributes.get('last_activity_at')
                if last_activity and last_activity >= time_stamp_date:
                    yield project
            return
        
        datetime_filter: str = strftime_datetime_filter(dt=time_stamp_date)
        projects = gl.projects.list(
            iterator=True, 
            created_after=datetime_filter,
            **self.project_filters
        )
        
        if not any([
                    self.do_extract_issues, self.do_extract_commits, self.do_extract_branches,
                    self.do_extract_labels, self.do_extract_milestones, self.do_extract_merge_requests,
                ]):
            return projects
        
        for project in projects:
            if self.do_extract_issues:
                yield from project.issues.list(
                    iterator=True, 
                    updated_after=datetime_filter,
                    **self.issue_filters
                )
                
            if self.do_extract_commits:
                yield from project.commits.list(
                    iterator=True, 
                    since=datetime_filter, 
                    **self.commit_filters
                )
            
            if self.do_extract_branches:
                for branch in project.branches.list(
                            sort='desc', order_by='updated',
                            iterator=True,
                        ):
                    if committed_date := branch.attributes.get('commit', {}).get('committed_date', None):
                        commit_date = datetime.fromisoformat(committed_date)
                        if commit_date >= time_stamp_date:
                            yield branch
                        else:
                            break
            
            if self.do_extract_labels:
                yield from project.labels.list(iterator=True, **self.label_filters)
                
            if self.do_extract_milestones:
                yield from project.milestones.list(
                    iterator=True, 
                    updated_after=datetime_filter,
                    **self.milestone_filters
                )
                
            if self.do_extract_merge_requests:
                self.merge_request_filters['updated_after'] = time_stamp_date
                yield from project.mergerequests.list(
                    iterator=True, 
                    updated_after=datetime_filter, 
                    **self.merge_request_filters
                )
            
            yield project
    
    def manage_data(self, data: Project | ProjectIssue | ProjectCommit | ProjectBranch | ProjectLabel | ProjectMilestone | ProjectMergeRequest, end_timestamp: float) -> dict[str, Any]:
        info: dict[str, Any] = data.attributes
        
        content_id: UUID = get_object_content_id(resource=data)
        
        
        if isinstance(data, Project):
            raw_content_elements = [info['description'] or '', info['name'] or '', *info['topics'],]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'project'
            
        elif isinstance(data, ProjectIssue):
            raw_content_elements = [info['state'] or '', info['title'] or '', info['description'], *info['labels'],]
            raw_content = format_raw_content(model=''.join(raw_content_elements))
            payload_key = 'issue'
            
        elif isinstance(data, ProjectCommit):
            raw_content_elements = [info['title'] or '', info['author_name'] or '', info['author_email'], info['committer_name'], info['committer_email'], info['message'],]
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