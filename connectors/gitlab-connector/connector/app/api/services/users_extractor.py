from collections.abc import Iterator
from datetime import datetime
from typing import Any, override

from gitlab import Gitlab
from gitlab.v4.objects import User

from .base_extractor import BaseExtractor
from .util.utility import format_raw_content, strftime_datetime_filter

from ..models.util.models_utility import  (
	BoolFilter,
	FilterComposite,
)


class UserDataExtractor(BaseExtractor):
    def __init__(self, *, domain: str, access_token: str, timestamp: int, datasource_id: int, schedule_id: str, tenant_id: str, items_per_page: int, fetch_user_details: bool,
                filter_active: BoolFilter, filter_external: BoolFilter, filter_blocked: BoolFilter, filter_human: BoolFilter, exclude_active: BoolFilter, exclude_external: BoolFilter, exclude_humans: BoolFilter, exclude_internal: BoolFilter) -> None:
        super().__init__(domain, access_token, timestamp, datasource_id, schedule_id, tenant_id, items_per_page)

        self.fetch_user_details = fetch_user_details

        self.user_filters: dict[str, Any] = FilterComposite.compose_params(
            filters={
                'active': filter_active,
                'external': filter_external,
                'blocked': filter_blocked,
                'humans': filter_human,
                'exclude_active': exclude_active,
                'exclude_external': exclude_external,
                'exclude_humans': exclude_humans,
                'exclude_internal': exclude_internal,
            })
    
    @override
    def extract_data(self, gl: Gitlab, time_stamp_date: datetime) -> Iterator[User]:
        if gl.user and gl.user.attributes.get('is_admin', False):
            for user in gl.users.list(
	                iterator=True, 
	                **self.user_filters, 
	            ):
                if last_activity := user.attributes.get('last_activity_on', None):
                    if datetime.strptime(last_activity, '%Y-%m-%d') >= time_stamp_date:
                        yield user
        else:
            datetime_filter: str = strftime_datetime_filter(dt=time_stamp_date)
            for user in gl.users.list(
                iterator=True,
                created_after=datetime_filter,
                **self.user_filters,
            ):
                # The list endpoint exposes a reduced field set to non-admin
                # tokens; optionally fetch each user by id for the fuller profile.
                
                user = gl.users.get(user.id) if self.fetch_user_details else user
                self.status_logger.info(user.to_json())
                yield user
    
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
        