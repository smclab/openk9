import React from "react";
import { Handle, Position } from "react-flow-renderer";
import { useCreateOrUpdateRuleQueryMutation, useDeleteRulesMutation } from "../graphql-generated";
import { RuleQuery } from "./Rule";
import { RulesQuery } from "./Rules";
import { AddRuleToQueryAnalyses, QueryAnalysesRule, RemoveRuleFromQueryAnalyses } from "./QueryAnalysesRules";
import { useToast } from "./ToastProvider";

export default function NodeGraphRule(props: any) {
  const { data } = props;
  const showToast = useToast();
  const [inputText, setInputText] = React.useState("");
  const [isPanelOpen, setPanelOpen] = React.useState(false);
  const [createOrUpdateRuleMutate, createOrUpdateRuleMutation] = useCreateOrUpdateRuleQueryMutation({
    refetchQueries: [RuleQuery, RulesQuery, QueryAnalysesRule, AddRuleToQueryAnalyses, RemoveRuleFromQueryAnalyses],
    onCompleted(data) {
      showToast({ displayType: "success", title: "Rule created", content: "create" ?? "" });
    },
  });
  const handleNodeClick = () => {
    data.rulesQuery.refetch();

    setPanelOpen(!isPanelOpen);
  };
  const [deleteRuleMutate] = useDeleteRulesMutation({
    refetchQueries: [RuleQuery, RulesQuery, QueryAnalysesRule, AddRuleToQueryAnalyses, RemoveRuleFromQueryAnalyses],
    onCompleted(data) {
      if (data.deleteRule?.id) {
        showToast({ displayType: "success", title: "Plugin drivers deleted", content: data.deleteRule.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Plungi drivers nerror", content: error.message ?? "" });
    },
  });

  return (
    <div
      style={{
        height: "60px",
        paddingInline: "20px",
        background: "white",
        border: "1px solid red",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        borderRadius: "50px",
        cursor: "pointer",
      }}
    >
      <Handle type="target" position={Position.Top} isConnectable={true} />
      <div onClick={handleNodeClick}> {data.label}</div>
      <Handle type="source" position={Position.Bottom} isConnectable={true} />

      {isPanelOpen && (
        <div
          style={{
            position: "absolute",
            top: "0",
            left: "0",
            transform: "translateX(-50%)",
            background: "white",
            border: "1px solid gray",
            padding: "9px",
            minWidth: "240px",
          }}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: "5px" }}>
            <label>New Rule: </label>
            <input
              type="text"
              value={inputText}
              onChange={(event) => {
                setInputText(event.currentTarget.value);
              }}
            />
            <div style={{ display: "flex", gap: "5px" }}>
              <button
                type="submit"
                onClick={() => {
                  createOrUpdateRuleMutate({
                    variables: { id: undefined, name: data.label + "_" + inputText, lhs: data.label, rhs: inputText },
                  });
                  setPanelOpen(false);
                }}
              >
                Invia
              </button>
              <button type="submit" onClick={handleNodeClick}>
                Chiudi
              </button>
              <button
                onClick={() => {
                  const removeRule = data?.rules?.find((rules: { node: { id: string; name: string; lhs: string; rhs: string } }) => {
                    return rules.node.rhs === data.label;
                  });                  
                   deleteRuleMutate({ variables: { id: removeRule?.node?.id || ""} });
                }}
              >
                Cancella
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
