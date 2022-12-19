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
              <SideNavigationItem label="Tenant" path="/tenants" />
              <SideNavigationItem label="Process" path="/process" />
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
