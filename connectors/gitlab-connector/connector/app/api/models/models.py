from enum import Enum, IntEnum, StrEnum
from typing import Literal

from pydantic import BaseModel, Field
from pydantic.alias_generators import to_snake

from ..services import (
    users_extractor,
    projects_extractor
)

from .util.models_utility import (
    ProjectMinAccessLevelStrings,
)


class HealthStatus(StrEnum):
    UP = "UP"
    DOWN = "DOWN"
    UNKNOWN = "UNKNOWN"


class HealthCheck(BaseModel):
    """Response model to validate and return when performing a health check.

    Args:
        status (HealthStatus): Health status enum
    """
    status: HealthStatus = HealthStatus.UNKNOWN


class GitlabBaseExtractionRequest(BaseModel):
    """GitLabBaseExtractionRequest: base gitlab request class
    
    Args:
        domain (str): Gitlab domain (es: https://git.smc.it)
        accessToken (str): Gitlab accessToken
        itemsPerPage (int): Gitlab api pagination. (es: 20, default: 100)
        
        timestamp (int): Extracts items after timestamp. Provided by OpenK9. (es. 0)
        datasourceId (int): OpenK9 datasourceId. Provided by OpenK9. (es. 0)
        scheduleId (str): OpenK9 scheduleId. Provided by OpenK9 as UUID. (es. "0")
        tenantId (str): OpenK9 tenantId. Provided by OpenK9 as UUID. (es. "0")
    """
    domain: str = Field(..., frozen=True)
    accessToken: str = Field(..., frozen=True)
    itemsPerPage: int  = Field(default=100, frozen=True)
    timestamp: int = Field(..., frozen=True)
    datasourceId: int = Field(..., frozen=True)
    scheduleId: str = Field(..., frozen=True)
    tenantId: str = Field(..., frozen=True)


class UsersGitlabExtractionRequest(GitlabBaseExtractionRequest):
    """UsersGitlabExtractionRequest: Request model to /users/execute endpoint

    Args:
        filterActive (bool): Filters only active users. Default is false.
        filterExternal (bool): Filters only external users. Default is false.
        filterBlocked (bool): Filters only blocked users. Default is false.
        filterHuman (bool): Filters only regular users that are not bot or internal users. Default is false.
        excludeActive (bool): Filters only non active users. Default is false.
        excludeExternal (bool): Filters only non external users. Default is false.
        excludeHumans (bool): Filters only bot or internal users. Default is false.
        excludeInternal (bool): Filters only non internal users. Default is false.
    """
    
    filterActive: bool = Field(default=False, frozen=True)
    filterExternal: bool = Field(default=False, frozen=True)
    filterBlocked: bool = Field(default=False, frozen=True)
    filterHuman: bool = Field(default=False, frozen=True)
    excludeActive: bool = Field(default=False, frozen=True)
    excludeExternal: bool = Field(default=False, frozen=True)
    excludeHumans: bool = Field(default=False, frozen=True)
    excludeInternal: bool = Field(default=False, frozen=True)
    
    def create_extractor(self) -> users_extractor.UserDataExtractor:
        return users_extractor.UserDataExtractor(
            domain=self.domain,
            access_token=self.accessToken,
            timestamp=self.timestamp,
            datasource_id=self.datasourceId,
            schedule_id=self.scheduleId,
            tenant_id=self.tenantId,
            items_per_page=self.itemsPerPage,
            filter_active=self.filterActive,
            filter_external=self.filterExternal,
            filter_blocked=self.filterBlocked,
            filter_human=self.filterHuman,
            exclude_active=self.excludeActive,
            exclude_external=self.excludeExternal,
            exclude_humans=self.excludeHumans,
            exclude_internal=self.excludeInternal,
        )


