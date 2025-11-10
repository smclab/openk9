import { BooleanInput, KeyValue, NumberInputSimple, TextInputSimple } from "@components/Form";
import Autocomplete from "@components/Form/Form/AutoComplete";
import { Box, FormControl, Grid, InputLabel, MenuItem, Select, Typography } from "@mui/material";
import React from "react";
import { StringMapInput } from "./StringMap/StringMap";
import { InformationField } from "@components/Form/utils/informationField";

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
    const filteredTemplate = template ? filterValidFields(template) : null;
    if (filteredTemplate) {
      const dynamicTemplateUpdate = jsonConfig
        ? convertJsonToTemplate({ template: filteredTemplate, jsonConfig })
        : filteredTemplate;
      const jsonDynamicTemplateUpdate = convertTemplateToJson({ fields: dynamicTemplateUpdate.fields });

      setDynamicTemplate(dynamicTemplateUpdate);
      setDynamicFormJson(jsonDynamicTemplateUpdate);
    } else {
      setDynamicTemplate(null);
      setDynamicFormJson(null);
    }
  }, [template, jsonConfig]);

  const changeValueTemplate = (
    fieldName: string,
    newValue: string | number | boolean | string[] | { location: string; title: string }[],
  ) => {
    if (dynamicTemplate) {
      const updatedFields = dynamicTemplate.fields.map((field) => {
        if (field.name === fieldName) {
          let updatedValues: FieldValue[] = [];
          if (field.type === "multiselect") {
            if (field.values && field.values.length > 0) {
              updatedValues = field?.values?.map((value) => ({
                ...value,
                isDefault: (newValue as string[])?.includes(value.value as string),
              }));
            }
          } else if (field.type === "stringMap") {
            updatedValues = newValue as any;
          } else if (field.type === "select") {
            if (field.values && field.values.length > 0) {
              updatedValues = field?.values?.map((value) => ({
                ...value,
                isDefault: value?.value === newValue,
              }));
            } else {
              updatedValues = [
                {
                  value: String(newValue),
                  isDefault: true,
                },
              ];
            }
          } else if (field.type === "checkbox" || field.type === "boolean") {
            updatedValues = [
              {
                value: typeof newValue === "boolean" ? newValue : newValue === "true",
                isDefault: true,
              },
            ];
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

      const newDynamicTemplate = { fields: [...updatedFields] };
      const newJsonDynamicTemplateUpdate = convertTemplateToJson(newDynamicTemplate);

      setDynamicTemplate(newDynamicTemplate);
      setDynamicFormJson(newJsonDynamicTemplateUpdate);
    }
  };

  return { dynamicTemplate, changeValueTemplate, dynamicFormJson };
}

function convertJsonToTemplate({ template, jsonConfig }: { template: Template; jsonConfig: string }): Template {
  const parsedConfig = JSON.parse(jsonConfig);

  const updatedFields = template.fields.map((field) => {
    if (parsedConfig[field.name] !== undefined) {
      const fieldConfig = parsedConfig[field.name];

      let updatedValues: FieldValue[] = [];
      if (field.type === "stringMap") {
        const obj = typeof fieldConfig === "object" && fieldConfig !== null ? fieldConfig : {};
        updatedValues = Object.entries(obj).map(([key, value]) => ({
          [key]: value,
        })) as any[];
      } else if (field.type === "multiselect") {
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
      case "stringMap":
        acc[field.name] = fieldValue as Record<string, string>;
        break;
      default:
        acc[field.name] = fieldValue;
        break;
    }
    return acc;
  }, {} as { [key: string]: any });
  return JSON.stringify(jsonConfig);
}

function getDefaultValue(field: Field): string | number | boolean | string[] | Record<string, string> {
  if (!field.values || field.values.length === 0) {
    switch (field.type) {
      case "text":
        return "";
      case "number":
        return 0;
      case "checkbox":
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
      case "stringMap":
        return {};
      default:
        return "";
    }
  }
  if (field.type === "stringMap") {
    const val = (field.values as KeyValue[]).reduce((acc, curr) => {
      if (curr.key !== undefined && curr.value !== undefined) {
        acc[curr.key] = curr.value;
      }
      return acc;
    }, {} as Record<string, string>);
    return val;
  }

  if (field.type === "list" || field.type === "multiselect") {
    return field.values.filter((value) => value.isDefault).map((value) => value.value as string);
  }
  if (field.type === "checkbox" || field.type === "boolean") {
    return !!field.values.find((value) => value.isDefault)?.value;
  }
  return field.values.find((value) => value.isDefault)?.value ?? field.values[0]?.value ?? "";
}

type FieldValue = {
  value: string | Array<string> | boolean | KeyValue;
  isDefault: boolean;
  [key: string]: any;
};

export type FieldValueWithLocation = {
  location?: string;
  title?: string;
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
    | "stringMap"
    | "multiselect";

  size: number;
  required: boolean;
  values: FieldValue[];
  validator: Validator;
};

export type Template = {
  fields: Field[];
};

export type ChangeValueKey = (
  fieldName: string,
  newValue: string | number | boolean | string[] | { location: string; title: string }[],
) => void;

export function GenerateDynamicForm({
  templates,
  changeValueKey,
  disabled,
}: {
  templates: Template | null;
  changeValueKey: ChangeValueKey;
  disabled: boolean;
}) {
  const firstStringMapValues = React.useMemo(() => {
    if (!templates || !templates.fields) return {};
    return templates.fields.reduce((acc, field) => {
      if (field.type === "stringMap") {
        acc[field.name] = field?.values && field.values.length > 0 ? Object.assign({}, ...field.values) : {};
      }
      return acc;
    }, {} as Record<string, any>);
  }, [templates]);

  const renderField = (field: Field) => {
    const value = getDefaultValue(field);
    const jsx = (() => {
      switch (field.type) {
        case "text":
          return (
            <TextInputSimple
              key={field.name}
              isRequired={field.required}
              label={field.label}
              value={value as string}
              description={field.info}
              disabled={disabled}
              sx={{ paddingBottom: "0px" }}
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
              isRequired={field.required}
              description={field.info}
              disabled={disabled}
              setStyles={{ paddingBottom: 0 }}
              onChange={(e) => {
                changeValueKey(field.name, Number(e.currentTarget.value));
              }}
            />
          );
        case "stringMap":
          const firstValue = firstStringMapValues[field.name] || {};
          return (
            <StringMapInput
              key={field.name}
              defaultValue={
                typeof firstValue === "object" && firstValue !== null
                  ? Object.keys(firstValue).map((value: string) => ({
                      key: value || "",
                      value: (firstValue as KeyValue)[value] || "",
                    }))
                  : []
              }
              label={field.label}
              description={field.info}
              onChange={(newMap) => changeValueKey(field.name, newMap as any)}
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
              {field.required && <span style={{ color: "red", marginLeft: "3px" }}>*</span>}
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
              {field.required && <span style={{ color: "red", marginLeft: "3px" }}>*</span>}
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
              <div style={{ display: "flex", alignItems: "center", gap: "10px", justifyContent: "space-between" }}>
                <div style={{ display: "flex", gap: "10px" }}>
                  <Typography variant="subtitle1" component="label">
                    {field.label}
                  </Typography>
                  {field.required && <span style={{ color: "red", marginLeft: "3px" }}>*</span>}
                </div>
                {field.info && <InformationField description={field.info} />}
              </div>
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
              isRequired={field.required}
              label={field.label}
              type="password"
              value={value as string}
              description={field.info}
              sx={{ paddingBottom: "0px" }}
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
              isRequired={field.required}
              label={field.label}
              type="email"
              value={value as string}
              description={field.info}
              sx={{ paddingBottom: "0px" }}
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
              isRequired={field.required}
              label={field.label}
              sx={{ paddingBottom: "0px" }}
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
              isRequired={field.required}
              label={field.label}
              type="date"
              value={value as string}
              sx={{ paddingBottom: "0px" }}
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
              isRequired={field.required}
              label={field.label}
              type="url"
              value={value as string}
              sx={{ paddingBottom: "0px" }}
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

  const renderedFields = templates?.fields
    ?.map((field) => ({ field, jsx: renderField(field) }))
    ?.filter(({ jsx }) => jsx !== null);

  return (
    <Box>
      <Grid container spacing={5} alignItems="center">
        {renderedFields.map(({ field, jsx }, index) => (
          <Grid item key={field.name} xs={12} sm={field.size} md={field.size} lg={field.size} xl={field.size}>
            {jsx}
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}

function filterValidFields(template: any): Template | null {
  if (!template || typeof template !== "object" || !Array.isArray(template.fields)) {
    console.log("Template structure is invalid");
    return null;
  }
  const validTypes = [
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
    "stringMap",
    "multiselect",
  ];
  const validFields = template.fields.filter((field: any) => {
    const isFieldValid =
      typeof field.label === "string" && typeof field.name === "string" && validTypes.includes(field.type);
    if (!isFieldValid) {
      console.log("Invalid field:", field);
    }
    return isFieldValid;
  });
  if (validFields.length === 0) return null;
  return { fields: validFields };
}

export function DynamicFormArray({
  templates,
  jsonConfigs,
  onChangeJsonConfig,
}: {
  templates: (Template | null | undefined)[];
  jsonConfigs: (string | null | undefined)[];
  onChangeJsonConfig: (idx: number, newJson: string) => void;
}) {
  return templates?.map((template, idx) => {
    const filteredTemplate = template ? filterValidFields(template) : null;
    let dynamicTemplate: Template | null = null;
    let dynamicFormJson: string | null = null;

    if (filteredTemplate) {
      const dynamicTemplateUpdate = jsonConfigs[idx]
        ? convertJsonToTemplate({ template: filteredTemplate, jsonConfig: jsonConfigs[idx]! })
        : filteredTemplate;
      dynamicTemplate = dynamicTemplateUpdate;
      dynamicFormJson = convertTemplateToJson({ fields: dynamicTemplateUpdate.fields });
    }

    const changeValueTemplate = (
      fieldName: string,
      newValue: string | number | boolean | string[] | Array<{ location: string; title: string }>,
    ) => {
      if (dynamicTemplate) {
        const updatedFields = dynamicTemplate.fields.map((field) => {
          if (field.name === fieldName) {
            let updatedValues: FieldValue[] = [];
            if (field.type === "multiselect") {
              if (field.values && field.values.length > 0) {
                updatedValues = field.values.map((value) => ({
                  ...value,
                  isDefault: (newValue as string[]).find((val) => val === value.value) ? true : false,
                }));
              }
            } else if (field.type === "stringMap") {
              updatedValues = newValue as any;
            } else if (field.type === "select") {
              if (field.values && field.values.length > 0) {
                updatedValues = field?.values?.map((value) => ({
                  ...value,
                  isDefault: value?.value === newValue,
                }));
              } else {
                updatedValues = [
                  {
                    value: String(newValue),
                    isDefault: true,
                  },
                ];
              }
            } else if (field.type === "checkbox" || field.type === "boolean") {
              updatedValues = [
                {
                  value: typeof newValue === "boolean" ? newValue : newValue === "true",
                  isDefault: true,
                },
              ];
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

        dynamicFormJson = convertTemplateToJson({ fields: updatedFields });
        dynamicTemplate = { fields: updatedFields };
        onChangeJsonConfig(idx, dynamicFormJson);
      }
    };

    return { dynamicTemplate, changeValueTemplate, dynamicFormJson };
  });
}
