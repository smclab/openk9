import { Box, CircularProgress, Typography } from "@mui/material";
import React from "react";

export function LoadingOverlay() {
  return (
    <Box
      sx={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        backdropFilter: "blur(4px)",
        backgroundColor: "rgba(255, 255, 255, 0.7)",
        zIndex: 2000,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      <CircularProgress
        size={60}
        thickness={4}
        sx={{
          color: "primary.main",
        }}
      />
      <Typography
        sx={{
          mt: 3,
          letterSpacing: 2,
          fontSize: "1.2rem",
          fontWeight: 500,
          color: "primary.main",
          animation: "pulse 1.5s ease-in-out infinite",
          "@keyframes pulse": {
            "0%": { opacity: 0.6 },
            "50%": { opacity: 1 },
            "100%": { opacity: 0.6 },
          },
        }}
      >
        LOADING...
      </Typography>
    </Box>
  );
}
