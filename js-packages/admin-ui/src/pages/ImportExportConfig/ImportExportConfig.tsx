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
import { ContainerFluid } from "@components/Form";
import { Box, Tab, Tabs, Typography } from "@mui/material";
import React from "react";
import { ExportTab } from "./ExportTab";
import { ImportTab } from "./ImportTab";

type TabId = "export" | "import";

/**
 * Moves the tenant configuration between environments: one tab to export it
 * selectively, one to import a package back.
 *
 * The two tabs are mounted one at a time on purpose — leaving the inactive one
 * mounted would keep an uploaded package and its report alive behind the export
 * form, and switching tab is exactly how an operator restarts from scratch.
 */
export function ImportExportConfig() {
  const [tab, setTab] = React.useState<TabId>("export");

  return (
    <ContainerFluid size="lg" flexColumn>
      <Box>
        <Typography component="h1" variant="h1" fontWeight="600">
          Import / Export configurations
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Export the tenant configuration selectively, or import it back: export by type, shallow or deep, dry run and a
          detailed report.
        </Typography>
      </Box>

      <Tabs
        value={tab}
        onChange={(_event, value: TabId) => setTab(value)}
        sx={{ borderBottom: "1px solid", borderColor: "divider" }}
      >
        <Tab value="export" label="Export configuration" id="import-export-tab-export" sx={{ textTransform: "none" }} />
        <Tab value="import" label="Import configuration" id="import-export-tab-import" sx={{ textTransform: "none" }} />
      </Tabs>

      <Box role="tabpanel" aria-labelledby={`import-export-tab-${tab}`}>
        {tab === "export" ? <ExportTab /> : <ImportTab />}
      </Box>
    </ContainerFluid>
  );
}
