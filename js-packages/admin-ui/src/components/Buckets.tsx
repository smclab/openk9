import React from "react";
import { gql } from "@apollo/client";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import { useBucketsQuery, useDeleteBucketMutation, useEnableBucketMutation } from "../graphql-generated";
import { ClayToggle } from "@clayui/form";

export const BucketsQuery = gql`
  query Buckets($searchText: String, $cursor: String) {
    buckets(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          enabled
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
  mutation DeleteBucket($id: ID!) {
    deleteBucket(bucketId: $id) {
      id
      name
    }
  }
`;

export function Buckets() {
  const showToast = useToast();
  const bucketsQuery = useBucketsQuery();
  const [updateBucketsMutate] = useEnableBucketMutation({
    refetchQueries: [BucketsQuery],
  });
  const [deleteBucketMutate] = useDeleteBucketMutation({
    refetchQueries: [BucketsQuery],
    onCompleted(data) {
      if (data.deleteBucket?.id) {
        showToast({ displayType: "success", title: "Bucket deleted", content: data.deleteBucket.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Buckets error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: bucketsQuery,
        field: (data) => data?.buckets,
      }}
      onCreatePath="/buckets/new"
      onDelete={(tenant) => {
        if (tenant?.id) deleteBucketMutate({ variables: { id: tenant.id } });
      }}
      columns={[
        { header: "Name", content: (tenant) => formatName(tenant) },
        { header: "Description", content: (tenant) => tenant?.description },
        {
          header: "Enabled",
          content: (buckets) => (
            <ClayToggle
              toggled={buckets?.enabled ?? false}
              onToggle={(enabled) => {
                if (buckets && buckets.id && buckets.name && !buckets.enabled)
                  updateBucketsMutate({
                    variables: { id: buckets.id },
                  });
              }}
            />
          ),
        },
      ]}
    />
  );
}
