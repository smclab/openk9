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
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";
import { Box } from "@mui/material";
import IconButton from "@mui/material/IconButton";
import React from "react";

interface ThemeSwitcherProps {
  isDarkMode: boolean;
  toggleTheme: () => void;
}

const ThemeSwitcher: React.FC<ThemeSwitcherProps> = ({ isDarkMode, toggleTheme }) => {
  const [hovered, setHovered] = React.useState(false);
  return (
    <IconButton
      onClick={toggleTheme}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      sx={{
        borderRadius: "10px",
        transition: "transform 0.3s ease",
        backgroundColor: hovered ? "rgba(0, 0, 0, 0.04)" : "transparent",
      }}
    >
      <Box
        sx={{
          display: "flex",
          transform: hovered ? "rotate(20deg)" : "rotate(0deg)",
          transition: "transform 0.3s ease",
        }}
      >
        {isDarkMode ? <DarkModeIcon /> : <LightModeIcon />}
      </Box>
    </IconButton>
  );
};

export default ThemeSwitcher;

