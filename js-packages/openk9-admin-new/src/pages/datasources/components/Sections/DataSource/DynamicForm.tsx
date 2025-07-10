import { BooleanInput, NumberInputSimple, TextInputSimple } from "@components/Form";
import Autocomplete from "@components/Form/Form/AutoComplete";
import { Box, FormControl, InputLabel, Typography, Select, MenuItem } from "@mui/material";
import React from "react";

export default function DynamicForm({
  template,
  jsonConfig,
}: {
  template: Template | null | undefined;
  jsonConfig: string | null | undefined;
}) {
  const [dynamicTemplate, setDynamicTemplate] = React.useState<Template | null>(null);
  const [dynamicFormJson, setDynamicFormJson] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (template && validateTemplate(template)) {
      const dynamicTemplateUpdate = jsonConfig ? convertJsonToTemplate({ template, jsonConfig }) : template;
      const jsonDynamicTemplateUpdate = convertTemplateToJson({ fields: dynamicTemplateUpdate.fields });

      setDynamicTemplate(dynamicTemplateUpdate);
      setDynamicFormJson(jsonDynamicTemplateUpdate);
    } else {
      setDynamicTemplate(null);
      setDynamicFormJson(null);
    }
  }, [template, jsonConfig]);

  const changeValueTemplate = (fieldName: string, newValue: string | number | Array<string> | boolean) => {
    if (dynamicTemplate) {
      const updatedFields = dynamicTemplate.fields.map((field) => {
        if (field.name === fieldName) {
          let updatedValues: FieldValue[] = [];
          if (field.type === "multiselect") {
            if (field.values && field.values.length > 0) {
              updatedValues = field?.values?.map((value) => ({
                ...value,
                isDefault: (newValue as string[]).find((val) => val === value.value) ? true : false,
              }));
            }
          } else if (field.type === "select") {
            if (field.values && field.values.length > 0) {
              updatedValues = field?.values?.map((value) => ({
                ...value,
                isDefault: value.value === newValue,
              }));
            } else {
              updatedValues = [
                {
                  value: String(newValue),
                  isDefault: true,
                },
              ];
            }
          } else {
            const formattedValue = field.type === "list" ? (newValue as string[]) : [String(newValue)];
            updatedValues = formattedValue.map((val) => ({
              value: val,
              isDefault: true,
            }));
          }

          return {
            ...field,
            values: updatedValues,
          };
        }
        return field;
      });

      const jsonDynamicTemplateUpdate = convertTemplateToJson({ fields: updatedFields });
      setDynamicFormJson(jsonDynamicTemplateUpdate);
      setDynamicTemplate({ fields: updatedFields });
    }
  };

  return { dynamicTemplate, changeValueTemplate, dynamicFormJson };
}

function validateTemplate(template: any): template is Template {
  if (!template || typeof template !== "object" || !Array.isArray(template.fields)) {
    console.log("Template structure is invalid");
    return false;
  }
  const isValid = template.fields.every((field: any) => {
    const isFieldValid =
      typeof field.label === "string" &&
      typeof field.name === "string" &&
      [
        "text",
        "number",
        "list",
        "select",
        "boolean",
        "password",
        "email",
        "time",
        "date",
        "url",
        "checkbox",
        "multiselect",
      ].includes(field.type);
    if (!isFieldValid) {
      console.log("Invalid field:", field);
    }
    return isFieldValid;
  });
  return isValid;
}

