import React from "react";
import { gql } from "@apollo/client";
import {
  FieldType,
  useCreateOrUpdateDocumentTypeFieldMutation,
  useDeleteDocumentTypeFieldMutation,
  useDocTypeFieldsByParentQuery,
  useDocumentTypeFieldsQuery,
} from "../graphql-generated";
import { formatName, TableWithSubFields } from "./Table";
import { useParams } from "react-router-dom";
import { DocumentTypeFieldsQuery } from "./SubFieldsDocumentType";
import { ClayToggle } from "@clayui/form";
import { ContainerFluid, StyleToggle } from "./Form";
import { ClayTooltipProvider } from "@clayui/tooltip";
import ClayIcon from "@clayui/icon";
import ClayList from "@clayui/list";
import DropDown from "@clayui/drop-down";

gql`
  mutation DeleteDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID!) {
    removeDocTypeField(docTypeId: $documentTypeId, docTypeFieldId: $documentTypeFieldId) {
      right
    }
  }
`;

export const DocumentTypeFieldsParentQuery = gql`
  query DocTypeFieldsByParent($searchText: String) {
    docTypeFieldsByParent(parentId: 0, searchText: $searchText, first: 10) {
      edges {
        node {
          id
          name
          fieldName
          searchable
          exclude
          sortable
        }
      }
    }
  }
`;

export function DocumentTypeFields() {
  const { documentTypeId } = useParams();
  const documentTypeFieldsQuery = useDocumentTypeFieldsQuery({
    variables: { documentTypeId: documentTypeId! },
    skip: !documentTypeId,
  });
  const documentTypeFieldsParentsQuery = useDocTypeFieldsByParentQuery({
    variables: { searchText: "" },
    skip: !documentTypeId,
  });
  if (!documentTypeFieldsParentsQuery.loading) console.log(documentTypeFieldsParentsQuery.data?.docTypeFieldsByParent?.edges);

  const [deleteDocumentTypeFieldMutate] = useDeleteDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });
  const [updateDocumentTypeFieldMutate] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });
  if (!documentTypeId) throw new Error();

  return (
    <React.Fragment>
      <ContainerFluid>
        <ClayList>
          {documentTypeFieldsParentsQuery.data?.docTypeFieldsByParent?.edges?.map((documentType, index) => (
            <React.Fragment>
              <ClayList.Item flex>
                <ClayList.ItemField expand>
                  <ClayList.ItemTitle>{formatName({ id: documentType?.node?.id, name: documentType?.node?.name })}</ClayList.ItemTitle>
                </ClayList.ItemField>
                <ClayList.ItemField>
                  <ClayList.Item flex style={{ border: "none" }}>
                    <ClayList.QuickActionMenu.Item
                      aria-label="Plus doc type fields"
                      title="Plus doc type fields"
                      onClick={() => alert("Clicked the trash!")}
                      symbol="plus"
                    />

                    <DropDown
                      trigger={
                        <button
                          className="btn btn-unstyled nav-btn nav-btn-monospaced"
                          style={{ border: "1px solid #8F8F8F", width: "50px", height: "35px" }}
                        >
                          <ClayIcon symbol={"angle-down"} style={{ color: "black" }} />
                        </button>
                      }
                    >
                      <DropDown.ItemList>
                        <DropDown.Item>
                          <div style={{ display: "flex", justifyContent: "space-between" }}>
                            <div>
                              <label>Searchable</label>
                            </div>
                            <div>
                              <ClayToggle
                                toggled={documentType?.node?.searchable ?? false}
                                onToggle={(schedulable) => {
                                  // if (dataSource && dataSource.id && dataSource.name && dataSource.scheduling)
                                  //   updateDataSourceMutate({
                                  //     variables: {
                                  //       id: dataSource.id,
                                  //       schedulable,
                                  //       name: dataSource.name,
                                  //       scheduling: dataSource.scheduling,
                                  //       jsonConfig: dataSource.jsonConfig ?? "{}",
                                  //       description: dataSource.description ?? "",
                                  //       reindex: dataSource.reindex || false,
                                  //     },
                                  //   });
                                }}
                              />
                            </div>
                          </div>
                        </DropDown.Item>
                        <DropDown.Divider />
                        <DropDown.Item>
                          <div style={{ display: "flex", justifyContent: "space-between" }}>
                            <div>
                              <label>Exclude</label>
                            </div>
                            <div>
                              <ClayToggle
                                toggled={documentType?.node?.exclude ?? false}
                                onToggle={(schedulable) => {
                                  // if (dataSource && dataSource.id && dataSource.name && dataSource.scheduling)
                                  //   updateDataSourceMutate({
                                  //     variables: {
                                  //       id: dataSource.id,
                                  //       schedulable,
                                  //       name: dataSource.name,
                                  //       scheduling: dataSource.scheduling,
                                  //       jsonConfig: dataSource.jsonConfig ?? "{}",
                                  //       description: dataSource.description ?? "",
                                  //       reindex: dataSource.reindex || false,
                                  //     },
                                  //   });
                                }}
                              />
                            </div>
                          </div>
                        </DropDown.Item>
                        <DropDown.Divider />
                        <DropDown.Item>
                          {" "}
                          <div style={{ display: "flex", justifyContent: "space-between" }}>
                            <div>
                              <label>Sortable</label>
                            </div>
                            <div>
                              <ClayToggle
                                toggled={documentType?.node?.sortable ?? false}
                                onToggle={(schedulable) => {
                                  // if (dataSource && dataSource.id && dataSource.name && dataSource.scheduling)
                                  //   updateDataSourceMutate({
                                  //     variables: {
                                  //       id: dataSource.id,
                                  //       schedulable,
                                  //       name: dataSource.name,
                                  //       scheduling: dataSource.scheduling,
                                  //       jsonConfig: dataSource.jsonConfig ?? "{}",
                                  //       description: dataSource.description ?? "",
                                  //       reindex: dataSource.reindex || false,
                                  //     },
                                  //   });
                                }}
                              />
                            </div>
                          </div>
                        </DropDown.Item>
                      </DropDown.ItemList>
                    </DropDown>
                  </ClayList.Item>
                </ClayList.ItemField>
              </ClayList.Item>
            </React.Fragment>
          ))}
        </ClayList>
      </ContainerFluid>
    </React.Fragment>
  );
}
