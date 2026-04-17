import React from "react";
import { QueryClient } from "@tanstack/react-query";
import { OpenApiRestClient } from "../../openapi-generated";
import { getAccessToken } from "./oidcClient";

export const queryClient = new QueryClient();
const RestClientContext = React.createContext(
  new OpenApiRestClient({
    async HEADERS() {
      const token = await getAccessToken();
      if (token) {
        return { Authorization: `Bearer ${token}` };
      }
      return {} as any;
    },
  })
);
export function useRestClient() {
  return React.useContext(RestClientContext);
}
