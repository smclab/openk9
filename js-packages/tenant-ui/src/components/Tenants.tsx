import { gql } from "@apollo/client";
import { useTenantsQuery } from "../graphql-generated";
import { formatVirtualHost, Table } from "./Table";
import React from "react";

const TenantsQuery = gql`
  query Tenants($searchText: String, $cursor: String) {
    tenants(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          virtualHost
          createDate
          modifiedDate
        }
      }
    }
  }
`;

export function Tenants() {
  const bucketsQuery = useTenantsQuery();
  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: bucketsQuery,
          field: (data) => data?.tenants,
        }}
        onCreatePath="/buckets/new"
        columns={[
          { header: "Virtual Host", content: (tenant) => formatVirtualHost(tenant) },
          { header: "Create Date", content: (tenant) => tenant?.createDate },
          { header: "Modify Date", content: (tenant) => tenant?.modifiedDate },
        ]}
      />
    </React.Fragment>
  );
}
