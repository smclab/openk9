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
import { Box, Typography } from "@mui/material";
import React from "react";
import { AppearanceSection } from "./AppearanceSection";
import { ImportExportSection } from "./ImportExportSection";

/**
 * The tenant-wide preferences of the administrator.
 *
 * Every control here acts on its own: the appearance preference is stored as
 * soon as it is picked, and import and export run immediately. There is no
 * page-level save step.
 */
export function AdminSettings() {
  return (
    <ContainerFluid size="lg" flexColumn>
      <Box>
        <Typography component="h1" variant="h1" fontWeight="600">
          Admin Settings
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage the global preferences of the tenant.
        </Typography>
      </Box>

      <AppearanceSection />
      <ImportExportSection />
    </ContainerFluid>
  );
}
