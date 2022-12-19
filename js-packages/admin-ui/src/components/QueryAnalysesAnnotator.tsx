import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import {
  useAddAnnotatorsToQueryAnalysesMutation,
  useQueryAnalysesAnnotatorsQuery,
  useRemoveAnnotatorFromQueryAnalysesMutation,
} from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query QueryAnalysesAnnotators($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    queryAnalysis(id: $parentId) {
      id

      annotators(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
        edges {
          node {
            id
            name
            fieldName
            fuziness
            size
            description
            type
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
  mutation AddAnnotatorsToQueryAnalyses($childId: ID!, $parentId: ID!) {
    addAnnotatorToQueryAnalysis(annotatorId: $childId, id: $parentId) {
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
  mutation RemoveAnnotatorFromQueryAnalyses($childId: ID!, $parentId: ID!) {
    removeAnnotatorFromQueryAnalysis(annotatorId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function QueryAnalysesAnnotators() {
  const { queryAnalysisId } = useParams();
  if (!queryAnalysisId) return null;
  return (
    <AssociatedEntities
      label="Associate Annotator"
      parentId={queryAnalysisId}
      list={{
        useListQuery: useQueryAnalysesAnnotatorsQuery,
        field: (data) => data?.queryAnalysis?.annotators,
      }}
      useAddMutation={useAddAnnotatorsToQueryAnalysesMutation}
      useRemoveMutation={useRemoveAnnotatorFromQueryAnalysesMutation}
    />
  );
}
