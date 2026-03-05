import { ApolloClient, InMemoryCache, HttpLink } from "@apollo/client";
import { relayStylePagination } from "@apollo/client/utilities";

export const apolloClient = new ApolloClient({
  link: new HttpLink({
    uri: "/api/tenant-manager/graphql",
    async fetch(input, init?) {
      const basicToken = sessionStorage.getItem("basic_auth_token");
      if (basicToken) {
        const headers = { ...(init ?? { headers: {} }).headers, Authorization: `Basic ${basicToken}` };
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

