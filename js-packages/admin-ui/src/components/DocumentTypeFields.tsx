import React from "react";
import { QueryResult, gql } from "@apollo/client";
import {
  DocTypeFieldsByParentQuery,
  Exact,
  FieldType,
  InputMaybe,
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
import { apolloClient } from "./apolloClient";
import { log } from "console";

gql`
  mutation DeleteDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID!) {
    removeDocTypeField(docTypeId: $documentTypeId, docTypeFieldId: $documentTypeFieldId) {
      right
    }
  }
`;

export const DocumentTypeFieldsParentQuery = gql`
  query DocTypeFieldsByParent($searchText: String, $parentId: BigInteger!) {
    docTypeFieldsByParent(parentId: $parentId, searchText: $searchText, first: 10) {
      edges {
        node {
          id
          name
          fieldName
          searchable
          exclude
          sortable
          parent {
            id
            fieldName
          }
        }
      }
    }
  }
`;

export function DocumentTypeFields() {
  const { documentTypeId } = useParams();

  const [allDocTypes, setAllDocTypes] = React.useState<Array<DocTypesQueryType>>([
    {
      idParent: 0,
      queryData: useDocTypeFieldsByParentQuery({
        variables: { searchText: "", parentId: 0 },
        skip: !documentTypeId,
      }),
      level: 0,
    },
  ]);
  const documentTypeFieldsParentsQuery = useDocTypeFieldsByParentQuery({
    variables: { searchText: "", parentId: 0 },
    skip: !documentTypeId,
  });
  const [deleteDocumentTypeFieldMutate] = useDeleteDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });
  const [updateDocumentTypeFieldMutate] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });

  async function addElement({ parentId }: { parentId: number }) {
    try {
      const { data } = await apolloClient.query({
        query: DocumentTypeFieldsParentQuery,
        variables: { searchText: "", parentId: parentId },
      });
      setAllDocTypes((prevAllDocTypes) => [
        ...prevAllDocTypes,
        {
          idParent: parentId,
          queryData: data,
          level: parentId,
        },
      ]);
    } catch (error) {
      console.error("Errore nella query:", error);
    }
  }
  if (!documentTypeId) throw new Error();

  return (
    <React.Fragment>
      <ContainerFluid>
        <ClayList>
          {documentTypeFieldsParentsQuery.data?.docTypeFieldsByParent?.edges?.map((documentType, index) => {
            return (
              <React.Fragment>
                <ClayList.Item flex className="align-items-center">
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
                          <button className="btn btn-unstyled nav-btn nav-btn-monospaced" style={{ width: "50px", height: "35px" }}>
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
                      <ClayList.QuickActionMenu.Item
                        aria-label="View sub doc type fields"
                        title="View sub doc type fields"
                        onClick={() => {
                          addElement({ parentId: Number(documentType?.node?.parent?.id || 0) });
                          findLevel({ docTypes: allDocTypes, addedId: Number(documentType?.node?.parent?.id || 0) });
                        }}
                        symbol="angle-right"
                      />
                    </ClayList.Item>
                  </ClayList.ItemField>
                </ClayList.Item>
              </React.Fragment>
            );
          })}
        </ClayList>
      </ContainerFluid>
    </React.Fragment>
  );
}

type DocTypesType = {
  idParent: number;
  element: React.ReactNode;
  level: number;
};

type DocTypesQueryType = {
  idParent: number;
  queryData: QueryResult<
    DocTypeFieldsByParentQuery,
    Exact<{
      searchText?: InputMaybe<string> | undefined;
      parentId: any;
    }>
  >;
  level: number;
};

function removeElementsByLevels(elements: DocTypesType[], levelSelect: number): DocTypesType[] {
  return elements.filter((elementInfo) => elementInfo.level <= levelSelect);
}

function findLevel({ docTypes, addedId }: { docTypes: Array<DocTypesQueryType>; addedId: number }): number {
  const docElement = docTypes.find((document) => document.idParent === addedId);
  console.log(docElement);

  return docElement?.level || -1;
}
