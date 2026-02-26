from gitlab.v4.objects import User

from ..base_data.data import Data
from ..util.utility import format_raw_content


class UserData(Data):

    def get_info(self, element: User):
        user = element.asdict()

        # Attributes
        user_id = user['id']
        username = user['username']
        name = user['name']

        # Content ID
        content_id = user_id

        # Datasource Payload
        datasource_payload = {
            "user": user
        }

        # Raw Content
        raw_content_elements = [str(name or ''), str(username or '')]
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
