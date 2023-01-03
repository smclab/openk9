import ClayIcon from "@clayui/icon";
import React from "react";
import { NavLink } from "react-router-dom";
import { BrandLogo } from "./BrandLogo";

export function SideNavigation({ isSideMenuOpen }: { isSideMenuOpen: boolean }) {
  return (
    <div className={`sidenav-fixed sidenav-menu-slider ${isSideMenuOpen ? "open" : "close"}`}>
      <div className="sidebar sidebar-dark sidenav-menu product-menu">
        <div className="sidebar-header">
          <div style={{ display: "inline-block", margin: "-16px 16px -16px 0px" }}>
            <BrandLogo size={32} />
          </div>
          OpenK9 Admin
        </div>
        <div className="sidebar-body">
          <nav className="menubar">
            <ul className="nav nav-nested">
              <SideNavigationItem label="Dashboard" path="/" />
              <SideNavigationItem label="Connect your data source" path="/wizards" />
              <SideNavigationCollapsible label="Datasource configuration">
                <SideNavigationItem label="Buckets" path="/buckets" />
                <SideNavigationItem label="Data Sources" path="/data-sources" />
                <SideNavigationItem label="Plugin Drivers" path="/plugin-drivers" />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Data Enrichment">
                <SideNavigationItem label="Enrich Pipelines" path="/enrich-pipelines" />
                <SideNavigationItem label="Enrich Items" path="/enrich-items" />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Mappings configuration">
                <SideNavigationItem label="Document Types" path="/document-types" />
                <SideNavigationItem label="Document Type Templates" path="/document-type-templates" />
                <SideNavigationItem label="Analyzers" path="/analyzers" />
                <SideNavigationItem label="Tokenizers" path="/tokenizers" />
                <SideNavigationItem label="Token Filters" path="/token-filters" />
                <SideNavigationItem label="Char Filters" path="/char-filters" />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Search Filters Configuration">
                <SideNavigationItem label="Search Config" path="/search-configs" />
                <SideNavigationItem label="Suggestion Categories" path="/suggestion-categories" />
                <SideNavigationItem label="Tabs" path="/tabs" />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Query Analysis configuration">
                <SideNavigationItem label="Query Analyses" path="/query-analyses" />
                <SideNavigationItem label="Rules" path="/rules" />
                <SideNavigationItem label="Annotators" path="/annotators" />
              </SideNavigationCollapsible>
              <SideNavigationCollapsible label="Monitoring">
                <SideNavigationItem label="Events" path="/monitoring-events" />
                <SideNavigationItem label="Logs" path="/logs" />
              </SideNavigationCollapsible>
              <SideNavigationItem label="Maching Learning" path="maching-learning" />
            </ul>
          </nav>
        </div>
      </div>
    </div>
  );
}

type SideNavigationItemProps = {
  label: string;
  path: string;
};
function SideNavigationItem({ label, path }: SideNavigationItemProps) {
  return (
    <li className="nav-item">
      <NavLink to={path} className="nav-link">
        {label}
      </NavLink>
    </li>
  );
}

type SideNavigationCollapsibleProps = {
  label: string;
  children: React.ReactNode;
};
function SideNavigationCollapsible({ label, children }: SideNavigationCollapsibleProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  return (
    <li className="nav-item">
      <button
        className={`collapse-icon nav-link btn-unstyled ${!isOpen ? "collapsed" : ""}`}
        onClick={() => {
          setIsOpen(!isOpen);
        }}
      >
        {label}
        <span className="collapse-icon-closed">
          <ClayIcon symbol="caret-right" />
        </span>
        <span className="collapse-icon-open">
          <ClayIcon symbol="caret-bottom" />
        </span>
      </button>
      <div className={`collapse ${isOpen ? "show" : ""}`} id="navCollapse01">
        <ul className="nav nav-stacked">{children}</ul>
      </div>
    </li>
  );
}
