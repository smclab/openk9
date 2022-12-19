import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import {
  useAddDocumentTypeFieldToSuggestionCategoryMutation,
  useRemoveDocumentTypeFieldFromSuggestionCategoryMutation,
  useSuggestionCategoryDocumentTypeFieldsQuery,
} from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query SuggestionCategoryDocumentTypeFields($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    suggestionCategory(id: $parentId) {
      id
      docTypeFields(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
        edges {
          node {
            id
            name
            description
            docType {
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
  }
`;

gql`
  mutation AddDocumentTypeFieldToSuggestionCategory($childId: ID!, $parentId: ID!) {
    addDocTypeFieldToSuggestionCategory(docTypeFieldId: $childId, suggestionCategoryId: $parentId) {
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
  mutation RemoveDocumentTypeFieldFromSuggestionCategory($childId: ID!, $parentId: ID!) {
    removeDocTypeFieldFromSuggestionCategory(docTypeFieldId: $childId, suggestionCategoryId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function SuggestionCategoryDocumentTypeFields() {
  const { suggestionCategoryId } = useParams();
  if (!suggestionCategoryId) return null;
  return (
    <AssociatedEntities
      label="Associate Document Type Fields"
      parentId={suggestionCategoryId}
      list={{
        useListQuery: useSuggestionCategoryDocumentTypeFieldsQuery,
        field: (data) => data?.suggestionCategory?.docTypeFields,
      }}
      useAddMutation={useAddDocumentTypeFieldToSuggestionCategoryMutation}
      useRemoveMutation={useRemoveDocumentTypeFieldFromSuggestionCategoryMutation}
    />
  );
}
