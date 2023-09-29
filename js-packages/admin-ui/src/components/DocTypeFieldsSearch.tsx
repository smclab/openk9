import ClayToolbar from "@clayui/toolbar";
import { ContainerFluid, ContainerFluidWithoutView, EmptySpace } from "./Form";
import { ClayButtonWithIcon } from "@clayui/button";
import ClayTable from "@clayui/table";
import React from "react";
import { Link, useParams } from "react-router-dom";
import useDebounced from "./useDebounced";
import { useDocumentTypeFieldsQuery } from "../graphql-generated";
import { TableVirtuoso } from "react-virtuoso";

export function DocTypeFieldsSearch() {
  const { documentTypeId = "new", searchText = "" } = useParams();
  const [searchTextDoc, setSearchTextDoc] = React.useState(searchText);
  const searchTextDebounced = useDebounced(searchTextDoc);
  const documentTypeFieldsQuery = useDocumentTypeFieldsQuery({
    variables: { documentTypeId: documentTypeId, searchText: searchTextDebounced },
  });

  React.useEffect(() => {
    documentTypeFieldsQuery.refetch({ documentTypeId, searchText: searchTextDebounced });
  }, [searchTextDebounced]);

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
                  value={searchTextDoc}
                  onChange={(event) => {
                    setSearchTextDoc(event.currentTarget.value);
                  }}
                />
                {searchText !== "" && (
                  <ClayButtonWithIcon
                    aria-label=""
                    symbol="times"
                    className="component-action"
                    onClick={() => {
                      setSearchTextDoc("");
                    }}
                    style={{ position: "absolute", right: "10px", top: "0px" }}
                  />
                )}
              </div>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
      </ClayToolbar>
      <ContainerFluid>
        <TableVirtuoso
          totalCount={documentTypeFieldsQuery.data?.docTypeFieldsFromDocType?.edges?.length}
          style={{ height: "80vh" }}
          data={documentTypeFieldsQuery.data?.docTypeFieldsFromDocType?.edges as any}
          components={{
            Table: (props) => (
              <table
                {...props}
                style={{ ...props.style, tableLayout: "fixed" }}
                className="table table-hover show-quick-actions-on-Hover table-list"
              />
            ),
            EmptyPlaceholder: () => (
              <tbody>
                <tr>
                  <td colSpan={2} style={{ backgroundColor: "white" }}>
                    <EmptySpace description="There are no matching entities" title="No entities" extraClass="c-empty-state-animation" />
                  </td>
                </tr>
              </tbody>
            ),
          }}
          fixedHeaderContent={() => (
            <ClayTable.Row>
              <ClayTable.Cell headingCell headingTitle>
                <span className="text-truncate">Name</span>
              </ClayTable.Cell>
              <ClayTable.Cell headingCell headingTitle>
                <span className="text-truncate">Description</span>
              </ClayTable.Cell>
            </ClayTable.Row>
          )}
          itemContent={(index) => {
            const row = documentTypeFieldsQuery.data?.docTypeFieldsFromDocType?.edges?.[index]?.node ?? undefined;
            return (
              <React.Fragment>
                <ClayTable.Cell>
                  {row?.id && (
                    <Link
                      style={{
                        color: "#da1414",
                        textDecoration: "none",
                        font: "Helvetica",
                        fontWeight: "700",
                        fontSize: "15px",
                        lineHeight: "44px",
                      }}
                      to={"/document-types/" + documentTypeId + "/document-type-fields/" + row.id}
                    >
                      {row.name}
                    </Link>
                  )}
                </ClayTable.Cell>
                <ClayTable.Cell>{row?.description}</ClayTable.Cell>
              </React.Fragment>
            );
          }}
        />
      </ContainerFluid>
    </div>
  );
}
