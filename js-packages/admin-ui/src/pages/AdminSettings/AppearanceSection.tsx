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
import DarkModeOutlinedIcon from "@mui/icons-material/DarkModeOutlined";
import LightModeOutlinedIcon from "@mui/icons-material/LightModeOutlined";
import PaletteOutlinedIcon from "@mui/icons-material/PaletteOutlined";
import { Box, FormControlLabel, Paper, Radio, RadioGroup, Typography } from "@mui/material";
import React from "react";
import { ThemeMode, useThemeMode } from "utils/themeMode";
import { SettingsSection } from "./SettingsSection";

type ModeOption = {
  mode: ThemeMode;
  label: string;
  description: string;
  icon: React.ReactNode;
};

const MODE_OPTIONS: ModeOption[] = [
  { mode: "dark", label: "Dark", description: "Use the dark theme.", icon: <DarkModeOutlinedIcon /> },
  { mode: "light", label: "Light", description: "Use the light theme.", icon: <LightModeOutlinedIcon /> },
];

/** Theme selector. The choice applies and is stored as soon as it is picked. */
export function AppearanceSection() {
  const { mode, setMode } = useThemeMode();

  const onModeChange = (_event: React.ChangeEvent<HTMLInputElement>, value: string) => {
    const option = MODE_OPTIONS.find((candidate) => candidate.mode === value);
    if (option) {
      setMode(option.mode);
    }
  };

  return (
    <SettingsSection
      icon={<PaletteOutlinedIcon />}
      title="Appearance"
      description="Choose how the OpenK9 interface looks."
    >
      <RadioGroup
        name="theme-mode"
        value={mode}
        onChange={onModeChange}
        sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)" }, gap: 1.5 }}
      >
        {MODE_OPTIONS.map((option) => (
          <Paper
            key={option.mode}
            variant="outlined"
            sx={{
              p: 1.5,
              borderColor: mode === option.mode ? "primary.main" : undefined,
              // An inset ring rather than a thicker border: changing borderWidth
              // would shift the tile content by 1px on every selection.
              boxShadow: mode === option.mode ? (theme) => `inset 0 0 0 1px ${theme.palette.primary.main}` : undefined,
            }}
          >
            <FormControlLabel
              value={option.mode}
              control={<Radio size="small" />}
              labelPlacement="start"
              sx={{ m: 0, width: "100%", justifyContent: "space-between", alignItems: "flex-start" }}
              label={
                <Box>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    {option.icon}
                    <Typography variant="h4" fontWeight="600">
                      {option.label}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {option.description}
                  </Typography>
                </Box>
              }
            />
          </Paper>
        ))}
      </RadioGroup>
    </SettingsSection>
  );
}
