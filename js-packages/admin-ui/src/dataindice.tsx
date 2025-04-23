import { gql } from "@apollo/client";
import React from "react";
import { useParams } from "react-router-dom";
import { CodeInput } from "./components/CodeInput";
import {
  ClayListComponents,
  ContainerFluid,
  CustomFormGroup,
  InformationField,
  MutationHook,
  QueryHook,
  TextInput,
} from "./components/Form";
import { useToast } from "./components/ToastProvider";
import { useDataIndexQuery, useVectorIndicesAssociationQuery } from "./graphql-generated";
import { ClayInput } from "@clayui/form";
import ClayModal, { useModal } from "@clayui/modal";
import useDebounced from "./components/useDebounced";
import ClayButton from "@clayui/button";
import { Virtuoso } from "react-virtuoso";
import ClayList from "@clayui/list";

export default function DataIndice() {
  const { dataIndiceId = "new" } = useParams();
  const showToast = useToast();
  const dataIndicesQuery = useDataIndexQuery({
    variables: { id: dataIndiceId as string },
    errorPolicy: "ignore",
  });
  const vectorIndicesAssociation = useFindDataIndexById(dataIndiceId as string);

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
          {/* <SearchSelect
            label="Vector Indice"
            value={dataIndex.?.id}
            useValueQuery={useQueryAnalysisValueQuery}
            useOptionsQuery={useQueryAnalysisOptionsQuery}
            useChangeMutation={useBindQueryAnalysisToBucketMutation}
            mapValueToMutationVariables={(queryAnalysis) => ({ bucketId, queryAnalysis })}
            useRemoveMutation={useUnbindQueryAnalysisFromBucketMutation}
            mapValueToRemoveMutationVariables={() => ({ bucketId })}
            invalidate={() => bucketQuery.refetch()}
            description={"Query Analysis configuration for current bucket"}
          /> */}
        </div>
      </ContainerFluid>
    </React.Fragment>
  );
}

const DataIndexQuery = gql`
  query DataIndex($id: ID!) {
    dataIndex(id: $id) {
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

export function useFindDataIndexById(id: string) {
  const { data, loading, error } = useVectorIndicesAssociationQuery({
    variables: { searchText: "", cursor: null },
    fetchPolicy: "cache-and-network",
  });

  if (loading) return { dataIndex: null, unassociatedVectorIndices: [], loading, error };
  if (error) {
    console.error("Error fetching vector indices association:", error);
    return { dataIndex: null, unassociatedVectorIndices: [], loading, error };
  }

  const foundNode = data?.vectorIndices?.edges?.find((edge) => edge?.node?.dataIndex?.id === id);
  const unassociatedVectorIndices = data?.vectorIndices?.edges?.filter((edge) => !edge?.node?.dataIndex)?.map((edge) => edge?.node) || [];

  return { dataIndex: foundNode?.node || null, unassociatedVectorIndices, loading, error };
}
