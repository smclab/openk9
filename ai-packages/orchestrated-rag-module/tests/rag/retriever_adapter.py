#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

"""The interface of OpenSearchRetriever of agentic-rag-module over
retrieve_documents, so that the retriever tests of that module run unchanged
against the new retrieval and prove it behaves the same."""

from types import SimpleNamespace

from app.rag.agentic.retrieval import retrieve_documents


class OpenSearchRetriever:
    def __init__(self, **configuration):
        self.configuration = configuration

    def invoke(self, query):
        return [
            SimpleNamespace(
                page_content=document["content"],
                metadata={k: v for k, v in document.items() if k != "content"},
            )
            for document in retrieve_documents(query=query, **self.configuration)
        ]
