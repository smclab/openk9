import React from "react";
import { gql } from "@apollo/client";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import {
  useBucketsQuery,
  useDeleteBucketMutation,
  useEmbeddingModelsQuery,
  useEnableBucketMutation,
  useLargeLanguageModelsQuery,
} from "../graphql-generated";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";

export const LargeLanguageModelsQuery = gql`
  query LargeLanguageModels($searchText: String, $cursor: String) {
    largeLanguageModels(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

export function LargeLanguageModels() {
  const showToast = useToast();
  const largeLanguageModels = useLargeLanguageModelsQuery();
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
        queryResult: largeLanguageModels,
        field: (data) => data?.largeLanguageModels,
      }}
      onCreatePath=""
      onDelete={(tenant) => {
        //   if (tenant?.id) deleteBucketMutate({ variables: { id: tenant.id } });
      }}
      viewAdd={false}
      haveActions={false}
      columns={[
        { header: "Name", content: (tenant) => formatName(tenant) },
        { header: "Description", content: (tenant) => tenant?.description },
        // {
        //   header: "Enabled",
        //   content: (buckets) => (
        //     <React.Fragment>
        //       <ClayToggle
        //         toggled={buckets?.enabled ?? false}
        //         onToggle={(enabled) => {
        //           if (buckets && buckets.id && buckets.name && !buckets.enabled)
        //             updateBucketsMutate({
        //               variables: { id: buckets.id },
        //             });
        //         }}
        //       />
        //       <style type="text/css">{StyleToggle}</style>
        //     </React.Fragment>
        //   ),
        // },
      ]}
    />
  );
}
