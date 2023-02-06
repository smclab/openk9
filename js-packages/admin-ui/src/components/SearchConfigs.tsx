import React from "react";
import { gql } from "@apollo/client";
import { useCreateOrUpdateSearchConfigMutation, useDeleteSearchConfigMutation, useSearchConfigsQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";

export const SearchConfigsQuery = gql`
  query SearchConfigs($searchText: String, $cursor: String) {
    searchConfigs(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          minScore
          minScoreSuggestions
          minScoreSearch
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
  const [updateSearchConfigMutate] = useCreateOrUpdateSearchConfigMutation({
    refetchQueries: [SearchConfigsQuery],
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
          {
            header: "minScoreSuggestions",
            content: (searchConfig) => (
              <React.Fragment>
                <ClayToggle
                  toggled={searchConfig?.minScoreSuggestions ?? false}
                  onToggle={(schedulable) => {
                    if (searchConfig && searchConfig.id && searchConfig.name)
                      updateSearchConfigMutate({
                        variables: {
                          id: searchConfig.id,
                          minScoreSuggestions: !searchConfig.minScoreSuggestions,
                          name: searchConfig.name,
                          minScore: searchConfig.minScore ?? 0,
                          minScoreSearch: searchConfig.minScoreSearch ?? false,
                          description: searchConfig.description ?? "",
                        },
                      });
                  }}
                />
                <style type="text/css">{StyleToggle}</style>
              </React.Fragment>
            ),
          },
          {
            header: "min Score Search",
            content: (searchConfig) => (
              <React.Fragment>
                <ClayToggle
                  toggled={searchConfig?.minScoreSearch ?? false}
                  onToggle={(schedulable) => {
                    if (searchConfig && searchConfig.id && searchConfig.name)
                      updateSearchConfigMutate({
                        variables: {
                          id: searchConfig.id,
                          minScoreSuggestions: searchConfig.minScoreSuggestions ?? false,
                          name: searchConfig.name,
                          minScore: searchConfig.minScore ?? 0,
                          minScoreSearch: !searchConfig.minScoreSearch,
                          description: searchConfig.description ?? "",
                        },
                      });
                  }}
                />
                <style type="text/css">{StyleToggle}</style>
              </React.Fragment>
            ),
          },
        ]}
      />
    </React.Fragment>
  );
}
