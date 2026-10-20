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

"""The response node gives the model the image of the query and the retrieved
source images, when the model accepts them, and tells the user about the
images the model did not see."""

import base64
import logging
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import pytest
from langchain_core.documents import Document

from app.models.models import Media
from app.rag import agentic_rag
from app.rag.agentic_rag import (
    MEDIA_ONLY_QUERY_INSTRUCTION,
    MEDIA_ONLY_QUERY_WITH_IMAGE_INSTRUCTION,
    GraphState,
    RagGraph,
)
from app.rag.image_inputs import SourceImage

PHOTO = base64.b64encode(b"\x89PNG photo of the user").decode()
SOURCE_PHOTO = base64.b64encode(b"\xff\xd8 photo of the source").decode()
VISUAL_DETAILS_BAN = "do not describe visual details"


class _Rejected(Exception):
    status_code = 400


def _graph(media=PHOTO, **configuration):
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = "CHAT_RAG"
    graph.answer_only_with_context = False
    graph.tenant_id = "tenant-1"
    graph.user_id = None
    graph.chat_id = None
    graph.configuration = {
        "prompt_template": "SYSTEM",
        "prompt_no_rag": "SYSTEM",
        "media": Media(data=media, contentType="image/png") if media else None,
        **configuration,
    }
    graph.llm = MagicMock(return_value=SimpleNamespace(content="ANSWER"))
    return graph


def _image_source(name="retro.jpg", url="https://photos.example.com/retro.jpg"):
    return Document(
        page_content="Foto del retro",
        metadata={"document_id": "doc-1", "image_source_url": url, "image_name": name},
    )


def _state(query="cosa raffigura?", context=None, use_rag=True):
    return GraphState(
        current_query=query,
        target_lang="Italian",
        use_rag=use_rag,
        context=context if context is not None else [Document("Testo di contesto")],
    )


def _run(graph, state, accepts=True, fetched=None):
    with (
        patch.object(agentic_rag, "accepts_images", return_value=accepts),
        patch.object(
            agentic_rag, "fetch_source_images", return_value=fetched or []
        ) as fetch,
        patch.object(agentic_rag, "mark_text_only") as mark,
    ):
        result = graph.llm_response_node(state)
    return result, fetch, mark


def _sent(graph, call=-1):
    """The messages the model received in a call."""
    return graph.llm.call_args_list[call].args[0]


def _blocks(graph, call=-1):
    messages = _sent(graph, call)
    content = messages[-1].content
    return content if isinstance(content, list) else [{"type": "text", "text": content}]


def _text(graph, call=-1):
    sent = _sent(graph, call)
    if hasattr(sent, "to_string"):
        return sent.to_string()
    return "\n".join(b["text"] for b in _blocks(graph, call) if b["type"] == "text")


def _images(graph, call=-1):
    sent = _sent(graph, call)
    if hasattr(sent, "to_string"):
        return []
    return [b["base64"] for b in _blocks(graph, call) if b["type"] == "image"]


def test_query_image_reaches_model_with_the_question():
    graph = _graph()

    _run(graph, _state())

    assert _images(graph) == [PHOTO]
    assert "Question: cosa raffigura?" in _text(graph)


def test_query_image_reaches_model_without_rag():
    graph = _graph()

    _run(graph, _state(use_rag=False, context=[]))

    assert _images(graph) == [PHOTO]
    assert "Current question: cosa raffigura?" in _text(graph)


def test_source_image_next_to_its_text():
    graph = _graph(media=None)
    source = _image_source()

    _, fetch, _ = _run(
        graph,
        _state(context=[Document("Testo"), source]),
        fetched=[SourceImage(SOURCE_PHOTO, "image/jpeg")],
    )

    fetch.assert_called_once_with(["https://photos.example.com/retro.jpg"])
    assert _images(graph) == [SOURCE_PHOTO]
    text = _text(graph)
    assert "[Image 1]\nFoto del retro" in text
    assert "[Image 1] of the context:" in text