function convertJsonToTemplate({ template, jsonConfig }: { template: Template; jsonConfig: string }): Template {
  const parsedConfig = JSON.parse(jsonConfig);

  const updatedFields = template.fields.map((field) => {
    if (parsedConfig[field.name] !== undefined) {
      const fieldConfig = parsedConfig[field.name];

      let updatedValues: FieldValue[] = [];
      if (field.type === "multiselect") {
        if (Array.isArray(field.values) && field.values.length > 0) {
          updatedValues = field.values.map((value) => ({
            ...value,
            isDefault: Array.isArray(fieldConfig) && fieldConfig.includes(value.value),
          }));
        } else {
          updatedValues = Array.isArray(fieldConfig)
            ? fieldConfig.map((value) => ({
                value,
                isDefault: true,
              }))
            : [];
        }
      } else if (field.type === "select") {
        if (Array.isArray(field.values) && field.values.length > 0) {
          updatedValues = field.values.map((value) => ({
            ...value,
            isDefault: value.value === fieldConfig,
          }));
        } else {
          updatedValues = [
            {
              value: fieldConfig,
              isDefault: true,
            },
          ];
        }
      } else if (field.type === "list") {
        updatedValues = Array.isArray(fieldConfig)
          ? fieldConfig.map((value) => ({
              value,
              isDefault: true,
            }))
          : [];
      } else {
        updatedValues = [
          {
            value: fieldConfig,
            isDefault: true,
          },
        ];
      }
      return {
        ...field,
        values: updatedValues,
      };
    }
    return {
      ...field,
      values: Array.isArray(field.values) ? field.values : [],
    };
  });

  return { fields: updatedFields };
}

function convertTemplateToJson(template: Template): string {
  const jsonConfig = template.fields.reduce((acc, field) => {
    const fieldValue = getDefaultValue(field);
    switch (field.type) {
      case "text":
        acc[field.name] = fieldValue as string;
        break;
      case "number":
        acc[field.name] = fieldValue as number;
        break;
      case "boolean":
        acc[field.name] = fieldValue as boolean;
        break;
      case "list":
        acc[field.name] = fieldValue as string[];
        break;
      case "select":
        acc[field.name] = fieldValue as string;
        break;
      default:
        acc[field.name] = fieldValue;
        break;
    }
    return acc;
  }, {} as { [key: string]: any });
  return JSON.stringify(jsonConfig);
}

function getDefaultValue(field: Field): string | number | boolean | string[] {
  if (!field.values || field.values.length === 0) {
    switch (field.type) {
      case "text":
        return "";
      case "number":
        return 0;
      case "checkbox":
        return false;
      case "boolean":
        return false;
      case "list":
        return [];
      case "select":
        return "";
      case "password":
      case "email":
      case "time":
      case "date":
      case "url":
        return "";
      default:
        return "";
    }
  }
  if (field.type === "list" || field.type === "multiselect") {
    return field.values.filter((value) => value.isDefault).map((value) => value.value as string);
  }
  return field.values.find((value) => value.isDefault)?.value ?? field.values[0]?.value ?? "";
}

type FieldValue = {
  value: string | Array<string> | boolean;
  isDefault: boolean;
};

type Validator = {
  min: number;
  max: number;
  regex: string;
};

export type Field = {
  info: string;
  label: string;
  name: string;
  type:
    | "text"
    | "number"
    | "list"
    | "select"
    | "boolean"
    | "password"
    | "email"
    | "time"
    | "date"
    | "url"
    | "checkbox"
    | "multiselect";
  size: number;
  required: boolean;
  values: FieldValue[];
  validator: Validator;
};

export type Template = {
  fields: Field[];
};

