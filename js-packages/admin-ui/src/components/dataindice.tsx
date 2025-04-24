import { gql, QueryResult } from "@apollo/client";
import ClayButton from "@clayui/button";
import { ClayInput } from "@clayui/form";
import ClayList from "@clayui/list";
import ClayModal, { useModal } from "@clayui/modal";
import React from "react";
import { useParams } from "react-router-dom";
import { Virtuoso } from "react-virtuoso";
import {
  Exact,
  InputMaybe,
  Scalars,
  useBindVectorIndexMutation,
  useDataIndexQuery,
  useUnBindVectorIndexMutation,
  useVectorIndicesAssociationQuery,
} from "../graphql-generated";
import { CodeInput } from "./CodeInput";
import { ContainerFluid, CustomFormGroup, TextInput } from "./Form";

export default function DataIndice() {
  const { dataIndiceId = "new" } = useParams();
  const dataIndicesQuery = useDataIndexQuery({
    variables: { id: dataIndiceId as string },
    errorPolicy: "ignore",
  });
  const vectorQuery = useVectorIndicesAssociationQuery({
    variables: { searchText: "", cursor: null },
    fetchPolicy: "cache-and-network",
  });
  const vectorIndicesAssociation = useFindDataIndexById(dataIndiceId as string, vectorQuery);
  const [changeMutate] = useBindVectorIndexMutation({});
  const [removeMutate] = useUnBindVectorIndexMutation({});

  if (!dataIndiceId && dataIndicesQuery.loading && vectorIndicesAssociation.loading) return null;
  const dataIndex = dataIndicesQuery.data?.dataIndex;

  return (
    <React.Fragment>
      <ContainerFluid>
        <div style={{ padding: "20px", backgroundColor: "white" }}>
          <div className="autofit-col autofit-col-expand" style={{ alignItems: "center" }}>
            <p
              className="navbar-title navbar-text-truncate "
              style={{
                color: "#C22525",
                fontFamily: "Helvetica",
                fontStyle: "normal",
                fontWeight: "700",
                fontSize: "18px",
                lineHeight: "44px",
              }}
            >
              Attribute
            </p>
          </div>
          <TextInput
            disabled
            id="input-name"
            label="name"
            onChange={() => {}}
            value={dataIndex?.name || ""}
            validationMessages={[]}
          ></TextInput>
          <TextInput
            disabled
            id="input-name"
            label="description"
            onChange={() => {}}
            value={dataIndex?.description || ""}
            validationMessages={[]}
          ></TextInput>
          <CodeInput
            disabled
            readonly
            id="input-settings"
            label="Settings"
            language="json"
            onChange={() => {}}
            validationMessages={[]}
            value={dataIndex?.settings || ""}
          ></CodeInput>
          <SearchSelect
            value={vectorIndicesAssociation.dataIndex?.id}
            associatedVectorIndex={vectorIndicesAssociation.dataIndex}
            unassociatedVectorIndices={vectorIndicesAssociation.unassociatedVectorIndices.map((edge: any) => edge || {})}
            onAssociate={(id) => {
              dataIndex?.id &&
                removeMutate({
                  variables: {
                    dataIndexId: dataIndex.id,
                  },
                }).then(() => {
                  dataIndex?.id &&
                    changeMutate({
                      variables: {
                        vectorIndexId: id,
                        dataIndexId: dataIndex?.id,
                      },
                    }).then(() => {
                      vectorQuery.refetch();
                    });
                });
            }}
            onDisassociate={() => {
              dataIndex?.id &&
                removeMutate({
                  variables: {
                    dataIndexId: dataIndex.id,
                  },
                  refetchQueries: [VectorIndicesAssociationQuery],
                }).then(() => {
                  vectorQuery.refetch();
                });
            }}
          />
        </div>
      </ContainerFluid>
    </React.Fragment>
  );
}

const DataIndexQuery = gql`
  query DataIndex($id: ID!) {
    dataIndex(id: $id) {
      id
      name
      description
      settings
      docTypes {
        edges {
          node {
            id
            name
            __typename
          }
          __typename
        }
        __typename
      }
    }
  }
`;

