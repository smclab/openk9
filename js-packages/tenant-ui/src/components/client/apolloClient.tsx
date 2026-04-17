import { ApolloClient, InMemoryCache, HttpLink } from "@apollo/client";
import { relayStylePagination } from "@apollo/client/utilities";
import { getAccessToken } from "./oidcClient";

export const apolloClient = new ApolloClient({
  link: new HttpLink({
    uri: "/api/tenant-manager/graphql",
    async fetch(input, init?) {
      const token = await getAccessToken();
      if (token) {
        const headers = { ...(init ?? { headers: {} }).headers, Authorization: `Bearer ${token}` };
        return await fetch(input, { ...init, headers });
      } else {
        return await fetch(input, init);
      }
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

