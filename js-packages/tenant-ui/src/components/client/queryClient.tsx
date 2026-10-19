import React from "react";
import { QueryClient } from "@tanstack/react-query";
import { createConnector, createTenant, initTenant } from "../../openapi-generated";
import type { CreateConnectorRequest, CreateTenantRequest, InitTenantRequest } from "../../openapi-generated";
import { client } from "../../openapi-generated/client.gen";
import { getAuthHeader, touchSession } from "./authStore";

// The OpenAPI spec produced at build time carries bare paths, so prepend it here.
client.setConfig({ baseUrl: "/api/tenant-manager", headers: { Accept: "application/json" } });
client.interceptors.request.use((request) => {
  const authHeader = getAuthHeader();
  if (authHeader) request.headers.set("Authorization", authHeader);
  return request;
});
client.interceptors.response.use((response) => {
  try {
    if (response.ok) touchSession();
  } catch {
    // A failing touch must not reject a request the server already handled.
  }
  return response;
});
// The SDK throws the bare error body: wrap it in an Error with the status text the old client used.
const statusMessages: Record<number, string> = {
  400: "Bad Request",
  401: "Unauthorized",
  403: "Forbidden",
  404: "Not Found",
  500: "Internal Server Error",
  502: "Bad Gateway",
  503: "Service Unavailable",
};
client.interceptors.error.use((body, response) =>
  // Skip an already wrapped Error: HMR can register this interceptor twice.
  response && !response.ok && !(body instanceof Error)
    ? Object.assign(new Error(statusMessages[response.status] ?? "Generic Error"), { body })
    : body
);

const t = { throwOnError: true } as const;

export const queryClient = new QueryClient();
const RestClientContext = React.createContext({
  tenantProvisioning: {
    createConnector: (body: CreateConnectorRequest) => createConnector({ body, ...t }).then((r) => r.data),
    initTenant: (body: InitTenantRequest) => initTenant({ body, ...t }).then((r) => r.data),
    createTenant: (body: CreateTenantRequest) => createTenant({ body, ...t }).then((r) => r.data),
  },
});
export function useRestClient() {
  return React.useContext(RestClientContext);
}
