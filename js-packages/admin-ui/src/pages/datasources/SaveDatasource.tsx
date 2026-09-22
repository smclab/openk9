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
import { ContainerFluid, ModalConfirm, ModalConfirmRadio, useForm, useToast } from "@components/Form";
import { useRestClient } from "@components/queryClient";
import { extractProblemDetails, mapHealthStatus } from "../../utils/health";
import Recap, { mappingCardRecap, RecapSingleSection } from "@pages/Recap/SaveRecap";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { Provisioning, useDataSourceQuery } from "../../graphql-generated";
import { Section } from "./components/Sections/Connectors/ConfigureConnectors";
import DynamicForm from "./components/Sections/DataSource/DynamicForm";
import { defaultModal, useGenerateDocumentTypesMutation } from "./Function";
import { useDatasourceForm } from "./hooks/useDatasourceForm";
import { useDatasourceMutations } from "./hooks/useDatasourceMutations";
import { constructTabs, useRecoveryForm } from "./RecoveryData";
import { FormSection, Header, TabsSection } from "./StructureDatasource";

export function SaveDatasource({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const { datasourceId = "new", mode = "view", landingTabId = "monitoring" } = useParams();
  const [areaEnabled, setAreaEnabled] = useState<Section>("card");
  const [isRecap, setIsRecap] = React.useState(false);
  const [activeTab, setActiveTab] = useState(mode === "create" ? "connectors" : landingTabId);
  const messagesEndRef = React.useRef<HTMLDivElement | null>(null);
  const [showDialog, setShowDialog] = useState(defaultModal);
  const [modalHeaderButton, setModalHeaderButton] = React.useState<
    { label: string | null | undefined; action(): void } | null | undefined
  >(undefined);
  const navigate = useNavigate();
  const { updateDatasource, createDatasource } = useDatasourceMutations(datasourceId, navigate);
  const datasourceQuery = useDataSourceQuery({
    variables: { id: datasourceId, searchText: "" },
    skip: datasourceId === "new",
  });
  const { formValues, setFormValues } = useDatasourceForm(datasourceId, datasourceQuery);
  const generateDocumentTypes = useGenerateDocumentTypesMutation();
  const toast = useToast();
  const restClient = useRestClient();
  const pluginDriverId = formValues.pluginDriverSelect?.id;

  const [requestBody, setRequestBody] = React.useState<any>({
    name: formValues.pluginDriverSelect?.nameConnectors,
    description: formValues.pluginDriverSelect?.description,
    type: formValues.pluginDriverSelect?.pluginDriverType,
    provisioning: formValues.pluginDriverSelect?.provisioning,
  });
  const { formCustom, loadingFormCustom, recoveryFormStandart, setFormCustom } = useRecoveryForm(
    restClient,
    formValues,
    requestBody,
  );
  const { dynamicFormJson, dynamicTemplate, changeValueTemplate } = DynamicForm({
    template: recoveryFormStandart,
    jsonConfig: formValues.jsonConfig,
  });

  const baseDisabled =
    (pluginDriverId === null || pluginDriverId === undefined || !formValues?.name) && areaEnabled !== "selectConnectos";

  const hasMissingRequiredDynamic = !!dynamicTemplate?.fields?.some((f: any) => f?.required && isFieldEmpty(f));

  const isDisabledNextStep = baseDisabled || hasMissingRequiredDynamic;
  const tabs = constructTabs({ datasourceId, isDisabledNextStep, mode, isRecap, t });

  const isView = mode === "view";

  React.useEffect(() => {
    setRequestBody({
      name: formValues.pluginDriverSelect?.nameConnectors,
      description: formValues.pluginDriverSelect?.description,
      type: formValues.pluginDriverSelect?.pluginDriverType,
      provisioning: formValues.pluginDriverSelect?.provisioning,
      jsonConfig: JSON.stringify({
        host: formValues.pluginDriverSelect?.host,
        method: formValues.pluginDriverSelect?.method,
        path: formValues.pluginDriverSelect?.path,
        port: formValues.pluginDriverSelect?.port,
        secure: formValues.pluginDriverSelect?.secure,
      }),
    });
  }, [formValues]);

  React.useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [activeTab, isRecap]);

  const handleTabChange = (event: React.SyntheticEvent | null, newValue: string) => {
    const currentTabIndex = tabs.findIndex((tab) => tab.value === activeTab);
    const newTabIndex = tabs.findIndex((tab) => tab.value === newValue);
    const selectedTab = tabs[newTabIndex];

    if (isView) {
      navigate(selectedTab.path);
      setActiveTab(newValue);
    } else if (newTabIndex === currentTabIndex - 1) {
      navigate(selectedTab.path);
      setActiveTab(newValue);
    } else if (newTabIndex === currentTabIndex + 1) {
      if (selectedTab && !selectedTab.disabled) {
        navigate(selectedTab.path);
        setActiveTab(newValue);
      }
    }
  };

  const getHealthInfo = async (id: number) => {
    try {
      const response = await restClient.pluginDriverResource.getApiDatasourcePluginDriversHealth(id);
      const ui = mapHealthStatus(response.status);
      if (ui === "success") {
        toast({
          displayType: "success",
          title: t("pages.datasources.success"),
          content: t("pages.datasources.connection-successful"),
        });
      } else if (ui === "down") {
        toast({
          displayType: "error",
          title: t("pages.datasources.service-unavailable"),
          content: t("pages.datasources.the-service-is-reachable-but-reports-down"),
        });
      } else {
        toast({
          displayType: "warning",
          title: t("pages.datasources.service-status-unknown"),
          content: t("pages.datasources.unexpected-status", { status: response.status }),
        });
      }
    } catch (error) {
      const { title, detail } = extractProblemDetails(error, t);
      toast({ displayType: "error", title, content: detail ?? t("pages.datasources.unable-to-reach-the-service") });
    }
  };

  const getHealthInfoWithoutId = async () => {
    try {
      const response = await restClient.pluginDriverResource.postApiDatasourcePluginDriversHealth(requestBody);
      const ui = mapHealthStatus(response.status);
      if (ui === "success") {
        alert(t("pages.datasources.connection-successful"));
      } else if (ui === "down") {
        alert(t("pages.datasources.the-service-is-reachable-but-reports-down"));
      } else {
        alert(t("pages.datasources.unexpected-status", { status: response.status }));
      }
    } catch (error) {
      const { title, detail } = extractProblemDetails(error, t);
      alert(detail ? `${title}: ${detail}` : title);
    }
  };

  const handleDatasource = () => {
    const commonVariables = {
      name: formValues.name || "",
      schedulable: formValues.isCronSectionscheduling || false,
      reindexable: formValues.isCronSectionreindex || false,
      reindexing: formValues.reindexing || "0 0 1 * * ?",
      scheduling: formValues.scheduling || "0 */30 * ? * * *",
      jsonConfig: dynamicTemplate ? dynamicFormJson || formValues.jsonConfig : formValues.jsonConfig,
      description: formValues.description,
      pluginDriverId: Number(formValues.pluginDriverSelect?.id),
      pipelineId: formValues.enrichPipeline?.id || null,
      purging: formValues.purging || "0 0 1 * * ?",
      purgeable: formValues.isCronSectionpurge || false,
      purgeMaxAge: formValues.purgeMaxAge || "2d",
      ...(formValues.enrichPipelineCustom?.name && {
        pipeline: {
          name: formValues.enrichPipelineCustom.name,
          items: formValues?.enrichPipelineCustom.linkedEnrichItems?.map((forms) => ({
            enrichItemId: forms.id || "",
            weight: forms.weight || 0,
          })) as [],
        },
      }),
    };
    const isNewDatasource = formValues.datasourceId === "new";
    if (!isNewDatasource) {
      updateDatasource({
        variables: {
          ...commonVariables,
          datasourceId: formValues.datasourceId,
          dataIndexId: formValues.dataIndex?.id,
        },
        onError: (error) => {
          setActiveTab("recap");
          toast({
            title: t("pages.datasources.error-updating-datasource"),
            content: error.message || "An error occurred while updating the datasource",
            displayType: "error",
          });
        },
      });
    } else {
      createDatasource({
        variables: {
          ...commonVariables,
          dataIndex: {
            ...(formValues.dataIndex?.description && { description: formValues.dataIndex.description }),
            knnIndex: formValues?.vectorIndex?.knnIndex || false,
            ...(formValues.vectorIndex?.docTypeIds && { docTypeIds: formValues.vectorIndex.docTypeIds }),
            name: formValues.dataIndex?.name || "",
            ...(formValues?.vectorIndex?.chunkType && { chunkType: formValues?.vectorIndex?.chunkType }),
            ...(formValues?.vectorIndex?.chunkWindowSize && {
              chunkWindowSize: formValues?.vectorIndex?.chunkWindowSize,
            }),
            ...(formValues.vectorIndex?.embeddingDocTypeFieldId?.id && {
              embeddingDocTypeFieldId: formValues.vectorIndex.embeddingDocTypeFieldId.id,
            }),
            ...(formValues.vectorIndex?.embeddingJsonConfig && {
              embeddingJsonConfig: formValues.vectorIndex.embeddingJsonConfig,
            }),
          },
        },
        refetchQueries: ["DataSources"],
        onCompleted: () => {
          toast({
            title: isNewDatasource ? t("pages.datasources.created-title") : t("pages.datasources.updated-title"),
            content: isNewDatasource ? t("pages.datasources.created-content") : t("pages.datasources.updated-content"),
            displayType: "success",
          });
          navigate(`/data-sources/`, { replace: true });
        },
        onError: (error) => {
          setActiveTab("recap");
          toast({
            title: t("pages.datasources.error-creating-datasource"),
            content: error.message || "An error occurred while creating the datasource",
            displayType: "error",
          });
        },
      });
    }
    setIsRecap(false);
  };

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        // Datasource base
        name: formValues.name || "",
        description: formValues.description || "",

        // Connector/PluginDriver
        ...(formValues.pluginDriverSelect?.id && {
          pluginDriverSelect: {
            id: formValues.pluginDriverSelect?.id || null,
            nameConnectors: formValues.pluginDriverSelect?.nameConnectors || "",
            provisioning: formValues.pluginDriverSelect?.provisioning || Provisioning.System,
            json: formValues.pluginDriverSelect?.json || "",
          },
        }),

        // Cron sections
        datasourceId: formValues.datasourceId,
        isCronSectionreindex: formValues.isCronSectionreindex || false,
        isCronSectionscheduling: formValues.isCronSectionscheduling || false,
        isCronSectionpurge: formValues.isCronSectionpurge || false,
        reindexing: formValues.reindexing || "",
        scheduling: formValues.scheduling || "",
        purging: formValues.purging || "",

        // Pipeline
        enrichPipeline: {
          id: formValues.enrichPipeline?.id || "",
          name: formValues.enrichPipeline?.name || "",
        },
        enrichPipelineCustom: {
          id: formValues.enrichPipelineCustom?.id || "",
          name: formValues.enrichPipelineCustom?.name || "",
          linkedEnrichItems: formValues.enrichPipelineCustom?.linkedEnrichItems || [],
        },

        // Data Index
        dataIndex: {
          id: formValues.dataIndex?.id || "",
          name: formValues.dataIndex?.name || "",
          description: formValues.dataIndex?.description || "",
        },
        vectorIndex: {
          chunkType: formValues.vectorIndex?.chunkType || null,
          chunkWindowSize: formValues.vectorIndex?.chunkWindowSize || 0,
          embeddingJsonConfig: formValues.vectorIndex?.embeddingJsonConfig || "",
          knnIndex: formValues.vectorIndex?.knnIndex || false,
          embeddingDocTypeFieldId: formValues.vectorIndex?.embeddingDocTypeFieldId || { id: "", name: "" },
          docTypeIds: formValues.vectorIndex?.docTypeIds || [],
        },
        dataIndices: formValues.dataIndices || [],

        // Dynamic JSON (se lo passi da fuori)
        dynamicFormJson: dynamicFormJson || "",
      }),
      [formValues, dynamicFormJson],
    ),

    originalValues: {},
    isLoading: datasourceQuery.loading,

    onSubmit(updated: any) {
      setFormValues((prev) => ({
        ...prev,
        ...updated,
        // Ripopola nested objects preservando struttura
        pluginDriverSelect: {
          ...prev.pluginDriverSelect,
          ...updated.pluginDriverSelect,
        },
        enrichPipeline: {
          ...prev.enrichPipeline,
          ...updated.enrichPipeline,
        },
        dataIndex: {
          ...prev.dataIndex,
          ...updated.dataIndex,
        },
        vectorIndex: {
          ...prev.vectorIndex,
          ...updated.vectorIndex,
        },
      }));
    },
  });

  const connectorSection: RecapSingleSection = {
    id: "Recap",
    title: t("common.recap"),
    section: { sectionId: "Connector", sectionLabel: "Connector" },
    fields: [
      {
        key: "pluginDriverSelect.nameConnectors",
        label: t("common.name"),
        value: formValues.pluginDriverSelect?.nameConnectors ?? null,
        type: typeof formValues.pluginDriverSelect?.nameConnectors === "number" ? "number" : "string",
        isValid: true,
      },
      {
        key: "pluginDriverSelect.provisioning",
        label: t("pages.datasources.provisioning"),
        value: formValues.pluginDriverSelect?.provisioning ?? null,
        type: typeof formValues.pluginDriverSelect?.provisioning === "boolean" ? "boolean" : "string",
        isValid: true,
      },
    ],
  };

  let pipelineSection: RecapSingleSection | null = null;
  if (formValues.enrichPipeline?.name) {
    pipelineSection = {
      id: "Pipeline",
      section: { sectionId: "Pipeline", sectionLabel: "Pipeline" },
      fields: [
        {
          key: "enrichPipeline.name",
          label: t("common.name"),
          value: formValues.enrichPipeline.name,
          type: "string",
          isValid: true,
        },
      ],
    };
  }

  let pipelineCustomSection: RecapSingleSection | null = null;
  if (
    formValues.enrichPipelineCustom?.linkedEnrichItems &&
    Array.isArray(formValues.enrichPipelineCustom.linkedEnrichItems) &&
    formValues.enrichPipelineCustom.linkedEnrichItems.length > 0
  ) {
    pipelineCustomSection = {
      id: "Pipeline Custom",
      section: { sectionId: "Pipeline Custom", sectionLabel: "Pipeline Custom" },
      fields: [
        {
          key: "enrichPipelineCustom.linkedEnrichItems",
          label: t("pages.datasources.enrich-item-custom"),
          value: formValues.enrichPipelineCustom.linkedEnrichItems,
          type: "array",
          isValid: true,
        },
      ],
    };
  }

  const datasourceSection = mappingCardRecap({
    form: form as any,
    valueOverride: {
      dynamicFormJson: dynamicTemplate ? dynamicFormJson : formValues.jsonConfig,
    },
    sections: [
      {
        label: t("pages.datasources.datasource"),
        cell: [
          { key: "name", label: t("common.name") },
          { key: "isCronSectionreindex", label: t("pages.datasources.reindexing") },
          { key: "isCronSectionscheduling", label: t("pages.datasources.scheduling") },
          { key: "isCronSectionpurge", label: t("pages.datasources.purging") },
          { key: "reindexing", label: t("pages.datasources.reindexing") },
          { key: "scheduling", label: t("pages.datasources.scheduling") },
          { key: "purging", label: t("pages.datasources.purging") },
          { key: "dynamicFormJson", label: t("pages.datasources.json") },
        ],
      },
    ],
  });

  const dataIndexSection = mappingCardRecap({
    form: form as any,
    sections: [
      {
        label: t("pages.datasources.data-index"),
        cell: [
          { key: "dataIndex.name", label: t("common.name") },
          { key: "dataIndex.description", label: t("common.description") },
          { key: "vectorIndex.chunkType", label: t("fields.chunk-type") },
          { key: "vectorIndex.chunkWindowSize", label: t("fields.chunk-window-size") },
          { key: "vectorIndex.embeddingJsonConfig", label: t("pages.datasources.embedding-json-config") },
          { key: "vectorIndex.knnIndex", label: t("pages.datasources.embedding-knn-index") },
          { key: "vectorIndex.embeddingDocTypeFieldId.name", label: t("fields.doc-type") },
          { key: "dataIndices", label: t("pages.data-indices.title") },
        ],
      },
    ],
  });

  const recapSections: RecapSingleSection[] = [
    connectorSection,
    ...datasourceSection,
    ...(pipelineSection ? [pipelineSection] : []),
    ...(pipelineCustomSection ? [pipelineCustomSection] : []),
    ...dataIndexSection,
  ];

  return (
    <ContainerFluid style={{ width: "100%", paddingBottom: "4rem" }}>
      {modalHeaderButton && (
        <ModalConfirmRadio
          callbackClose={() => setModalHeaderButton(undefined)}
          callbackConfirm={() => {
            modalHeaderButton.action();
            setModalHeaderButton(undefined);
          }}
          title={t("pages.datasources.confirm-decision")}
          message={modalHeaderButton.label || ""}
        />
      )}
      {showDialog.isShow && (
        <ModalConfirm
          title={t("pages.datasources.confirm-change")}
          body={showDialog.message}
          labelConfirm={t("common.change")}
          actionConfirm={() => {
            showDialog.callbackConfirm();
          }}
          close={() => showDialog.callbackClose()}
        />
      )}
      <Header
        landingTabId={landingTabId}
        mode={mode}
        navigate={navigate}
        setActiveTab={setActiveTab}
        datasourceId={datasourceId}
        generateDocumentTypes={generateDocumentTypes}
      />
      <FormSection formValues={formValues} setFormValues={setFormValues} isView={isView} isRecap={isRecap} />
      <TabsSection
        tabs={tabs}
        activeTab={activeTab}
        handleTabChange={handleTabChange}
        setActiveTab={setActiveTab}
        areaEnabled={areaEnabled}
        formValues={formValues}
        getHealthInfo={getHealthInfo}
        getHealthInfoWithoutId={getHealthInfoWithoutId}
        isView={isView}
        setAreaEnabled={setAreaEnabled}
        setFormValues={setFormValues}
        setShowDialog={setShowDialog}
        requestBody={requestBody}
        formCustom={formCustom}
        setFormCustom={setFormCustom}
        datasourceId={datasourceId}
        dynamicTemplate={dynamicTemplate}
        changeValueTemplate={changeValueTemplate}
        dynamicFormJson={dynamicFormJson}
        loadingFormCustom={loadingFormCustom}
        isRecap={isRecap}
        setIsRecap={setIsRecap}
        handleDatasource={handleDatasource}
        isCreated={datasourceId === "new"}
        setExtraFab={setExtraFab}
      />
      <Recap
        actions={{
          onBack: () => {
            setActiveTab("dataIndex");
            setIsRecap(false);
          },
          submitLabel: datasourceId === "new" ? t("entity.create") : t("entity.update"),
          onSubmit: () => {
            handleDatasource();
          },
        }}
        recapData={recapSections}
        setExtraFab={setExtraFab}
        forceFullScreen={isRecap}
      />
    </ContainerFluid>
  );
}

