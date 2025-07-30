import { Refresh as RefreshIcon } from "@mui/icons-material";
import { Paper, Stack, Typography } from "@mui/material";
import React from "react";

interface RefreshOptionsLayoutProps {
  children: React.ReactNode;
}

const RefreshOptionsLayout: React.FC<RefreshOptionsLayoutProps> = ({ children }) => {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        borderRadius: 2,
        borderColor: "grey.300",
        mb: "16px",
      }}
    >
      <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
        <Stack direction="row" spacing={1} alignItems="center">
          <RefreshIcon sx={{ fontSize: 16, color: "grey.600" }} />
          <Typography
            variant="body2"
            sx={{
              fontWeight: 500,
              color: "grey.700",
            }}
          >
            Refresh on:
          </Typography>
        </Stack>

        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
          {children}
        </Stack>
      </Stack>
    </Paper>
  );
};

export default RefreshOptionsLayout;
