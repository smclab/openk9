import React from "react";
import { BrowserRouter, Routes, Route, NavLink } from "react-router-dom";
import { SideNavigation } from "./components/SideNavigation";
import "@clayui/css/lib/css/atlas.css";
import { Provider } from "@clayui/core";
import spritemap from "@clayui/css/lib/images/icons/icons.svg";
import { ApplicationBar } from "./components/ApplicationBar";
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

export default function App() {
  const [isSideMenuOpen, setIsSideMenuOpen] = React.useState(true);
  return (
    <AuthenticationProvider>
      <QueryClientProvider client={queryClient}>
        <ApolloProvider client={apolloClient}>
          <Provider spritemap={spritemap}>
            <ToastProvider>
              <BrowserRouter basename="/admin">
                <SideNavigation isSideMenuOpen={isSideMenuOpen} />
                <ApplicationBar isSideMenuOpen={isSideMenuOpen} onSideMenuToggle={setIsSideMenuOpen} />
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
          </Provider>
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
