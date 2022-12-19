import ClayIcon from "@clayui/icon";
import { Route, Routes } from "react-router-dom";
import ClayButton, { ClayButtonWithIcon } from "@clayui/button";
import { keycloak, useAuthentication } from "./authentication";

export function ApplicationBar({ isSideMenuOpen, onSideMenuToggle }: { isSideMenuOpen: boolean; onSideMenuToggle(isOpen: boolean): void }) {
  const { isAuthenticated } = useAuthentication();
  return (
    <div
      className="control-menu-container"
      style={{ paddingLeft: isSideMenuOpen ? "320px" : "", position: "sticky", top: "0px", zIndex: 1 }}
    >
      <nav className="application-bar application-bar-dark navbar navbar-expand-md">
        <div className="container-fluid container-fluid-max-xl">
          <ul className="navbar-nav">
            <li className="nav-item">
              <button
                className="btn btn-unstyled nav-btn nav-btn-monospaced"
                onClick={() => {
                  onSideMenuToggle(!isSideMenuOpen);
                }}
              >
                <ClayIcon symbol={isSideMenuOpen ? "product-menu-open" : "product-menu-closed"} />
              </button>
            </li>
          </ul>
          <div className="navbar-title navbar-text-truncate">
            <Routes>
              <Route path="" element={"Dashboard"} />
              <Route path="tenants" element={"Tenant"} />
              <Route path="tenants/*" element={"Tenant"} />
              <Route path="process" element={"Process"} />
              <Route path="process/*" element={"Process"} />
            </Routes>
          </div>
          <ul className="navbar-nav">
            <li className="nav-item">
              {!isAuthenticated && (
                <ClayButton
                  onClick={() => {
                    keycloak.login();
                  }}
                >
                  Login
                </ClayButton>
              )}
              {isAuthenticated && (
                <ClayButtonWithIcon
                  symbol="logout"
                  displayType="danger"
                  onClick={() => {
                    keycloak.logout();
                  }}
                />
              )}
            </li>
          </ul>
        </div>
      </nav>
    </div>
  );
}
