import { ApolloClient, InMemoryCache, HttpLink } from "@apollo/client";
import { relayStylePagination } from "@apollo/client/utilities";
import { keycloak } from "./authentication";

export const apolloClient = new ApolloClient({
  link: new HttpLink({
    uri: "/graphql",
    async fetch(input, init?) {
      if (keycloak.authenticated) {
        await keycloak.updateToken(30);
        const headers = { ...(init ?? { headers: {} }).headers, Authorization: `Bearer ${keycloak.token}` };
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
