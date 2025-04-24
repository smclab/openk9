import { gql } from "@apollo/client";
import { formatName, Table } from "./Table";
import React from "react";
import { useDataIndicesQuery } from "../graphql-generated";

export default function DataIndices() {
  const dataIndicesQuery = useDataIndicesQuery({});

  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: dataIndicesQuery,
          field: (data) => data?.dataIndices,
        }}
        label="Data indices"
        onCreatePath=""
        viewAdd={false}
        haveActions={false}
        onDelete={() => {}}
        columns={[
          { header: "Name", content: (dataIndices) => formatName(dataIndices) },
          { header: "Description", content: (dataIndices) => dataIndices?.description },
        ]}
      />
    </React.Fragment>
  );
}

export const DataIndicesQuery = gql`
  query DataIndices($searchText: String, $cursor: String) {
    dataIndices(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;
