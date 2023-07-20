import ClayIcon from "@clayui/icon";
import React, { Dispatch, SetStateAction } from "react";
import { NavLink } from "react-router-dom";
import { BrandLogo } from "./BrandLogo";

export function SideNavigation({ isSideMenuOpen }: { isSideMenuOpen: boolean }) {
  const [select, setSelect] = React.useState("");
  return (
    <div className={`sidenav-fixed sidenav-menu-slider ${isSideMenuOpen ? "open" : "close"}`}>
      <div
        className="sidebar sidebar-dark sidenav-menu product-menu"
        style={{ backgroundColor: "white", borderRight: "1px solid #00000017" }}
      >
        <div className="sidebar-header" style={{ color: "black" }}>
          <div style={{ display: "inline-block", margin: "-16px 16px -16px 0px" }}>
            <BrandLogo height={32} width={32} />
          </div>
          OpenK9 Admin
        </div>
        <div className="sidebar-body">
          <nav className="menubar">
            <ul className="nav nav-nested " style={{ color: "#5c5d63" }}>
              <SideNavigationItem IsChildren={false} setSelect={setSelect} elementSelect={select} label="Dashboard" path="/" />
              <SideNavigationItem
                IsChildren={false}
                setSelect={setSelect}
                elementSelect={select}
                label="Connect your data source"
                path="/wizards"
              />
              <SideNavigationCollapsible label="Datasource configuration">
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Buckets" path="/buckets" />
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Data Sources"
                  path="/data-sources"
                />
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Plugin Drivers"
                  path="/plugin-drivers"
                />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Data Enrichment">
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Enrich Pipelines"
                  path="/enrich-pipelines"
                />
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Enrich Items"
                  path="/enrich-items"
                />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Mappings configuration">
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Document Types"
                  path="/document-types"
                />
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Document Type Templates"
                  path="/document-type-templates"
                />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Analyzers" path="/analyzers" />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Tokenizers" path="/tokenizers" />
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Token Filters"
                  path="/token-filters"
                />
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Char Filters"
                  path="/char-filters"
                />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Search Filters Configuration">
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Search Config"
                  path="/search-configs"
                />
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Suggestion Categories"
                  path="/suggestion-categories"
                />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Tabs" path="/tabs" />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Token tabs" path="/token-tabs" />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Languages" path="/languages" />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Query Analysis configuration">
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Query Analyses"
                  path="/query-analyses"
                />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Rules" path="/rules" />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Annotators" path="/annotators" />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Monitoring">
                <SideNavigationItem
                  IsChildren={true}
                  setSelect={setSelect}
                  elementSelect={select}
                  label="Events"
                  path="/monitoring-events"
                />
                <SideNavigationItem IsChildren={true} setSelect={setSelect} elementSelect={select} label="Logs" path="/logs" />
              </SideNavigationCollapsible>
              <SideNavigationItem
                IsChildren={false}
                setSelect={setSelect}
                elementSelect={select}
                label="Maching Learning"
                path="maching-learning"
              />
            </ul>
          </nav>
        </div>
      </div>
      <style type="text/css"> {StyleSideNavigation}</style>
    </div>
  );
}

type SideNavigationItemProps = {
  label: string;
  path: string;
  setSelect: Dispatch<SetStateAction<string>>;
  elementSelect: string;
  IsChildren: boolean;
};
function SideNavigationItem({ label, path, setSelect, elementSelect, IsChildren }: SideNavigationItemProps) {
  return (
    <React.Fragment>
      <li className="nav-item" style={{ display: "flex", alignItems: "center" }}>
        <NavLink
          to={path}
          className="nav-link "
          style={{ outline: "none", boxShadow: "none" }}
          onClick={() => {
            setSelect(label);
          }}
        >
          {elementSelect === label ? (
            <div>
              <span style={{ borderLeft: "3px solid red", marginLeft: IsChildren ? "-15px" : "" }}></span>
              <span style={{ marginLeft: "15px" }}>{label}</span>
            </div>
          ) : (
            label
          )}
        </NavLink>
      </li>
    </React.Fragment>
  );
}

type SideNavigationCollapsibleProps = {
  label: string;
  children: React.ReactNode;
};
function SideNavigationCollapsible({ label, children }: SideNavigationCollapsibleProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  return (
    <li className="nav-item ">
      <button
        className={`collapse-icon nav-link btn-unstyled ${!isOpen ? "collapsed" : ""}`}
        onClick={() => {
          setIsOpen(!isOpen);
        }}
      >
        {label}
        <span className="collapse-icon-closed">
          <ClayIcon symbol="caret-bottom" />
        </span>
        <span className="collapse-icon-open">
          <ClayIcon symbol="caret-top" />
        </span>
      </button>
      <div className={`collapse ${isOpen ? "show" : ""}`} id="navCollapse01">
        <ul className="nav nav-stacked">{children}</ul>
      </div>
    </li>
  );
}

export const StyleSideNavigation = `
.sidebar-dark .nav-nested .nav-link:hover {
  color: black;
}

.sidebar-dark .nav-nested .nav-link.active {
  color: black;
}

.sidebar-dark .nav-nested .nav-link {
  color: #686c99;
}
`;
