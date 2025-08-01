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

from collections import defaultdict


def init_context(chunk, window_size: int):
    ctxt = "".join(chunk["prev"][-window_size:])
    ctxt += chunk["content"]
    return ctxt


def end_context(chunk, window_size: int):
    ctxt = "".join(chunk["next"][:window_size])
    return ctxt


def dist(idx_a, idx_b):
    return abs(idx_b - idx_a) - 1


def get_context_window_merged(chunks, window_size: int = 2):
    """Enlarge context with prev and next context and merge them if once enlarged they overlap"""
    document_chunks_map = defaultdict(list)
    for chunk in chunks:
        document_chunks_map[chunk["document_id"]].append(chunk)

    documents = []
    for document_chunks in document_chunks_map.values():
        document_chunks.sort(key=lambda c: c["chunk_idx"])

        ctxt = init_context(document_chunks[0], window_size)
        prev_chunk_idx = int(document_chunks[0]["chunk_idx"])
        for k, chunk in enumerate(document_chunks[1:], start=1):
            curr_chunk_idx = int(chunk["chunk_idx"])
            idx_dist = dist(prev_chunk_idx, curr_chunk_idx)
            if idx_dist > 2 * window_size:
                # end current context and init next one
                ctxt += end_context(document_chunks[k - 1], window_size)
                document = {
                    "title": document_chunks[0]["title"],
                    "url": document_chunks[0]["url"],
                    "document_id": document_chunks[0]["document_id"],
                    "score": document_chunks[0]["score"],
                    "content": ctxt,
                }
                documents.append(document)
                ctxt = init_context(chunk, window_size)
            elif idx_dist <= window_size:
                ctxt += "".join(document_chunks[k - 1]["next"][:idx_dist])
                ctxt += chunk["content"]
            else:
                ctxt += "".join(document_chunks[k - 1]["next"][:window_size])
                ctxt += "".join(chunk["prev"][-(idx_dist - window_size) :])
                ctxt += chunk["content"]
            prev_chunk_idx = curr_chunk_idx

        ctxt += end_context(chunk, window_size)
        document = {
            "title": document_chunks[0]["title"],
            "url": document_chunks[0]["url"],
            "document_id": document_chunks[0]["document_id"],
            "score": document_chunks[0]["score"],
            "content": ctxt,
        }
        documents.append(document)

    return documents
