import { gql } from "@apollo/client";
import { useTenantsQuery } from "../graphql-generated";
import { formatVirtualHost, Table } from "./Table";
import React from "react";
import { useNavigate } from "react-router-dom";

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
  const navigate = useNavigate();

  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: bucketsQuery,
          field: (data) => data?.tenants,
        }}
        onCreatePath="/buckets/new"
        columns={[
          { header: "Virtual Host", content: (tenant) => tenant?.virtualHost },
          { header: "Create Date", content: (tenant) => tenant?.createDate },
          { header: "Modify Date", content: (tenant) => tenant?.modifiedDate },
        ]}
        onDelete={() => {}}
        rowActions={[
          {
            label: "View",
            action: (tenant) => {
              if (tenant?.id) navigate(`${tenant.id}`);
            },
          },
        ]}
      />
    </React.Fragment>
  );
}
