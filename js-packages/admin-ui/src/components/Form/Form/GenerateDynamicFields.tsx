import { Box, FormControl, MenuItem, Select, Typography } from "@mui/material";
import { TemplateType } from "@pages/Analyzer/gql";
import React from "react";
import { BooleanInput, NumberInputSimple, TextInputSimple } from "../Inputs";
import { AutocompleteDropdownWithOptions } from "../Select/AutocompleteDropdown";
import { InformationField } from "../utils/informationField";
import Autocomplete from "./AutoComplete";

function GenerateDynamicFields<E extends Record<string, any>>({
  templates,
  setType,
  isRecap,
  changeValueKey,
  type,
  template,
}: {
  templates: TemplateType[];
  setType: (type: string) => void;
  isRecap: boolean;
  changeValueKey: (key: string, value: string | number | Array<string> | boolean) => void;
  type: string;
  template: TemplateType | null | undefined;
}) {
  const renderField = (template: TemplateType) => {
    const values = template?.value;
    const jsx = values?.map((constructField) => {
      switch (constructField.type) {
        case "string":
          return (
            <TextInputSimple
              key={constructField.name}
              label={constructField.name}
              value={constructField.value as string}
              description={constructField.description}
              disabled={isRecap}
              onChange={(e) => {
                changeValueKey(constructField.name, e.currentTarget.value);
              }}
            />
          );
        case "number":
          return (
            <NumberInputSimple
              key={constructField.name}
              label={constructField.name}
              disabled={isRecap}
              value={constructField.value as number}
              description={constructField.description}
              onChange={(e) => {
                changeValueKey(constructField.name, Number(e.currentTarget.value));
              }}
            />
          );
        case "boolean":
          return (
            <BooleanInput
              disabled={isRecap}
              label={constructField.name}
              description={constructField.description}
              onChange={(value) => changeValueKey(constructField.name, value)}
              id={"boolean-input" + constructField.name}
              validationMessages={[]}
              value={constructField.value as boolean}
            />
          );
        case "select":
          return (
            <FormControl fullWidth>
              <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
                <Typography variant="subtitle1" component="label">
                  {constructField.name}
                </Typography>
                {constructField.description && <InformationField description={constructField.description} />}
              </Box>
              <Select disabled={isRecap} id="demo-simple-select" value={constructField.value}>
                {constructField?.options?.map((option) => (
                  <MenuItem
                    value={option.value}
                    onChange={(e) => {
                      const newValue = e.currentTarget.value;
                      changeValueKey(constructField.name, newValue);
                    }}
                  >
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          );
        case "multi-select":
          return (
            <>
              <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
                <Typography variant="subtitle1" component="label">
                  {constructField.name}
                </Typography>
                {constructField.description && <InformationField description={constructField.description} />}
              </Box>
              <Autocomplete
                defaultChip={constructField.value as Array<string>}
                disabled={isRecap}
                setChips={(newValue) => {
                  changeValueKey(constructField.name, newValue);
                }}
              />
            </>
          );
        default:
          return null;
      }
    });

    return jsx;
  };

  return (
    <div>
      <Box display={"flex"} flexDirection={"column"} gap={"20px"}>
        <Box display={"flex"} flexDirection={"column"}>
          <AutocompleteDropdownWithOptions
            onChange={(event) => setType(event.id as E[string])}
            label="Type"
            disabled={isRecap}
            value={{ id: type, name: type }}
            clearLabel="Unset"
            onClear={() => setType("")}
            optionsDefault={templates.map((item) => ({ label: item.title, value: item.title }))}
          />
        </Box>
        <div>{template && renderField(template)}</div>
      </Box>
    </div>
  );
}

export const GenerateDynamicFieldsMemo = React.memo(GenerateDynamicFields);