function isFieldEmpty(field: any): boolean {
  const type = field?.type;
  const required = !!field?.required;

  if (!required) return false;

  const values = Array.isArray(field?.values) ? field.values : [];

  const defaultValue = (() => {
    if (type === "stringMap") {
      const obj = values.length > 0 ? Object.assign({}, ...values) : {};
      return obj;
    }

    if (type === "list" || type === "multiselect") {
      return values.filter((v: any) => v?.isDefault).map((v: any) => v?.value);
    }

    if (type === "checkbox" || type === "boolean") {
      const v = values.find((x: any) => x?.isDefault);
      return !!v?.value;
    }

    const v = values.find((x: any) => x?.isDefault);
    return v?.value ?? "";
  })();

  if (type === "stringMap") {
    const obj = defaultValue && typeof defaultValue === "object" ? defaultValue : {};
    return Object.keys(obj).length === 0 || Object.values(obj).every((v) => String(v ?? "").trim() === "");
  }

  if (type === "list" || type === "multiselect") {
    return !Array.isArray(defaultValue) || defaultValue.length === 0;
  }

  if (type === "checkbox" || type === "boolean") {
    return defaultValue !== true;
  }

  if (type === "number") {
    return defaultValue === null || defaultValue === undefined || defaultValue === "";
  }

  return String(defaultValue ?? "").trim() === "";
}
