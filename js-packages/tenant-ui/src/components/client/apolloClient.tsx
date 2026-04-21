import { ApolloClient, HttpLink, InMemoryCache } from "@apollo/client";
import { relayStylePagination } from "@apollo/client/utilities";
import { forceSignOut, getAuthHeader, touchSession } from "./authStore";

export const apolloClient = new ApolloClient({
  link: new HttpLink({
    uri: "/api/tenant-manager/graphql",
    async fetch(input, init?) {
      const authHeader = getAuthHeader();
      const headers = authHeader
        ? { ...(init?.headers ?? {}), Authorization: authHeader }
        : init?.headers;
      const response = await fetch(input, { ...init, headers });
      if (response.status === 401) {
        forceSignOut();
        if (!window.location.pathname.endsWith("/login")) {
          window.location.assign("/tenant/login");
        }
      } else if (response.ok) {
        touchSession();
      }
      return response;
    },
  }),
  cache: new InMemoryCache({
    typePolicies: {
      Query: {
        fields: {
          tenants: relayStylePagination(["searchText"]),
          datasources: relayStylePagination(["searchText"]),
          enrichItems: relayStylePagination(["searchText"]),
          enrichPipelines: relayStylePagination(["searchText"]),
          pluginDrivers: relayStylePagination(["searchText"]),
          suggestionCategories: relayStylePagination(["searchText"]),
          docTypes: relayStylePagination(["searchText"]),
        },
      },
      Tenant: {
        fields: {
          suggestionCategories: relayStylePagination(["searchText", "notEqual"]),
        },
      },
      EnrichPipeline: {
        fields: {
          enrichItems: relayStylePagination(["searchText", "not"]),
        },
      },
    },
  }),
});
