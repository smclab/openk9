import { List } from "@mui/material";
import { SideNavigationItem } from "./SideNavigationItem";
import { menuItems } from "./menuItems";

interface SideNavigationProps {
  isSideMenuOpen: boolean;
}

export function SideNavigation({ isSideMenuOpen }: SideNavigationProps) {
  return (
    <List component="nav" sx={{ p: 1 }}>
      {menuItems.map((item, index) => (
        <SideNavigationItem key={index} item={item} />
      ))}
    </List>
  );
}
