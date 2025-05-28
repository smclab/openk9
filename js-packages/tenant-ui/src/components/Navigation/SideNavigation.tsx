import { Box, List } from "@mui/material";
import { SideNavigationItem } from "./SideNavigationItem";
import { menuItems } from "./menuItems";

interface SideNavigationProps {
  isSideMenuOpen: boolean;
}

export function SideNavigation({ isSideMenuOpen }: SideNavigationProps) {
  return (
    <List component="nav" sx={{ p: 1 }}>
      {menuItems.map((item: any, index: number) => (
        <SideNavigationItem key={index} item={item} />
      ))}
    </List>
  );
}
