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

"""Request batches for the Vertex AI text embedding models.

A Vertex embedding request takes at most MAX_TEXTS_PER_REQUEST texts and
MAX_TOKENS_PER_REQUEST input tokens; langchain-google-vertexai split the
texts within those limits up to 2.x, and sends them in one request since
3.x. The token count is estimated without a tokenizer, with the same rule
langchain used: twice the words, punctuation and whitespace characters.
"""

import re
import string

MAX_TEXTS_PER_REQUEST = 250
MAX_TOKENS_PER_REQUEST = 20000

_SEGMENT = re.compile(f"([{re.escape(string.punctuation)}\t\n ])")


def estimated_tokens(text):
    """A conservative estimate of the tokens of a text."""
    return 2 * len([segment for segment in _SEGMENT.split(text) if segment])


def batches(texts):
    """Splits the texts, in order, into request batches within the limits.

    A text over the token limit on its own still gets a batch, so the
    provider error reaches the caller.
    """
    batch = []
    batch_tokens = 0

    for text in texts:
        tokens = estimated_tokens(text)

        if batch and (
            len(batch) == MAX_TEXTS_PER_REQUEST
            or batch_tokens + tokens > MAX_TOKENS_PER_REQUEST
        ):
            yield batch
            batch = []
            batch_tokens = 0

        batch.append(text)
        batch_tokens += tokens

    if batch:
        yield batch
