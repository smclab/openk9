import { Box, Button } from "@mui/material";
import { TemplateType, TemplateValue } from "@pages/Analyzer/gql";
import React, { useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { KeyValue } from "../utils";

const useTemplate = ({
  templateSelected,
  jsonConfig,
  type,
}: {
  templateSelected: TemplateType[];
  jsonConfig?: string | null | undefined;
  type?: string | null | undefined;
}) => {
  const [typeSelected, setTypeSelected] = useState<string | null>(null);
  const [template, setTemplate] = useState<TemplateType | null | undefined>(null);

  React.useEffect(() => {
    if (type && jsonConfig && (typeSelected === null || typeSelected === type)) {
      const defaultValue = templateSelected.find((template) => template.title === type);
      const jsonRecoveryData: KeyValue = jsonConfig ? JSON.parse(jsonConfig) : null;
      const singleElement = defaultValue?.value;
      const constructValue: TemplateValue[] = singleElement
        ? singleElement.map((element) => ({
            name: element.name,
            value: jsonRecoveryData[element.name],
            type: element.type,
            description: element.description,
            options: element.options,
          }))
        : [];

      const initialTemplate: TemplateType | undefined =
        (typeSelected === null || type === typeSelected) && jsonRecoveryData
          ? defaultValue && {
              title: defaultValue.title || "",
              description: defaultValue.description || "",
              type: defaultValue.type || "",
              value: constructValue,
            }
          : defaultValue;

      setTypeSelected(type);
      setTemplate(initialTemplate);
    }
  }, [type, jsonConfig]);

  const changeValueKey = useCallback(
    (key: string, value: string | number | Array<string> | boolean) => {
      setTemplate((pre: TemplateType | null | undefined) =>
        pre ? { ...pre, value: [...pre.value].map((el) => (el.name === key ? { ...el, value } : el)) } : pre,
      );
    },
    [setTemplate, template],
  );
  const changeType = useCallback(
    (type: string) => {
      setTypeSelected(type);
      const find = templateSelected.find((template) => template.title === type);
      setTemplate(find);
    },
    [setTemplate, templateSelected],
  );

  return { template, changeValueKey, changeType, typeSelected: typeSelected || "", setTypeSelected };
};

export default useTemplate;

export function createJsonString({
  template,
  type,
}: {
  template?: Array<TemplateValue> | null | undefined;
  type?: string | null | undefined;
}) {
  if (!template) {
    console.error("input error.");
    return "{}";
  }

  const result: Record<string, any> = {};
  if (type) {
    result.type = type;
  }
  template.forEach((temp) => {
    result[temp.name] = temp.value;
  });
  return JSON.stringify(result);
}

export function NavigationButtons({
  isRecap,
  goToRecap,
  submitForm,
  removeRecap,
  pathBack,
}: {
  isRecap: boolean;
  goToRecap(): void;
  submitForm(): void;
  removeRecap(): void;
  pathBack?: string;
}) {
  const navigate = useNavigate();
  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: pathBack ? "space-between" : "flex-end",
        width: "100%",
        paddingBlock: "20px",
      }}
    >
      {pathBack && (
        <Button
          className="btn btn-secondary"
          variant="outlined"
          type="button"
          onClick={() => (isRecap ? removeRecap() : navigate(pathBack))}
        >
          Back
        </Button>
      )}
      <Button className="btn btn-danger" variant="contained" type="button" onClick={isRecap ? submitForm : goToRecap}>
        {isRecap ? "Create Entity" : "Save and Continue"}
      </Button>
    </Box>
  );
}
