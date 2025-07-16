from gitlab.v4.objects import ProjectMilestone

from ..base_data.data import Data
from ..util.utility import format_raw_content


class MilestoneData(Data):

    def get_info(self, element: ProjectMilestone):
        milestone = element.asdict()

        # Attributes
        milestone_id = milestone['id']
        title = milestone['title']
        description = milestone['description']

        # Content ID
        content_id = milestone_id

        # Datasource Payload
        datasource_payload = {
            "milestone": milestone
        }

        # Raw Content
        raw_content_elements = [str(title or ''), str(description or '')]
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
