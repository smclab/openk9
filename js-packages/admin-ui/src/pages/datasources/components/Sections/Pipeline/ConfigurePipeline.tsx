import { ModalConfirm } from "@components/Form";
import {
  Box,
  Button,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Radio,
  RadioGroup,
  Select,
  Typography,
} from "@mui/material";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useEnrichPipelineOptionsQuery } from "../../../../../graphql-generated";
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
  const enrichPipelines = useEnrichPipelineOptionsQuery({
    fetchPolicy: "network-only",
  });
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
            <FormControl fullWidth>
              <InputLabel id="my-select-pipeline">Select Pipeline</InputLabel>
              <Select
                labelId="my-select-pipeline"
                id="selectMyPipeline"
                disabled={disabled || pipelineArea !== "present-pipeline"}
                value={dataDatasource.enrichPipeline?.id}
                onChange={(event) => {
                  setDataDatasource((dat) => ({
                    ...dat,
                    enrichPipeline: {
                      id: "" + event?.target?.value,
                      name:
                        enrichPipelines?.data?.options?.edges?.find((item) => item?.node?.id === event.target.value)
                          ?.node?.name || undefined,
                    },
                  }));
                }}
                label="Select Label"
              >
                <MenuItem value="">
                  <em>Select Value</em>
                </MenuItem>
                {enrichPipelines?.data?.options?.edges?.map((item, index) => (
                  <MenuItem key={index} value={"" + item?.node?.id || ""}>
                    {item?.node?.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
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
