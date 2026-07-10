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

import json
import re

# Courtesy message returned when the request carries no textual content. Kept
# distinct from the no-context message of the answer_only_with_context flow:
# there the knowledge base had nothing to say, here the user asked nothing.
BLANK_QUERY_MESSAGE = "Please enter a question so I can help you."

# Matches any character that is neither a "word" character nor an underscore,
# i.e. its negation detects a single Unicode letter or digit. Underscore is
# excluded because a lone "_" carries no textual content.
_TEXTUAL_CHARACTER = re.compile(r"[^\W_]", re.UNICODE)


def is_blank_query(search_text) -> bool:
    """Return True when ``search_text`` carries no textual content.

    A query is considered blank when, after normalization, it contains no
    Unicode letter or digit. This treats whitespace (including unicode spaces,
    non-breaking spaces and zero-width characters), emoji, symbols and pure
    punctuation as blank, while real text in any script and numeric queries
    remain valid.
    """
    if not isinstance(search_text, str):
        return True
    return _TEXTUAL_CHARACTER.search(search_text) is None


def blank_query_stream():
    """Yield the controlled SSE response for a blank query.

    Emits a START/CHUNK/END sequence carrying the courtesy message, coherent
    with the event format produced by the RAG pipeline. No DOCUMENT and no
    ERROR events are emitted.
    """
    yield json.dumps({"chunk": "", "type": "START"})
    yield json.dumps({"chunk": BLANK_QUERY_MESSAGE, "type": "CHUNK"})
    yield json.dumps({"chunk": "", "type": "END"})
