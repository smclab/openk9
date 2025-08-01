import { TitleEntity } from "@components/Form";
import { Box, Button, styled, Tab, Tabs, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { ConfigureDatasource, MonitoringTab } from "./Function";
import Reindex from "./Function/Reindex";
import RecapDatasource from "./RecapDatasource";
import ReindexArea from "./components/Sections/ReindexArea";
import { ConfigureConnectors } from "./components/Sections/Connectors/ConfigureConnectors";
import DataIndex from "./components/Sections/DataIndex/ConfigureDataIndex";
import ConfigurePipeline from "./components/Sections/Pipeline/ConfigurePipeline";
import { HeaderType, tabsPropsConstructor } from "./datasourceType";
import { ConnectionData } from "./types";

export const TabsSection = ({
  tabs,
  activeTab,
  handleTabChange,
  setActiveTab,
  areaEnabled,
  formValues,
  getHealthInfo,
  getHealthInfoWithoutId,
  isView,
  setAreaEnabled,
  setFormValues,
  setShowDialog,
  requestBody,
  formCustom,
  setFormCustom,
  datasourceId,
  dynamicTemplate,
  changeValueTemplate,
  dynamicFormJson,
  loadingFormCustom,
  isRecap,
  setIsRecap,
  handleDatasource,
  isCreated,
}: tabsPropsConstructor) => {
  const isRecapTab = "recap";
  isRecap && handleTabChange(null, isRecapTab);
  const navigate = useNavigate();
  const area = [
    formValues.pluginDriverSelect?.id && {
      title: "Connector",
      description: "area del plugin driver",
      fields: [
        {
          label: "Name Connector",
          value: formValues.pluginDriverSelect?.nameConnectors,
          type: "string",
        },
        { label: "Host", value: formValues.pluginDriverSelect?.host, type: "string" },
        { label: "Port", value: formValues.pluginDriverSelect?.port, type: "string" },
        { label: "Method", value: formValues.pluginDriverSelect?.method, type: "string" },
        { label: "Path", value: formValues.pluginDriverSelect?.path, type: "string" },
        { label: "Provisioning", value: formValues.pluginDriverSelect?.provisioning, type: "string" },
        { label: "Secure", value: formValues.pluginDriverSelect?.secure, type: "boolean" },
      ],
    },
    formValues.datasourceId
      ? {
          title: "Datasource",
          description: "area del datasource",
          fields: [
            { label: "Name", value: formValues.name, type: "string" },
            { label: "Description", value: formValues.description, type: "string" },
            { label: "Reindexing", value: formValues.reindex, type: "string" },
            { label: "Scheduling", value: formValues.scheduling, type: "string" },
            {
              label: "json",
              type: "json",
              value: formValues.jsonConfig ?? {},
            },
          ],
        }
      : null,
    formValues.enrichPipeline?.name
      ? {
          title: "Pipeline",
          description: "area delle pipeline",
          fields: [{ label: "Name", type: "string", value: formValues.enrichPipeline?.name }],
        }
      : null,
    (formValues?.enrichPipelineCustom?.linkedEnrichItems?.length || 0) > 0 && {
      title: "Pipeline Custom",
      description: "area delle pipeline custom",
      fields: [
        {
          type: "array",
          value: formValues.enrichPipelineCustom?.linkedEnrichItems?.map((form) => ({ name: form.name })) ?? [],
        },
      ],
    },
  ].filter((section): section is { title: string; description: string; fields: any[] } => !!section);

  return (
    <>
      <Tabs value={activeTab} onChange={handleTabChange}>
        {tabs.map((tab, index) => {
          const currentTabIndex = tabs.findIndex((t) => t.value === activeTab);
          const isDisabled = isView
            ? false
            : tab.disabled || (index !== currentTabIndex + 1 && index !== currentTabIndex - 1);
          const isClickable = !isDisabled && activeTab !== tab.value;

          return (
            <Tab
              key={tab.value}
              label={
                <Box sx={{ display: "flex", alignItems: "center" }}>
                  {tab.step && (
                    <StepCircle active={activeTab === tab.value} clickable={isClickable}>
                      {tab.step}
                    </StepCircle>
                  )}
                  {tab.label}
                </Box>
              }
              value={tab.value}
              disabled={isDisabled}
              sx={{
                "&.Mui-disabled": {
                  color: activeTab === tab.value ? "primary.main" : undefined,
                },
              }}
            />
          );
        })}
      </Tabs>
      {activeTab === "connectors" && (
        <ConfigureConnectors
          areaEnabled={areaEnabled}
          formValues={formValues}
          getHealthInfo={getHealthInfo}
          getHealthInfoWithoutId={getHealthInfoWithoutId}
          disabled={isView}
          tabs={tabs}
          setActiveTab={setActiveTab}
          setAreaEnabled={setAreaEnabled}
          setFormValues={setFormValues}
          setShowDialog={setShowDialog}
        />
      )}

      {activeTab === "datasource" && (
        <ConfigureDatasource
          dataDatasource={formValues}
          setDataDatasource={setFormValues}
          setActiveTab={setActiveTab}
          disabled={isView}
          isRecap={isRecap}
          tabs={tabs}
          setIsRecap={setIsRecap}
          requestBody={requestBody}
          formCustom={formCustom}
          setFormCustom={setFormCustom}
          datasourceId={datasourceId}
          dynamicTemplate={dynamicTemplate}
          changeValueTemplate={changeValueTemplate}
          dynamicFormJson={dynamicFormJson}
          loadingFormCustom={loadingFormCustom}
        />
      )}
      {activeTab === "pipeline" && (
        <ConfigurePipeline
          dataDatasource={formValues}
          setDataDatasource={setFormValues}
          disabled={isView}
          datasourceId={datasourceId}
          isRecap={isRecap}
          tabs={tabs}
          setIsRecap={setIsRecap}
          setActiveTab={setActiveTab}
        />
      )}
      {activeTab === "monitoring" && datasourceId !== "new" && <MonitoringTab id={datasourceId} />}
      {activeTab === "reindex" && datasourceId !== "new" && (
        <Reindex id={datasourceId} data={formValues.lastIngestionDate} />
      )}
      {activeTab === "dataIndex" && isCreated ? (
        <DataIndex
          id={formValues.pluginDriverSelect?.id || ""}
          resetDataIndex={() => {
            setFormValues((prev: ConnectionData) => ({
              ...prev,
              dataIndex: null,
              dataIndices: null,
            }));
          }}
          changeExtraParamsDataIndex={(key, value) => {
            setFormValues((prev: ConnectionData) => ({
              ...prev,
              vectorIndex: {
                ...prev.vectorIndex,
                [key]: value,
              },
            }));
          }}
          setActiveTab={setActiveTab}
          extraParamsDataIndex={{
            knnIndex: formValues.vectorIndex?.knnIndex || false,
            chunkWindowSize: formValues.vectorIndex?.chunkWindowSize || 0,
            embeddingDocTypeFieldId: {
              id: formValues.vectorIndex?.embeddingDocTypeFieldId?.id || "",
              name: formValues.vectorIndex?.embeddingDocTypeFieldId?.name || "",
            },
            embeddingJsonConfig: formValues?.vectorIndex?.embeddingJsonConfig || "",
            chunkType: formValues?.vectorIndex?.chunkType,
          }}
          microForm={{
            name: formValues.dataIndex?.name || "",
            description: formValues.dataIndex?.description || "",
            knnIndex: formValues.vectorIndex?.knnIndex || false,
            docTypeId: formValues.vectorIndex?.docTypeIds || [],
          }}
          setDataIndexForm={({ key, value }: { key: "name" | "description" | "knnIndex"; value: string | boolean }) => {
            setFormValues((prev: ConnectionData) => ({
              ...prev,
              dataIndex: {
                ...prev.dataIndex,
                [key]: value,
              },
            }));
          }}
          isDisabled={false}
          isCreated={isCreated}
          setIsRecap={setIsRecap}
          dataIndixes={formValues.dataIndices}
          defaultDataIndex={(defaultData: { id: string; name: string }[]) => {
            setFormValues((prev: ConnectionData) => ({
              ...prev,
              dataIndices: defaultData,
            }));
          }}
          changeDataIndex={({ dataIndex }: { dataIndex: { id: string } }) => {
            const isPresentDataIndex = formValues.dataIndices?.find((item) => item.id === dataIndex.id);

            const updatedTypes = isPresentDataIndex
              ? formValues.dataIndices?.filter((item) => item.id !== dataIndex.id)
              : [...(formValues.dataIndices || []), dataIndex];
            setFormValues((prev: ConnectionData) => ({
              ...prev,
              dataIndices: updatedTypes,
            }));
          }}
        />
      ) : (
        activeTab === "dataIndex" && (
          <ReindexArea
            connectionData={formValues}
            isNew={false}
            isView={isView}
            setConnectionData={setFormValues}
            setActiveTab={setActiveTab}
            setIsRecap={setIsRecap}
          />
        )
      )}
      {isRecap && activeTab === "recap" && (
        <>
          <Box sx={{ marginBlock: 2 }}>
            {/* name: formValues.name || "",
      schedulable: formValues.isCronSectionscheduling || false,
      reindexable: formValues.isCronSectionreindex || false,
      reindexing: formValues.reindexing || "0 0 1 * * ?",
      scheduling: formValues.scheduling || "0 30 * ? * * *",
      jsonConfig: dynamicFormJson,
      description: formValues.description,
      pluginDriverId: Number(formValues.pluginDriverSelect?.id),
      pipelineId: formValues.enrichPipeline?.id || null,
      purging: formValues.purging || "0 0 1 * * ?",
      purgeable: formValues.isCronSectionpurge || false,
      purgeMaxAge: formValues.purgeMaxAge || "2d",
      ...(formValues.enrichPipelineCustom?.name && { */}
            {/* pipeline: {
          name: formValues.enrichPipelineCustom.name,
          items: formValues?.enrichPipelineCustom.linkedEnrichItems?.map((forms) => ({
            enrichItemId: forms.id || "",
            weight: forms.weight || 0,
          })) as [],
        },
      }), */}
            <RecapDatasource area={area} />
          </Box>
          <Box display={"flex"} justifyContent={"space-between"}>
            <Button
              variant="contained"
              color="secondary"
              onClick={() => {
                setIsRecap(false);
                setActiveTab("dataIndex");
                const pipelineTab = tabs.find((tab) => tab.value === "dataIndex");
                if (pipelineTab) {
                  navigate(pipelineTab.path);
                }
              }}
            >
              Back
            </Button>
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                handleDatasource();
              }}
            >
              Confirm
            </Button>
          </Box>
        </>
      )}
    </>
  );
};

