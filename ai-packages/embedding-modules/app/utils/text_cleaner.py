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


def clean_text(raw_text):
    """
    Cleans the input text by performing several text processing steps.

    This function removes HTML tags, corrects hyphenated words that are split across lines,
    eliminates excessive punctuation, drops the empty-target markdown links docling emits
    for summaries, and removes unwanted characters while preserving certain formats like
    fractions, accented letters and apostrophes. It collapses runs of spaces and tabs but
    keeps newlines, which mark the boundary between list entries.

    Parameters:
    raw_text (str): The raw input text that needs to be cleaned.

    Returns:
    str: The cleaned text with unwanted elements removed and formatting corrected.
    """
    # 1. Remove HTML tags like <br>, <p>, etc.
    cleaned_text = re.sub(r"<[^>]+>", "", raw_text)

    # 2. Correct word breaks like "Organiz- \nzation" -> "Organization"
    cleaned_text = re.sub(r"-\s*\n", "", cleaned_text)

    # 3. Remove sequences of long dots (more than 3) like "..........."
    cleaned_text = re.sub(r"\.{4,}", "", cleaned_text)

    # 3-bis. Drop the summary and figure-list lines docling emits as markdown
    # links with an empty target, e.g. "[3.2 Attachments](.)". This has to run
    # HERE, before step 4 strips the square brackets and makes them
    # indistinguishable from prose: on a real corpus they were 33% of the
    # indexed chunks.
    cleaned_text = re.sub(r"(?m)^[ \t]*[-*]?[ \t]*\[[^\]]*\]\(\.\)[ \t]*$\n?", "", cleaned_text)

    # 4. Remove unwanted characters while keeping numbers with / (e.g., 3/27)
    #
    # `\w` is Unicode-aware in Python 3, so it keeps accented letters. The
    # previous ASCII-only class dropped them silently and mutilated Italian
    # ("entita'" -> "entit", "dell'istanza" -> "dellistanza": zero accented
    # characters across 591k indexed ones). Both apostrophe forms are in the
    # keep-list because without them words glue together. Same semantics as
    # the Java counterpart in core/app/tika/.../TextCleaner.java, which
    # uses \p{L}.
    cleaned_text = re.sub(
        r"[^\w\s.,;:?!()/%'\u2019-]|(?<!\d)/|/(?!\d)", "", cleaned_text
    )

    # 5. Collapse horizontal whitespace but KEEP newlines: they are the
    # boundary between list entries, and collapsing them merged distinct
    # entries into a single line, which moved a qualifier onto the wrong item
    # (e.g. "draft only" ended up applying to the entry above it as well).
    cleaned_text = re.sub(r"[ \t]+", " ", cleaned_text)
    cleaned_text = re.sub(r"\n{3,}", "\n\n", cleaned_text)

    return cleaned_text.strip()
