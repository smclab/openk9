from gitlab.v4.objects import ProjectIssue
from ..base_data.data import Data
from ..util.utility import format_raw_content, to_content_id


class IssueData(Data):

    def get_info(self, element: ProjectIssue):
        issue = element.asdict()

        # Attributes
        issue_id = issue['id']
        description = issue['description']
        title = issue['title']
        labels = issue['labels']
        state = issue['state']

        # Content ID
        content_id = issue_id

        # Datasource Payload
        datasource_payload = {
            "issue": issue
        }

        # Raw Content
        raw_content_elements = [str(state or ''), str(title or ''), str(description or ''), ' '.join(labels)]
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
