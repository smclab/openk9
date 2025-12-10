import os
import tempfile

from chonkie import MarkdownChef


def matching_percentage(chunk, gold_span):
    max_hits = 0
    for i, text in enumerate(chunk):
        current_max = 0
        for j, span in enumerate(gold_span):
            try:
                if span == chunk[i + j]:
                    current_max += 1
                    if current_max > max_hits:
                        max_hits = current_max
                # print(span, chunk[i+j], current_max)
            except:
                pass
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
    partial_list = []

    for sopan in table_span:
        max_hits = 0
        partial = 0
        pi_sopan = sopan.split()
        union = 0
        for chunk in chunks:
            pi_chunk = chunk.text.split()
            pilen = len(pi_sopan)

            hits = matching_percentage(pi_chunk, pi_sopan)
            if hits / pilen > max_hits / pilen:
                max_hits = hits
                union = pilen + len(pi_chunk) - max_hits
                partial = max_hits / union

        if max_hits == pilen:
            coverage += 1
        partial_list.append(partial)

        print(f"coverage: {True if max_hits == pilen else False}")
    #    print(f"hits: {max_hits}/{pilen} - {round(max_hits/pilen,3)} IoU: {max_hits/union} ")
    #    hits_list.append(max_hits/union)
    os.remove(path)
    return {"coverage": round(coverage / (len(table_span) + 0.0000001), 3)}
