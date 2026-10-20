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
import { Box, Card, Typography } from "@mui/material";
import React from "react";

type SettingsSectionProps = {
  icon: React.ReactNode;
  title: string;
  description: string;
  children: React.ReactNode;
};

/** One row of the settings page: a labelled left column and its controls. */
export function SettingsSection({ icon, title, description, children }: SettingsSectionProps) {
  return (
    <Card sx={{ p: 2.5 }}>
      <Box sx={{ display: "flex", flexDirection: { xs: "column", md: "row" }, gap: 2.5 }}>
        <Box sx={{ display: "flex", gap: 1.5, flex: "0 0 auto", width: { xs: "100%", md: 240 } }}>
          <Box sx={{ color: "primary.main", display: "flex", pt: 0.25 }}>{icon}</Box>
          <Box>
            <Typography component="h2" variant="h3" fontWeight="600">
              {title}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {description}
            </Typography>
          </Box>
        </Box>
        <Box sx={{ flex: 1, minWidth: 0 }}>{children}</Box>
      </Box>
    </Card>
  );
}
