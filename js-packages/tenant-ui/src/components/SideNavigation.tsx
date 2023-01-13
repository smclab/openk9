import React, { Dispatch, SetStateAction } from "react";
import { NavLink } from "react-router-dom";
import { BrandLogo } from "./BrandLogo";

export function SideNavigation({ isSideMenuOpen }: { isSideMenuOpen: boolean }) {
  const [elementSelect, setSelect] = React.useState("");
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
              <SideNavigationItem setSelect={setSelect} elementSelect={elementSelect} IsChildren={false} label="Dashboard" path="/" />
              <SideNavigationItem setSelect={setSelect} elementSelect={elementSelect} IsChildren={false} label="Tenant" path="/tenants" />
              <SideNavigationItem setSelect={setSelect} elementSelect={elementSelect} IsChildren={false} label="Process" path="/process" />
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
  setSelect: Dispatch<SetStateAction<string>>;
  elementSelect: string;
  IsChildren: boolean;
};
function SideNavigationItem({ label, path, setSelect, elementSelect, IsChildren }: SideNavigationItemProps) {
  return (
    <li className="nav-item">
      <NavLink
        to={path}
        className="nav-link"
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
  );
}
