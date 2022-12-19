import React from "react";
import { gql } from "@apollo/client";
import { useAnnotatorsQuery, useDeleteAnnotatosMutation } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const AnnotatorsQuery = gql`
  query Annotators($searchText: String, $cursor: String) {
    annotators(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          size
          type
          fieldName
          fuziness
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
  mutation DeleteAnnotatos($id: ID!) {
    deleteAnnotator(annotatorId: $id) {
      id
      name
    }
  }
`;

export function Annotators() {
  const annotatorsQuery = useAnnotatorsQuery();
  const showToast = useToast();
  const [deleteAnnotatorMutate] = useDeleteAnnotatosMutation({
    refetchQueries: [AnnotatorsQuery],
    onCompleted(data) {
      if (data.deleteAnnotator?.id) {
        showToast({ displayType: "success", title: "Annotators deleted", content: data.deleteAnnotator.name ?? "" });
      }
    },
  });

  return (
    <Table
      data={{
        queryResult: annotatorsQuery,
        field: (data) => data?.annotators,
      }}
      onCreatePath="/annotators/new"
      onDelete={(annotator) => {
        if (annotator?.id) deleteAnnotatorMutate({ variables: { id: annotator.id } });
      }}
      columns={[
        { header: "Name", content: (annotator) => formatName(annotator) },
        { header: "Description", content: (annotator) => annotator?.description },
        { header: "Field Name", content: (annotator) => annotator?.fieldName },
        { header: "Fuziness", content: (annotator) => annotator?.fuziness },
        { header: "Size", content: (annotator) => annotator?.size },
        { header: "Type", content: (annotator) => annotator?.type },
      ]}
    />
  );
}
