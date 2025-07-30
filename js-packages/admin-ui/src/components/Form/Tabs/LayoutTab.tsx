import { Box, Paper, Tab, Tabs, Typography } from "@mui/material";
import React, { useState } from "react";
import { TooltipDescription } from "../utils";

function a11yProps(tabId: string) {
  return {
    id: tabId,
  };
}

export default function AssociationsLayout({
  children,
  title = "Associations",
  tabs,
  setTabsId,
}: {
  children?: any;
  title?: string;
  tabs: Array<{ label: string; id: string; tooltip?: string }>;
  setTabsId: React.Dispatch<string>;
}) {
  const [value, setValue] = useState<string>(tabs[0]?.id || "");

  const handleChange = (event: any, newValue: string) => {
    setValue(newValue);
    setTabsId(newValue);
  };

  return (
    <Box sx={{ width: "100%", maxWidth: "100%" }}>
      <Typography
        variant="h6"
        sx={{
          p: 2,
          fontWeight: 600,
          color: "#333",
        }}
      >
        {title}
      </Typography>

      <Paper
        elevation={0}
        sx={{
          border: "1px solid #e0e0e0",
          borderRadius: "10px",
          width: "100%",
          overflow: "scroll",
        }}
      >
        <Box sx={{ borderBottom: "1px solid #e0e0e0" }}>
          <Tabs
            value={value}
            onChange={handleChange}
            aria-label="association tabs"
            variant="fullWidth"
            sx={{
              "& .MuiTab-root": {
                textTransform: "none",
                minHeight: 72,
                fontSize: "14px",
                fontWeight: 500,
                color: "#666",
                flexDirection: "column",
                gap: 0.5,
                "&.Mui-selected": {
                  color: "#d21919",
                  bgcolor: "#ba39392e",
                },
              },
              "& .MuiTabs-indicator": {
                backgroundColor: "#d21919",
                height: 2,
              },
            }}
          >
            {tabs.map((tab) => (
              <Tab
                key={tab.id}
                value={tab.id}
                label={
                  <Box display="flex" justifyContent="center" alignItems="center">
                    <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 0.5 }}>
                      <Typography variant="body1" fontWeight={600}>
                        {tab.label}
                      </Typography>
                    </Box>
                    {tab.tooltip && <TooltipDescription informationDescription={tab.tooltip} />}
                  </Box>
                }
                {...a11yProps(tab.id)}
              />
            ))}
          </Tabs>
          <Box p={2} display={"flex"} justifyContent={"center"}>
            {children}
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}
