import { gql } from "@apollo/client";
import { useDeleteRulesMutation, useRulesQuery } from "../graphql-generated";
import { AddRuleToQueryAnalyses, QueryAnalysesRule, RemoveRuleFromQueryAnalyses } from "./QueryAnalysesRules";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import ReactFlow, { addEdge, MiniMap, Controls, Background, ReactFlowProvider, Node, Edge } from "react-flow-renderer";
import React from "react";
import NodeGraphRule from "./NodeGraphRule";

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

const nodeTypes = {
  custom: NodeGraphRule,
};

export function BuildGraph({
  node,
  edgesValue,
}: {
  node: Node<any>[];
  edgesValue: Edge<{ id: string; source: string; target: string }>[];
}) {
  const [nodes, setNodes] = React.useState(node);
  const [edges, setEdges] = React.useState(edgesValue);

  console.log(nodes, edges);

  return (
    <React.Fragment>
      <ReactFlow nodes={nodes} edges={edges} style={{ height: "600px", margin: "0 auto" }} nodeTypes={nodeTypes}>
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>
    </React.Fragment>
  );
}

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
    const nodeWidth = 100;
    const nodeHeight = 50;

    const startX = 400;
    const startY = 30;

    const uniqueRules = Array.from(
      rules
        .reduce((map, rule) => {
          const lhs = rule?.node?.lhs;
          if (!map.has(lhs)) {
            map.set(lhs, rule);
          }
          return map;
        }, new Map())
        .values()
    );
    uniqueRules.forEach((ruleElement, index) => {
      const rule = ruleElement?.node;
      const x = startX + index * nodeWidth;
      const y = startY + index * nodeHeight * 2;
      elements.push({
        id: rule?.lhs || "",
        type: "custom",
        data: { label: rule?.lhs || "", id: rule?.lhs || "" },
        position: { x, y },
      });
    });
  }

  const edges: Edge<any>[] | undefined = [];

  rules?.forEach((rule) => {
    const lhs = rule?.node?.lhs;
    const rhs = rule?.node?.rhs;

    if (lhs && rhs) {
      const edge = {
        id: rule.node?.name || "",
        source: lhs,
        target: rhs,
      };

      edges.push(edge);
    }
  });

  return <BuildGraph node={elements} edgesValue={edges} />;
}
