import React from "react";
import { QueryClient } from "@tanstack/react-query";
import { OpenApiRestClient } from "../../openapi-generated";
import { getAuthHeader } from "./authStore";

export const queryClient = new QueryClient();
const RestClientContext = React.createContext(
  new OpenApiRestClient({
    async HEADERS() {
      const authHeader = getAuthHeader();
      if (authHeader) {
        return { Authorization: authHeader };
      }
      return {} as any;
    },
  })
);
export function useRestClient() {
  return React.useContext(RestClientContext);
}
