import React from "react";
import { useLocation } from "react-router-dom";

export type NamePath = "admin" | "dashboard" | "tenant" | "tenants";

export const namePath: { label: NamePath; value: NamePath }[] = [
  { label: "admin", value: "admin" },
  { label: "dashboard", value: "dashboard" },
  { label: "tenant", value: "tenant" },
  { label: "tenants", value: "tenants" },
];

type SideNavigationContextValue = {
  changaSideNavigation: React.Dispatch<React.SetStateAction<NamePath>>;
  navigation: NamePath;
};

const AuthenticationContext = React.createContext<SideNavigationContextValue>(null as any);

export function SideNavigationContextProvider({ children }: { children: React.ReactNode }) {
  const params = useLocation().pathname.replace("/", "");
  const pathNavigate = namePath.find((item) => params.startsWith(item.label));
  const [navigation, changaSideNavigation] = React.useState<NamePath>(pathNavigate?.value || "dashboard");

  return <AuthenticationContext.Provider value={{ navigation, changaSideNavigation }}>{children}</AuthenticationContext.Provider>;
}

export function useSideNavigation() {
  return React.useContext(AuthenticationContext);
}
