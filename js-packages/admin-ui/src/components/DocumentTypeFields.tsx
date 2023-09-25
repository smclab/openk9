import React from "react";
import { QueryResult, gql, useQuery } from "@apollo/client";
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
import { Link, useParams } from "react-router-dom";
import { DocumentTypeFieldsQuery } from "./SubFieldsDocumentType";
import { ClayToggle } from "@clayui/form";
import { ContainerFluid, ContainerFluidWithoutView, StyleToggle } from "./Form";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { CategoricalChartFunc } from "recharts/types/chart/generateCategoricalChart";
import ClayIcon from "@clayui/icon";
import ClayList from "@clayui/list";
import DropDown from "@clayui/drop-down";
import { apolloClient } from "./apolloClient";
import ClayToolbar from "@clayui/toolbar";
import { ClayButtonWithIcon } from "@clayui/button";
import { ClassNameButton } from "../App";
import useDebounced from "./useDebounced";

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
          description
          fieldType
          boost
          searchable
          exclude
          fieldName
          jsonConfig
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

  const [selectedDocumentId, setSelectedDocumentId] = React.useState<string | null>(null);
  const [searchText, setSearchText] = React.useState("");

  const { data, loading, error } = useQuery(DocumentTypeFieldsParentQuery, {
    variables: {
      searchText: "",
      // parentId: Number(documentTypeId || "0"),
      parentId: 0,
    },
  });
  const [updateDoctype] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [DocumentTypeFieldsQuery, DocumentTypeFieldsParentQuery],
  });
  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  const handleDocumentClick = (documentId: string) => {
    setSelectedDocumentId(documentId);
  };

  return (
    <div>
      <ClayToolbar light>
        <ContainerFluidWithoutView>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand>
              <div style={{ position: "relative" }}>
                <ClayToolbar.Input
                  placeholder="Search..."
                  sizing="sm"
                  value={searchText}
                  onChange={(event) => {
                    setSearchText(event.currentTarget.value);
                  }}
                />
                {searchText !== "" && (
                  <ClayButtonWithIcon
                    aria-label=""
                    symbol="times"
                    className="component-action"
                    onClick={() => {
                      setSearchText("");
                    }}
                    style={{ position: "absolute", right: "10px", top: "0px" }}
                  />
                )}
              </div>
            </ClayToolbar.Item>
            <ClayToolbar.Item>
              <Link to={"/document-types/" + documentTypeId + "/document-type-fields/search-document-type-field/search/" + searchText}>
                <ClayButtonWithIcon className={`${ClassNameButton} btn-sm`} symbol="plus" aria-label="search" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
      </ClayToolbar>

      <ContainerFluid>
        <div style={{ display: "flex", background: "white", overflowX: "auto" }}>
          <ClayList style={{ marginBottom: "0", width: "400px" }}>
            {data.docTypeFieldsByParent.edges.map(
              ({
                node,
              }: {
                node: {
                  id: string;
                  name: string;
                  description: string;
                  fieldType: FieldType.Text;
                  boost: number;
                  searchable: boolean;
                  exclude: boolean;
                  fieldName: string;
                  jsonConfig: string;
                  sortable: boolean;
                };
              }) => (
                <ClayList.Item key={node.id} style={{ padding: "8px 8px" }} className={node.id === selectedDocumentId ? "selected" : ""}>
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                    {node?.id && (
                      <Link
                        style={{
                          color: "#da1414",
                          textDecoration: "none",
                          font: "Helvetica",
                          fontWeight: "700",
                          fontSize: "15px",
                          lineHeight: "44px",
                        }}
                        to={node.id}
                      >
                        {node.name}
                      </Link>
                    )}
                    <div style={{ display: "flex", gap: "3px" }}>
                      <div>
                        <Link to={`newSubFields/${node?.id}/new`}>
                          <button
                            style={{ background: "inherit", backgroundColor: "inherit", border: "none" }}
                            aria-label="Plus doc type fields"
                            title="Plus doc type fields"
                          >
                            <ClayIcon symbol={"plus"} />
                          </button>
                        </Link>
                      </div>
                      <div>
                        <DropDown
                          trigger={
                            <button
                              style={{ background: "inherit", backgroundColor: "inherit", border: "none" }}
                              aria-label="Open Drop Down for more option"
                              title="Open Drop Down"
                            >
                              <ClayIcon symbol={"angle-down"} />
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
                                    toggled={node?.searchable ?? false}
                                    onToggle={(searchable) => {
                                      if (node && node.id && node.name && node.fieldName && node.fieldType && documentTypeId)
                                        updateDoctype({
                                          variables: {
                                            documentTypeId: documentTypeId,
                                            documentTypeFieldId: node.id,
                                            name: node.name,
                                            description: node.description,
                                            fieldType: node.fieldType,
                                            boost: node.boost,
                                            searchable: searchable,
                                            exclude: node.exclude,
                                            fieldName: node.fieldName,
                                            jsonConfig: node.jsonConfig,
                                            sortable: node.sortable,
                                          },
                                        });
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
                                    toggled={node?.exclude ?? false}
                                    onToggle={(exclude) => {
                                      if (node && node.id && node.name && node.fieldName && node.fieldType && documentTypeId)
                                        updateDoctype({
                                          variables: {
                                            documentTypeId: documentTypeId,
                                            documentTypeFieldId: node.id,
                                            name: node.name,
                                            description: node.description,
                                            fieldType: node.fieldType,
                                            boost: node.boost,
                                            searchable: node.searchable,
                                            exclude: exclude,
                                            fieldName: node.fieldName,
                                            jsonConfig: node.jsonConfig,
                                            sortable: node.sortable,
                                          },
                                        });
                                    }}
                                  />
                                </div>
                              </div>
                            </DropDown.Item>
                            <DropDown.Divider />
                            <DropDown.Item>
                              <div style={{ display: "flex", justifyContent: "space-between" }}>
                                <div>
                                  <label>Sortable</label>
                                </div>
                                <div>
                                  <ClayToggle
                                    toggled={node?.sortable ?? false}
                                    onToggle={(sortable) => {
                                      if (node && node.id && node.name && node.fieldName && node.fieldType && documentTypeId)
                                        updateDoctype({
                                          variables: {
                                            documentTypeId: documentTypeId,
                                            documentTypeFieldId: node.id,
                                            name: node.name,
                                            description: node.description,
                                            fieldType: node.fieldType,
                                            boost: node.boost,
                                            searchable: node.searchable,
                                            exclude: node.exclude,
                                            fieldName: node.fieldName,
                                            jsonConfig: node.jsonConfig,
                                            sortable: sortable,
                                          },
                                        });
                                    }}
                                  />
                                </div>
                              </div>
                            </DropDown.Item>
                          </DropDown.ItemList>
                        </DropDown>
                      </div>
                      <div>
                        <button style={{ background: "inherit", backgroundColor: "inherit", border: "none" }}>
                          <ClayIcon symbol={"angle-right"} onClick={() => handleDocumentClick(node.id)} />
                        </button>
                      </div>
                    </div>
                  </div>
                </ClayList.Item>
              )
            )}
          </ClayList>
          {selectedDocumentId && <ChildListComponent documentId={selectedDocumentId} documentTypeId={documentTypeId} />}
        </div>
      </ContainerFluid>
    </div>
  );
}

interface ChildListComponentProps {
  documentId: string;
  documentTypeId: string | undefined;
}

const ChildListComponent: React.FC<ChildListComponentProps> = ({ documentId, documentTypeId }) => {
  const { data, loading, error } = useQuery(DocumentTypeFieldsParentQuery, {
    variables: {
      searchText: "",
      parentId: Number(documentId),
    },
  });

  const [updateDoctype] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [DocumentTypeFieldsQuery, DocumentTypeFieldsParentQuery],
  });

  const [selectedChildDocumentId, setSelectedChildDocumentId] = React.useState<string | null>(null);

  React.useEffect(() => {
    setSelectedChildDocumentId(null);
  }, [documentId]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  const childDocuments = data.docTypeFieldsByParent.edges;

  const handleChildClick = (childId: string) => {
    setSelectedChildDocumentId(childId);
  };

  return (
    <div>
      <div style={{ display: "flex" }}>
        <ClayList style={{ width: "400px" }}>
          {childDocuments.map(
            ({
              node,
            }: {
              node: {
                id: string;
                name: string;
                description: string;
                fieldType: FieldType.Text;
                boost: number;
                searchable: boolean;
                exclude: boolean;
                fieldName: string;
                jsonConfig: string;
                sortable: boolean;
              };
            }) => (
              <ClayList.Item
                key={node.id}
                style={{ cursor: "pointer", padding: "8px 8px" }}
                className={node.id === selectedChildDocumentId ? "selected" : ""}
              >
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  {node?.id && (
                    <Link
                      style={{
                        color: "#da1414",
                        textDecoration: "none",
                        font: "Helvetica",
                        fontWeight: "700",
                        fontSize: "15px",
                        lineHeight: "44px",
                      }}
                      to={node.id}
                    >
                      {node.name}
                    </Link>
                  )}
                  <div style={{ display: "flex", gap: "3px" }}>
                    <div>
                      <Link to={`newSubFields/${node?.id}/new`}>
                        <button
                          style={{ background: "inherit", backgroundColor: "inherit", border: "none" }}
                          aria-label="Plus doc type fields"
                          title="Plus doc type fields"
                        >
                          <ClayIcon symbol={"plus"} />
                        </button>
                      </Link>
                    </div>
                    <div>
                      <DropDown
                        trigger={
                          <button
                            style={{ background: "inherit", backgroundColor: "inherit", border: "none" }}
                            aria-label="Open Drop Down for more option"
                            title="Open Drop Down"
                          >
                            <ClayIcon symbol={"angle-down"} />
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
                                  toggled={node?.searchable ?? false}
                                  onToggle={(searchable) => {
                                    if (node && node.id && node.name && node.fieldName && node.fieldType && documentTypeId)
                                      updateDoctype({
                                        variables: {
                                          documentTypeId: documentTypeId,
                                          documentTypeFieldId: node.id,
                                          name: node.name,
                                          description: node.description,
                                          fieldType: node.fieldType,
                                          boost: node.boost,
                                          searchable: searchable,
                                          exclude: node.exclude,
                                          fieldName: node.fieldName,
                                          jsonConfig: node.jsonConfig,
                                          sortable: node.sortable,
                                        },
                                      });
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
                                  toggled={node?.exclude ?? false}
                                  onToggle={(exclude) => {
                                    if (node && node.id && node.name && node.fieldName && node.fieldType && documentTypeId)
                                      updateDoctype({
                                        variables: {
                                          documentTypeId: documentTypeId,
                                          documentTypeFieldId: node.id,
                                          name: node.name,
                                          description: node.description,
                                          fieldType: node.fieldType,
                                          boost: node.boost,
                                          searchable: node.searchable,
                                          exclude: exclude,
                                          fieldName: node.fieldName,
                                          jsonConfig: node.jsonConfig,
                                          sortable: node.sortable,
                                        },
                                      });
                                  }}
                                />
                              </div>
                            </div>
                          </DropDown.Item>
                          <DropDown.Divider />
                          <DropDown.Item>
                            <div style={{ display: "flex", justifyContent: "space-between" }}>
                              <div>
                                <label>Sortable</label>
                              </div>
                              <div>
                                <ClayToggle
                                  toggled={node?.sortable ?? false}
                                  onToggle={(sortable) => {
                                    if (node && node.id && node.name && node.fieldName && node.fieldType && documentTypeId)
                                      updateDoctype({
                                        variables: {
                                          documentTypeId: documentTypeId,
                                          documentTypeFieldId: node.id,
                                          name: node.name,
                                          description: node.description,
                                          fieldType: node.fieldType,
                                          boost: node.boost,
                                          searchable: node.searchable,
                                          exclude: node.exclude,
                                          fieldName: node.fieldName,
                                          jsonConfig: node.jsonConfig,
                                          sortable: sortable,
                                        },
                                      });
                                  }}
                                />
                              </div>
                            </div>
                          </DropDown.Item>
                        </DropDown.ItemList>
                      </DropDown>
                    </div>
                    <div>
                      <button style={{ background: "inherit", backgroundColor: "inherit", border: "none" }}>
                        <ClayIcon symbol={"angle-right"} onClick={() => handleChildClick(node.id)} />
                      </button>
                    </div>
                  </div>
                </div>
              </ClayList.Item>
            )
          )}
        </ClayList>
        {selectedChildDocumentId && <ChildListComponent documentId={selectedChildDocumentId} documentTypeId={documentTypeId} />}
      </div>
    </div>
  );
};
