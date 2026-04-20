import { ApolloProvider } from "@apollo/client";
import { Clear, Logout, Search } from "@mui/icons-material";
import { AppBar, Box, Button, InputAdornment, List, TextField, ThemeProvider, Toolbar, Typography } from "@mui/material";
import { QueryClientProvider } from "@tanstack/react-query";
import React, { useMemo, useState } from "react";
import { BrowserRouter, Outlet, Route, Routes, useNavigate } from "react-router-dom";
import { BrandLogo } from "./components/BrandLogo";
import { apolloClient } from "./components/client/apolloClient";
import { AuthProvider, RedirectIfAuthenticated, RequireAuth, useAuth } from "./components/client/authStore";
import { queryClient } from "./components/client/queryClient";
import { ModalProvider } from "./components/Modals";
import { useFilteredMenuItems } from "./components/Navigation/menuItems";
import { SideNavigationItem } from "./components/Navigation/SideNavigationItem";
import { DashBoard } from "./components/Page/Dashboard";
import { LoginPage } from "./components/Page/LoginPage";
import { Tenant } from "./components/Page/Tenant";
import { Tenants } from "./components/Page/Tenants";
import { SideNavigationContextProvider } from "./components/sideNavigationContext";
import { TenantCreate } from "./components/TenantCreate";
import { darkTheme, lightTheme } from "./components/Themes";
import ThemeSwitcher from "./components/ThemeSwitcher";
import { ToastProvider } from "./components/ToastProvider";
import "./index.css";

type LayoutProps = {
  isDarkMode: boolean;
  toggleTheme: () => void;
};

export default function App() {
  const savedTheme = localStorage.getItem("isDarkMode");
  const [isDarkMode, setIsDarkMode] = React.useState(savedTheme === "true");
  const memoizedTheme = useMemo(() => (isDarkMode ? darkTheme : lightTheme), [isDarkMode]);
  const toggleTheme = () => {
    const newTheme = !isDarkMode;
    setIsDarkMode(newTheme);
    localStorage.setItem("isDarkMode", newTheme.toString());
  };

  return (
    <ThemeProvider theme={memoizedTheme}>
      <QueryClientProvider client={queryClient}>
        <ApolloProvider client={apolloClient}>
          <ToastProvider>
            <ModalProvider>
              <BrowserRouter basename="/tenant">
                <AuthProvider>
                  <Routes>
                    <Route
                      path="/login"
                      element={
                        <RedirectIfAuthenticated>
                          <LoginPage />
                        </RedirectIfAuthenticated>
                      }
                    />
                    <Route
                      path="/"
                      element={
                        <RequireAuth>
                          <MainLayout isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
                        </RequireAuth>
                      }
                    >
                      <Route index element={<DashBoard />} />
                      <Route path="tenants">
                        <Route index element={<Tenants />} />
                        <Route path="tenant-create" element={<TenantCreate />} />
                        <Route path=":tenantId" element={<Tenant />} />
                      </Route>
                    </Route>
                  </Routes>
                </AuthProvider>
              </BrowserRouter>
            </ModalProvider>
          </ToastProvider>
        </ApolloProvider>
      </QueryClientProvider>
    </ThemeProvider>
  );
}

function MainLayout({ isDarkMode, toggleTheme }: LayoutProps) {
  const [searchTerm, setSearchTerm] = useState("");
  const filteredMenuItems = useFilteredMenuItems(searchTerm);
  const borderColor = isDarkMode ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";
  const { signOut } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    signOut();
    navigate("/login", { replace: true });
  };

  return (
    <SideNavigationContextProvider>
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          backgroundColor: isDarkMode ? "#1e1e1e" : "#f5f5f5",
          height: "100vh",
          padding: 2,
          boxSizing: "border-box",
        }}
      >
        <AppBar
          elevation={0}
          sx={{
            position: "static",
            backgroundColor: "background.paper",
            mb: 2,
            borderRadius: "8px",
            border: `1px solid ${borderColor}`,
            height: 56,
            zIndex: 2,
          }}
        >
          <Toolbar sx={{ minHeight: 56 }}>
            <Box display="flex" alignItems="center" justifyContent="space-between" width="100%">
              <Box display="flex" alignItems="center">
                <BrandLogo width={30} colorFill="#c22525" />
                <Typography variant="h6" ml={1} color="text.primary" fontWeight={700}>
                  Open
                </Typography>
                <Typography variant="h5" fontWeight={700} color="text.primary" ml={0.5}>
                  K9
                </Typography>
              </Box>
              <Box display="flex" alignItems="center" gap={1.5}>
                <ThemeSwitcher isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
                <Button
                  variant="outlined"
                  size="small"
                  startIcon={<Logout />}
                  onClick={handleLogout}
                  sx={{ textTransform: "none" }}
                >
                  Logout
                </Button>
              </Box>
            </Box>
          </Toolbar>
        </AppBar>

        <Box
          sx={{
            display: "flex",
            flexGrow: 1,
            maxHeight: "calc(100% - 64px)",
            overflow: "hidden",
          }}
        >
          <Box
            sx={{
              display: "flex",
              flexDirection: "column",
              width: "250px",
              mr: 2,
              flexShrink: 0,
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
                height: "100%",
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
          </Box>
          <Box
            component="main"
            sx={{
              flexGrow: 1,
              backgroundColor: "background.paper",
              borderRadius: "8px",
              boxShadow: "none",
              border: `1px solid ${borderColor}`,
              overflow: "auto",
              p: 2,
              minWidth: 0,
            }}
          >
            <Outlet />
          </Box>
        </Box>
      </Box>
    </SideNavigationContextProvider>
  );
}
