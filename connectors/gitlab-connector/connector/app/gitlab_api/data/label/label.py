from gitlab.v4.objects import ProjectLabel

from ..base_data.data import Data
from ..util.utility import format_raw_content, to_content_id


class LabelData(Data):

    def get_info(self, element: ProjectLabel):
        label = element.asdict()

        # Attributes

        label_id = label['id']
        label_title = label['title']
        label_description = label['description']
        label_color = label['color']

        # Content ID
        content_id = label_id

        # Datasource Payload
        datasource_payload = {
            "label": label
        }

        # Raw Content
        raw_content_elements = [str(label_title or ''), str(label_description or ''), str(label_color or '')]
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
