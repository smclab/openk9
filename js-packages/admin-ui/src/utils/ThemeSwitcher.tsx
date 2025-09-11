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
