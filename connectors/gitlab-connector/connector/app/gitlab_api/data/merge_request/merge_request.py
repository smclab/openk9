from gitlab.v4.objects import ProjectMergeRequest

from ..base_data.data import Data
from ..util.utility import format_raw_content


class MergeRequestData(Data):

    def get_info(self, element: ProjectMergeRequest):
        merge_request = element.asdict()

        # Attributes

        merge_request_id = merge_request['id']
        title = merge_request['title']
        description = merge_request['description']

        labels = merge_request['labels']

        # Content ID
        content_id = merge_request_id

        # Datasource Payload
        datasource_payload = {
            "mergeRequest": merge_request
        }

        # Raw Content
        raw_content_elements = [str(title or ''), str(description or '')]
        raw_content_elements.extend(labels)
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
