import React from "react";
import { gql } from "@apollo/client";
import {
  FieldType,
  useCreateOrUpdateDocumentTypeFieldMutation,
  useDeleteDocumentTypeFieldMutation,
  useDocumentTypeFieldsQuery,
} from "../graphql-generated";
import { formatName, TableWithSubFields } from "./Table";
import { useParams } from "react-router-dom";
import { DocumentTypeFieldsQuery } from "./SubFieldsDocumentType";
import { ClayToggle } from "@clayui/form";
import { StyleToggle, StyleToggleDisabled } from "./Form";
import { ClayTooltipProvider } from "@clayui/tooltip";

gql`
  mutation DeleteDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID!) {
    removeDocTypeField(docTypeId: $documentTypeId, docTypeFieldId: $documentTypeFieldId) {
      right
    }
  }
`;

export function DocumentTypeFields() {
  const { documentTypeId } = useParams();
  const documentTypeFieldsQuery = useDocumentTypeFieldsQuery({
    variables: { documentTypeId: documentTypeId! },
    skip: !documentTypeId,
  });
  const [deleteDocumentTypeFieldMutate] = useDeleteDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });
  const [updateDocumentTypeFieldMutate] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });
  if (!documentTypeId) throw new Error();

  return (
    <React.Fragment>
      <TableWithSubFields
        data={{
          queryResult: documentTypeFieldsQuery,
          field: (data) => data?.docTypeFieldsFromDocType,
        }}
        label="Document Type Fields"
        onCreatePath="new"
        onCreatePathSubFields="newSubFields"
        onDelete={(documentTypeField) => {
          if (documentTypeField?.id) {
            deleteDocumentTypeFieldMutate({ variables: { documentTypeId: documentTypeId!, documentTypeFieldId: documentTypeField.id } });
          }
        }}
        id={documentTypeId}
        columns={[
          { header: "Name", content: (documentTypeField) => formatName(documentTypeField) },
          { header: "Description", content: (documentTypeField) => documentTypeField?.description },
          { header: "Field Type", content: (documentTypeField) => documentTypeField?.fieldType },
          { header: "Boost", content: (documentTypeField) => documentTypeField?.boost },
          {
            header: "Searchable",
            content: (documentTypeField) => (
              <React.Fragment>
                <ClayToggle
                  toggled={documentTypeField?.searchable ?? false}
                  onToggle={(searchable) => {
                    if (
                      documentTypeField &&
                      documentTypeField.id &&
                      documentTypeField.fieldType &&
                      documentTypeField.name &&
                      documentTypeField.fieldName
                    ) {
                      updateDocumentTypeFieldMutate({
                        variables: {
                          documentTypeFieldId: documentTypeField.id,
                          documentTypeId: documentTypeId,
                          searchable,
                          sortable: documentTypeField.sortable ?? false,
                          fieldType: documentTypeField.fieldType,
                          boost: documentTypeField.boost,
                          name: documentTypeField.name,
                          description: documentTypeField.description,
                          fieldName: documentTypeField.fieldName,
                        },
                      });
                    }
                  }}
                />
                <style type="text/css">{StyleToggle}</style>
              </React.Fragment>
            ),
          },
          {
            header: "Exclude",
            content: (documentTypeField) => (
              <ClayToggle
                toggled={documentTypeField?.exclude ?? false}
                onToggle={(exclude) => {
                  if (
                    documentTypeField &&
                    documentTypeField.id &&
                    documentTypeField.fieldType &&
                    documentTypeField.name &&
                    documentTypeField.fieldName &&
                    typeof documentTypeField.searchable === "boolean"
                  ) {
                    updateDocumentTypeFieldMutate({
                      variables: {
                        documentTypeFieldId: documentTypeField.id,
                        documentTypeId: documentTypeId,
                        exclude,
                        description: documentTypeField.description,
                        boost: documentTypeField.boost,
                        fieldType: documentTypeField.fieldType,
                        name: documentTypeField.name,
                        sortable: documentTypeField.sortable ?? false,
                        searchable: documentTypeField.searchable,
                        fieldName: documentTypeField.fieldName,
                      },
                    });
                  }
                }}
              />
            ),
          },
          {
            header: "Sortable",
            content: (documentTypeField) => (
              <React.Fragment>
                {documentTypeField?.fieldType !== FieldType.Date && documentTypeField?.fieldType !== FieldType.Keyword ? (
                  <ClayTooltipProvider autoAlign>
                    <span title={"Only Document Type Fields of type Keyword or Date are sortable."}>
                      <ClayToggle
                        toggled={documentTypeField?.sortable ?? false}
                        style={{
                          cursor: "not-allowed",
                        }}
                        onToggle={(sortable) => {
                          if (
                            (documentTypeField?.fieldType === FieldType.Date || documentTypeField?.fieldType === FieldType.Keyword) &&
                            documentTypeField &&
                            documentTypeField.id &&
                            documentTypeField.fieldType &&
                            documentTypeField.name &&
                            documentTypeField.fieldName &&
                            typeof documentTypeField.searchable === "boolean"
                          ) {
                            updateDocumentTypeFieldMutate({
                              variables: {
                                documentTypeFieldId: documentTypeField.id,
                                documentTypeId: documentTypeId,
                                exclude: documentTypeField.exclude,
                                description: documentTypeField.description,
                                fieldType: documentTypeField.fieldType,
                                name: documentTypeField.name,
                                sortable,
                                searchable: documentTypeField.searchable,
                                boost: documentTypeField.boost,
                                fieldName: documentTypeField.fieldName,
                              },
                            });
                          }
                        }}
                      />
                    </span>
                  </ClayTooltipProvider>
                ) : (
                  <ClayToggle
                    toggled={documentTypeField?.sortable ?? false}
                    onToggle={(sortable) => {
                      if (
                        documentTypeField &&
                        documentTypeField.id &&
                        documentTypeField.fieldType &&
                        documentTypeField.name &&
                        documentTypeField.fieldName &&
                        typeof documentTypeField.searchable === "boolean"
                      ) {
                        updateDocumentTypeFieldMutate({
                          variables: {
                            documentTypeFieldId: documentTypeField.id,
                            documentTypeId: documentTypeId,
                            exclude: documentTypeField.exclude,
                            description: documentTypeField.description,
                            fieldType: documentTypeField.fieldType,
                            name: documentTypeField.name,
                            sortable,
                            searchable: documentTypeField.searchable,
                            boost: documentTypeField.boost,
                            fieldName: documentTypeField.fieldName,
                          },
                        });
                      }
                    }}
                  />
                )}
              </React.Fragment>
            ),
          },
        ]}
      />
    </React.Fragment>
  );
}
