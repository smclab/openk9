import { ApolloProvider } from "@apollo/client";
import { Clear, Search } from "@mui/icons-material";
import { AppBar, Box, List, TextField, ThemeProvider, Toolbar, Typography, InputAdornment, createTheme } from "@mui/material";
import { red } from "@mui/material/colors";
import { QueryClientProvider } from "@tanstack/react-query";
import React, { useMemo, useState } from "react";
import { BrowserRouter, NavLink, Route, Routes } from "react-router-dom";
import { apolloClient } from "./components/apolloClient";
import { AuthenticationProvider } from "./components/authentication";
import { DashBoard } from "./components/Dashboard";
import { useFilteredMenuItems } from "./components/Navigation/menuItems";
import { SideNavigationItem } from "./components/Navigation/SideNavigationItem";
import { queryClient } from "./components/queryClient";
import { Tenant } from "./components/Tenant";
import { TenantCreate } from "./components/TenantCreate";
import { Tenants } from "./components/Tenants";
import { ToastProvider } from "./components/ToastProvider";
import { SideNavigationContextProvider } from "./components/sideNavigationContext";
import "./index.css";

export default function App() {
  const [isSideMenuOpen, setIsSideMenuOpen] = React.useState(true);
  const savedTheme = localStorage.getItem("isDarkMode");
  const [isDarkMode, setIsDarkMode] = React.useState(savedTheme === "true");
  const memoizedTheme = useMemo(() => (isDarkMode ? darkTheme : lightTheme), [isDarkMode]);
  const [searchTerm, setSearchTerm] = useState("");
  const filteredMenuItems = useFilteredMenuItems(searchTerm);
  const borderColor = isDarkMode ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";

  return (
    <AuthenticationProvider>
      <QueryClientProvider client={queryClient}>
        <ApolloProvider client={apolloClient}>
          <ThemeProvider theme={memoizedTheme}>
            <ToastProvider>
              <BrowserRouter basename="/tenant">
                <SideNavigationContextProvider>
                  <Box
                    sx={{
                      display: "flex",
                      backgroundColor: isDarkMode ? "#1e1e1e" : "#f5f5f5",
                      minHeight: "100vh",
                      p: 0.5,
                    }}
                  >
                    <AppBar
                      position="fixed"
                      elevation={0}
                      sx={{
                        zIndex: (theme) => theme.zIndex.drawer + 1,
                        backgroundColor: "background.paper",
                        m: 0.5,
                        width: "calc(100% - 8px)",
                        borderRadius: "8px",
                        border: `1px solid ${borderColor}`,
                        height: 56,
                        justifyContent: "center",
                      }}
                    >
                      <Toolbar sx={{ minHeight: 56 }}>
                        <Box display="flex" alignItems="center" flexGrow={1}>
                          <Typography variant="h6" ml={1} color="text.primary" fontWeight={700}>
                            Open
                          </Typography>
                          <Typography variant="h5" fontWeight={700} color="text.primary" ml={0.5}>
                            K9
                          </Typography>
                        </Box>
                      </Toolbar>
                    </AppBar>

                    <Box
                      sx={{
                        flexShrink: 0,
                        mt: "64px",
                        mr: 1,
                        height: "calc(100vh - 72px)",
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "space-between",
                        width: "unset",
                      }}
                    >
                      <Box
                        sx={{
                          backgroundColor: "background.paper",
                          borderRadius: "8px",
                          boxShadow: "none",
                          border: `1px solid ${borderColor}`,
                          flexGrow: 1,
                          overflow: "auto",
                          width: "unset",

                          mb: 1,
                        }}
                      >
                        <TextField
                          fullWidth
                          size="small"
                          placeholder="Cerca sezione..."
                          value={searchTerm}
                          onChange={(e) => setSearchTerm(e.target.value)}
                          sx={{
                            p: 1,
                            "& .MuiInputBase-root": {
                              transition: "padding-left 0.3s ease-in-out",
                              paddingLeft: searchTerm ? "14px" : "40px",
                            },
                            width: "unset",
                          }}
                          autoComplete="off"
                          InputProps={{
                            startAdornment: (
                              <InputAdornment
                                position="start"
                                sx={{
                                  position: "absolute",
                                  left: "14px",
                                  transition: "opacity 0.3s ease-in-out",
                                  opacity: searchTerm ? 0 : 1,
                                  pointerEvents: searchTerm ? "none" : "auto",
                                  width: "unset",
                                }}
                              >
                                <Search />
                              </InputAdornment>
                            ),
                            endAdornment: searchTerm && (
                              <InputAdornment position="end">
                                <Clear onClick={() => setSearchTerm("")} sx={{ cursor: "pointer" }} />
                              </InputAdornment>
                            ),
                          }}
                        />
                        <List component="nav" sx={{ p: 1 }}>
                          {filteredMenuItems.map((item, index) => (
                            <SideNavigationItem key={index} item={item} />
                          ))}
                        </List>
                      </Box>
                      <Box
                        sx={{
                          backgroundColor: "background.paper",
                          borderRadius: "8px",
                          boxShadow: "none",
                          border: `1px solid ${borderColor}`,
                          p: 1.5,
                          display: "flex",
                          justifyContent: "center",
                        }}
                      ></Box>
                    </Box>

                    <Box
                      component="main"
                      sx={{
                        flexGrow: 1,
                        p: 2,
                        mt: "64px",
                        backgroundColor: "background.paper",
                        borderRadius: "8px",
                        boxShadow: "none",
                        border: `1px solid ${borderColor}`,
                        overflow: "auto",
                        height: "calc(100vh - 72px)",
                        position: "relative",
                        ml: 0,
                      }}
                    >
                      <AppRoutes />
                    </Box>
                  </Box>
                </SideNavigationContextProvider>
              </BrowserRouter>
            </ToastProvider>
          </ThemeProvider>
        </ApolloProvider>
      </QueryClientProvider>
    </AuthenticationProvider>
  );
}

