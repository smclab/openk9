import { gql } from "@apollo/client";
import { useCreateOrUpdateRuleQueryMutation, useDeleteRulesMutation, useRulesQuery } from "../graphql-generated";
import { AddRuleToQueryAnalyses, QueryAnalysesRule, RemoveRuleFromQueryAnalyses } from "./QueryAnalysesRules";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import ReactFlow, { addEdge, MiniMap, Controls, Background, ReactFlowProvider, Node, Edge } from "react-flow-renderer";
import React from "react";
import NodeGraphRule from "./NodeGraphRule";
import { RuleQuery } from "./Rule";

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

export function BuildGraph({ node, edgesValue }: { node: Node<any>[]; edgesValue: Edge<any>[] }) {
  
  const [nodes, setNodes] = React.useState(node);
  const [edges, setEdges] = React.useState(edgesValue);

  React.useEffect(() => {
    setNodes(node);
    setEdges(edgesValue);
  }, [node, edgesValue]);
  
  return (
    <React.Fragment>
      <ReactFlow nodes={nodes} edges={edges} style={{ height: "600px", margin: "0 auto" }} nodeTypes={nodeTypes} fitView>
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>
    </React.Fragment>
  );
}

export function Rules() {
  const rulesQuery = useRulesQuery();

  if (rulesQuery.loading) return <div>caricamento</div>;
  const elements: Node[] = [];
  const rules = rulesQuery.data?.rules?.edges;

  if (rules) {
    const nodeWidth = 100;
    const nodeHeight = 50;

    const startX = 400;
    const startY = 30;

    const uniqueIds: string[] = rules.reduce<string[]>((ids, itemElement) => {
      const item = itemElement?.node;
      if (item && item.lhs && !ids.includes(item.lhs)) ids.push(item.lhs);
      if (item && item.rhs && !ids.includes(item.rhs)) ids.push(item.rhs);
      return ids;
    }, []);
        
    uniqueIds.forEach((ruleElement, index) => {
      const lhs = ruleElement;
      const x = startX + index * nodeWidth;
      const y = startY + index * nodeHeight * 2;
      elements.push({
        id: lhs || "",
        type: "custom",
        data: { label: lhs || "", id: lhs||"",rulesQuery:rulesQuery,rules:rules},
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

