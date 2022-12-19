import React from "react";
import { gql } from "@apollo/client";
import { useDeleteSearchConfigMutation, useSearchConfigsQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const SearchConfigsQuery = gql`
  query SearchConfigs($searchText: String, $cursor: String) {
    searchConfigs(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          minScore
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

gql`
  mutation DeleteSearchConfig($id: ID!) {
    deleteSearchConfig(searchConfigId: $id) {
      id
      name
    }
  }
`;

export function SearchConfigs() {
  const searchConfigsQuery = useSearchConfigsQuery();
  const showToast = useToast();
  const [deleteSearchConfigMutate] = useDeleteSearchConfigMutation({
    refetchQueries: [SearchConfigsQuery],
    onCompleted(data) {
      if (data.deleteSearchConfig?.id) {
        showToast({ displayType: "success", title: "Search config deleted", content: data.deleteSearchConfig.id ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Rule error", content: error.message ?? "" });
    },
  });

  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: searchConfigsQuery,
          field: (data) => data?.searchConfigs,
        }}
        onCreatePath="/search-configs/new"
        onDelete={(searchConfig) => {
          if (searchConfig?.id) deleteSearchConfigMutate({ variables: { id: searchConfig.id } });
        }}
        columns={[
          { header: "Name", content: (documentType) => formatName(documentType) },
          { header: "Description", content: (documentType) => documentType?.description },
          { header: "Min Score", content: (documentType) => documentType?.minScore },
        ]}
      />
    </React.Fragment>
  );
}
