import ClayToolbar from "@clayui/toolbar";
import { ContainerFluidWithoutView } from "./Form";
import { ClayButtonWithIcon } from "@clayui/button";
import React from "react";
import { useParams } from "react-router-dom";
import useDebounced from "./useDebounced";
import { useDocumentTypeFieldsQuery } from "../graphql-generated";

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
  );
}
