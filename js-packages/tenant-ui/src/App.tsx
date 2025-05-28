import React, { useMemo } from "react";
import { BrowserRouter, Routes, Route, NavLink } from "react-router-dom";
import { SideNavigation } from "./components/SideNavigation";
import "@clayui/css/lib/css/atlas.css";
import spritemap from "@clayui/css/lib/images/icons/icons.svg";
import "./index.css";
import { DashBoard } from "./components/Dashboard";
import { ToastProvider } from "./components/ToastProvider";
import { QueryClientProvider } from "@tanstack/react-query";
import { apolloClient } from "./components/apolloClient";
import { ApolloProvider } from "@apollo/client";
import { queryClient } from "./components/queryClient";
import { AuthenticationProvider } from "./components/authentication";
import { Tenants } from "./components/Tenants";
import { Tenant } from "./components/Tenant";
import { TenantCreate } from "./components/TenantCreate";
import { createTheme, ThemeProvider } from "@mui/material";
import { red } from "@mui/material/colors";

export default function App() {
  const [isSideMenuOpen, setIsSideMenuOpen] = React.useState(true);
  const savedTheme = localStorage.getItem("isDarkMode");
  const [isDarkMode, setIsDarkMode] = React.useState(savedTheme === "true");
  const memoizedTheme = useMemo(() => (isDarkMode ? darkTheme : lightTheme), [isDarkMode]);

  return (
    <AuthenticationProvider>
      <QueryClientProvider client={queryClient}>
        <ApolloProvider client={apolloClient}>
          <ThemeProvider theme={memoizedTheme}>
            <ToastProvider>
              <BrowserRouter basename="/tenant">
                <SideNavigation isSideMenuOpen={isSideMenuOpen} />
                <div style={{ paddingLeft: isSideMenuOpen ? "320px" : "0px" }}>
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
                </div>
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
  shape: { borderRadius: 10 },
  components: {
    MuiTable: {
      styleOverrides: {
        root: { background: themeColor.light.secondary },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.light.secondary, // Colore di sfondo per l'intestazione
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
    // h5: { fontSize: "0.83rem" },
    // h6: { fontSize: "0.67rem" },
  },
});

const darkTheme = createTheme({
  shape: { borderRadius: 10 },
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
          borderRadius: "0 0 10px 10px",
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
          borderBottom: "1px solid #555", // Colore grigio scuro per le righe della tabella
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary, // Colore di sfondo per l'intestazione
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
      paper: "#303030", // Sfondo più scuro
      default: "#2c2a29", // Sfondo generale scuro
    },
    primary: {
      light: red[100],
      main: red[500],
      dark: red[800],
      contrastText: themeColor.light.primary,
    },
    secondary: {
      main: themeColor.light.primary, // Secondario chiaro per contrasto
    },
    text: {
      primary: "#FFFFFF", // Testo chiaro per il tema scuro
      secondary: "#CCC", // Testo secondario meno prominente
      disabled: "#666", // Testo disabilitato chiaro
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
      color: "#FFFFFF", // Colore per i titoli principali
    },
    h2: {
      fontSize: "1.5rem",
      fontWeight: "bold",
      color: "#FFFFFF", // Colore per i titoli secondari
    },
    h3: { fontSize: "1.17rem", color: "#FFFFFF" },
    h4: { fontSize: "1rem", color: "#FFFFFF" },
    h5: { color: "#FFFFFF" },
    h6: { color: "#FFFFFF" },
    body1: {
      color: "#FFFFFF", // Colore per il testo del corpo
    },
    body2: {
      color: "#CCC", // Colore per il testo secondario
    },
  },
});
