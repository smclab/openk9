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
