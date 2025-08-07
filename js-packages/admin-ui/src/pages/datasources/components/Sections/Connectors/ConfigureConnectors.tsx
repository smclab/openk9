import { ModalConfirm } from "@components/Form";
import AddIcon from "@mui/icons-material/Add";
import CheckIcon from "@mui/icons-material/Check";
import { Box, Button, ButtonBase, Card, CardContent, Grid, Tooltip, Typography } from "@mui/material";
import { red } from "@mui/material/colors";
import Divider from "@mui/material/Divider";
import { useTheme } from "@mui/material/styles";
import { SavePluginnDriverModel } from "@pages/PluginDriver";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { PluginDriverType, Provisioning, usePluginDriversQuery } from "../../../../../graphql-generated";
import { tabsType } from "../../../datasourceType";
import { ConnectionData } from "../../../types";
import { BoxArea } from "../../BoxArea";
import { PluginDriverCards } from "./PluginDriverCards";

export type Section = "selectConnectos" | "preconfiguredConnectorsa" | "card";

interface ConfigureConnectorsProps {
  areaEnabled: Section;
  formValues: ConnectionData;
  getHealthInfo: (id: number) => Promise<void>;
  getHealthInfoWithoutId: () => Promise<void>;
  disabled: boolean;
  tabs: tabsType;
  setActiveTab: React.Dispatch<React.SetStateAction<string>>;
  setAreaEnabled: React.Dispatch<React.SetStateAction<Section>>;
  setFormValues: React.Dispatch<React.SetStateAction<ConnectionData>>;
  setShowDialog: React.Dispatch<
    React.SetStateAction<{
      isShow: boolean;
      message: string;
      title: string;
      callbackConfirm: () => void;
      callbackClose: () => void;
    }>
  >;
}

