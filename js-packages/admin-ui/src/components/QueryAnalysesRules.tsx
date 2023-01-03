import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import {
  useAddRulesToQueryAnalysesMutation,
  useQueryAnalysesRulesQuery,
  useRemoveRuleFromQueryAnalysesMutation,
} from "../graphql-generated";
import { AssociatedEntities } from "./Form";

export const QueryAnalysesRule = gql`
  query QueryAnalysesRules($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    queryAnalysis(id: $parentId) {
      id
      rules(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
        edges {
          node {
            id
            name
            lhs
            rhs
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

export const AddRuleToQueryAnalyses = gql`
  mutation AddRulesToQueryAnalyses($childId: ID!, $parentId: ID!) {
    addRuleToQueryAnalysis(ruleId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export const RemoveRuleFromQueryAnalyses = gql`
  mutation RemoveRuleFromQueryAnalyses($childId: ID!, $parentId: ID!) {
    removeRuleFromQueryAnalysis(ruleId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function QueryAnalysesRules() {
  const { queryAnalysisId } = useParams();
  if (!queryAnalysisId) return null;
  return (
    <AssociatedEntities
      label="Associate Rules"
      parentId={queryAnalysisId}
      list={{
        useListQuery: useQueryAnalysesRulesQuery,
        field: (data) => data?.queryAnalysis?.rules,
      }}
      useAddMutation={useAddRulesToQueryAnalysesMutation}
      useRemoveMutation={useRemoveRuleFromQueryAnalysesMutation}
    />
  );
}