export const StepCircle = styled(Box)<{ active?: boolean; clickable?: boolean }>(({ theme, active, clickable }) => ({
  width: 20,
  height: 20,
  borderRadius: "50%",
  backgroundColor: active
    ? theme.palette.primary.main
    : clickable
    ? theme.palette.text.primary
    : theme.palette.text.disabled,
  color: active ? theme.palette.primary.contrastText : theme.palette.background.paper,
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  marginRight: 8,
  fontSize: "0.75rem",
}));

export const Header = ({
  landingTabId,
  mode,
  navigate,
  datasourceId,
  generateDocumentTypes,
  setActiveTab,
}: HeaderType) => {
  const initialStateEditMessage = {
    title: "Edit Datasource",
    body: "Are you sure you want to edit this datasource?",
    labelConfirm: "Edit",
  };
  type modalMessageType = { title: string; body: string; labelConfirm: string };
  const [modalMessage, setModalMessage] = useState<modalMessageType>(initialStateEditMessage);
  const { openConfirmModal, ConfirmModal } = useConfirmModal(modalMessage);

  const handleEditClick = async (typology: "editModal" | "generateModal") => {
    setModalMessage(initialStateEditMessage);
    const confirmed = await openConfirmModal();
    setActiveTab("datasource");
    confirmed && typology === `editModal` && navigate(`/data-source/${datasourceId}/mode/edit/landingTab/datasource`);
  };

  return (
    <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
      <TitleEntity nameEntity="Datasource" description="Descrizione del datasource" id="new" />
      <Box sx={{ display: "flex", gap: "10px" }}>
        {mode === "view" && (
          <Button variant="contained" onClick={() => handleEditClick("editModal")}>
            Edit
          </Button>
        )}
      </Box>
      <ConfirmModal />
    </Box>
  );
};

export const FormSection = ({
  formValues,
  setFormValues,
  isView,
  isRecap,
}: {
  formValues: ConnectionData;
  setFormValues: React.Dispatch<React.SetStateAction<ConnectionData>>;
  isView: boolean;
  isRecap: boolean;
}) => (
  <Box display={"flex"} flexDirection={"column"} gap={"10px"} maxWidth={"250px"}>
    <Typography>Name</Typography>
    <TextField
      disabled={isView || isRecap}
      value={formValues?.name}
      onChange={(event) =>
        setFormValues((pre) => ({
          ...pre,
          name: event?.target?.value,
        }))
      }
    />
  </Box>
);
