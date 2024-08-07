import React from "react";
import { gql } from "@apollo/client";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import { useEmbeddingModelsQuery, useEnableEmbeddingModelMutation } from "../graphql-generated";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";

export const EmbeddingModelsQuery = gql`
  query EmbeddingModels($searchText: String, $cursor: String) {
    embeddingModels(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          enabled
        }
      }
    }
  }
`;

gql`
  mutation EnableEmbeddingModel($id: ID!) {
    enableEmbeddingModel(id: $id) {
      id
      name
    }
  }
`;

export function EmbeddingModels() {
  const showToast = useToast();
  const embeddingModelsQuery = useEmbeddingModelsQuery();
  const [updateEnableModel] = useEnableEmbeddingModelMutation({
    refetchQueries: [EmbeddingModelsQuery],
  });
  //   const [updateBucketsMutate] = useEnableBucketMutation({
  //     refetchQueries: [EmbeddingModelsQuery],
  //   });
  //   const [deleteBucketMutate] = useDeleteBucketMutation({
  //     refetchQueries: [EmbeddingModelsQuery],
  //     onCompleted(data) {
  //       if (data.deleteBucket?.id) {
  //         showToast({ displayType: "success", title: "Bucket deleted", content: data.deleteBucket.name ?? "" });
  //       }
  //     },
  //     onError(error) {
  //       showToast({ displayType: "danger", title: "Buckets error", content: error.message ?? "" });
  //     },
  //   });

  return (
    <Table
      data={{
        queryResult: embeddingModelsQuery,
        field: (data) => data?.embeddingModels,
      }}
      viewAdd={true}
      onCreatePath="create/new/"
      haveActions={false}
      onDelete={(tenant) => {}}
      columns={[
        { header: "Name", content: (enabledModel) => formatName(enabledModel) },
        { header: "Description", content: (enabledModel) => enabledModel?.description },
        {
          header: "Enabled",
          content: (enabledModel) => (
            <React.Fragment>
              <ClayToggle
                toggled={enabledModel?.enabled ?? false}
                onToggle={() => {
                  if (enabledModel && enabledModel.id && enabledModel.name && !enabledModel.enabled)
                    updateEnableModel({
                      variables: { id: enabledModel.id },
                    });
                }}
              />
              <style type="text/css">{StyleToggle}</style>
            </React.Fragment>
          ),
        },
      ]}
    />
  );
}
