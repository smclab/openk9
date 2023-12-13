import React from "react";
import { Handle, Position } from "react-flow-renderer";
import { useCreateOrUpdateRuleQueryMutation, useDeleteRulesMutation } from "../graphql-generated";
import { RuleQuery } from "./Rule";
import { RulesQuery } from "./Rules";
import { AddRuleToQueryAnalyses, QueryAnalysesRule, RemoveRuleFromQueryAnalyses } from "./QueryAnalysesRules";
import { useToast } from "./ToastProvider";

export default function NodeGraphRuleDouble(props: any) {
  const { data } = props;
  const showToast = useToast();
  const [inputText, setInputText] = React.useState("");
  const firstLabelPart = data.label.substring(0, data.label.indexOf(" "));
  const secondLabelPart = data.label.slice(data.label.indexOf(" ") + 1);
  const [modify, setModify] = React.useState(data.label);
  const [modifyFirstLabelPart, setModifyFirstLabelPart] = React.useState(firstLabelPart);
  const [modifySecondLabelPart, setModifySecondLabelPart] = React.useState(secondLabelPart);
  const [subNode, setSubNode] = React.useState(0);
  const [isPanelOpen, setPanelOpen] = React.useState(false);
  const [isOptional, setIsOptional] = React.useState(false);
  const [isTerminal, setIsTerminal] = React.useState(false);
  const [isModify, setIsModify] = React.useState(false);

  const [createOrUpdateRuleMutate, createOrUpdateRuleMutation] = useCreateOrUpdateRuleQueryMutation({
    refetchQueries: [RuleQuery, RulesQuery, QueryAnalysesRule, AddRuleToQueryAnalyses, RemoveRuleFromQueryAnalyses],
    onCompleted(data) {
      showToast({ displayType: "success", title: "Rule created", content: "create" ?? "" });
    },
  });
  const handleNodeClick = () => {
    setPanelOpen(!isPanelOpen);
  };
  const handleClick = (subNode: number) => {
    if (isPanelOpen) {
      return;
    }
    setPanelOpen(!isPanelOpen);
    setSubNode(subNode);
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
        border: "1px solid black",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        borderRadius: "50px",
        cursor: "pointer",
      }}
      // onClick={!isPanelOpen ? handleNodeClick : undefined}
    >
      <Handle type="target" position={Position.Top} isConnectable={true} />
      <div style={{ display: "flex" }}>
        <div
          style={{
            height: "58px",
            width: "50%",
            paddingInline: "20px",
            background: "white",
            // border: "1px solid black",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            borderRadius: "50px",
            cursor: "pointer",
          }}
          onClick={() => {
            handleClick(1);
          }}
        >
          {firstLabelPart}
        </div>
        <div
          style={{
            height: "58px",
            width: "50%",
            paddingInline: "20px",
            background: "white",
            // border: "1px solid black",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            borderRadius: "50px",
            cursor: "pointer",
          }}
          onClick={() => {
            handleClick(2);
          }}
        >
          {secondLabelPart}
        </div>
      </div>
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
          {!isModify && (
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <div>
                  {" "}
                  <label>New Rule: </label>
                </div>
                <div>
                  <button type="submit" style={{ background: "white", border: "none", width: "35px" }} onClick={handleNodeClick}>
                    X
                  </button>
                </div>
              </div>
              <input
                type="text"
                style={{ border: "1px solid black" }}
                value={inputText}
                onChange={(event) => {
                  setInputText(event.currentTarget.value);
                }}
              />
              <div style={{ display: "flex", gap: "5px", alignItems: "baseline" }}>
                <input type="checkbox" onChange={() => setIsTerminal(!isTerminal)} checked={isTerminal} />
                <label>Terminal </label>
              </div>
              <div style={{ display: "flex", gap: "5px" }}>
                <input type="checkbox" onChange={() => setIsOptional(!isOptional)} checked={isOptional} />
                <label>Optional </label>
              </div>
              <div style={{ display: "flex", gap: "5px", alignItems: "baseline" }}>
                <button
                  type="submit"
                  onClick={() => {
                    const variableSymbol: "$?" | "?" | "$" | "" =
                      isOptional && isTerminal ? "$?" : isOptional ? "?" : isTerminal ? "$" : "";
                    createOrUpdateRuleMutate({
                      variables: {
                        id: undefined,
                        name: data.label + "_" + variableSymbol + inputText,
                        lhs: data.label,
                        rhs: variableSymbol + "" + inputText,
                      },
                    });
                    setPanelOpen(false);
                    setInputText("");
                    setIsTerminal(false);
                    setIsOptional(false);
                  }}
                >
                  Create
                </button>
                {data.isDelete && <button onClick={() => setIsModify(true)}>Edit</button>}
                {data.isDelete && (
                  <button
                    onClick={() => {
                      const removeRule = data?.rules?.find((rules: { node: { id: string; name: string; lhs: string; rhs: string } }) => {
                        return rules.node.rhs === data.label;
                      });
                      deleteRuleMutate({ variables: { id: removeRule?.node?.id || "" } });
                    }}
                  >
                    Delete
                  </button>
                )}
              </div>
            </div>
          )}
          {isModify && data.idAssociation && subNode === 1 && (
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              <label>Edit</label>
              <input
                type="text"
                style={{ border: "1px solid black" }}
                value={modifyFirstLabelPart}
                onChange={(event) => setModifyFirstLabelPart(event.currentTarget.value)}
              ></input>
              <div style={{ display: "flex", gap: "3px" }}>
                <button onClick={() => setIsModify(false)}>Back</button>
                <button
                  onClick={() => {
                    const variableSymbol: "$?" | "?" | "$" | "" =
                      isOptional && isTerminal ? "$?" : isOptional ? "?" : isTerminal ? "$" : "";
                    createOrUpdateRuleMutate({
                      variables: {
                        id: data.idAssociation,
                        name: data.fatherLabel + "_" + variableSymbol + modifyFirstLabelPart + " " + modifySecondLabelPart,
                        lhs: data.fatherLabel,
                        rhs: modifyFirstLabelPart + " " + modifySecondLabelPart,
                      },
                    });
                  }}
                >
                  Save
                </button>
              </div>
            </div>
          )}
          {isModify && data.idAssociation && subNode === 2 && (
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              <label>Edit</label>
              <input
                type="text"
                style={{ border: "1px solid black" }}
                value={modifySecondLabelPart}
                onChange={(event) => setModifySecondLabelPart(event.currentTarget.value)}
              ></input>
              <div style={{ display: "flex", gap: "3px" }}>
                <button onClick={() => setIsModify(false)}>Back</button>
                <button
                  onClick={() => {
                    const variableSymbol: "$?" | "?" | "$" | "" =
                      isOptional && isTerminal ? "$?" : isOptional ? "?" : isTerminal ? "$" : "";
                    createOrUpdateRuleMutate({
                      variables: {
                        id: data.idAssociation,
                        name: data.fatherLabel + "_" + variableSymbol + modifyFirstLabelPart + " " + modifySecondLabelPart,
                        lhs: data.fatherLabel,
                        rhs: modifyFirstLabelPart + " " + modifySecondLabelPart,
                      },
                    });
                  }}
                >
                  Save
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
