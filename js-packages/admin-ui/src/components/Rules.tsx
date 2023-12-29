import { gql } from "@apollo/client";
import { useRulesQuery } from "../graphql-generated";
import ReactFlow, { MiniMap, Controls, Background, Node, Edge, applyNodeChanges } from "react-flow-renderer";
import React from "react";
import NodeGraphRule from "./NodeGraphRule";
import NodeGraphRuleDouble from "./NodeGraphRuleDouble";

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
  customDouble: NodeGraphRuleDouble,
};

function checkNodeType(entity: string) {
  const type = entity.includes(" ") ? "customDouble" : "custom";
  return type;
}

export function BuildGraph({ node, edgesValue }: { node: Node<any>[]; edgesValue: Edge<any>[] }) {
  const [nodes, setNodes] = React.useState(node);
  const [edges, setEdges] = React.useState(edgesValue);

  React.useEffect(() => {
    setNodes(node);
    setEdges(edgesValue);
  }, [node, edgesValue]);

  const onNodesChange = React.useCallback((changes: any) => setNodes((nds) => applyNodeChanges(changes, nds)), [setNodes]);

  return (
    <React.Fragment>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        style={{ minHeight: "90vh", margin: "0 auto" }}
        nodeTypes={nodeTypes}
        onNodesChange={onNodesChange}
        fitView
      >
        <MiniMap style={{ zIndex: "0" }} />
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

  const customDoubleRules: string[] = [];

  rules?.forEach((rule, index) => {
    const rhs = rule?.node?.rhs;
    const type = checkNodeType(rhs || "");
    if (type === "customDouble") {
      const rhsSplitted = rhs?.split(" ");
      rhsSplitted?.forEach((rhsSplit, index) => {
        customDoubleRules.push(rhsSplit);
      });
    }
  });

  if (rules) {
    const nodeHeight = 50;
    const startX = 400;
    const startY = 30;

    const uniqueIds: string[] = rules.reduce<string[]>((ids, itemElement) => {
      const item = itemElement?.node;
      if (item && item.lhs && !ids.includes(item.lhs)) ids.push(item.lhs);
      if (item && item.rhs && !ids.includes(item.rhs)) ids.push(item.rhs);
      return ids;
    }, []);

    uniqueIds.forEach((entity, index) => {
      const isFather = rules.find((rule) => rule?.node?.rhs === entity);
      const type = checkNodeType(entity);
      if (!isFather) {
        const lhs = entity;
        const x = startX;
        const y = startY + index * nodeHeight * 5;
        if (!customDoubleRules?.includes(lhs)) {
          elements.push({
            id: lhs || "",
            type: type,
            data: { label: lhs || "", id: lhs || "", rulesQuery: rulesQuery, rules: rules },
            position: { x, y },
          });
        }

        const son = recoveryValue({ entity, rules, position: { x, y } });
        son.forEach((sin) => {
          if (!customDoubleRules?.includes(sin.id)) {
            elements.push(sin);
          }
        });
        if (!customDoubleRules?.includes(lhs)) {
          elements.push({
            id: lhs || "",
            type: type,
            data: { label: lhs || "", id: lhs || "", rulesQuery: rulesQuery, rules: rules },
            position: { x, y },
          });
        }
      }
      return "";
    });
  }
  const edges: Edge<any>[] | undefined = [];

  rules?.forEach((rule) => {
    const lhs = rule?.node?.lhs;
    const rhs = rule?.node?.rhs;
    const type = checkNodeType(rhs || "");
    if (lhs && rhs) {
      const edge = {
        id: rule.node?.name || "",
        source: lhs,
        target: rhs,
      };

      edges.push(edge);
    }
    if (lhs && rhs && type === "customDouble") {
      const customDoubleNodes = rhs.split(" ");

      rules?.forEach((rule, index) => {
        const customDoubleNodesLhs = rule?.node?.lhs || "";
        const customDoubleNodesRhs = rule?.node?.rhs || "";
        if (customDoubleNodes?.includes(customDoubleNodesLhs)) {
          const edge = {
            id: rhs + "_" + customDoubleNodesRhs,
            source: rhs,
            target: customDoubleNodesRhs,
            // sourceHandle: index.toString(),
          };

          edges.push(edge);
        }
      });
    }
  });

  return <BuildGraph node={elements} edgesValue={edges} />;
}

function recoveryValue({
  entity,
  rules,
  position,
  elmentOnRoW,
  indexElement,
}: {
  entity: string;
  rules: any[];
  position: { x: number; y: number };
  elmentOnRoW?: number;
  indexElement?: number;
}): any[] {
  const matchingRules: any[] = rules.filter(({ node: { lhs } }: { node: { lhs: string } }) => entity === lhs);

  const type = checkNodeType(entity);

  const result: any[] = matchingRules.map(({ node: { id, lhs, rhs } }: any, index: number) => {
    return {
      id: lhs || "",
      type: type,
      data: { label: lhs || "", id: lhs || "", rulesQuery: undefined, rules: rules },
      position: { x: position.x + 100 * (index + 1) + (indexElement ? indexElement * 250 : 1), y: position.y },
    };
  });

  matchingRules.forEach(({ node: { rhs } }: { node: { rhs: string } }, index) => {
    const data = recoveryValue({ entity: rhs, rules, position: { x: position.x + 200, y: position.y + 200 }, indexElement: index });
    if (data) {
      data.forEach((element, index) => {
        if (element.data.hasOwnProperty("fatherLabel")) {
          const parent = data.find((node) => node.id === element.data.fatherLabel);
          if (parent) {
            const sons = data.filter((son) => son.data.fatherLabel === parent.id);
            const elementIndex = sons.findIndex((son) => son.id === element.id);
            const toAdd = elementIndex > 0 ? sons[elementIndex].id.length : 0;
            element.position.x = parent.position.x + toAdd * 50;
            // element.position.y = parent.position.y + 100 * elementIndex + 10;
          }
        }
      });
      result.push(...data);
    }
  });

  if (matchingRules.length === 0) {
    const data = rules.find(({ node: { rhs } }: { node: { rhs: string } }) => entity === rhs);
    result.push({
      id: entity,
      type: type,
      data: {
        label: entity || "",
        id: entity || "",
        rulesQuery: undefined,
        rules: rules,
        isDelete: true,
        idAssociation: data.node.id,
        fatherLabel: data.node.lhs,
      },
      position: { x: position.x + (indexElement ? (indexElement + 1) * 250 : 1), y: position.y },
    });
  }

  return result;
}
