import { gql } from "@apollo/client";
import { useVectorIndicesListQuery } from "./graphql-generated";
import { formatName, Table } from "./components/Table";
import React from "react";

export default function Vectorizations() {
  const dataIndicesQuery = useVectorIndicesListQuery({});

  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: dataIndicesQuery,
          field: (data) => data?.vectorIndices,
        }}
        label=""
        onCreatePath="new"
        haveActions={false}
        onDelete={() => {}}
        columns={[
          { header: "Name", content: (vectorization) => formatName(vectorization) },
          { header: "Description", content: (vectorization) => vectorization?.description },
        ]}
      />
    </React.Fragment>
  );
}

export const DataIndicesQuery = gql`
  query vectorIndicesList($searchText: String, $cursor: String) {
    vectorIndices(searchText: $searchText, first: 25, after: $cursor) {
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
