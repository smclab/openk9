import React from "react";
import { gql } from "@apollo/client";
import { useDocumentTypeTemplatesQuery, useDeleteDocumentTypeTemplateMutation } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const DocumentTypeTemplatesQuery = gql`
  query DocumentTypeTemplates($searchText: String, $cursor: String) {
    docTypeTemplates(searchText: $searchText, first: 25, after: $cursor) {
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

gql`
  mutation DeleteDocumentTypeTemplate($id: ID!) {
    deleteDocTypeTemplate(docTypeTemplateId: $id) {
      id
      name
    }
  }
`;

export function DocumentTypeTemplates() {
  const showToast = useToast();
  const docTypeTemplatesQuery = useDocumentTypeTemplatesQuery();
  const [deleteDocumentTypeTemplateMutate] = useDeleteDocumentTypeTemplateMutation({
    refetchQueries: [DocumentTypeTemplatesQuery],
    onCompleted(data) {
      if (data.deleteDocTypeTemplate?.id) {
        showToast({ displayType: "success", title: "Tempalate deleted", content: data.deleteDocTypeTemplate.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Document Type error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: docTypeTemplatesQuery,
        field: (data) => data?.docTypeTemplates,
      }}
      onCreatePath="/document-type-templates/new"
      onDelete={(documentTypeTemplate) => {
        if (documentTypeTemplate?.id) deleteDocumentTypeTemplateMutate({ variables: { id: documentTypeTemplate.id } });
      }}
      columns={[
        { header: "Name", content: (documentTypeTemplate) => formatName(documentTypeTemplate) },
        { header: "Description", content: (documentTypeTemplate) => documentTypeTemplate?.description },
      ]}
    />
  );
}
