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
        backgroundColor: (theme) =>
          (theme.components?.MuiCard?.defaultProps?.sx as any)?.backgroundColor || theme.palette.background.paper,
        maxHeight: 265,
        overflow: "auto",
        ...sx,
      }}
    >
      {title && (
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
      )}
      {children}
    </Card>
  );
}
