from collections.abc import Iterator
from datetime import datetime
from typing import Any, override

from gitlab import Gitlab
from gitlab.v4.objects import User

from .base_extractor import BaseExtractor
from .util.utility import format_raw_content


class UserDataExtractor(BaseExtractor):
    def __init__(self, *, domain: str, access_token: str, timestamp: int, datasource_id: int, schedule_id: str, tenant_id: str, items_per_page: int,
                filter_active: bool, filter_external: bool, filter_blocked: bool, filter_human: bool, exclude_active: bool, exclude_external: bool, exclude_humans: bool, exclude_internal: bool) -> None:
        super().__init__(domain, access_token, timestamp, datasource_id, schedule_id, tenant_id, items_per_page)
        
        self.filter_active: bool = filter_active
        self.filter_external: bool = filter_external
        self.filter_blocked: bool = filter_blocked
        self.filter_human: bool = filter_human
        self.exclude_active: bool = exclude_active
        self.exclude_external: bool = exclude_external
        self.exclude_humans: bool = exclude_humans
        self.exclude_internal: bool = exclude_internal
        
    
    @override
    def extract_data(self, gl: Gitlab, time_stamp_date: datetime) -> Iterator[User]:
        return gl.users.list(
            iterator=True, 
            created_after=time_stamp_date,
            active=self.filter_active,
            external=self.filter_external,
            blocked=self.filter_blocked,
            humans=self.filter_human,
            exclude_active=self.exclude_active,
            exclude_external=self.exclude_external,
            exclude_humans=self.exclude_humans,
            exclude_internal=self.exclude_internal
        )
    
    @override
    def manage_data(self, data: User, end_timestamp: float) -> dict[str, Any]:
        info = data.attributes
        content_id = info['id']
        
        raw_content_elements = [str(info['name'] or ''), str(info['username'] or '')]
        raw_content = format_raw_content(''.join(raw_content_elements))
        
        return {
            "datasourceId": self.datasource_id,
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "contentId": content_id,
            "parsingDate": int(end_timestamp),
            "rawContent": raw_content,
            "datasourcePayload": {"user": info},
        }
        