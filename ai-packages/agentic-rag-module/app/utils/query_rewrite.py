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

import re
import unicodedata

# Common Italian and English stopwords, plus generic follow-up wording, ignored
# when checking whether a rewritten query still carries the conversation subject.
_REWRITE_STOPWORDS = {
    # Italian
    "che", "chi", "cosa", "cos", "come", "dove", "quando", "perche", "quale",
    "quali", "della", "delle", "dello", "degli", "del", "dei", "con", "per",
    "tra", "fra", "una", "uno", "gli", "lo", "la", "le", "il", "non",
    "piu", "puoi", "puo", "vorrei", "dammi", "dimmi", "fornisci", "fornirmi",
    "spiegami", "spiegarmi", "maggiori", "dettagli", "ulteriori", "informazioni",
    "meglio", "ancora", "questo", "questa", "questi", "queste", "quel", "quello",
    "sono", "essere", "fare",
    # English
    "what", "which", "who", "where", "when", "why", "how", "the", "and", "for",
    "with", "you", "can", "could", "would", "give", "tell", "more", "details",
    "this", "that", "these", "those", "please", "explain", "information",
    "about", "are", "is",
}


def escape_curly_braces(text):
    """Escape literal curly braces so dynamic content is safe inside a
    ``PromptTemplate``.

    Tenant-configured prompts may contain literal ``{``/``}`` (e.g. JSON
    examples). Doubling them prevents ``PromptTemplate.from_template`` from
    interpreting them as template variables.
    """
    return text.replace("{", "{{").replace("}", "}}")


def _strip_accents(text):
    """Remove diacritics so that accented Italian words match their stopwords."""
    return "".join(
        char
        for char in unicodedata.normalize("NFKD", text)
        if not unicodedata.combining(char)
    )


def significant_terms(text):
    """Return the set of lowercase significant terms in ``text``.

    Tokens shorter than three characters and common stopwords are discarded so
    that only content-bearing words (entities, concepts) remain.
    """
    if not text:
        return set()

    normalized = _strip_accents(text.lower())
    tokens = re.findall(r"\w+", normalized)
    return {
        token
        for token in tokens
        if len(token) >= 3 and token not in _REWRITE_STOPWORDS
    }


def shares_significant_terms(rewritten_query, previous_query):
    """Whether the rewritten query still shares the subject of the previous query.

    Returns ``True`` when the previous query has no significant terms to compare
    against, otherwise ``True`` only if at least one significant term is shared.
    """
    previous_terms = significant_terms(previous_query)
    if not previous_terms:
        return True

    return bool(significant_terms(rewritten_query) & previous_terms)
