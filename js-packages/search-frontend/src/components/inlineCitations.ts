/**
 * The RAG answer marks each sentence with the number of the source it took
 * its statement from: "SMC opera dal 1995 [2]." This view renders no
 * citations, so those markers are noise the reader cannot act on, and they
 * are removed before the answer is shown.
 *
 * Only the removal lives here. The rest of the handling - turning a marker
 * into a link, the source panel - belongs to the view that can render it,
 * and on this branch that is talk-to, whose copy of this module is the
 * complete one (`js-packages/talk-to/src/components/inlineCitations.ts`).
 * The two are not shared because talk-to is still built by Create React
 * App, whose ModuleScopePlugin refuses imports from outside its `src/`.
 */

/**
 * One to three digits in square brackets. Kept in step with the pattern the
 * RAG module parses (`CITATION_MARKER` in `agentic_rag.py`).
 */
const MARKER = /\[(\d{1,3})\]/g;

/**
 * Drop the markers from a line of prose, taking the space before each with
 * it: "in marketing [1]." has to read "in marketing.", not "in marketing .".
 */
export function stripCitationMarkers(text: string): string {
  return text.replace(/[ \t]*\[\d{1,3}\]/g, "");
}

type LooseNode = { type?: string; value?: unknown; children?: unknown[] };

function strip(node: unknown): void {
  if (typeof node !== "object" || node === null) return;
  const parent = node as LooseNode;
  if (!Array.isArray(parent.children)) return;

  for (const child of parent.children) {
    const candidate = child as LooseNode | null;
    if (
      typeof candidate !== "object" ||
      candidate === null ||
      candidate.type !== "text" ||
      typeof candidate.value !== "string"
    ) {
      strip(child);
      continue;
    }
    MARKER.lastIndex = 0;
    if (MARKER.test(candidate.value)) {
      candidate.value = stripCitationMarkers(candidate.value);
    }
  }
}

/**
 * A remark plugin that removes the markers from the prose.
 *
 * A plugin and not a string replace over the whole answer because an array
 * index inside a fenced block is a `code` node, not a `text` one, and must
 * survive: remark visits `text` nodes only.
 *
 * The tree is typed loosely on purpose: mdast's types are not a dependency
 * of this app.
 */
export function remarkStripCitationMarkers() {
  return (tree: unknown): void => strip(tree);
}
