/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { useToast } from "@components/Form/Form/ToastProvider";
import { Typography, useTheme } from "@mui/material";
import React from "react";
import { Handle, Position } from "react-flow-renderer";
import { useCreateOrUpdateRuleQueryMutation, useDeleteRulesMutation } from "../../../graphql-generated";

export default function NodeGraphRule(props: any) {
  const { data } = props;
  const [inputText, setInputText] = React.useState("");
  const [modify, setModify] = React.useState(data.label);
  const [isPanelOpen, setPanelOpen] = React.useState(false);
  const [isOptional, setIsOptional] = React.useState(false);
  const [isTerminal, setIsTerminal] = React.useState(false);
  const [isModify, setIsModify] = React.useState(false);
  const theme = useTheme();

  const [createOrUpdateRuleMutate] = useCreateOrUpdateRuleQueryMutation({
    refetchQueries: ["Rule", "Rules", "QueryAnalysesRules", "AddRulesToQueryAnalyses", "RemoveRuleFromQueryAnalyses"],
    onCompleted(data) {},
  });
  const handleNodeClick = () => {
    setPanelOpen(!isPanelOpen);
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
        border: "1px solid red",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        borderRadius: "50px",
        cursor: "pointer",
      }}
      onClick={!isPanelOpen ? handleNodeClick : undefined}
    >
      <Handle type="target" position={Position.Top} isConnectable={true} />
      <Typography variant="body1" ml={1} color={theme.palette.primary.main}>
        {data.label}
      </Typography>
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
                  <button
                    type="submit"
                    style={{
                      background: "white",
                      border: "none",
                      width: "35px",
                    }}
                    onClick={handleNodeClick}
                  >
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
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              <label>Edit</label>
              <input
                type="text"
                style={{ border: "1px solid black" }}
                value={modify}
                onChange={(event) => setModify(event.currentTarget.value)}
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
                        name: data.fatherLabel + "_" + variableSymbol + modify,
                        lhs: data.fatherLabel,
                        rhs: modify,
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

