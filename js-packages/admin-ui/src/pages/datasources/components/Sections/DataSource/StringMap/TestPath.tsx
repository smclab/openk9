/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
        error: "âŒ Errore di parsing XML/HTML",
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
      error: "âŒ XPath non valido: " + (e as Error).message,
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
      error: "âŒ Errore JSONPath: " + (e as Error).message,
    };
  }
}

