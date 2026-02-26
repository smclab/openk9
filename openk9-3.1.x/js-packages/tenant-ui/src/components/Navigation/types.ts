import { NamePath } from "../sideNavigationContext";

export type MenuItem = {
  label: string;
  path?: string;
  IsChildren: boolean;
  value: NamePath;
  isGroup?: boolean;
  children?: MenuItem[];
}; 