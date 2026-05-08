import os
import tempfile

from chonkie import MarkdownChef

EPS = 1e-8


def matching_percentage(chunk, gold_span):
    clen, glen = len(chunk), len(gold_span)
    if not clen or not glen:
        return 0

    max_hits = 0

    for i in range(clen):
        remaining = clen - i
        current_hits = 0
        limit = min(glen, remaining)

        for j in range(limit):
            if gold_span[j] == chunk[i + j]:
                current_hits += 1
                if current_hits > max_hits:
                    max_hits = current_hits

    return max_hits


def calculate_lf(chunks, doc):
    with tempfile.NamedTemporaryFile(mode="w+", suffix=".md", delete=False) as tmp_f:
        tmp_f.write(doc)
        tmp_f.flush()
        path = tmp_f.name
    mdchef = MarkdownChef()
    mddoc = mdchef.process(path)
    table_span = []
    for table in mddoc.tables:
        table_span.append(table.content)

    coverage = 0

    for sopan in table_span:
        max_hits = 0
        pi_sopan = sopan.split()
        for chunk in chunks:
            pi_chunk = chunk.text.split()
            pilen = len(pi_sopan)

            hits = matching_percentage(pi_chunk, pi_sopan)
            if hits / pilen > max_hits / pilen:
                max_hits = hits

        if max_hits == pilen:
            coverage += 1

        print(f"coverage: {True if max_hits == pilen else False}")
    #    print(f"hits: {max_hits}/{pilen} - {round(max_hits/pilen,3)} IoU: {max_hits/union} ")
    #    hits_list.append(max_hits/union)
    os.remove(path)
    return {"coverage": round(coverage / (len(table_span) + EPS), 3)}
