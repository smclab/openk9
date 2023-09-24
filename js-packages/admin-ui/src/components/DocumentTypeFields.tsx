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
import { ContainerFluid, StyleToggle } from "./Form";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { CategoricalChartFunc } from "recharts/types/chart/generateCategoricalChart";
import ClayIcon from "@clayui/icon";
import ClayList from "@clayui/list";
import DropDown from "@clayui/drop-down";
import { apolloClient } from "./apolloClient";

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

  const [selectedDocumentId, setSelectedDocumentId] = React.useState<string | null>(null);

  const { data, loading, error } = useQuery(DocumentTypeFieldsParentQuery, {
    variables: {
      searchText: "",
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
      <ContainerFluid>
        <div style={{ display: "flex", background: "white" }}>
          <ClayList style={{ marginBottom: "0" }}>
            {data.docTypeFieldsByParent.edges.map(
              ({
                node,
              }: {
                node: { id: string; name: string; fieldName: string; searchable: boolean; exclude: boolean; sortable: boolean };
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
                        <button
                          style={{ background: "inherit", backgroundColor: "inherit", border: "none" }}
                          aria-label="Plus doc type fields"
                          title="Plus doc type fields"
                          onClick={() => alert("Clicked the add!")}
                        >
                          <ClayIcon symbol={"plus"} />
                        </button>
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
                                      //  if (node && node.id && node.name && node.exclude && node.fieldName && node.name)
                                      //    updateDoctype({
                                      //      variables: {
                                      //        documentTypeId:node.id,
                                      //        name:node.name,
                                      //        fieldName:node.fieldName,
                                      //        searchable:node.searchable,
                                      //        sortable:node.sortable,
                                      //      },
                                      //    });
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
                                    toggled={node?.sortable ?? false}
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
          {selectedDocumentId && <ChildListComponent documentId={selectedDocumentId} />}
        </div>
      </ContainerFluid>
    </div>
  );
}

interface ChildListComponentProps {
  documentId: string;
}

const ChildListComponent: React.FC<ChildListComponentProps> = ({ documentId }) => {
  const { data, loading, error } = useQuery(DocumentTypeFieldsParentQuery, {
    variables: {
      searchText: "",
      parentId: Number(documentId),
    },
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
        <ClayList>
          {childDocuments.map(({ node }: { node: { id: string; name: string } }) => (
            <ClayList.Item
              key={node.id}
              onClick={() => handleChildClick(node.id)}
              style={{ cursor: "pointer", padding: "8px 8px" }}
              className={node.id === selectedChildDocumentId ? "selected" : ""}
            >
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
            </ClayList.Item>
          ))}
        </ClayList>
        {selectedChildDocumentId && <ChildListComponent documentId={selectedChildDocumentId} />}
      </div>
    </div>
  );
};
