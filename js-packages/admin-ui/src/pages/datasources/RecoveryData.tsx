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
import React from "react";
import type { TFunction } from "i18next";
import { Field, Template } from "./components/Sections/DataSource/DynamicForm";
import { CustomForm } from "./Function";
import { useRestClient } from "@components/queryClient";

type RestClient = ReturnType<typeof useRestClient>;
/** Risposta dell'endpoint form del plugin driver, derivata dal client OpenAPI. */
type PluginDriverForm = Awaited<ReturnType<RestClient["pluginDriverResource"]["getApiDatasourcePluginDriversForm"]>>;

/** Un default salvato nel jsonConfig: un valore singolo o una lista di valori. */
type DefaultValue = { value?: unknown; isDefault?: boolean };
type DefaultConfig = Record<string, DefaultValue[] | DefaultValue | undefined>;

function parseDefaultConfig(raw: string): DefaultConfig {
  try {
    const parsed: unknown = JSON.parse(raw || "{}");
    return typeof parsed === "object" && parsed !== null ? (parsed as DefaultConfig) : {};
  } catch {
    return {};
  }
}

type RecoveryFormValues = {
  pluginDriverSelect?: { id?: string | null } | null;
  jsonConfig?: string | null;
};

export async function recoveryForm({
  restClient,
  id,
  defaultData,
  requestBody,
}: {
  restClient: RestClient;
  id: string | undefined | null;
  defaultData: string;
  // NOTA: parametro trasportato ma mai usato nel corpo della funzione.
  requestBody: unknown;
}) {
  // Il jsonConfig salvato e' testo libero: si legge solo dopo aver verificato la forma.
  const defaultParsed: DefaultConfig = parseDefaultConfig(defaultData);

  const remapFormData = (data: PluginDriverForm) => {
    const fields = data.fields;
    if (!Array.isArray(fields)) return data;

    return fields.map((item) => {
      const defaults = item.name ? defaultParsed[item.name] : undefined;
      if (!defaults) return item;
      const values = item.values ?? [];

      if (item.type === "text" || item.type === "number") {
        const first = Array.isArray(defaults) ? defaults[0] : defaults;
        return { ...item, values: [{ value: first?.value, isDefault: true }] };
      }

      if (item.type === "list") {
        return { ...item, values: Array.isArray(defaults) ? defaults : [defaults] };
      }

      if (item.type === "select") {
        const selected = Array.isArray(defaults) ? defaults[0]?.value : defaults.value;
        return {
          ...item,
          values: values.map((v) => (v.value === selected ? { ...v, isDefault: true } : { ...v })),
        };
      }

      if (item.type === "multiselect") {
        const selectedValues = Array.isArray(defaults) ? defaults : [defaults];
        return {
          ...item,
          values: values.map((v) =>
            selectedValues.some((selected) => selected?.value === v.value) ? { ...v, isDefault: true } : { ...v },
          ),
        };
      }

      return item;
    });
  };

  if (id) {
    const response = await restClient.pluginDriverResource.getApiDatasourcePluginDriversForm(Number(id));
    return remapFormData(response);
  }
}

export function useRecoveryForm(restClient: RestClient, formValues: RecoveryFormValues, requestBody: unknown) {
  const [formCustom, setFormCustom] = React.useState<CustomForm[] | undefined>([]);
  const [loadingFormCustom, setLoadingFormCustom] = React.useState(true);
  const [recoveryFormStandart, setRecoveryFormStandart] = React.useState<Template | undefined>(undefined);
  React.useEffect(() => {
    setRecoveryFormStandart(undefined);
    setFormCustom([]);
    setLoadingFormCustom(true);

    if (!formValues?.pluginDriverSelect?.id) {
      setLoadingFormCustom(false);
      return;
    }

    let cancelled = false;
    const formCustomClient = recoveryForm({
      restClient,
      id: formValues?.pluginDriverSelect?.id,
      defaultData: formValues?.jsonConfig || "{}",
      requestBody,
    });

    formCustomClient
      .then((response) => {
        if (cancelled) return;
        // Il payload REST dichiara ogni campo opzionale; il form dinamico lavora
        // sul modello `Field`. Unico punto di adattamento fra i due.
        const data = (Array.isArray(response) ? response : []) as Field[];
        const remappedData = data
          ?.map((formItem) => {
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
        if (cancelled) return;
        console.error("Errore durante il recupero del form:", error);
        setRecoveryFormStandart(undefined);
        setFormCustom(undefined);
        setLoadingFormCustom(false);
      });

    return () => {
      cancelled = true;
    };
  }, [formValues.pluginDriverSelect?.id]);

  return { formCustom, loadingFormCustom, recoveryFormStandart, setFormCustom };
}

export const constructTabs = ({
  datasourceId,
  mode,
  isDisabledNextStep,
  isRecap,
  t,
}: {
  datasourceId: string;
  mode: string;
  isDisabledNextStep: boolean;
  isRecap: boolean;
  t: TFunction;
}) => [
  {
    label: t("pages.datasources.recovery.connectors"),
    value: "connectors",
    step: 1,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/connectors`,
    disabled: isDisabledNextStep,
  },
  {
    label: t("pages.datasources.recovery.datasource"),
    value: "datasource",
    step: 2,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/datasource`,
    disabled: isDisabledNextStep,
  },
  {
    label: t("pages.datasources.recovery.pipeline"),
    value: "pipeline",
    step: 3,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/pipeline`,
    disabled: isDisabledNextStep,
  },
  {
    label: t("pages.datasources.recovery.data-index"),
    value: "dataIndex",
    step: 4,
    path: `/data-source/${datasourceId}/mode/${mode}/landingTab/dataIndex`,
    disabled: isDisabledNextStep,
  },
  ...(isRecap
    ? [
        {
          label: t("common.recap"),
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
          label: t("pages.datasources.recovery.monitoring"),
          value: "monitoring",
          path: `/data-source/${datasourceId}/mode/${mode}/landingTab/monitoring`,
        },
      ]
    : []),
  ...(datasourceId !== "new"
    ? [
        {
          label: t("pages.datasources.recovery.reindex"),
          value: "reindex",
          path: `/data-source/${datasourceId}/mode/${mode}/landingTab/reindex`,
        },
      ]
    : []),
];

