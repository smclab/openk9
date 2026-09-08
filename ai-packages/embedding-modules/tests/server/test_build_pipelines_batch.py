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

"""The indexing capabilities embed the chunks of a document in one request.

`router.text_pieces` already hands the whole chunk list to `embed_texts`:
what the text-only path injects has to send it as it is. `embed_documents`
is part of the langchain `Embeddings` interface, so this holds for every
provider.
"""

from app import server as server_module

TEXTS = ["uno", "quattro", "due"]


class _FakeLangchainEmbeddings:
    def __init__(self, calls):
        self.calls = calls

    def embed_documents(self, texts):
        self.calls.append(list(texts))
        return [[float(len(text))] for text in texts]

    def embed_query(self, text):
        raise AssertionError("the chunks must not be embedded one at a time")


def test_the_text_only_path_embeds_the_chunks_in_one_call(monkeypatch):
    calls = []
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(calls),
    )

    pipelines = server_module.build_pipelines({}, chunker=None)
    vectors = pipelines.embed_texts(TEXTS)

    assert calls == [TEXTS]
    assert vectors == [[3.0], [7.0], [3.0]]
