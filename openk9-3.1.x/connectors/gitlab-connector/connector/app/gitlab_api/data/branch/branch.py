from gitlab.v4.objects import ProjectBranch
from ..base_data.data import Data
from ..util.utility import format_raw_content, to_content_id


class BranchData(Data):

    def get_info(self, element: ProjectBranch):
        branch = element.asdict()

        # Attributes
        name = branch['name']
        web_url = branch['web_url']

        # Content ID
        content_id = to_content_id(web_url)

        # Datasource Payload
        datasource_payload = {
            "branch": branch
        }

        # Raw Content
        raw_content_elements = [str(name or '')]
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