export function GenerateDynamicForm({
  templates,
  changeValueKey,
  disabled,
}: {
  templates: Template | null;
  changeValueKey: (fieldName: string, newValue: string | number | Array<string> | boolean) => void;
  disabled: boolean;
}) {
  const renderField = (field: Field) => {
    const value = getDefaultValue(field);
    const jsx = (() => {
      switch (field.type) {
        case "text":
          return (
            <TextInputSimple
              key={field.name}
              label={field.label}
              value={value as string}
              description={field.info}
              disabled={disabled}
              onChange={(e) => {
                changeValueKey(field.name, e.currentTarget.value);
              }}
            />
          );
        case "number":
          return (
            <NumberInputSimple
              key={field.name}
              label={field.label}
              value={Number(value)}
              description={field.info}
              disabled={disabled}
              onChange={(e) => {
                changeValueKey(field.name, Number(e.currentTarget.value));
              }}
            />
          );
        case "checkbox":
        case "boolean":
          return (
            <BooleanInput
              key={field.name}
              label={field.label}
              disabled={disabled}
              id="input"
              validationMessages={[]}
              value={Boolean(value)}
              description={field.info}
              onChange={(value) => changeValueKey(field.name, value)}
            />
          );
        case "multiselect":
          return (
            <FormControl disabled={disabled} fullWidth key={field.name} sx={{ paddingBottom: "20px" }}>
              <InputLabel>{field.label}</InputLabel>
              <Select
                label={field.label}
                multiple
                value={Array.isArray(value) ? value : []}
                onChange={(e) => {
                  changeValueKey(field.name, e.target.value as string[]);
                }}
                renderValue={(selected) => (selected as string[]).join(", ")}
              >
                {field.values && field.values.length > 0 ? (
                  field?.values?.map((option) => (
                    <MenuItem key={option.value as string} value={option.value as string}>
                      <Typography>{option.value as string}</Typography>
                    </MenuItem>
                  ))
                ) : (
                  <MenuItem value="">
                    <em>Nessuna opzione</em>
                  </MenuItem>
                )}
              </Select>
            </FormControl>
          );
        case "select":
          return (
            <FormControl disabled={disabled} fullWidth key={field.name}>
              <InputLabel>{field.label}</InputLabel>
              <Select
                label={field.label}
                value={value as string}
                onChange={(e) => {
                  changeValueKey(field.name, e.target.value as string);
                }}
              >
                {field.values && field.values.length > 0 ? (
                  field?.values?.map((option) => (
                    <MenuItem
                      key={option.value as string}
                      value={option.value as string | number | readonly string[] | undefined}
                    >
                      {option.value as string}
                    </MenuItem>
                  ))
                ) : (
                  <MenuItem value="">
                    <em>Nessuna opzione</em>
                  </MenuItem>
                )}
              </Select>
            </FormControl>
          );
        case "list":
          return (
            <>
              <Typography variant="subtitle1" component="label">
                {field.label}
              </Typography>
              <Autocomplete
                disabled={disabled}
                defaultChip={Array.isArray(value) ? value : []}
                setChips={(newValue) => {
                  changeValueKey(field.name, newValue);
                }}
              />
            </>
          );
        case "password":
          return (
            <TextInputSimple
              key={field.name}
              label={field.label}
              type="password"
              value={value as string}
              description={field.info}
              disabled={disabled}
              onChange={(e) => {
                changeValueKey(field.name, e.currentTarget.value);
              }}
            />
          );
        case "email":
          return (
            <TextInputSimple
              key={field.name}
              label={field.label}
              type="email"
              value={value as string}
              description={field.info}
              disabled={disabled}
              onChange={(e) => {
                changeValueKey(field.name, e.currentTarget.value);
              }}
            />
          );
        case "time":
          return (
            <TextInputSimple
              key={field.name}
              label={field.label}
              type="time"
              value={value as string}
              description={field.info}
              disabled={disabled}
              onChange={(e) => {
                changeValueKey(field.name, e.currentTarget.value);
              }}
            />
          );
        case "date":
          return (
            <TextInputSimple
              key={field.name}
              label={field.label}
              type="date"
              value={value as string}
              description={field.info}
              disabled={disabled}
              onChange={(e) => {
                changeValueKey(field.name, e.currentTarget.value);
              }}
            />
          );
        case "url":
          return (
            <TextInputSimple
              key={field.name}
              label={field.label}
              type="url"
              value={value as string}
              description={field.info}
              disabled={disabled}
              onChange={(e) => {
                changeValueKey(field.name, e.currentTarget.value);
              }}
            />
          );
        default:
          return null;
      }
    })();

    return jsx;
  };
  if (!templates) return null;
  return (
    <div>
      <Box display={"flex"} flexDirection={"column"} gap={"20px"}>
        <div>
          {templates?.fields?.map((field, index) => (
            <div key={index}>{renderField(field)}</div>
          ))}
        </div>
      </Box>
    </div>
  );
}
