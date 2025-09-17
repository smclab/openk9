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
