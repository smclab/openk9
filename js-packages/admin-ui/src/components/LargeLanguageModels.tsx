import React from "react";
import { gql } from "@apollo/client";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import { useEnableLargeLanguageModelMutation, useLargeLanguageModelsQuery } from "../graphql-generated";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";

export const LargeLanguageModelsQuery = gql`
  query LargeLanguageModels($searchText: String, $cursor: String) {
    largeLanguageModels(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          enabled
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
  mutation EnableLargeLanguageModel($id: ID!) {
    enableLargeLanguageModel(id: $id) {
      id
      name
    }
  }
`;

export function LargeLanguageModels() {
  const showToast = useToast();
  const largeLanguageModels = useLargeLanguageModelsQuery();
  const [updateEnableLargeLaguageModel] = useEnableLargeLanguageModelMutation({
    refetchQueries: [LargeLanguageModelsQuery],
  });

  return (
    <Table
      data={{
        queryResult: largeLanguageModels,
        field: (data) => data?.largeLanguageModels,
      }}
      onCreatePath="create/new/"
      onDelete={(tenant) => {}}
      haveActions={false}
      columns={[
        { header: "Name", content: (tenant) => formatName(tenant) },
        { header: "Description", content: (tenant) => tenant?.description },
        {
          header: "Enabled",
          content: (largeLanguage) => (
            <React.Fragment>
              <ClayToggle
                toggled={largeLanguage?.enabled ?? false}
                onToggle={() => {
                  if (largeLanguage && largeLanguage.id && largeLanguage.name && !largeLanguage.enabled)
                    updateEnableLargeLaguageModel({
                      variables: { id: largeLanguage.id },
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
