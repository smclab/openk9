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
import { useTranslation } from "react-i18next";

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
  const { t } = useTranslation();
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
            message: t("pages.datasources.pipeline-section.are-you-sure-you-want-to-change"),
            title: t("pages.datasources.pipeline-section.area-scheduling"),
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
          label={t("pages.datasources.pipeline-section.no-pipeline")}
        />
        <FormControlLabel
          value="present-pipeline"
          control={<Radio disabled={disabled} color={disabled ? "default" : "primary"} />}
          label={t("pages.datasources.pipeline-section.select-pipeline-from-preset-elements")}
        />
        <Box sx={{ display: "flex", flexWrap: "wrap" }}>
          <BoxArea isActive={pipelineArea === "present-pipeline"}>
            <AutocompleteDropdown
              label={t("pages.datasources.pipeline-section.select-pipeline")}
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
          label={t("pages.datasources.pipeline-section.create-custom-pipeline")}
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
          // marginBottom: "70px",
          display: "flex",
          justifyContent: "space-between",
        }}
      >
        <Button
          variant="contained"
          color="secondary"
          aria-label={t("common.back")}
          onClick={() => {
            setActiveTab("datasource");
            const pipelineTab = tabs.find((tab) => tab.value === "datasource");
            if (pipelineTab) {
              navigate(pipelineTab.path);
            }
          }}
        >
          {t("common.back")}
        </Button>
        <Button
          variant="contained"
          aria-label={t("common.recap")}
          onClick={() => {
            setActiveTab("dataIndex");
          }}
        >
          {t("common.next-step")}
        </Button>
      </Box>
      {showDialog.isShow && (
        <ModalConfirm
          title={t("pages.datasources.pipeline-section.confirm-change")}
          body={showDialog.message}
          labelConfirm={t("common.change")}
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