function NavTabs({ tabs }: { tabs: Array<{ label: string; path: string }> }) {
  return (
    <div className="navbar navbar-underline navigation-bar navigation-bar-secondary navbar-expand-md" style={{ position: "sticky" }}>
      <div className="container-fluid container-fluid-max-xl">
        <ul className="navbar-nav">
          {tabs.map(({ label, path }, index) => {
            return (
              <li className="nav-item" key={index}>
                <NavLink className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`} to={path} end={true}>
                  <span className="navbar-text-truncate">{label}</span>
                </NavLink>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}

export const themeColor = {
  light: {
    primary: "#FFFFFF",
    secondary: "#FAFAFA",
  },
  dark: {
    primary: "#2c2a29",
    secondary: "#303030",
  },
  main: {
    default: red[500],
    lighter: red[100],
    darker: "#7f1818bf",
  },
};

const lightTheme = createTheme({
  shape: { borderRadius: 8 },
  components: {
    MuiTable: {
      styleOverrides: {
        root: { background: themeColor.light.secondary },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.light.secondary,
        },
      },
    },
    MuiCard: {
      defaultProps: {
        variant: "outlined",
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          "&:hover": {
            backgroundColor: themeColor.main.lighter,
            color: themeColor.dark.primary,
          },
          "&.active .MuiListItemText-root": {
            backgroundColor: themeColor.main.default,
            color: themeColor.light.primary,
            paddingLeft: "10px",
            borderRadius: "8px",
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: { background: themeColor.light.primary },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { background: "#333", padding: "10px" },
      },
    },
  },
  zIndex: { modal: 500, drawer: 100, snackbar: 510 },
  palette: {
    error: {
      main: red[500],
    },
    success: {
      main: "#2EC071",
    },
    warning: {
      main: "#F39C12",
    },
    info: {
      main: "#2980B9",
    },
    background: {
      paper: themeColor.light.secondary,
      default: themeColor.light.primary,
    },
    primary: {
      light: red[100],
      main: red[500],
      dark: red[800],
      contrastText: themeColor.light.primary,
    },
    secondary: {
      main: themeColor.light.primary,
    },
    mode: "light",
  },
  typography: {
    fontFamily: "Lato, sans-serif",
    h1: {
      fontSize: "2.125rem",
    },
    h2: {
      fontSize: "1.5rem",
      fontWeight: "bold",
    },
    h3: { fontSize: "1.17rem" },
    h4: { fontSize: "1rem" },
  },
});

const darkTheme = createTheme({
  shape: { borderRadius: 8 },
  components: {
    MuiSelect: {
      styleOverrides: {
        root: { background: themeColor.dark.secondary },
      },
    },
    MuiDialogContent: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary,
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary,
          borderRadius: "0 0 8px 8px",
        },
      },
    },
    MuiTable: {
      styleOverrides: {
        root: {},
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: "1px solid #555",
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary,
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary,
        },
      },
    },
    MuiTableBody: {
      styleOverrides: {
        root: {},
      },
    },
    MuiCard: {
      defaultProps: {
        variant: "outlined",
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          "&:hover": {
            backgroundColor: themeColor.main.darker,
            color: themeColor.light.primary,
          },
          "&.active .MuiListItemText-root": {
            paddingLeft: "10px",
            color: "#FFFFFF",
            borderRadius: "8px",
          },
          ".MuiListItemText-root": {
            backgrundColor: "transparent",
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: { background: themeColor.dark.primary },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          background: themeColor.dark.secondary,
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { background: "#FFFFFF", color: themeColor.dark.secondary, padding: "10px" },
      },
    },
  },
  zIndex: { modal: 500, drawer: 100, snackbar: 510 },
  palette: {
    mode: "dark",
    error: {
      main: red[500],
    },
    success: {
      main: "#2EC071",
    },
    warning: {
      main: "#F39C12",
    },
    info: {
      main: "#2980B9",
    },
    background: {
      paper: "#303030",
      default: "#2c2a29",
    },
    primary: {
      light: red[100],
      main: red[500],
      dark: red[800],
      contrastText: themeColor.light.primary,
    },
    secondary: {
      main: themeColor.light.primary,
    },
    text: {
      primary: "#FFFFFF",
      secondary: "#CCC",
      disabled: "#666",
    },
  },
  typography: {
    fontFamily: "Lato, sans-serif",
    subtitle1: {
      color: "#FFFFFF",
    },
    h1: {
      fontSize: "2.125rem",
      fontWeight: "bold",
      color: "#FFFFFF",
    },
    h2: {
      fontSize: "1.5rem",
      fontWeight: "bold",
      color: "#FFFFFF",
    },
    h3: { fontSize: "1.17rem", color: "#FFFFFF" },
    h4: { fontSize: "1rem", color: "#FFFFFF" },
    h5: { color: "#FFFFFF" },
    h6: { color: "#FFFFFF" },
    body1: {
      color: "#FFFFFF",
    },
    body2: {
      color: "#CCC",
    },
  },
});

const AppRoutes = () => (
  <Routes>
    <Route path="" element={<DashBoard />} />
    <Route path="tenants">
      <Route path="" element={<Tenants />} />
      <Route path="tenant-create" element={<TenantCreate />} />
      <Route path=":tenantId">
        <Route path="" element={<Tenant />}></Route>
      </Route>
    </Route>
  </Routes>
);
