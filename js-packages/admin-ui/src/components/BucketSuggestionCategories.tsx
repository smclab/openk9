import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import {
  useAddSuggestionCategoryToBucketMutation,
  useBucketSuggestionCategoriesQuery,
  useRemoveSuggestionCategoryFromBucketMutation,
} from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query BucketSuggestionCategories($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      suggestionCategories(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
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
  mutation AddSuggestionCategoryToBucket($childId: ID!, $parentId: ID!) {
    addSuggestionCategoryToBucket(suggestionCategoryId: $childId, bucketId: $parentId) {
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
  mutation RemoveSuggestionCategoryFromBucket($childId: ID!, $parentId: ID!) {
    removeSuggestionCategoryFromBucket(suggestionCategoryId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function BucketSuggestionCategories() {
  const { bucketId } = useParams();
  if (!bucketId) return null;
  return (
    <AssociatedEntities
      label="Associate Suggestion Categories"
      parentId={bucketId}
      list={{
        useListQuery: useBucketSuggestionCategoriesQuery,
        field: (data) => data?.bucket?.suggestionCategories,
      }}
      useAddMutation={useAddSuggestionCategoryToBucketMutation}
      useRemoveMutation={useRemoveSuggestionCategoryFromBucketMutation}
    />
  );
}
