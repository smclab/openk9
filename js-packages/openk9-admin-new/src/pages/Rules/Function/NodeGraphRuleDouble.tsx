import { useToast } from "@components/Form";
import React from "react";
import { Handle, Position } from "react-flow-renderer";
import { useCreateOrUpdateRuleQueryMutation, useDeleteRulesMutation } from "../../../graphql-generated";

export default function NodeGraphRuleDouble(props: any) {
  const { data } = props;
  const [inputText, setInputText] = React.useState("");
  const [modify, setModify] = React.useState(data.label);
  const [subNode, setSubNode] = React.useState(0);
  const [isPanelOpen, setPanelOpen] = React.useState(false);
  const [isOptional, setIsOptional] = React.useState(false);
  const [isTerminal, setIsTerminal] = React.useState(false);
  const [isModify, setIsModify] = React.useState(false);

  const labelParts = data.label.split(" ");

  const fathers: string[] = [];

  data.rules.forEach((rule: any) => {
    fathers.push(rule.node.lhs);
  });

  const hasSon = labelParts.filter((x: string) => fathers.includes(x)).length;

  const [createOrUpdateRuleMutate, createOrUpdateRuleMutation] = useCreateOrUpdateRuleQueryMutation({
    refetchQueries: ["Rule", "Rules", "QueryAnalysesRules", "AddRulesToQueryAnalyses", "RemoveRuleFromQueryAnalyses"],
    onCompleted(data) {
      // showToast({ displayType: "success", title: "Rule created", content: "create" ?? "" });
    },
  });
  const handleNodeClick = (subNode: number) => {
    if (isPanelOpen) {
      return;
    }
    setModify(labelParts[subNode - 1]);
    setPanelOpen(!isPanelOpen);
    setSubNode(subNode);
  };
  const toast = useToast();
  const [deleteRuleMutate] = useDeleteRulesMutation({
    refetchQueries: ["Rule", "Rules", "QueryAnalysesRules", "AddRulesToQueryAnalyses", "RemoveRuleFromQueryAnalyses"],
    onCompleted(data) {
      if (data.deleteRule?.id) {
        toast({
          title: "Rule Deleted",
          content: "Rule has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Rule",
        displayType: "error",
      });
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
      {labelParts.map((label: string, index: number) => {
        return (
          <div key={index + 1}>
            <Handle type="target" position={Position.Top} isConnectable={true} />
            <div style={{ display: "flex" }}>
              <div
                style={{
                  height: "56px",
                  width: "50%",
                  paddingInline: "20px",
                  background: "white",
                  // border: "1px solid black",
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                  // borderRadius: "50px",
                  cursor: "pointer",
                  color: fathers.includes(label) ? "red" : "black",
                }}
                onClick={() => {
                  handleNodeClick(index + 1);
                }}
              >
                {label}
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
                  <div
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      gap: "8px",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                      }}
                    >
                      <div>
                        {" "}
                        {(fathers.includes(labelParts[subNode - 1]) || hasSon === 0) && (
                          <label>New Rule for {labelParts[subNode - 1]}: </label>
                        )}
                        {!fathers.includes(labelParts[subNode - 1]) && hasSon > 0 && (
                          <label>Edit {labelParts[subNode - 1]}</label>
                        )}
                      </div>
                      <div>
                        <button
                          type="submit"
                          style={{
                            background: "white",
                            border: "none",
                            width: "35px",
                          }}
                          onClick={() => {
                            setPanelOpen(!isPanelOpen);
                          }}
                        >
                          X
                        </button>
                      </div>
                    </div>
                    {(fathers.includes(labelParts[subNode - 1]) || hasSon === 0) && (
                      <div>
                        <input
                          type="text"
                          style={{ border: "1px solid black" }}
                          value={inputText}
                          onChange={(event) => {
                            setInputText(event.currentTarget.value);
                          }}
                        />
                        <div
                          style={{
                            display: "flex",
                            gap: "5px",
                            alignItems: "baseline",
                          }}
                        >
                          <input type="checkbox" onChange={() => setIsTerminal(!isTerminal)} checked={isTerminal} />
                          <label>Terminal </label>
                        </div>
                        <div style={{ display: "flex", gap: "5px" }}>
                          <input type="checkbox" onChange={() => setIsOptional(!isOptional)} checked={isOptional} />
                          <label>Optional </label>
                        </div>
                      </div>
                    )}
                    <div
                      style={{
                        display: "flex",
                        gap: "5px",
                        alignItems: "baseline",
                      }}
                    >
                      {(fathers.includes(labelParts[subNode - 1]) || hasSon === 0) && (
                        <button
                          type="submit"
                          onClick={() => {
                            const variableSymbol: "$?" | "?" | "$" | "" =
                              isOptional && isTerminal ? "$?" : isOptional ? "?" : isTerminal ? "$" : "";
                            createOrUpdateRuleMutate({
                              variables: {
                                id: undefined,
                                name: labelParts[subNode - 1] + "_" + variableSymbol + inputText,
                                lhs: labelParts[subNode - 1],
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
                      )}
                      {data.isDelete && !fathers.includes(labelParts[subNode - 1]) && (
                        <button onClick={() => setIsModify(true)}>Edit</button>
                      )}
                      {data.isDelete && !fathers.includes(labelParts[subNode - 1]) && hasSon === 0 && (
                        <button
                          onClick={() => {
                            const removeRule = data?.rules?.find(
                              (rules: {
                                node: {
                                  id: string;
                                  name: string;
                                  lhs: string;
                                  rhs: string;
                                };
                              }) => {
                                return rules.node.rhs === data.label;
                              },
                            );
                            deleteRuleMutate({
                              variables: { id: removeRule?.node?.id || "" },
                            });
                          }}
                        >
                          Delete
                        </button>
                      )}
                    </div>
                  </div>
                )}
                {isModify && data.idAssociation && (
                  <div
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      gap: "8px",
                    }}
                  >
                    <label>Edit</label>
                    <input
                      type="text"
                      style={{ border: "1px solid black" }}
                      value={modify}
                      onChange={(event) => {
                        setModify(event.currentTarget.value);
                      }}
                    ></input>
                    <div style={{ display: "flex", gap: "3px" }}>
                      <button onClick={() => setIsModify(false)}>Back</button>
                      <button
                        onClick={() => {
                          labelParts[subNode - 1] = modify;
                          const variableSymbol: "$?" | "?" | "$" | "" =
                            isOptional && isTerminal ? "$?" : isOptional ? "?" : isTerminal ? "$" : "";
                          createOrUpdateRuleMutate({
                            variables: {
                              id: data.idAssociation,
                              name: data.fatherLabel + "_" + variableSymbol + labelParts.join(" "),
                              lhs: data.fatherLabel,
                              rhs: labelParts.join(" "),
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
      })}
    </div>
  );
}
