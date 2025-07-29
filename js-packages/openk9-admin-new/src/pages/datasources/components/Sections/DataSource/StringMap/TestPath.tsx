import { JSONPath } from "jsonpath-plus";

export function testXPath(
  htmlOrXml: string,
  xpathExpr: string,
): {
  valid: boolean;
  matched: boolean;
  error?: string;
  results?: string[];
} {
  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(htmlOrXml, "text/xml");

    if (doc.getElementsByTagName("parsererror").length > 0) {
      return {
        valid: false,
        matched: false,
        error: "❌ Errore di parsing XML/HTML",
      };
    }

    const result = doc.evaluate(xpathExpr, doc, null, XPathResult.ANY_TYPE, null);
    let matched = false;
    const results: string[] = [];

    switch (result.resultType) {
      case XPathResult.STRING_TYPE:
        matched = !!result.stringValue;
        results.push(result.stringValue);
        break;
      case XPathResult.BOOLEAN_TYPE:
        matched = result.booleanValue;
        results.push(String(result.booleanValue));
        break;
      case XPathResult.NUMBER_TYPE:
        matched = !isNaN(result.numberValue);
        results.push(String(result.numberValue));
        break;
      case XPathResult.UNORDERED_NODE_ITERATOR_TYPE:
      case XPathResult.ORDERED_NODE_ITERATOR_TYPE: {
        let node = result.iterateNext();
        while (node) {
          matched = true;
          results.push((node as Element).outerHTML || node.textContent || "[nodo]");
          node = result.iterateNext();
        }
        break;
      }
      case XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE:
      case XPathResult.ORDERED_NODE_SNAPSHOT_TYPE: {
        for (let i = 0; i < result.snapshotLength; i++) {
          const node = result.snapshotItem(i);
          matched = true;
          results.push((node as Element).outerHTML || node?.textContent || "[nodo]");
        }
        break;
      }
      case XPathResult.ANY_UNORDERED_NODE_TYPE:
      case XPathResult.FIRST_ORDERED_NODE_TYPE:
        if (result.singleNodeValue) {
          matched = true;
          const node = result.singleNodeValue;
          results.push((node as Element).outerHTML || node.textContent || "[nodo]");
        }
        break;
    }

    return { valid: true, matched, results };
  } catch (e) {
    return {
      valid: false,
      matched: false,
      error: "❌ XPath non valido: " + (e as Error).message,
    };
  }
}

export function testJsonPath(
  jsonText: string,
  jsonPathExpr: string,
): {
  valid: boolean;
  matched: boolean;
  error?: string;
  results?: string[];
} {
  try {
    const obj = JSON.parse(jsonText);
    const results = JSONPath({ path: jsonPathExpr, json: obj });
    return {
      valid: true,
      matched: Array.isArray(results) && results.length > 0,
      results: results.map((r: any) => (typeof r === "object" ? JSON.stringify(r, null, 2) : String(r))),
    };
  } catch (e) {
    return {
      valid: false,
      matched: false,
      error: "❌ Errore JSONPath: " + (e as Error).message,
    };
  }
}
