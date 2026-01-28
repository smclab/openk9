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
import { Box, Card, Typography, useTheme } from "@mui/material";
import React from "react";

type DashboardCardProps = {
  children: React.ReactNode;
  sx?: object;
  title?: React.ReactNode;
};

export default function DashboardCard({ children, sx = {}, title }: DashboardCardProps) {
  const theme = useTheme();
  return (
    <Card
      sx={{
        borderRadius: "12px",
        p: "16px 14px",
        width: "100%",
        maxHeight: 265,
        overflow: "auto",
        ...sx,
      }}
    >
      {title && (
        <Box
          sx={{
            position: "sticky",
            top: 0,
            zIndex: 10,
            backgroundColor:
              theme.palette.mode === "dark" ? theme.palette.secondary.dark : theme.palette.background.default,
          }}
        >
          <Typography
            variant="h6"
            sx={{
              fontWeight: 600,
              color: theme.palette.text.primary,
              letterSpacing: 0.2,
              pb: 1,
            }}
          >
            {title}
          </Typography>
        </Box>
      )}
      <Box
        sx={{
          overflowY: "auto",
          flexGrow: 1,
        }}
      >
        {children}
      </Box>
    </Card>
  );
}