function ButtonAddPluginDrivers({ disabled, pluginDriverRefetch }: { disabled: boolean; pluginDriverRefetch: any }) {
  const [selected, setSelected] = useState(false);
  const theme = useTheme();
  const formRef = React.useRef<{ submit: () => void } | null>(null);

  const handleClick = () => {
    setSelected(true);
  };

  const handleConfirm = () => {
    formRef.current?.submit();
  };

  const borderColor = theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";

  return (
    <>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={4} key={"addPluginDrivers"}>
          <ButtonBase disabled={disabled} sx={{ width: "100%", borderRadius: "10px" }} onClick={handleClick}>
            <Card
              sx={{
                height: "150px",
                display: "flex",
                flex: 1,
                flexDirection: "column",
                justifyContent: "center",
                alignItems: "center",
                transition: "border 0.3s ease",
                border: selected ? `2px solid ${red[500]}` : `1px solid ${borderColor}`,
                boxShadow: selected ? "0 4px 8px rgba(0, 0, 0, 0.2)" : "none",
              }}
            >
              {selected && (
                <Box
                  sx={{
                    position: "absolute",
                    top: 10,
                    right: 10,
                    width: 20,
                    height: 20,
                    borderRadius: "50%",
                    backgroundColor: red[500],
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                >
                  <CheckIcon sx={{ color: "white", fontSize: 16 }} />
                </Box>
              )}
              <CardContent sx={{ justifyContent: "center", alignItems: "center" }}>
                <Typography variant="h5" sx={{ opacity: disabled ? "0.3" : "unset" }}>
                  Add new connector
                </Typography>
                <AddIcon sx={{ opacity: disabled ? "0.3" : "unset" }} />
              </CardContent>
            </Card>
          </ButtonBase>
        </Grid>
      </Grid>
      {selected && (
        <ModalConfirm
          fullWidth
          maxWidth="lg"
          actionConfirm={handleConfirm}
          labelConfirm="Save"
          title="Add new connector"
          body=""
          close={() => setSelected(false)}
        >
          <SavePluginnDriverModel isConnector onSubmitSuccess={() => setSelected(false)} ref={formRef} />
        </ModalConfirm>
      )}
    </>
  );
}

export function ConfigureConnectors({
  areaEnabled,
  formValues,
  getHealthInfo,
  getHealthInfoWithoutId,
  tabs,
  disabled,
  setActiveTab,
  setAreaEnabled,
  setFormValues,
  setShowDialog,
}: ConfigureConnectorsProps) {
  const pluginDrivers = usePluginDriversQuery();
  const navigate = useNavigate();

  // Stati derivati
  const pluginDriverId = formValues.pluginDriverSelect?.id;
  const isDisabledNextStep =
    (pluginDriverId === null || pluginDriverId === undefined || !formValues?.name) && areaEnabled !== "selectConnectos";

  const systemPluginDrivers = pluginDrivers.data?.pluginDriversPageFilter?.content
    ?.filter((element) => element?.provisioning === "SYSTEM")
    .filter((sy) => sy?.id);
  const userPluginDrivers = pluginDrivers.data?.pluginDriversPageFilter?.content
    ?.filter((element) => element?.provisioning === "USER")
    .filter((el) => el?.id);

  return (
    <Box>
      <h2>Choose only one type of connectors from the list below</h2>
      <Box>
        <BoxArea isActive={areaEnabled === "card"}>
          {systemPluginDrivers && systemPluginDrivers.length > 0 && (
            <>
              <Typography variant="h3" gutterBottom>
                Preconfigured Connectors
              </Typography>
              <PluginDriverCards
                systemPluginDrivers={systemPluginDrivers}
                disabled={areaEnabled !== "card" || disabled}
                activeCardId={pluginDriverId || null}
                setActiveCardId={(
                  id: string | null,
                  name: string | null,
                  description?: string | null,
                  host?: string | null,
                  path?: string | null,
                  port?: string | null,
                  secure?: boolean | null,
                  method?: string | null,
                  provisioning?: Provisioning,
                  pluginDriverType?: PluginDriverType,
                  json?: string | null,
                ) => {
                  id &&
                    setFormValues((pre) => ({
                      ...pre,
                      pluginDriverSelect: {
                        ...pre,
                        id,
                        nameConnectors: name,
                        description,
                        host,
                        path,
                        port,
                        secure,
                        method,
                        provisioning,
                        pluginDriverType,
                        json,
                      },
                    }));
                }}
              />
              <Divider sx={{ margin: "17.5px 0px" }} />
            </>
          )}
          {userPluginDrivers && userPluginDrivers.length > 0 && (
            <>
              <Typography variant="h3" gutterBottom>
                Custom Connectors
              </Typography>
              <PluginDriverCards
                systemPluginDrivers={userPluginDrivers}
                disabled={areaEnabled !== "card" || disabled}
                activeCardId={pluginDriverId || null}
                setActiveCardId={(
                  id: string | null,
                  name: string | null,
                  description?: string | null,
                  host?: string | null,
                  path?: string | null,
                  port?: string | null,
                  secure?: boolean | null,
                  method?: string | null,
                  provisioning?: Provisioning,
                  pluginDriverType?: PluginDriverType,
                  json?: string | null,
                ) => {
                  id &&
                    setFormValues((pre) => ({
                      ...pre,
                      pluginDriverSelect: {
                        ...pre,
                        id,
                        nameConnectors: name,
                        description,
                        host,
                        path,
                        port,
                        secure,
                        method,
                        provisioning,
                        pluginDriverType,
                        json,
                      },
                    }));
                }}
              />
              <Divider sx={{ margin: "17.5px 0px" }} />
            </>
          )}
          <ButtonAddPluginDrivers pluginDriverRefetch={pluginDrivers} disabled={disabled} />
          <Divider sx={{ margin: "17.5px 0px" }} />
          {!disabled && <p>Test connector</p>}
          {!disabled && (
            <Button
              variant="outlined"
              disabled={pluginDriverId === null || disabled}
              onClick={() => {
                getHealthInfo(Number(pluginDriverId));
              }}
            >
              Test it!
            </Button>
          )}
        </BoxArea>
      </Box>

      <Box
        sx={{
          display: "flex",
          marginTop: "10px",
          justifyContent: "space-between",
          width: "100%",
          paddingBlock: "20px",
        }}
      >
        <Button variant="contained" color="secondary" aria-label="Back" onClick={() => navigate("/data-sources")}>
          Back
        </Button>
        <Tooltip title={isDisabledNextStep ? "Please select on plugin or create one" : ""}>
          <span style={{ cursor: "pointer" }}>
            <Button
              variant="contained"
              disabled={isDisabledNextStep}
              onClick={() => {
                setActiveTab("datasource");
                const pipelineTab = tabs.find((tab) => tab.value === "datasource");
                if (pipelineTab) {
                  navigate(pipelineTab.path);
                }
              }}
            >
              Next step
            </Button>
          </span>
        </Tooltip>
      </Box>
    </Box>
  );
}
