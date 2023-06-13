import ClayIcon from "@clayui/icon";
import { Route, Routes } from "react-router-dom";
import ClayButton from "@clayui/button";
import { getUserProfile, keycloak, useAuthentication } from "./authentication";
import DropDown from "@clayui/drop-down";
import React from "react";
import { DropDownCustom } from "./Form";

export function ApplicationBar({ isSideMenuOpen, onSideMenuToggle }: { isSideMenuOpen: boolean; onSideMenuToggle(isOpen: boolean): void }) {
  const { isAuthenticated } = useAuthentication();
  const [name, setName] = React.useState("");
  getUserProfile().then((data) => {
    setName(JSON.parse(JSON.stringify(data))?.name);
  });
  return (
    <div
      className="control-menu-container"
      style={{ paddingLeft: isSideMenuOpen ? "320px" : "", position: "sticky", top: "0px", zIndex: 2 }}
    >
      <nav className="application-bar application-bar-dark navbar navbar-expand-md" style={{ backgroundColor: "white" }}>
        <div className="container-fluid container-fluid-max-xl">
          <ul className="navbar-nav">
            <li className="nav-item">
              <button
                className="btn btn-unstyled nav-btn nav-btn-monospaced"
                style={{ width: "50px", height: "35px" }}
                onClick={() => {
                  onSideMenuToggle(!isSideMenuOpen);
                }}
              >
                <ClayIcon symbol={isSideMenuOpen ? "product-menu-open" : "product-menu-closed"} />
              </button>
            </li>
          </ul>
          <div className="navbar-title navbar-text-truncate" style={{ color: "black" }}>
            <Routes>
              <Route path="" element={"Dashboard"} />
              <Route path="buckets" element={"Buckets"} />
              <Route path="buckets/*" element={"Bucket"} />
              <Route path="data-sources" element={"Data Sources"} />
              <Route path="data-sources/*" element={"Data Source"} />
              <Route path="plugin-drivers" element={"Plugin Drivers"} />
              <Route path="plugin-drivers/*" element={"Plugin Driver"} />
              <Route path="document-types" element={"Document Types"} />
              <Route path="document-types/:_/document-type-fields/:_" element={"Document Type Field"} />
              <Route path="document-types/*" element={"Document Type"} />
              <Route path="search-configs" element={"Search Configs"} />
              <Route path="search-configs/:_/query-parsers/:_" element={"Query Parsers"} />
              <Route path="search-configs/*" element={"Search Config"} />
              <Route path="enrich-pipelines" element={"Enrich Pipelines"} />
              <Route path="enrich-pipelines/*" element={"Enrich Pipeline"} />
              <Route path="enrich-items" element={"Enrich Items"} />
              <Route path="enrich-items/*" element={"Enrich Item"} />
              <Route path="suggestion-categories" element={"Suggestion Categories"} />
              <Route path="suggestion-categories/*" element={"Suggestion Category"} />
              <Route path="query-analyses" element={"Query Analyses"} />
              <Route path="query-analyses/*" element={"Query Analysis"} />
              <Route path="tokenizers" element={"Tokenizers"} />
              <Route path="tokenizers/*" element={"Tokenizers"} />
              <Route path="token-filters" element={"Token Filters"} />
              <Route path="token-filters/*" element={"Token Filters"} />
              <Route path="char-filters" element={"Char Filters"} />
              <Route path="char-filters/*" element={"Char Filters"} />
              <Route path="analyzers" element={"Analyzers Filters"} />
              <Route path="analyzers/*" element={"Analyzers Filters"} />
              <Route path="monitoring-events" element={"Events"} />
              <Route path="rules" element={"Rules"} />
              <Route path="rules/*" element={"Rule"} />
              <Route path="annotators" element={"Annotators"} />
              <Route path="annotators/*" element={"Annotator"} />
              <Route path="tabs" element={"Tabs"} />
              <Route path="tabs/:_/tab-tokens/:_" element={"Tab Tokens"} />
              <Route path="tabs/*" element={"Tabs"} />
              <Route path="token-tabs" element={"Token Tabs"} />
              <Route path="token-tabs/*" element={"Token Tabs"} />
              <Route path="document-type-templates" element={"Document Type Templates"} />
              <Route path="document-type-templates/*" element={"Document Type Template"} />
              <Route path="logs" element={"Logs"} />
              <Route path="logs/*" element={"Logs"} />
              <Route path="maching-learning" element={"Maching Learning"} />
              <Route path="maching-learning/*" element={"Maching Learning"} />
              <Route path="wizards">
                <Route path="" element={"Connect your stuff"} />
                <Route path="sitemap" element={"Connect Site Map"} />
                <Route path="web-crawler" element={"Connect Web Crawler"} />
                <Route path="database" element={"Connect Database"} />
                <Route path="server-email" element={"Connect Server Email"} />
                <Route path="github" element={"Connect GitHub"} />
                <Route path="gitlab" element={"Connect GitLab"} />
                <Route path="liferay" element={"Connect Liferay"} />
                <Route path="google-drive" element={"Connect Google Drive"} />
                <Route path="dropbox" element={"Connect Dropbox"} />
              </Route>
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
                <DropDown
                  trigger={
                    <button
                      className="btn btn-unstyled nav-btn nav-btn-monospaced"
                      style={{ border: "1px solid #8F8F8F", width: "50px", height: "35px" }}
                    >
                      <ClayIcon symbol={"user"} style={{ color: "black" }} fontSize="25px" />
                    </button>
                  }
                >
                  <DropDown.ItemList>
                    <DropDown.Item>
                      <div style={{ display: "inline-block", width: "100%" }} onClick={() => {}}>
                        <ClayIcon symbol={"user"} style={{ color: "black" }} />
                        <span style={{ marginLeft: "10px" }}> {name}</span>
                      </div>
                    </DropDown.Item>
                    <DropDown.Divider />
                    <DropDown.Item>
                      <div
                        style={{ display: "inline-block", width: "100%" }}
                        onClick={() => {
                          keycloak.logout();
                        }}
                      >
                        <ClayIcon symbol={"sign-in"} style={{ color: "black" }} />
                        <span style={{ marginLeft: "10px" }}> Logout</span>
                      </div>
                    </DropDown.Item>
                  </DropDown.ItemList>
                </DropDown>
              )}
              <DropDownCustom />
            </li>
          </ul>
        </div>
      </nav>
    </div>
  );
}
