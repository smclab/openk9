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

"""EmbedQuery hands the model exactly what the request carries: the query
text cleaned of its markup, the inline bytes and content type untouched,
and both together for a mixed query."""

from app.embedding.query import QueryCapabilities
from app.external_services.grpc.embedding import embedding_pb2

VECTOR = [3.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0]
INLINE = embedding_pb2.InlineMedia(data=b"png-bytes", contentType="image/png")


class _RecordingModel:
    """A fully-capable model that records every call it receives."""

    def __init__(self, calls):
        self.calls = calls

    def embed_text(self, text):
        self.calls.append(("embed_text", text))
        return VECTOR

    def embed_image(self, data, content_type):
        self.calls.append(("embed_image", data, content_type))
        return VECTOR

    def embed_mixed(self, text, data, content_type):
        self.calls.append(("embed_mixed", text, data, content_type))
        return VECTOR


def _recording_model(calls):
    model = _RecordingModel(calls)

    return lambda configuration: QueryCapabilities(
        embed_text=model.embed_text,
        embed_image=model.embed_image,
        embed_mixed=model.embed_mixed,
    )


def test_the_query_text_reaches_the_model_cleaned(make_stub):
    calls = []
    stub = make_stub(build_query_capabilities=_recording_model(calls))

    stub.EmbedQuery(
        embedding_pb2.EmbedQueryRequest(tenantId="mew", text="<b>un gatto</b> 🙂")
    )

    assert calls == [("embed_text", "un gatto")]


def test_the_inline_image_reaches_the_model_as_it_is(make_stub):
    calls = []
    stub = make_stub(build_query_capabilities=_recording_model(calls))

    stub.EmbedQuery(embedding_pb2.EmbedQueryRequest(tenantId="mew", inline=INLINE))

    assert calls == [("embed_image", b"png-bytes", "image/png")]


def test_a_mixed_query_reaches_the_model_in_one_call(make_stub):
    calls = []
    stub = make_stub(build_query_capabilities=_recording_model(calls))

    stub.EmbedQuery(
        embedding_pb2.EmbedQueryRequest(
            tenantId="mew", text="<i>un gatto</i>", inline=INLINE
        )
    )

    assert calls == [("embed_mixed", "un gatto", b"png-bytes", "image/png")]
