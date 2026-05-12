import { ModalConfirm } from "@components/Form";
import { AutocompleteDropdown } from "@components/Form/Select/AutocompleteDropdown";
import { Box, Button, FormControlLabel, Radio, RadioGroup, Typography } from "@mui/material";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useEnrichPipelineOptions } from "../../../../../utils/RelationOneToOne";
import { tabsType } from "../../../datasourceType";
import { defaultModal, EnrichItemsTable, PipelineRadioType } from "../DataSource/ConfigureDatasource";
import { ConnectionData } from "../../../types";
import { BoxArea } from "../../BoxArea";

interface ConfigurePipelineProps {
  dataDatasource: ConnectionData;
  setDataDatasource: React.Dispatch<React.SetStateAction<ConnectionData>>;
  disabled: boolean;
  datasourceId: string;
  tabs: tabsType;
  isRecap: boolean;
  setIsRecap: React.Dispatch<React.SetStateAction<boolean>>;
  setActiveTab: (value: React.SetStateAction<string>) => void;
}

const ConfigurePipeline: React.FC<ConfigurePipelineProps> = ({
  dataDatasource,
  setDataDatasource,
  disabled,
  datasourceId,
  tabs,
  isRecap,
  setIsRecap,
  setActiveTab,
}) => {
  const [showDialog, setShowDialog] = useState(defaultModal);
  const navigate = useNavigate();
  const [pipelineArea, setPipelineArea] = useState<PipelineRadioType>("no-pipeline");
  React.useEffect(() => {
    dataDatasource.enrichPipeline?.id && setPipelineArea("present-pipeline");
  }, [dataDatasource.enrichPipeline?.id]);

  return (
    <Box>
      {dataDatasource.pipeline?.id}
      <Typography variant="h2">Pipeline</Typography>
      <RadioGroup
        value={pipelineArea}
        onChange={(e) => {
          setShowDialog({
            isShow: true,
            message: "Are you sure you want to change pipelines? You would lose all other previously selected changes.",
            title: "Area scheduling",
            callbackClose: () => {
              setShowDialog(defaultModal);
            },
            callbackConfirm: () => {
              setPipelineArea(e.target.value as PipelineRadioType);
              setDataDatasource((data) => ({
                ...data,
                pipeline: undefined,
                enrichPipeline: undefined,
                linkedEnrichItems: undefined,
                enrichPipelineCustom: undefined,
              }));
              setShowDialog(defaultModal);
            },
          });
        }}
      >
        <FormControlLabel
          value="no-pipeline"
          control={<Radio disabled={disabled} color={disabled ? "default" : "primary"} />}
          label="No pipeline"
        />
        <FormControlLabel
          value="present-pipeline"
          control={<Radio disabled={disabled} color={disabled ? "default" : "primary"} />}
          label="Select pipeline from preset elements"
        />
        <Box sx={{ display: "flex", flexWrap: "wrap" }}>
          <BoxArea isActive={pipelineArea === "present-pipeline"}>
            <AutocompleteDropdown
              label="Select Pipeline"
              disabled={disabled || pipelineArea !== "present-pipeline"}
              value={
                dataDatasource.enrichPipeline?.id
                  ? {
                      id: dataDatasource.enrichPipeline.id,
                      name: dataDatasource.enrichPipeline.name || "",
                    }
                  : undefined
              }
              onChange={(val) =>
                setDataDatasource((dat) => ({
                  ...dat,
                  enrichPipeline: { id: val.id, name: val.name },
                }))
              }
              onClear={() =>
                setDataDatasource((dat) => ({
                  ...dat,
                  enrichPipeline: undefined,
                }))
              }
              useOptions={useEnrichPipelineOptions}
            />
          </BoxArea>
        </Box>
        <FormControlLabel
          value="custom-pipeline"
          control={<Radio disabled={disabled} color={disabled ? "default" : "primary"} />}
          label="Create custom pipeline"
        />
        {datasourceId && (
          <EnrichItemsTable
            connectionData={dataDatasource}
            setConnectionData={setDataDatasource}
            isActive={pipelineArea === "custom-pipeline" && !disabled}
            isView={disabled}
            isNew={datasourceId === "new"}
          />
        )}
      </RadioGroup>
      <Box
        sx={{
          marginTop: "10px",
          display: "flex",
          justifyContent: "space-between",
        }}
      >
        <Button
          variant="contained"
          color="secondary"
          aria-label="Back"
          onClick={() => {
            setActiveTab("datasource");
            const pipelineTab = tabs.find((tab) => tab.value === "datasource");
            if (pipelineTab) {
              navigate(pipelineTab.path);
            }
          }}
        >
          Back
        </Button>
        <Button
          variant="contained"
          aria-label="Recap"
          onClick={() => {
            setActiveTab("dataIndex");
          }}
        >
          Next Step
        </Button>
      </Box>
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
    </Box>
  );
};

export default ConfigurePipeline;
