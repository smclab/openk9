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
import { Box, Typography, useTheme } from "@mui/material";
import React from "react";

type DashboardInfoRowProps = {
  icon?: React.ReactNode;
  label: string;
  value?: React.ReactNode;
  borderColor?: string;
  children?: React.ReactNode;
  minHeight?: number | string;
  extraContent?: React.ReactNode;
  subtitle?: React.ReactNode;
};

export default function DashboardInfoRow({
  icon,
  label,
  value,
  borderColor,
  children,
  minHeight = 44,
  extraContent,
  subtitle,
}: DashboardInfoRowProps) {
  const theme = useTheme();
  return (
    <Box
      display="flex"
      alignItems="center"
      width="100%"
      sx={{
        borderLeft: borderColor ? `4px solid ${borderColor}` : undefined,
        pl: 1.5,
        minHeight,
        pr: 1,
      }}
    >
      {icon && <Box mr={1}>{icon}</Box>}
      <Box flex={1}>
        <Typography
          variant="subtitle2"
          sx={{
            fontWeight: 600,
            color: theme.palette.text.primary,
            lineHeight: 1.1,
          }}
        >
          {label}
        </Typography>
        {subtitle && (
          <Typography variant="caption" color="text.secondary" sx={{ display: "block", lineHeight: 1.1 }}>
            {subtitle}
          </Typography>
        )}
      </Box>
      {value}
      {children}
      {extraContent}
    </Box>
  );
}

