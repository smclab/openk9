/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { ApolloClient, InMemoryCache, HttpLink } from "@apollo/client";
import { relayStylePagination } from "@apollo/client/utilities";
import { keycloak } from "./authentication";

export const apolloClient = new ApolloClient({
  link: new HttpLink({
    uri: "/api/datasource/graphql",
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

