import React from "react";
import { gql } from "@apollo/client";
import { useDeleteSuggestionCategoryMutation, useSuggestionCategoriesQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const SuggestionCategoriesQuery = gql`
  query SuggestionCategories($searchText: String, $cursor: String) {
    suggestionCategories(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          priority
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
  mutation DeleteSuggestionCategory($id: ID!) {
    deleteSuggestionCategory(suggestionCategoryId: $id) {
      id
      name
    }
  }
`;

export function SuggestionCategories() {
  const suggestionCategoriesQuery = useSuggestionCategoriesQuery();
  const showToast = useToast();
  const [deleteSuggestionCategoryMutate] = useDeleteSuggestionCategoryMutation({
    refetchQueries: [SuggestionCategoriesQuery],
    onCompleted(data) {
      if (data.deleteSuggestionCategory?.id) {
        showToast({ displayType: "success", title: "Suggestion category deleted", content: data.deleteSuggestionCategory.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Rule error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: suggestionCategoriesQuery,
        field: (data) => data?.suggestionCategories,
      }}
      onCreatePath="/suggestion-categories/new"
      onDelete={(suggestionCategory) => {
        if (suggestionCategory?.id) deleteSuggestionCategoryMutate({ variables: { id: suggestionCategory.id } });
      }}
      columns={[
        { header: "Name", content: (suggestionCategory) => formatName(suggestionCategory) },
        { header: "Description", content: (suggestionCategory) => suggestionCategory?.description },
        { header: "Priority", content: (suggestionCategory) => suggestionCategory?.priority },
      ]}
    />
  );
}
