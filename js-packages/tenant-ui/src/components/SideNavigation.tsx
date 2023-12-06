import React, { Dispatch, SetStateAction } from "react";
import { NavLink } from "react-router-dom";
import { BrandLogo } from "./BrandLogo";
import ClayIcon from "@clayui/icon";

export function SideNavigation({ isSideMenuOpen }: { isSideMenuOpen: boolean }) {
  const [elementSelect, setSelect] = React.useState("");
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
              <SideNavigationItem setSelect={setSelect} elementSelect={elementSelect} IsChildren={false} label="Dashboard" path="/" />
              <SideNavigationItem setSelect={setSelect} elementSelect={elementSelect} IsChildren={false} label="Tenant" path="/tenants" />
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
