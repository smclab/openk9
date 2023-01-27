import React from "react";
import { gql } from "@apollo/client";
import {
  useCreateOrUpdateSuggestionCategoryMutation,
  useDeleteSuggestionCategoryMutation,
  useSuggestionCategoriesQuery,
} from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";

export const SuggestionCategoriesQuery = gql`
  query SuggestionCategories($searchText: String, $cursor: String) {
    suggestionCategories(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          priority
          multiSelect
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
  const [updateSuggestionCategoryMutate] = useCreateOrUpdateSuggestionCategoryMutation({
    refetchQueries: [SuggestionCategoriesQuery],
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
        {
          header: "Multi Select",
          content: (suggestionCategories) => (
            <React.Fragment>
              <ClayToggle
                toggled={suggestionCategories?.multiSelect ?? false}
                onToggle={(multiSelect) => {
                  if (suggestionCategories && suggestionCategories.id && suggestionCategories.name)
                    updateSuggestionCategoryMutate({
                      variables: {
                        id: suggestionCategories?.id,
                        multiSelect: !suggestionCategories?.multiSelect,
                        name: suggestionCategories?.name,
                        priority: suggestionCategories?.priority || 0,
                        description: suggestionCategories?.description ?? "",
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
  );
}
