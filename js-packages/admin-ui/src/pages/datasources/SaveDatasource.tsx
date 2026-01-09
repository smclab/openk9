import { ContainerFluid, ModalConfirm, ModalConfirmRadio, useForm, useToast } from "@components/Form";
import { useRestClient } from "@components/queryClient";
import Recap, { mappingCardRecap, RecapSingleSection } from "@pages/Recap/SaveRecap";
import React, { useState } from "react";
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
  const isDisabledNextStep =
    (pluginDriverId === null || pluginDriverId === undefined || !formValues?.name) && areaEnabled !== "selectConnectos";
  const tabs = constructTabs({ datasourceId, isDisabledNextStep, mode, isRecap });
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
    } else if (newTabIndex === currentTabIndex + 1 || newTabIndex === currentTabIndex - 1) {
      if (selectedTab && !selectedTab.disabled) {
        navigate(selectedTab.path);
        setActiveTab(newValue);
      }
    }
  };

  const getHealthInfo = async (id: number) => {
    try {
      const response = await restClient.pluginDriverResource.getApiDatasourcePluginDriversHealth(id);

      toast({
        displayType: "success",
        title: "Success",
        content: `Status: ${response.status}`,
      });
    } catch (error) {
      toast({
        displayType: "error",
        title: "Error",
        content: `${error}`,
      });
    }
  };

  const getHealthInfoWithoutId = async () => {
    try {
      const response = await restClient.pluginDriverResource.postApiDatasourcePluginDriversHealth(requestBody);
      alert(response.status);
    } catch (error) {
      alert("error");
    }
  };

  const handleDatasource = () => {
    // Validation for DataIndex section
    if (activeTab === "recap") {
      // Check if name is present
      if (!formValues.dataIndex?.name || formValues.dataIndex.name.trim() === "") {
        toast({
          title: "Validation Failed",
          content: "The 'Name' field is required for DataIndex",
          displayType: "error",
        });
        return; // Stop function execution
      }

      // Check if at least one datasource is selected
      // const hasSelectedDatasource =
      //   formValues.associatedDatasources &&
      //   Object.values(formValues.associatedDatasources).some((value) => value === true);

      // if (!hasSelectedDatasource) {
      //   toast({
      //     title: "Validation Failed",
      //     content: "You must select at least one datasource in 'Associate Datasource'",
      //     displayType: "error",
      //   });
      //   return;
      // }

      // // Check if a doc type is selected
      // if (!formValues.selectedDocType) {
      //   toast({
      //     title: "Validation Failed",
      //     content: "You must select a document type",
      //     displayType: "error",
      //   });
      //   return;
      // }
    }

    const commonVariables = {
      name: formValues.name || "",
      schedulable: formValues.isCronSectionscheduling || false,
      reindexable: formValues.isCronSectionreindex || false,
      reindexing: formValues.reindexing || "0 0 1 * * ?",
      scheduling: formValues.scheduling || "0 */30 * ? * * *",
      jsonConfig: dynamicFormJson || formValues.jsonConfig,
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

    if (formValues.datasourceId !== "new") {
      updateDatasource({
        variables: {
          ...commonVariables,
          datasourceId: formValues.datasourceId,
          dataIndexId: formValues.dataIndex?.id,
        },
        onError: (error) => {
          setActiveTab("recap");
          toast({
            title: "Error Updating Datasource",
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
        onError: (error) => {
          setActiveTab("recap");
          toast({
            title: "Error Creating Datasource",
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

  const recapSectionsFromMapping = mappingCardRecap({
    form: form as any,
    sections: [
      {
        label: "Datasource",
        cell: [
          { key: "name", label: "Name" },
          { key: "isCronSectionreindex", label: "Reindexing" },
          { key: "isCronSectionscheduling", label: "Scheduling" },
          { key: "isCronSectionpurge", label: "Purging" },
          { key: "reindexing", label: "Reindexing" },
          { key: "scheduling", label: "Scheduling" },
          { key: "purging", label: "Purging" },
          { key: "dynamicFormJson", label: "Json" },
        ],
      },
      {
        label: "Pipeline",
        cell: [{ key: "enrichPipeline.name", label: "Name" }],
      },
      {
        label: "Pipeline Custom",
        cell: [
          {
            key: "enrichPipelineCustom.linkedEnrichItems",
            label: "Enrich Item Custom",
          },
        ],
      },
      {
        label: "Data Index",
        cell: [
          { key: "dataIndex.name", label: "Name" },
          { key: "dataIndex.description", label: "Description" },
          { key: "vectorIndex.chunkType", label: "chunk Type" },
          { key: "vectorIndex.chunkWindowSize", label: "chunk Window Size" },
          {
            key: "vectorIndex.embeddingJsonConfig",
            label: "Embedding json Config",
          },
          {
            key: "vectorIndex.knnIndex",
            label: "Embedding knn index",
          },
          {
            key: "vectorIndex.embeddingDocTypeFieldId.name",
            label: "Doc Type",
          },
          {
            key: "dataIndices",
            label: "dataIndices",
          },
        ],
      },
    ],
  });

  const connectorSection: RecapSingleSection = {
    id: "Connector",
    section: { sectionId: "Connector", sectionLabel: "Connector" },
    fields: [
      {
        key: "pluginDriverSelect.nameConnectors",
        label: "Name",
        value: formValues.pluginDriverSelect?.nameConnectors ?? null,
        type: typeof formValues.pluginDriverSelect?.nameConnectors === "number" ? "number" : "string",
        isValid: true,
      },
      {
        key: "pluginDriverSelect.provisioning",
        label: "Provisioning",
        value: formValues.pluginDriverSelect?.provisioning ?? null,
        type: typeof formValues.pluginDriverSelect?.provisioning === "boolean" ? "boolean" : "string",
        isValid: true,
      },
    ],
  };

  const recapSections = [connectorSection, ...recapSectionsFromMapping];

  return (
    <ContainerFluid style={{ width: "100%" }}>
      {modalHeaderButton && (
        <ModalConfirmRadio
          callbackClose={() => setModalHeaderButton(undefined)}
          callbackConfirm={() => {
            modalHeaderButton.action();
            setModalHeaderButton(undefined);
          }}
          title="Confirm Decision"
          message={modalHeaderButton.label || ""}
        />
      )}
      {showDialog.isShow && (
        <ModalConfirm
          title="Confirm Change"
          body={showDialog.message}
          labelConfirm="Change"
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
        }}
        recapData={recapSections}
        setExtraFab={setExtraFab}
        forceFullScreen={isRecap}
      />
    </ContainerFluid>
  );
}
