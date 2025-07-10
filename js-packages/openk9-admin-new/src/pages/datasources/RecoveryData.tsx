import React from "react";
import { Field, Template } from "./components/Sections/DataSource/DynamicForm";
import { CustomForm } from "./Function";

export async function recoveryForm({
  restClient,
  id,
  defaultData,
  requestBody,
}: {
  restClient: any;
  id: string | undefined | null;
  defaultData: string;
  requestBody: any;
}) {
  const defaultParsed = JSON.parse(defaultData || "{}");

  const remapFormData = (data: any) => {
    const fields = data.fields;
    if (Array.isArray(fields)) {
      return fields?.map((item) => {
        if (defaultParsed[item.name] && (item.type === "text" || item.type === "number")) {
          return {
            ...item,
            values: [{ value: defaultParsed[item.name][0]?.value, isDefault: true }],
          };
        }
        if (defaultParsed[item.name] && item.type === "list") {
          return {
            ...item,
            values: defaultParsed[item.name],
          };
        }

        if (defaultParsed[item.name] && item.type === "select") {
          return {
            ...item,
            values: item.values.map((v: any) =>
              v === defaultParsed[item.name]?.value ? { ...v, isDefault: true } : { ...v },
            ),
          };
        }

        if (defaultParsed[item.name] && item.type === "multiselect") {
          return {
            ...item,
            values: item.values.map((v: any) =>
              defaultParsed[item.name]?.some((selected: any) => selected.value === v.value)
                ? { ...v, isDefault: true }
                : { ...v },
            ),
          };
        }

        return item;
      });
    }

    return data;
  };

  if (id) {
    const response = await restClient.pluginDriverResource.getApiDatasourcePluginDriversForm(id);
    return remapFormData(response);
  }
}

export function useRecoveryForm(restClient: any, formValues: any, requestBody: any) {
  const [formCustom, setFormCustom] = React.useState<CustomForm[] | undefined>([]);
  const [loadingFormCustom, setLoadingFormCustom] = React.useState(true);
  const [recoveryFormStandart, setRecoveryFormStandart] = React.useState<Template | undefined>(undefined);
  React.useEffect(() => {
    const formCustomClient = recoveryForm({
      restClient,
      id: formValues?.pluginDriverSelect?.id,
      defaultData: formValues?.jsonConfig || "{}",
      requestBody,
    });

    formCustomClient
      .then((data: Field[]) => {
        const remappedData = data
          .map((formItem) => {
            if (formItem.type === "text" && Array.isArray(formItem.values) && formItem.values.length === 0) {
              return {
                ...formItem,
                values: [{ isDefault: true, value: "" }],
              };
            }
            return formItem;
          })
          .map((v) => (v.name === null ? { ...v, name: v.label } : { ...v }));

        setRecoveryFormStandart({ fields: remappedData } as Template);
        setLoadingFormCustom(false);
      })
      .catch((error) => {
        console.error("Errore durante il recupero del form:", error);
        setFormCustom(undefined);
        setLoadingFormCustom(false);
      });
  }, [formValues.pluginDriverSelect?.id]);

  return { formCustom, loadingFormCustom, recoveryFormStandart, setFormCustom };
}

export const constructTabs = ({
  datasourceId,
  mode,
  isDisabledNextStep,
  isRecap,
}: {
  datasourceId: string;
  mode: string;
  isDisabledNextStep: boolean;
  isRecap: boolean;
}) => [
  {
    label: "CONNECTORS",
    value: "connectors",
    step: 1,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/connectors`,
    disabled: isDisabledNextStep,
  },
  {
    label: "DATASOURCE",
    value: "datasource",
    step: 2,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/datasource`,
    disabled: isDisabledNextStep,
  },
  {
    label: "PIPELINE",
    value: "pipeline",
    step: 3,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/pipeline`,
    disabled: isDisabledNextStep,
  },
  {
    label: "Data Index",
    value: "dataIndex",
    step: 4,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/data-index`,
    disabled: isDisabledNextStep,
  },
  ...(isRecap
    ? [
        {
          label: "RECAP",
          value: "recap",
          step: 5,
          path: `/data-source/${datasourceId}/mode/${mode}/landingTab/recap`,
          disabled: isDisabledNextStep,
        },
      ]
    : []),
  ...(datasourceId !== "new"
    ? [
        {
          label: "Monitoring",
          value: "monitoring",
          path: `/data-source/${datasourceId}/mode/${mode}/landingTab/monitoring`,
        },
      ]
    : []),
  ...(datasourceId !== "new"
    ? [
        {
          label: "Reindex",
          value: "reindex",
          path: `/data-source/${datasourceId}/mode/${mode}/landingTab/reindex`,
        },
      ]
    : []),
];
