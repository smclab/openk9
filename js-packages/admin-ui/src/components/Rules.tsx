import { gql } from "@apollo/client";
import { useDeleteRulesMutation, useRulesQuery } from "../graphql-generated";
import { AddRuleToQueryAnalyses, QueryAnalysesRule, RemoveRuleFromQueryAnalyses } from "./QueryAnalysesRules";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const RulesQuery = gql`
  query Rules($searchText: String, $cursor: String) {
    rules(searchText: $searchText, first: 25, after: $cursor) {
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
`;

gql`
  mutation DeleteRules($id: ID!) {
    deleteRule(ruleId: $id) {
      id
      name
    }
  }
`;
export function Rules() {
  const rulesQuery = useRulesQuery();
  const showToast = useToast();

  const [deleteRuleMutate] = useDeleteRulesMutation({
    refetchQueries: [RulesQuery, QueryAnalysesRule, AddRuleToQueryAnalyses, RemoveRuleFromQueryAnalyses],
    onCompleted(data) {
      if (data.deleteRule?.id) {
        showToast({ displayType: "success", title: "Rule deleted", content: data.deleteRule.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Rule error", content: error.message ?? "" });
    },
  });
  return (
    <Table
      data={{
        queryResult: rulesQuery,
        field: (data) => data?.rules,
      }}
      onCreatePath="/rules/new"
      onDelete={(rule) => {
        if (rule?.id) deleteRuleMutate({ variables: { id: rule.id } });
      }}
      columns={[
        { header: "Name", content: (rule) => formatName(rule) },
        { header: "Left Hand Side", content: (rule) => rule?.lhs },
        { header: "Right Hand Side", content: (rule) => rule?.rhs },
      ]}
    />
  );
}