class ProjectsGitlabExtractionRequest(GitlabBaseExtractionRequest):
    """UsersGitlabExtractionRequest: Request model to /projects/execute endpoint
    
    Lists all projects on the instance accessible to the authenticated user. 
    Unauthenticated requests return only public projects with a limited subset of attributes.

    Args:
        projectList (list | None): List of project ids to be extracted. Default is None.
        archived (bool): Limit by archived status. Default is false.
        membership (bool): Limit by projects that the current user is a member of. Default is false.
        owned (bool): Limit by projects explicitly owned by the current user. Default is false.
        starred (bool): Limit by projects starred by the current user. Default is false.
        withIssuesEnabled (bool): Limit by enabled issues feature. Default is false.
        withMergeRequestsEnabled (bool): Limit by enabled merge requests feature. Default is false.
        active (bool): Limit by projects that are not archived and not marked for deletion. Default is false.
        minAccessLevel (int | None): Limit to projects where the current user has at least the specified access level. Possible values: 5 (Minimal access), 10 (Guest), 15 (Planner), 20 (Reporter), 25 (Security Manager), 30 (Developer), 40 (Maintainer), or 50 (Owner). Default is None.
        visibility: (str | None): Limit by visibility public, internal, or private. Default is None
        doExtractIssues: (bool): Should also extract Issues. Default is True.
        doExtractCommits: (bool): Should also extract Commits. Default is True.
        doExtractBranches: (bool): Should also extract Branches. Default is True.
        doExtractLabels: (bool): Should also extract Labels. Default is True.
        doExtractMilestones: (bool): Should also extract Milestone. Default is True.
        doExtractMergeRequests: (bool): Should also extract Merge Requests. Default is True.

        TODO: 
            - Add extract + Filters
            - Add filters to porject extract 
            - Test
    """
    
    projectList: list | None = Field(default=None, frozen=True)
    archived: bool = Field(default=False, frozen=True)
    membership: bool = Field(default=False, frozen=True)
    owned: bool = Field(default=False, frozen=True)
    starred: bool = Field(default=False, frozen=True)
    withIssuesEnabled: bool = Field(default=False, frozen=True)
    withMergeRequestsEnabled: bool = Field(default=False, frozen=True)
    active: bool = Field(default=False, frozen=True)
    minAccessLevel: ProjectMinAccessLevelStrings = Field(default='NoFilter', frozen=True)
    visibility: Literal['NoFilter', 'public', 'internal', 'private'] = Field(default='NoFilter', frozen=True)
    # Issues extract + Filters
    doExtractIssues: bool = Field(default=True, frozen=True)
    issuesConfidential: Literal['confidential', 'public', 'NoFilter'] = Field(default='NoFilter', frozen=True)
    issueDueDate: Literal['0', 'any', 'today', 'tomorrow', 'overdue', 'week', 'month', 'next_month_and_previous_two_weeks']= Field(default='any', frozen=True)
    issueType: Literal['issue', 'incident', 'test_case', 'task', 'NoFilter'] = Field(default='NoFilter', frozen=True)
    issueScope: Literal['created_by_me', 'assigned_to_me', 'all'] = Field(default='all', frozen=True)
    issueState: Literal['opened', 'closed', 'all'] = Field(default='all', frozen=True)
    
    # Commits extract + Filters
    doExtractCommits: bool = Field(default=True, frozen=True)
    commitFirstParent: bool = Field(default=False, frozen=True)
    commitWithStats: bool = Field(default=False, frozen=True)
    
    # Branches extract + Filters
    doExtractBranches: bool = Field(default=True, frozen=True)
    
    # Labels extract + Filters
    doExtractLabels: bool = Field(default=True, frozen=True)
    labelWithCount: bool = Field(default=False, frozen=True)
    labelIncludeAncestorGroups: bool = Field(default=True, frozen=True)
    labelArchivedOnly: bool = Field(default=False, frozen=True)
    
    # Milestones extract + Filters
    doExtractMilestones: bool = Field(default=True, frozen=True)
    milestoneState: Literal['active', 'closed', 'NoFilter'] = Field(default='NoFilter', frozen=True)
    milestoneIncludeAncestors: bool = Field(default=False, frozen=True)
    
    # Merge Requests extract + Filters
    doExtractMergeRequests: bool = Field(default=True, frozen=True)
    mergeRequestEnvironment: str | None = Field(default=None, frozen=True)
    mergeRequestScope: Literal['created_by_me', 'assigned_to_me', 'reviews_for_me', 'all'] = Field(default='all', frozen=True)
    mergeRequestState: Literal['all', 'opened', 'closed', 'locked', 'merged'] = Field(default='all', frozen=True)
    mergeRequestDraft: Literal['NoFilter', 'draft', 'non-draft'] = Field(default='NoFilter', frozen=True)
    
    
    def create_extractor(self) -> projects_extractor.ProjectDataExtractor:
        return projects_extractor.ProjectDataExtractor(
            domain=self.domain,
            access_token=self.accessToken,
            timestamp=self.timestamp,
            datasource_id=self.datasourceId,
            schedule_id=self.scheduleId,
            tenant_id=self.tenantId,
            items_per_page=self.itemsPerPage,
            project_list=self.projectList,
            archived=self.archived,
            membership=self.membership,
            owned=self.owned,
            starred=self.starred,
            with_issues_enabled=self.withIssuesEnabled,
            with_merge_requests_enabled=self.withMergeRequestsEnabled,
            active=self.active,
            min_access_level=self.minAccessLevel,
            visibility=self.visibility,
            
            do_extract_issues=self.doExtractIssues,
            issues_confidential=self.issuesConfidential,
            issue_due_date=self.issueDueDate,
            issue_type=self.issueType,
            issue_scope=self.issueScope,
            issue_state=self.issueState,
            
            do_extract_commits=self.doExtractCommits,
            commit_first_parent=self.commitFirstParent,
            commit_with_stats=self.commitWithStats,
            
            do_extract_branches=self.doExtractBranches,
            
            do_extract_labels=self.doExtractLabels,
            label_with_count=self.labelWithCount,
            label_include_ancestor_groups=self.labelIncludeAncestorGroups,
            label_archived_only=self.labelArchivedOnly,
            
            do_extract_milestones=self.doExtractMilestones,
            milestone_state=self.milestoneState,
            milestone_include_ancestors=self.milestoneIncludeAncestors,
            
            do_extract_merge_requests=self.doExtractMergeRequests,
            merge_request_environment=self.mergeRequestEnvironment,
            merge_request_scope=self.mergeRequestScope,
            merge_request_state=self.mergeRequestState,
            merge_request_draft=self.mergeRequestDraft,
        )
        
