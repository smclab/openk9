import { MenuItem } from "./types";

export const menuItems: MenuItem[] = [
  { label: "Dashboard", path: "/", IsChildren: false, value: "dashboard" },
  { label: "Tenant", path: "/tenants", IsChildren: false, value: "tenant" },
];

export const useFilteredMenuItems = (searchTerm: string) => {
  const flattenItems = (items: MenuItem[]): MenuItem[] => {
    return items.reduce((acc: MenuItem[], item) => {
      if (item.children) {
        return [...acc, ...flattenItems(item.children)];
      }
      return [...acc, item];
    }, []);
  };

  const filterItems = (items: MenuItem[]): MenuItem[] => {
    if (!searchTerm) return items;

    const flattenedItems = flattenItems(items);
    return flattenedItems.filter((item) => item.label.toLowerCase().includes(searchTerm.toLowerCase()));
  };

  return filterItems(menuItems);
};
