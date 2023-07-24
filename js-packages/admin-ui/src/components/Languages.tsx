import { gql } from "@apollo/client";
import React from "react";
import { useToast } from "./ToastProvider";
import { useDeleteLanguageMutation, useLanguagesQuery } from "../graphql-generated";
import { Table, formatName } from "./Table";
export const LanguagesQuery = gql`
  query Languages($searchText: String, $cursor: String) {
    languages(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          value
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
  mutation DeleteLanguage($id: ID!) {
    deleteLanguage(languageId: $id) {
      id
      name
    }
  }
`;

export function Languages() {
  const dataLanguagesQuery = useLanguagesQuery();
  const showToast = useToast();
  const [deleteDataLanguages] = useDeleteLanguageMutation({
    refetchQueries: [LanguagesQuery],
    onCompleted(data) {
      if (data.deleteLanguage?.id) {
        showToast({ displayType: "success", title: "Language deleted", content: data.deleteLanguage.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Language error", content: error.message ?? "" });
    },
  });
  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: dataLanguagesQuery,
          field: (data) => data?.languages,
        }}
        onCreatePath="/languages/new"
        onDelete={(dataLanguage) => {
          if (dataLanguage?.id) deleteDataLanguages({ variables: { id: dataLanguage.id } });
        }}
        columns={[
          { header: "Name", content: (datalanguage) => formatName(datalanguage) },
          { header: "Value", content: (dataSource) => dataSource?.value },
        ]}
      />
    </React.Fragment>
  );
}
