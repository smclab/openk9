from gitlab.v4.objects import ProjectCommit
from ..base_data.data import Data
from ..util.utility import format_raw_content, to_content_id


class CommitData(Data):
    
    def get_info(self, element: ProjectCommit):
        commit = element.asdict()

        # Attributes

        commit_id = commit['id']
        title = commit['title']
        author_name = commit['author_name']
        author_email = commit['author_email']
        committer_name = commit['committer_name']
        committer_email = commit['committer_email']
        message = commit['message']

        # Content ID
        content_id = to_content_id(commit_id)

        # Datasource Payload
        datasource_payload = {
            "commit": commit
        }

        # Raw Content
        raw_content_elements = [str(title or ''), str(author_name or ''), str(author_email or ''),
                                str(committer_name or ''), str(committer_email or ''), str(message or '')]
        raw_content = format_raw_content(''.join(raw_content_elements))

        return content_id, datasource_payload, raw_content
