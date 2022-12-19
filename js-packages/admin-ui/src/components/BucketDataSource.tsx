import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useAddDataSourceToBucketMutation, useRemoveDataSourceFromBucketMutation, useBucketDataSourcesQuery } from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query BucketDataSources($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      datasources(searchText: $searchText, first: 25, after: $cursor, notEqual: $unassociated) {
        edges {
          node {
            id
            name
            schedulable
            lastIngestionDate
            scheduling
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
`;

gql`
  mutation AddDataSourceToBucket($childId: ID!, $parentId: ID!) {
    addDatasourceToBucket(datasourceId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation RemoveDataSourceFromBucket($childId: ID!, $parentId: ID!) {
    removeDatasourceFromBucket(datasourceId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function BucketDataSources() {
  const { bucketId } = useParams();
  if (!bucketId) return null;
  return (
    <AssociatedEntities
      label="Associate Data Sources"
      parentId={bucketId}
      list={{
        useListQuery: useBucketDataSourcesQuery,
        field: (data) => data?.bucket?.datasources,
      }}
      useAddMutation={useAddDataSourceToBucketMutation}
      useRemoveMutation={useRemoveDataSourceFromBucketMutation}
    />
  );
}
