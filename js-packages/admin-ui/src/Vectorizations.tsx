import { gql } from "@apollo/client";
import { useDeleteVectorIndexMutation, useVectorIndicesListQuery } from "./graphql-generated";
import { formatName, Table } from "./components/Table";
import React from "react";
import { useToast } from "./components/ToastProvider";

export default function Vectorizations() {
  const dataIndicesQuery = useVectorIndicesListQuery({});
  const showToast = useToast();

  const [deletevectorizationMutate] = useDeleteVectorIndexMutation({
    refetchQueries: [DataIndicesQuery],
    onCompleted(data) {
      if (data.deleteVectorIndex?.name) {
        showToast({ displayType: "success", title: "Vector index deleted", content: data.deleteVectorIndex.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Vector error", content: error.message ?? "" });
    },
  });
  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: dataIndicesQuery,
          field: (data) => data?.vectorIndices,
        }}
        label=""
        onCreatePath="new"
        onDelete={(vectorization) => {
          if (vectorization?.id) deletevectorizationMutate({ variables: { vectorIndexId: vectorization.id } });
        }}
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

export const deleteVectorization = gql`
  mutation deleteVectorIndex($vectorIndexId: ID!) {
    deleteVectorIndex(vectorIndexId: $vectorIndexId) {
      name
    }
  }
`;
