from gitlab.v4.objects import Project

from ..base_data.data import Data
from ..util.utility import format_raw_content


class ProjectData(Data):

    def get_info(self, element: Project):
        project = element.asdict()
        project.pop('_links', None)

        # Attributes

        project_id = project['id']
        description = project['description']
        topics = project['topics']
        name = project['name']

        # Content ID
        content_id = project_id

        # Datasource Payload
        datasource_payload = {
            "project": project
        }

        # Raw Content
        raw_content_elements = [str(description or ''), str(name or '')]
        raw_content_elements.extend(topics)
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
