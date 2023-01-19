import ClayIcon from "@clayui/icon";
import { Route, Routes } from "react-router-dom";
import ClayButton from "@clayui/button";
import { getUserProfile, keycloak, useAuthentication } from "./authentication";
import ClayDropDown from "@clayui/drop-down";
import React from "react";

export function ApplicationBar({ isSideMenuOpen, onSideMenuToggle }: { isSideMenuOpen: boolean; onSideMenuToggle(isOpen: boolean): void }) {
  const { isAuthenticated } = useAuthentication();
  const [name, setName] = React.useState("");
  getUserProfile().then((data) => {
    setName(JSON.parse(JSON.stringify(data))?.preferred_username);
  });
  return (
    <div
      className="control-menu-container"
      style={{ paddingLeft: isSideMenuOpen ? "320px" : "", position: "sticky", top: "0px", zIndex: 3 }}
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
                <ClayDropDown
                  trigger={
                    <button className="btn btn-unstyled nav-btn " style={{ border: "1px solid #8F8F8F", width: "50px", height: "35px" }}>
                      <ClayIcon symbol={"user"} style={{ color: "white" }} fontSize="25px" />
                    </button>
                  }
                >
                  <ClayDropDown.ItemList>
                    <style type="text/css">
                      {`
                          .dropdown-item:focus, .dropdown-item.focus
                          {
                           background-color: white;
                           box-shadow:none;
                          }
                      `}
                    </style>
                    <ClayDropDown.Item>
                      <React.Fragment>
                        <div style={{ display: "inline-block", width: "100%" }}>
                          <ClayIcon symbol={"user"} style={{ color: "black" }} />
                          <span style={{ marginLeft: "10px" }}> {name}</span>
                        </div>
                      </React.Fragment>
                    </ClayDropDown.Item>
                    <ClayDropDown.Divider />
                    <ClayDropDown.Item>
                      <div
                        style={{ display: "inline-block", width: "100%" }}
                        onClick={() => {
                          keycloak.logout();
                        }}
                      >
                        <ClayIcon symbol={"sign-in"} style={{ color: "black" }} />
                        <span style={{ marginLeft: "10px" }}> Logout</span>
                      </div>
                    </ClayDropDown.Item>
                  </ClayDropDown.ItemList>
                </ClayDropDown>
              )}
            </li>
          </ul>
        </div>
      </nav>
    </div>
  );
}
