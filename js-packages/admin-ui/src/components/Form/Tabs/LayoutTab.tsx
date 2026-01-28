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
        variant="subtitle1"
        sx={{
          margin: "8px 0",
        }}
      >
        {title}
      </Typography>

      <Paper
        variant="outlined"
        elevation={0}
        sx={{
          borderRadius: "10px",
          width: "100%",
          overflow: "scroll",
        }}
      >
        <Box>
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

