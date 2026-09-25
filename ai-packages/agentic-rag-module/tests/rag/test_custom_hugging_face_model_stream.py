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

"""Streaming from the custom Hugging Face backend.

CustomChatHuggingFaceModel posts the last message to the backend and relays
each piece of the streamed body as a message chunk, closing with an empty one.
"""

from unittest.mock import patch

from langchain_core.messages import AIMessageChunk, HumanMessage, SystemMessage
from langchain_core.outputs import ChatGenerationChunk

from app.rag import custom_hugging_face_model as model_module
from app.rag.custom_hugging_face_model import CustomChatHuggingFaceModel

BASE_URL = "http://hugging-face:8080/generate"


def _stream(messages, body):
    model = CustomChatHuggingFaceModel(base_url=BASE_URL)

    with patch.object(model_module.requests, "post", return_value=body) as post:
        chunks = list(model._stream(messages))

    return chunks, post


def test_streamed_body_is_relayed_chunk_by_chunk():
    chunks, _ = _stream([HumanMessage(content="Ciao")], [b"Buon", b"giorno"])

    assert all(isinstance(chunk, ChatGenerationChunk) for chunk in chunks)
    assert all(isinstance(chunk.message, AIMessageChunk) for chunk in chunks)
    assert [chunk.message.content for chunk in chunks] == ["Buon", "giorno", ""]


def test_only_the_last_message_is_sent_to_the_backend():
    _, post = _stream(
        [SystemMessage(content="system prompt"), HumanMessage(content="Ciao")],
        [b"Buongiorno"],
    )

    post.assert_called_once_with(BASE_URL, json={"input_str": "Ciao"}, stream=True)


def test_model_type():
    assert CustomChatHuggingFaceModel(base_url=BASE_URL)._llm_type == (
        "custom-chat-hugging-face-model"
    )
