import os
import tempfile

from chonkie import MarkdownChef

EPS = 1e-8


def matching_percentage(chunk, gold_span):
    max_hits = 0
    clen, glen = len(chunk), len(gold_span)

    for i in range(clen):
        hits = 0
        for j in range(min(glen, clen - i)):
            if chunk[i + j] == gold_span[j]:
                hits += 1
                if hits > max_hits:
                    max_hits = hits
            else:
                break
    return max_hits


def calculate_lf(chunks, doc):

    with tempfile.NamedTemporaryFile(mode="w+", suffix=".md", delete=False) as tmp_f:
        tmp_f.write(doc)
        path = tmp_f.name

    try:
        mdchef = MarkdownChef()
        mddoc = mdchef.process(path)

        table_span = [table.content for table in mddoc.tables]

        coverage = 0
        partial_list = []

        chunk_tokens = [chunk.text.split() for chunk in chunks]

        for sopan in table_span:
            pi_sopan = sopan.split()
            pilen = len(pi_sopan)

            if pilen == 0:
                continue

            max_hits = 0
            partial = 0.0

            for pi_chunk in chunk_tokens:
                hits = matching_percentage(pi_chunk, pi_sopan)

                if hits > max_hits:
                    max_hits = hits
                    union = pilen + len(pi_chunk) - max_hits
                    partial = max_hits / (union + EPS)

            if max_hits == pilen:
                coverage += 1

            partial_list.append(partial)

            print(f"coverage: {max_hits == pilen}")

        total_tables = len(table_span)
        return {"coverage": round(coverage / (total_tables + EPS), 3)}

    finally:
        os.remove(path)
