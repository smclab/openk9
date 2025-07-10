import React from "react";
import { QueryClient } from "@tanstack/react-query";
import { OpenApiRestClient } from "../openapi-generated/OpenApiRestClient";
import { keycloak } from "./authentication";

export const queryClient = new QueryClient();
const RestClientContext = React.createContext(
  new OpenApiRestClient({
    async HEADERS(options) {
      if (keycloak.authenticated) {
        await keycloak.updateToken(30);
        return { Authorization: `Bearer ${keycloak.token}` };
      } else {
        return {} as any;
      }
    },
  })
);
export function useRestClient() {
  return React.useContext(RestClientContext);
}
