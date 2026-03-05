import React from "react";
import { QueryClient } from "@tanstack/react-query";
import { OpenApiRestClient } from "../../openapi-generated";

export const queryClient = new QueryClient();
const RestClientContext = React.createContext(
  new OpenApiRestClient({
    async HEADERS(options) {
      const basicToken = sessionStorage.getItem("basic_auth_token");
      if (basicToken) {
        return { Authorization: `Basic ${basicToken}` };
      } else {
        return {} as any;
      }
    },
  })
);
export function useRestClient() {
  return React.useContext(RestClientContext);
}

