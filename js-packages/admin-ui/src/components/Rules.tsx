import { gql } from "@apollo/client";
import { useDeleteRulesMutation, useRulesQuery } from "../graphql-generated";
import { AddRuleToQueryAnalyses, QueryAnalysesRule, RemoveRuleFromQueryAnalyses } from "./QueryAnalysesRules";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import ReactFlow, { addEdge, MiniMap, Controls, Background, ReactFlowProvider, Node, Edge } from "react-flow-renderer";
import React from "react";

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
  if (rulesQuery.loading) return <div>caricamento</div>;
  const elements: Node[] = [];
  const rules = rulesQuery.data?.rules?.edges;

  if (rules) {
    const nodeWidth = 100; // Larghezza del nodo
    const nodeHeight = 50; // Altezza del nodo

    // Calcola la posizione del primo nodo in modo che sia centrato
    const startX = 400;
    const startY = 30;

    rules.forEach((ruleElement, index) => {
      const rule = ruleElement?.node;
      const x = startX + index * nodeWidth;
      const y = startY + index * nodeHeight;
      elements.push({
        id: rule?.id || "",
        type: "default",
        data: { label: rule?.name || "" },
        position: { x, y },
      });
    });
  }

  return (
    <React.Fragment>
      ciao
      <ReactFlow nodes={elements} style={{ height: "600px", margin: "0 auto" }}>
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>
    </React.Fragment>
  );
}