import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useAddTabToBucketMutation, useBucketTabsQuery, useRemoveTabFromBucketMutation } from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query BucketTabs($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      tabs(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
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
  }
`;

gql`
  mutation AddTabToBucket($childId: ID!, $parentId: ID!) {
    addTabToBucket(tabId: $childId, id: $parentId) {
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
  mutation RemoveTabFromBucket($childId: ID!, $parentId: ID!) {
    removeTabFromBucket(tabId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function BucketTabs() {
  const { bucketId } = useParams();
  if (!bucketId) return null;
  return (
    <AssociatedEntities
      label="Associate Tabs"
      parentId={bucketId}
      list={{
        useListQuery: useBucketTabsQuery,
        field: (data) => data?.bucket?.tabs,
      }}
      useAddMutation={useAddTabToBucketMutation}
      useRemoveMutation={useRemoveTabFromBucketMutation}
    />
  );
}
