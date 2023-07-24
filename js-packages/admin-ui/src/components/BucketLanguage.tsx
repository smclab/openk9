import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useBucketLanguagesQuery, useAddLanguageToBucketMutation, useRemoveLanguageFromBucketMutation } from "../graphql-generated";
import { AssociatedEntities } from "./Form";

export const Bucketslanguages = gql`
  query BucketLanguages($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      languages(searchText: $searchText, first: 25, after: $cursor, notEqual: $unassociated) {
        edges {
          node {
            id
            name
            value
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

export const AddLanguageToBucket = gql`
  mutation AddLanguageToBucket($childId: ID!, $parentId: ID!) {
    addLanguageToBucket(languageId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export const RemoveLanguageFromBucket = gql`
  mutation RemoveLanguageFromBucket($childId: ID!, $parentId: ID!) {
    removeLanguageFromBucket(languageId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function BucketLanguage() {
  const { bucketId } = useParams();
  if (!bucketId) return null;
  return (
    <AssociatedEntities
      label="Associate Language"
      parentId={bucketId}
      list={{
        useListQuery: useBucketLanguagesQuery,
        field: (data) => data?.bucket?.languages,
      }}
      useAddMutation={useAddLanguageToBucketMutation}
      useRemoveMutation={useRemoveLanguageFromBucketMutation}
    />
  );
}