def test_at_most_four_source_images_in_context_order():
    graph = _graph(media=None)
    sources = [_image_source(f"{n}.jpg", f"https://h/{n}.jpg") for n in range(6)]

    _, fetch, _ = _run(
        graph,
        _state(context=sources),
        fetched=[SourceImage(SOURCE_PHOTO, "image/jpeg")] * 4,
    )

    fetch.assert_called_once_with([f"https://h/{n}.jpg" for n in range(4)])


def test_failed_source_image_stays_text_and_is_announced():
    # over 20 MB, unreachable, refused by the whitelist: the reader returned None
    graph = _graph(media=None)

    _run(graph, _state(context=[_image_source()]), fetched=[None])

    assert _images(graph) == []
    text = _text(graph)
    assert "Foto del retro" in text
    assert "[Image 1]" not in text
    assert 'the image of the source "retro.jpg" could not be shown to you' in text
    assert "Start your answer by telling the user, in Italian" in text


def test_the_notice_keeps_the_photo_the_model_saw():
    # the photo reached the model and a source image did not: the notice must
    # not send the model back to the text alone
    graph = _graph()

    _run(graph, _state(context=[_image_source()]), fetched=[None])

    assert len(_images(graph)) == 1
    text = _text(graph)
    assert 'the image of the source "retro.jpg" could not be shown to you' in text
    assert "answer with the images you were shown and the text" in text
    assert "text only" not in text


def test_the_notice_sends_a_model_without_images_to_the_text():
    graph = _graph()

    _run(graph, _state(), accepts=False)

    text = _text(graph)
    assert "answer using the text only" in text
    assert "images you were shown" not in text


def test_image_chunk_without_url_stays_text():
    graph = _graph(media=None)
    chunk = Document("Foto del retro", metadata={"document_id": "doc-1"})

    _, fetch, _ = _run(graph, _state(context=[chunk]))

    fetch.assert_not_called()
    assert _images(graph) == []
    assert "could not be shown" not in _text(graph)


def test_text_only_model_gets_the_notice_not_the_photo():
    graph = _graph()

    _run(graph, _state(), accepts=False)

    assert _images(graph) == []
    assert "the image attached by the user could not be shown to you" in _text(graph)


def test_the_log_counts_the_images_of_the_turn(caplog):
    graph = _graph()
    sources = [_image_source("a.jpg", "https://h/a.jpg"), _image_source()]

    with caplog.at_level(logging.INFO, logger=agentic_rag.logger.name):
        _run(
            graph,
            _state(context=sources),
            fetched=[SourceImage(SOURCE_PHOTO, "image/jpeg"), None],
        )

    assert "[llm_response] images accepts=True query=1 sources=1 unseen=1" in (
        caplog.text
    )


def test_the_log_shows_a_text_only_model_kept_the_sources_back(caplog):
    graph = _graph(media=None)

    with caplog.at_level(logging.INFO, logger=agentic_rag.logger.name):
        _run(graph, _state(context=[_image_source()]), accepts=False)

    assert "[llm_response] images accepts=False query=0 sources=0 unseen=0" in (
        caplog.text
    )


def test_the_log_shows_a_model_still_to_try(caplog):
    graph = _graph()

    with caplog.at_level(logging.INFO, logger=agentic_rag.logger.name):
        _run(graph, _state(), accepts=None)

    assert "[llm_response] images accepts=None query=1 sources=0 unseen=0" in (
        caplog.text
    )


def _prompt_of_today(context):
    graph = _graph(media=None, source_images_to_llm=False)
    _run(graph, _state(context=context))
    return _text(graph)


def test_text_only_model_and_source_images_keep_todays_prompt():
    graph = _graph(media=None)
    context = [_image_source()]

    _, fetch, _ = _run(graph, _state(context=context), accepts=False)

    fetch.assert_not_called()
    assert _text(graph) == _prompt_of_today(context)


