import React from "react";
import { OpenK9Client } from "@openk9/rest-api";

export const OpenK9ClientProvider = React.createContext<
  ReturnType<typeof OpenK9Client>
>(null as any /* must break app if not provided */);

export function useOpenK9Client() {
  return React.useContext(OpenK9ClientProvider);
}
