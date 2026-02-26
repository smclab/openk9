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
import CheckIcon from "@mui/icons-material/Check";
import { Box, ButtonBase, Card, CardContent, Grid, Typography, useTheme } from "@mui/material";
import { red } from "@mui/material/colors";
import { PluginDriverType, Provisioning } from "../../../../../graphql-generated";

interface PluginDriver {
  __typename?: "PluginDriver";
  id?: string | null;
  name?: string | null;
  description?: string | null;
  type?: PluginDriverType | null;
  provisioning?: Provisioning | null;
  host?: string | null;
  path?: string | null;
  port?: string | null;
  secure?: boolean | null;
  method?: string | null;
  jsonConfig?: string | null;
}

interface CardsProps {
  systemPluginDrivers: (PluginDriver | null)[] | undefined;
  disabled: boolean;
  activeCardId: string | null;
  setActiveCardId: (
    id: string | null,
    name: string | null,
    description?: string | null,
    host?: string | null,
    path?: string | null,
    port?: string | null,
    secure?: boolean | null,
    method?: string | null,
    provisioning?: Provisioning,
    pluginDriverType?: PluginDriverType,
    json?: string | null,
  ) => void;
}

export function PluginDriverCards({ systemPluginDrivers, disabled, activeCardId, setActiveCardId }: CardsProps) {
  const handleToggle = (
    id: string | null | undefined,
    name: string | null | undefined,
    description?: string | null,
    host?: string | null,
    path?: string | null,
    port?: string | null,
    secure?: boolean | null,
    method?: string | null,
    provisioning?: Provisioning,
    pluginDriverType?: PluginDriverType,
    json?: string | null,
  ) => {
    if (!id) return;

    setActiveCardId(
      activeCardId === id ? null : id,
      name || null,
      description,
      host,
      path,
      port,
      secure,
      method,
      provisioning,
      pluginDriverType,
      json,
    );
  };

  const theme = useTheme();

  const borderColor = theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";

  return (
    <Grid container spacing={3}>
      {systemPluginDrivers?.map((driver) => {
        if (!driver || !driver.id) return null;
        const isActive = activeCardId === driver.id;

        return (
          <Grid item xs={12} sm={6} md={4} key={driver.id}>
            <ButtonBase
              disabled={disabled}
              sx={{ width: "100%", borderRadius: "10px" }}
              onClick={() =>
                handleToggle(
                  driver.id,
                  driver.name,
                  driver.description,
                  driver.host,
                  driver.path,
                  driver.port,
                  driver.secure,
                  driver.method,
                  driver.provisioning || Provisioning.System,
                  driver.type || PluginDriverType.Http,
                  driver.jsonConfig,
                )
              }
            >
              <Card
                sx={{
                  position: "relative",
                  height: "150px",
                  display: "flex",
                  flex: 1,
                  flexDirection: "column",
                  justifyContent: "center",
                  alignItems: "center",
                  transition: "border 0.3s ease",
                  border: isActive ? `2px solid ${red[500]}` : `1px solid ${borderColor}`,
                  boxShadow: isActive ? "0 4px 8px rgba(0, 0, 0, 0.2)" : "none",
                }}
              >
                {isActive && (
                  <Box
                    sx={{
                      position: "absolute",
                      top: 10,
                      right: 10,
                      width: 20,
                      height: 20,
                      borderRadius: "50%",
                      backgroundColor: red[500],
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <CheckIcon sx={{ color: "white", fontSize: 16 }} />
                  </Box>
                )}
                <CardContent>
                  <Typography variant="h5" sx={{ opacity: disabled ? "0.3" : "unset" }}>
                    {driver.name || "Unnamed Plugin"}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" sx={{ opacity: disabled ? "0.3" : "unset" }}>
                    {driver.description || "No description available"}
                  </Typography>
                </CardContent>
              </Card>
            </ButtonBase>
          </Grid>
        );
      })}
    </Grid>
  );
}

