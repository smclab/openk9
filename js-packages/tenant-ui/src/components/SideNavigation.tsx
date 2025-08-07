import React from "react";
import { NavLink } from "react-router-dom";
import { BrandLogo } from "./BrandLogo";
import Drawer from "@mui/material/Drawer";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";
import Collapse from "@mui/material/Collapse";
import Typography from "@mui/material/Typography";
import Divider from "@mui/material/Divider";
import ExpandLess from "@mui/icons-material/ExpandLess";
import ExpandMore from "@mui/icons-material/ExpandMore";

export function SideNavigation({ isSideMenuOpen }: { isSideMenuOpen: boolean }) {
  const [elementSelect, setSelect] = React.useState("");

  return (
    <Drawer
      variant="persistent"
      anchor="left"
      open={isSideMenuOpen}
      sx={{
        width: 240,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: 240,
          boxSizing: "border-box",
          backgroundColor: "white",
          borderRight: "1px solid #00000017",
        },
      }}
    >
      <div style={{ display: "flex", alignItems: "center", padding: 16 }}>
        <BrandLogo height={32} width={32} />
        <Typography variant="h6" sx={{ marginLeft: 2, color: "black" }}>
          OpenK9 Admin
        </Typography>
      </div>
      <Divider />
      <List>
        <SideNavigationItem setSelect={setSelect} elementSelect={elementSelect} label="Dashboard" path="/" />
        <SideNavigationItem setSelect={setSelect} elementSelect={elementSelect} label="Tenant" path="/tenants" />
      </List>
    </Drawer>
  );
}

type SideNavigationItemProps = {
  label: string;
  path: string;
  setSelect: React.Dispatch<React.SetStateAction<string>>;
  elementSelect: string;
  isChild?: boolean;
};
function SideNavigationItem({ label, path, setSelect, elementSelect, isChild = false }: SideNavigationItemProps) {
  return (
    <ListItem disablePadding sx={{ pl: isChild ? 4 : 2 }}>
      <ListItemButton
        component={NavLink}
        to={path}
        selected={elementSelect === label}
        onClick={() => setSelect(label)}
        sx={{
          "&.Mui-selected": {
            borderLeft: "3px solid red",
            color: "black",
          },
        }}
      >
        <ListItemText primary={label} />
      </ListItemButton>
    </ListItem>
  );
}

type SideNavigationCollapsibleProps = {
  label: string;
  children: React.ReactNode;
};
function SideNavigationCollapsible({ label, children }: SideNavigationCollapsibleProps) {
  const [open, setOpen] = React.useState(false);

  return (
    <>
      <ListItemButton onClick={() => setOpen(!open)}>
        <ListItemText primary={label} />
        {open ? <ExpandLess /> : <ExpandMore />}
      </ListItemButton>
      <Collapse in={open} timeout="auto" unmountOnExit>
        <List component="div" disablePadding>
          {children}
        </List>
      </Collapse>
    </>
  );
}
