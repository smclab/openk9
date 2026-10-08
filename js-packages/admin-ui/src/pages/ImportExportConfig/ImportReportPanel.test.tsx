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
import { render, screen } from "@testing-library/react";
import { describe, expect, test } from "vitest";
import type { ImportReport } from "openapi-generated";
import { ImportReportPanel } from "./ImportReportPanel";

// Parsed from JSON as the backend sends it: an action without an id carries
// "existingId": null, which the generated type does not model.
const REPORT: ImportReport = JSON.parse(`{
  "dryRun": true,
  "applied": false,
  "mode": "SKIP",
  "actions": [
    { "ref": "ENRICH_ITEM-82", "type": "ENRICH_ITEM", "action": "SKIP", "existingId": 82 },
    { "ref": "ENRICH_PIPELINE_ITEM-83-82", "type": "ENRICH_PIPELINE_ITEM", "action": "SKIP", "existingId": null },
    { "ref": "ENRICH_PIPELINE-84", "type": "ENRICH_PIPELINE", "action": "CREATE" }
  ],
  "created": 1,
  "overwritten": 0,
  "skipped": 2,
  "missingReferences": [],
  "secretsToReenter": [],
  "blockingErrors": [],
  "resolvedIds": {}
}`);

describe("the planned actions of the report", () => {
  test("show the existing id only when there is one", () => {
    render(<ImportReportPanel report={REPORT} />);

    expect(screen.getByText(/ENRICH_ITEM-82/)).toHaveTextContent("(existing id 82)");
    expect(screen.getByText(/ENRICH_PIPELINE_ITEM-83-82/)).not.toHaveTextContent("existing id");
    expect(screen.getByText(/ENRICH_PIPELINE-84/)).not.toHaveTextContent("existing id");
  });
});
