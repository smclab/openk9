import { AppBar, Box, Toolbar, Typography } from "@mui/material";
import ThemeSwitcher from "utils/ThemeSwitcher";
import { Logo } from "../common/Logo";

interface NavbarProps {
  isDarkMode: boolean;
  toggleTheme: () => void;
}

export function Navbar({ isDarkMode, toggleTheme }: NavbarProps) {
  return (
    <AppBar 
      position="fixed" 
      sx={{ 
        zIndex: (theme) => theme.zIndex.drawer + 1,
        backgroundColor: 'background.paper',
        boxShadow: 1
      }}
    >
      <Toolbar>
        <Box display="flex" alignItems="center" flexGrow={1}>
          <Logo size={45} />
          <Typography variant="h6" ml={1} color="text.primary">
            Open
          </Typography>
          <Typography variant="h5" fontWeight={700} color="text.primary">
            K9
          </Typography>
        </Box>
       <ThemeSwitcher isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
      </Toolbar>
    </AppBar>
  );
} 