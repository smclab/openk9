import React from "react";
import { gql } from "@apollo/client";
import { TableWithSubFields } from "./Table";
import { useProcessesQuery } from "../graphql-generated";

export const ProcessQuery = gql`
  query Processes {
    backgroundProcesses {
      edges {
        node {
          id
          name
          createDate
          status
          modifiedDate
          processId
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

export function Process() {
  const processsQuery = useProcessesQuery();

  return (
    <TableWithSubFields
      data={{
        queryResult: processsQuery,
        field: (data) => data?.backgroundProcesses,
      }}
      onCreatePath="/buckets/new"
      onDelete={() => {}}
      columns={[
        { header: "Create Date", content: (tenant) => tenant?.createDate },
        { header: "Modify Date", content: (tenant) => tenant?.modifiedDate },
      ]}
      complessColumns="name"
      subColumns="status"
    />
  );
}
