import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";
import IconButton from "@mui/material/IconButton";
import React from "react";

interface ThemeSwitcherProps {
  isDarkMode: boolean;
  toggleTheme: () => void;
}

const ThemeSwitcher: React.FC<ThemeSwitcherProps> = ({ isDarkMode, toggleTheme }) => {
  return (
    <IconButton
      sx={{
        borderRadius: "10px",
        transition: "transform 0.3s ease",
        "&:hover": {
          backgroundColor: "rgba(0, 0, 0, 0.04)",
          "& > *": {
            transform: "rotate(20deg)",
            transition: "transform 0.3s ease",
          },
        },
      }}
      onClick={toggleTheme}
    >
      {isDarkMode ? <DarkModeIcon /> : <LightModeIcon />}
    </IconButton>
  );
};

export default ThemeSwitcher;