const VectorIndicesQuery = gql`
  query VectorIndices($searchText: String, $cursor: String) {
    vectorIndices(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          name
          chunkType
          chunkWindowSize
          jsonConfig
          textEmbeddingField
          titleField
          urlField
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

const VectorIndicesAssociationQuery = gql`
  query VectorIndicesAssociation($searchText: String, $cursor: String) {
    vectorIndices(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          name
          id
          dataIndex {
            id
          }
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
  mutation BindVectorIndex($vectorIndexId: ID!, $dataIndexId: ID!) {
    bindVectorIndex(vectorIndexId: $vectorIndexId, dataIndexId: $dataIndexId) {
      id
    }
  }
`;

gql`
  mutation unBindVectorIndex($dataIndexId: ID!) {
    unbindVectorIndex(dataIndexId: $dataIndexId) {
      id
    }
  }
`;

export function useFindDataIndexById(
  id: string,
  vectorQuery: QueryResult<
    any,
    Exact<{
      searchText?: InputMaybe<Scalars["String"]>;
      cursor?: InputMaybe<Scalars["String"]>;
    }>
  >
) {
  const { data, loading, error } = vectorQuery;

  if (loading) return { dataIndex: null, unassociatedVectorIndices: [], loading, error };
  if (error) {
    console.error("Error fetching vector indices association:", error);
    return { dataIndex: null, unassociatedVectorIndices: [], loading, error };
  }

  const foundNode = data?.vectorIndices?.edges?.find((edge: any) => edge?.node?.dataIndex?.id === id);
  const unassociatedVectorIndices =
    data?.vectorIndices?.edges?.filter((edge: any) => !edge?.node?.dataIndex)?.map((edge: any) => edge?.node) || [];

  return { dataIndex: foundNode?.node || null, unassociatedVectorIndices, loading, error };
}

export function SearchSelect({
  value,
  associatedVectorIndex,
  unassociatedVectorIndices,
  onAssociate,
  onDisassociate,
}: {
  value: string | null | undefined;
  associatedVectorIndex: { id?: string | null; name?: string | null } | null;
  unassociatedVectorIndices: Array<{ id?: string | null; name?: string | null; description?: string | null }>;
  onAssociate: (id: string) => void;
  onDisassociate: () => void;
}) {
  const { observer, onOpenChange, open } = useModal();

  return (
    <React.Fragment>
      <CustomFormGroup>
        <label>Association with vectorization</label>
        <ClayInput.Group>
          <ClayInput.GroupItem>
            <ClayInput
              type="text"
              className="form-control"
              style={{ backgroundColor: "#f1f2f5" }}
              readOnly
              disabled={!associatedVectorIndex}
              value={associatedVectorIndex?.name ?? ""}
            />
          </ClayInput.GroupItem>
          <ClayInput.GroupItem append shrink>
            <ClayButton.Group>
              <ClayButton
                displayType="secondary"
                style={{
                  border: "1px solid #393B4A",
                  borderRadius: "3px",
                }}
                onClick={() => onOpenChange(true)}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Change
                </span>
              </ClayButton>
              <ClayButton
                displayType="secondary"
                style={{ marginLeft: "10px", border: "1px solid #393B4A", borderRadius: "3px" }}
                onClick={() => {
                  onDisassociate();
                }}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Remove
                </span>
              </ClayButton>
            </ClayButton.Group>
          </ClayInput.GroupItem>
        </ClayInput.Group>
      </CustomFormGroup>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>Vectorization</ClayModal.Header>
          <ClayModal.Body>
            <Virtuoso
              totalCount={unassociatedVectorIndices.length}
              style={{ height: "400px" }}
              itemContent={(index) => {
                const row = unassociatedVectorIndices[index];
                return (
                  <ClayList.Item flex>
                    <ClayList.ItemField expand>
                      <ClayList.ItemTitle>{row?.name || "..."}</ClayList.ItemTitle>
                      <ClayList.ItemText>{row?.description || "..."}</ClayList.ItemText>
                    </ClayList.ItemField>
                    <ClayList.ItemField>
                      <ClayButton
                        displayType="unstyled"
                        onClick={() => {
                          if (row?.id) {
                            onAssociate(row.id);
                            onOpenChange(false);
                          }
                        }}
                      >
                        <span className="inline-item inline-item-after">
                          <svg className="lexicon-icon lexicon-icon-play" focusable="false" role="presentation" viewBox="0 0 512 512">
                            <path d="M96 52v408l320-204L96 52z" fill="currentColor" />
                          </svg>
                        </span>
                      </ClayButton>
                    </ClayList.ItemField>
                  </ClayList.Item>
                );
              }}
            />
          </ClayModal.Body>
          <ClayModal.Footer
            first={
              <ClayButton
                displayType="secondary"
                onClick={() => {
                  onOpenChange(false);
                }}
              >
                Cancel
              </ClayButton>
            }
          />
        </ClayModal>
      )}
    </React.Fragment>
  );
}
