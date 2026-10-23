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
"""
Sources numbered with markers in the answer, with the contract of agentic-rag-module.

- Every document delivered to the orchestrator gets a source number when it is
  delivered, one per document for the whole turn: a document that a second
  delegation delivers again keeps its number. The orchestrator writes ``[n]``
  at the end of each sentence it takes from source ``n``.
- Before the answer the SOURCES event lists every delivered source with its
  number, so that a client can link ``[n]`` while the text arrives.
- At the end the markers decide which sources the answer used: the DOCUMENT
  events carry only those, each with its number and, when the tenant turns
  ``retrieve_citations`` on, the quotes the answer took from it. An answer with
  no marker falls back to ``used_document_ids``.

The numbers may have gaps: a delivered source the answer does not use keeps
its number, as in agentic-rag-module.
"""

import re

from app.rag.agentic import discovery
from app.rag.agentic.contracts import DocumentRef, OrchestratorOutput
from app.rag.agentic.deps import AgenticDeps
from app.utils.logger import get_logger

logger = get_logger(__name__)

MARKER = re.compile(r"\[(\d{1,3})\]")
MAX_QUOTES = 2

MARKERS_INSTRUCTION = """

Citing the sources: every document returned by `discover` has a `source` number. Every time a
sentence of your answer states something taken from a document, write its source number in square
brackets at the end of the sentence, before the full stop, like this: SMC opera dal 1995 [2]. Use
only the source numbers returned by `discover` and never invent one. Mark each sentence with the
one source it took its statement from. Do not mark a sentence you wrote yourself rather than took
from a document."""

MARKERS_ONLY_INSTRUCTION = """ Write no list of sources at the end of the message: these markers
are the only citation you must produce, and leave `citations` empty."""

QUOTES_INSTRUCTION = """ Write no list of sources at the end of the message. Fill `citations`
with the quotes: for each source number you marked, the passage your marked sentence took from it,
copied verbatim word for word and no longer than 25 words: never merge two passages into one
sentence, never rewrite them, and never quote something your answer does not say. Give one quote
per source unless the answer takes two distinct statements from it, and if there is no second
passage you can copy word for word, give one only: a quote you had to compose is worse than none.
List every number you marked and no other."""


def instructions(retrieve_citations: bool) -> str:
    """The part of the orchestrator prompt about the markers, and the quotes when asked for."""
    return MARKERS_INSTRUCTION + (
        QUOTES_INSTRUCTION if retrieve_citations else MARKERS_ONLY_INSTRUCTION
    )


def source_number(deps: AgenticDeps, document_id: str) -> int:
    """The source number of a delivered document: the next one, or the one it already has."""
    return deps.source_ordinals.setdefault(document_id, len(deps.source_ordinals) + 1)


def sources(deps: AgenticDeps) -> list[dict]:
    """Every source delivered in the turn with its number: the chunk of the SOURCES event."""
    return [
        {
            "ordinal": ordinal,
            "document_id": document_id,
            "title": deps.discovered.get(document_id, {}).get("title"),
            "url": deps.discovered.get(document_id, {}).get("url"),
        }
        for document_id, ordinal in deps.source_ordinals.items()
    ]


def _normalize(text: str) -> str:
    """Only the ASCII letters and digits, case-folded: the indexed text can lose its non-ASCII
    characters, so a quote spelled correctly would never be found in its own source."""
    return "".join(c for c in text if c.isascii() and c.isalnum()).casefold()


def _valid_quotes(citations: dict, cited: dict, deps: AgenticDeps) -> dict:
    """The quotes of each marked source that are really in it, at most :data:`MAX_QUOTES`."""
    quotes = {}
    for key, raw in (citations or {}).items():
        try:
            ordinal = int(key)
        except (TypeError, ValueError):
            continue
        if ordinal not in cited:
            # quoted but never marked: the markers win, they are what the reader sees
            logger.warning(f"[citations] source {ordinal} is quoted but carries no marker")
            continue
        source = _normalize(deps.discovered.get(cited[ordinal], {}).get("content", ""))
        kept = quotes.setdefault(ordinal, [])
        for quote in raw if isinstance(raw, list) else [raw]:
            if not isinstance(quote, str) or not quote.strip() or len(kept) == MAX_QUOTES:
                continue
            if _normalize(quote) not in source:
                logger.debug(f"[citations] quote not found in source {ordinal}: {quote!r}")
                continue
            kept.append(quote)
    return quotes


def cited_refs(deps: AgenticDeps, output: OrchestratorOutput) -> list[DocumentRef]:
    """
    The documents the answer cites, for the DOCUMENT events and the saved turn.

    With markers, the marked sources in the order of their numbers, each with
    its number and quotes. Without, the ``used_document_ids`` of the answer,
    with no number.
    """
    by_ordinal = {ordinal: document_id for document_id, ordinal in deps.source_ordinals.items()}
    cited, invented = {}, []
    for match in MARKER.finditer(output.message or ""):
        ordinal = int(match.group(1))
        if ordinal in by_ordinal:
            cited.setdefault(ordinal, by_ordinal[ordinal])
        elif ordinal not in invented:
            invented.append(ordinal)
    if invented:
        # a footnote leading nowhere: counted, not corrected
        logger.warning(f"[citations] markers {invented} point to no source")
    if not cited:
        return discovery.resolve_document_refs(deps, output.used_document_ids)
    quotes = _valid_quotes(output.citations, cited, deps)
    refs = discovery.resolve_document_refs(deps, [cited[ordinal] for ordinal in sorted(cited)])
    for ref in refs:
        ref.ordinal = deps.source_ordinals[ref.document_id]
        ref.quotes = quotes.get(ref.ordinal, [])
    logger.debug(f"[citations] {len(cited)} of {len(by_ordinal)} delivered sources cited")
    return refs