def test_media_only_with_a_visual_model():
    graph = _graph()

    _run(graph, _state(query=""))

    assert _images(graph) == [PHOTO]
    text = _text(graph)
    assert MEDIA_ONLY_QUERY_WITH_IMAGE_INSTRUCTION in text
    assert VISUAL_DETAILS_BAN not in text


def test_media_only_with_the_query_image_switched_off():
    graph = _graph(query_image_to_llm=False)

    _run(graph, _state(query=""))

    assert _images(graph) == []
    text = _text(graph)
    assert MEDIA_ONLY_QUERY_INSTRUCTION in text
    assert "could not be shown" not in text


def test_source_images_switched_off():
    graph = _graph(media=None, source_images_to_llm=False)

    _, fetch, _ = _run(graph, _state(context=[_image_source()]))

    fetch.assert_not_called()
    assert _images(graph) == []
    assert "Foto del retro" in _text(graph)
    assert "could not be shown" not in _text(graph)


def test_simple_generate_stays_text():
    graph = _graph()
    graph.rag_type = "SIMPLE_GENERATE"

    _, fetch, _ = _run(graph, _state(context=[_image_source()]))

    fetch.assert_not_called()
    assert _images(graph) == []


def test_unknown_model_rejection_retries_text_only_and_marks(caplog):
    graph = _graph()
    graph.llm.side_effect = [
        _Rejected("model does not support multimodal requests"),
        SimpleNamespace(content="ANSWER"),
    ]

    with caplog.at_level(logging.WARNING, logger="app.rag.agentic_rag"):
        state, _, mark = _run(graph, _state(), accepts=None)

    assert state.response == "ANSWER"
    assert _images(graph, call=0) == [PHOTO]
    assert _images(graph, call=1) == []
    assert "the image attached by the user could not be shown to you" in _text(
        graph, call=1
    )
    mark.assert_called_once_with(graph.llm)
    assert "does not support multimodal requests" in caplog.text


def test_retry_announces_every_image_of_the_turn():
    graph = _graph()
    graph.llm.side_effect = [_Rejected("too big"), SimpleNamespace(content="OK")]

    _run(
        graph,
        _state(context=[_image_source()]),
        accepts=True,
        fetched=[SourceImage(SOURCE_PHOTO, "image/jpeg")],
    )

    text = _text(graph, call=1)
    assert (
        'the image attached by the user, the image of the source "retro.jpg" '
        "could not be shown to you"
    ) in text
    assert "[Image 1]" not in text


def test_visual_model_rejection_is_not_marked():
    graph = _graph()
    graph.llm.side_effect = [
        _Rejected("image too large"),
        SimpleNamespace(content="OK"),
    ]

    _, _, mark = _run(graph, _state(), accepts=True)

    mark.assert_not_called()


def test_rate_limit_is_not_retried():
    graph = _graph()
    rate_limited = _Rejected("rate_limit_exceeded")
    rate_limited.status_code = 429
    graph.llm.side_effect = rate_limited

    with pytest.raises(_Rejected):
        _run(graph, _state(), accepts=None)

    assert graph.llm.call_count == 1


def test_error_without_images_is_not_retried():
    graph = _graph(media=None)
    graph.llm.side_effect = _Rejected("bad request")

    with pytest.raises(_Rejected):
        _run(graph, _state())

    assert graph.llm.call_count == 1


def test_images_do_not_stay_in_state_history_or_logs(caplog):
    graph = _graph()
    graph.chat_sequence_number = 2
    source = _image_source()

    with caplog.at_level(logging.DEBUG):
        state, _, _ = _run(
            graph,
            _state(context=[source]),
            fetched=[SourceImage(SOURCE_PHOTO, "image/jpeg")],
        )
        state = graph.history_saver_node(state)

    assert _images(graph) == [PHOTO, SOURCE_PHOTO]
    dumped = state.model_dump_json()
    assert PHOTO not in dumped and SOURCE_PHOTO not in dumped
    assert [message.content for message in state.messages] == [
        "cosa raffigura?",
        "ANSWER",
    ]
    assert PHOTO not in caplog.text and SOURCE_PHOTO not in caplog.text
