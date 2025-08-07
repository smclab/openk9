import { DocumentNode, useQuery } from "@apollo/client";
import { CodeInput, ModalConfirm } from "@components/Form";
import { useSideNavigation } from "@components/sideNavigationContext";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  Link,
  MenuItem,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useEnrichItemsQuery } from "../../../../../graphql-generated";
import { tabsType } from "../../../datasourceType";
import { ConnectionData } from "../../../types";
import { BoxArea } from "../../BoxArea";
import { DateTimeSection } from "./DateTimeSection";
import { ChangeValueKey, GenerateDynamicForm, Template } from "./DynamicForm";

export const defaultModal = {
  isShow: false,
  message: "",
  title: "",
  callbackConfirm: () => {},
  callbackClose: () => {},
};

export function ConfigureDatasource({
  dataDatasource,
  setDataDatasource,
  setActiveTab,
  disabled,
  isRecap,
  tabs,
  setIsRecap,
  datasourceId,
  dynamicFormJson,
  loadingFormCustom,
  dynamicTemplate,
  changeValueTemplate,
}: {
  dataDatasource: ConnectionData;
  setDataDatasource: React.Dispatch<React.SetStateAction<ConnectionData>>;
  setActiveTab: (value: React.SetStateAction<string>) => void;
  disabled: boolean;
  isRecap: boolean;
  tabs: tabsType;
  setIsRecap: React.Dispatch<React.SetStateAction<boolean>>;
  requestBody: any;
  formCustom: CustomForm[] | undefined;
  setFormCustom: React.Dispatch<React.SetStateAction<CustomForm[] | undefined>>;
  datasourceId: string;
  dynamicFormJson: string | null;
  loadingFormCustom: boolean;
  dynamicTemplate: Template | null;
  changeValueTemplate: ChangeValueKey;
}) {
  const [areaState, setAreaState] = useState<{
    schedulingArea: SchedulingRadioType | null;
    // pipelineArea: PipelineRadioType | null;
  }>({
    schedulingArea: dataDatasource.scheduling ? "present-scheduling" : "custom-scheduling",
    // pipelineArea: "no-pipeline",
  });
  const navigate = useNavigate();

  return (
    <React.Fragment>
      <Box sx={{ marginTop: "20px" }}>
        {!isRecap && (
          <Box sx={{ display: "flex", flexDirection: "column", gap: "20px" }}>
            {!loadingFormCustom ? (
              dynamicTemplate ? (
                <GenerateDynamicForm
                  templates={dynamicTemplate}
                  changeValueKey={changeValueTemplate}
                  disabled={disabled}
                />
              ) : (
                <CodeInput
                  language="json"
                  label="Configuration"
                  disabled={false}
                  id="code-input-datasource"
                  onChange={(e) => {
                    setDataDatasource((data) => ({ ...data, jsonConfig: e }));
                  }}
                  validationMessages={[]}
                  value={dataDatasource.jsonConfig || ""}
                  description="Json configuration sended to corresponding external parser when execution start"
                />
              )
            ) : (
              <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100%" }}>
                <CircularProgress />
              </Box>
            )}
            <DateTimeSection
              dataDatasource={dataDatasource}
              setDataDatasource={setDataDatasource}
              disabled={disabled}
            />
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
                  setActiveTab("connectors");
                  const pipelineTab = tabs.find((tab) => tab.value === "connectors");
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
                disabled={!areaState.schedulingArea}
                onClick={() => {
                  setActiveTab("pipeline");
                  const pipelineTab = tabs.find((tab) => tab.value === "pipeline");
                  if (pipelineTab) {
                    navigate(pipelineTab.path);
                  }
                }}
              >
                Next Step
              </Button>
            </Box>
          </Box>
        )}
      </Box>
    </React.Fragment>
  );
}

export function EnrichItemsTable({
  connectionData,
  setConnectionData,
  isActive,
  isView,
  isNew,
}: {
  connectionData: ConnectionData;
  setConnectionData: React.Dispatch<React.SetStateAction<ConnectionData>>;
  isActive: boolean;
  isView: boolean;
  isNew: boolean;
}) {
  const enrichItems = useEnrichItemsQuery({
    fetchPolicy: "network-only",
  });

  const handleOrderChange = (index: number, direction: "up" | "down") => {
    if (!connectionData?.enrichPipelineCustom?.linkedEnrichItems) return;

    const updatedEnrichItems = [...connectionData.enrichPipelineCustom?.linkedEnrichItems];

    if (direction === "up" && index > 0) {
      const currentWeight = updatedEnrichItems[index].weight;
      const previousWeight = updatedEnrichItems[index - 1].weight;
      updatedEnrichItems[index].weight = previousWeight;
      updatedEnrichItems[index - 1].weight = currentWeight;
    } else if (direction === "down" && index < updatedEnrichItems.length - 1) {
      const currentWeight = updatedEnrichItems[index].weight;
      const nextWeight = updatedEnrichItems[index + 1].weight;
      updatedEnrichItems[index].weight = nextWeight;
      updatedEnrichItems[index + 1].weight = currentWeight;
    }

    updatedEnrichItems.sort((a, b) => a.weight - b.weight);

    setConnectionData((prevData) => ({
      ...prevData,
      enrichPipelineCustom: {
        ...prevData.enrichPipelineCustom,
        id: prevData.enrichPipelineCustom?.id ?? "",
        name: prevData.enrichPipelineCustom?.name ?? "",
        linkedEnrichItems: updatedEnrichItems,
      },
    }));
  };

  const handleEnrichItemLink = (item: any) => {
    const weight = (connectionData?.enrichPipelineCustom?.linkedEnrichItems?.length || 0) + 1;
    const enrichItem = {
      id: item?.node?.id,
      name: item?.node?.name,
      description: item?.node?.description,
      weight,
    };
    setConnectionData((prevData) => ({
      ...prevData,
      enrichPipelineCustom: {
        id: prevData.enrichPipelineCustom?.id ?? "",
        name: prevData.enrichPipelineCustom?.name ?? "",
        linkedEnrichItems: [...(prevData?.enrichPipelineCustom?.linkedEnrichItems || []), enrichItem],
      },
    }));
  };

  const [openModal, setOpenModal] = React.useState(false);
  const [showDialog, setShowDialog] = useState(defaultModal);
  const [modalDataLost, setModalDataLost] = useState(false);
  const { changaSideNavigation } = useSideNavigation();
  const navigate = useNavigate();

  return (
    <div>
      {modalDataLost && (
        <ModalConfirm
          title="Confirm to leave from this page?"
          body="Are you sure you want to leave this page? This action is irreversible and all associated data will be lost."
          type="info"
          labelConfirm="Confirm"
          actionConfirm={() => {
            setModalDataLost(false);
            navigate("/enrich-item/new");
            changaSideNavigation("enrich-items");
          }}
          close={() => {
            setModalDataLost(false);
          }}
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
      <BoxArea isActive={isActive}>
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: "20px",
            paddingBlock: "30px",
          }}
        >
          <Typography variant="body1">Enrich items</Typography>
          <Typography variant="body1">Name</Typography>
          <TextField
            type={"text"}
            disabled={!isActive}
            value={connectionData?.enrichPipelineCustom?.name || ""}
            onChange={(e) => {
              const value = e.currentTarget.value;
              setConnectionData((d) => ({
                ...d,
                enrichPipelineCustom: {
                  id: d.enrichPipelineCustom?.id ?? "",
                  name: value,
                  linkedEnrichItems: d.enrichPipelineCustom?.linkedEnrichItems ?? [],
                },
              }));
            }}
          />
        </div>

        <Dialog open={openModal} fullWidth maxWidth="lg">
          <DialogTitle>{/* Titolo del Modale */}</DialogTitle>
          <DialogContent>
            <TableContainer>
              <Table>
                <TableHead sx={{ backgroundColor: "#f1f2f5" }}>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {enrichItems.data?.enrichItems?.edges?.map((item) => {
                    const isLinked = connectionData?.enrichPipelineCustom?.linkedEnrichItems?.some(
                      (enrichItem) => enrichItem.id === item?.node?.id,
                    );
                    if (!isLinked) {
                      return (
                        <TableRow key={item?.node?.id}>
                          <TableCell>{item?.node?.name}</TableCell>
                          <TableCell>{item?.node?.description}</TableCell>
                          <TableCell>
                            <Link component="button" variant="body2" onClick={() => handleEnrichItemLink(item)}>
                              Link
                            </Link>
                          </TableCell>
                        </TableRow>
                      );
                    }
                    return null;
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenModal(false)} color="primary">
              Apply
            </Button>
          </DialogActions>
        </Dialog>

        <div style={{ overflowX: "auto" }}>
          <Table>
            <TableHead sx={{ backgroundColor: "#f1f2f5" }}>
              <TableRow>
                <TableCell>Order</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {connectionData?.enrichPipelineCustom?.linkedEnrichItems
                ?.sort((a, b) => a.weight - b.weight)
                .map((item, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      <IconButton onClick={() => handleOrderChange(index, "up")} disabled={index === 0}>
                        <ArrowUpwardIcon />
                      </IconButton>
                      <IconButton
                        onClick={() => handleOrderChange(index, "down")}
                        disabled={index === (connectionData?.enrichPipelineCustom?.linkedEnrichItems || [])?.length - 1}
                      >
                        <ArrowDownwardIcon />
                      </IconButton>
                    </TableCell>
                    <TableCell>{item?.name}</TableCell>
                    <TableCell>{item?.description}</TableCell>
                    <TableCell>
                      <div style={{ display: "flex", gap: "20px" }}>
                        <Link
                          underline="always"
                          onClick={() => {
                            const filteredItems = connectionData?.enrichPipelineCustom?.linkedEnrichItems?.filter(
                              (element) => element.id !== item.id,
                            );
                            setConnectionData((prevData) => ({
                              ...prevData,
                              enrichPipelineCustom: {
                                ...prevData.enrichPipelineCustom,
                                id: prevData.enrichPipelineCustom?.id ?? "",
                                name: prevData.enrichPipelineCustom?.name ?? "",
                                linkedEnrichItems: filteredItems || [],
                              },
                            }));
                          }}
                        >
                          Unlink
                        </Link>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>
        </div>
        <div>
          {isActive && (
            <div
              style={{
                display: "flex",
                flexWrap: "wrap",
                minHeight: "50px",
                alignItems: "center",
              }}
            >
              <Button color="primary" variant="outlined" onClick={() => setOpenModal(true)}>
                Add Enrich Item
              </Button>
              <Button
                color="primary"
                style={{ marginLeft: "auto" }}
                onClick={() => {
                  setModalDataLost(true);
                }}
              >
                Create Enrich Item
              </Button>
            </div>
          )}
        </div>
      </BoxArea>
    </div>
  );
}

export type CustomForm = {
  info: string;
  label: string;
  name: string;
  required: boolean;
  size: number;
  type: string;
  validator: {};
  values: any;
};

export type SchedulingRadioType = "present-scheduling" | "custom-scheduling";
export type PipelineRadioType = "present-pipeline" | "custom-pipeline" | "no-pipeline";

export const schedulingPresets = [
  { label: "Every 5 Minutes", value: "0 */5 * ? * * *" },
  { label: "Every 30 Minutes", value: "0 */30 * ? * * *" },
  { label: "Every Hour", value: "0 0 * ? * * *" },
  { label: "Every Day at Midday", value: "0 0 12 * * ? *" },
  { label: "Every Day at Midnight", value: "0 0 0 * * ? *" },
];

export const parseCronString = (cronString: string) => {
  const [_, minute, hour, day, months] = cronString.split(" ");

  return {
    day,
    hour,
    minute,
    months,
  };
};

// export const generateCronString = ({
//   dayOfMonth,
//   hour,
//   minute,
//   months,
//   dayOfWeek,
// }: {
//   dayOfMonth: string | null | undefined;
//   hour: string | null | undefined;
//   minute: string | null | undefined;
//   months: string | null | undefined;
//   dayOfWeek: string | null | undefined;
// }) => {
//   const cronString = `0 ${minute || "*"} ${hour || "*"} ${dayOfMonth || "*"} ${months || "*"} ${dayOfWeek || "*"} *`;
//   return cronString;
// };

type Option = {
  id: string;
  name: string;
};

type InfiniteScrollSelectProps = {
  connectionData: any;
  setConnectionData: React.Dispatch<React.SetStateAction<any>>;
  isView: boolean;
  isNew: boolean;
  query: DocumentNode;
  accessKey: string;
};

const InfiniteScrollSelect: React.FC<InfiniteScrollSelectProps> = ({
  connectionData,
  setConnectionData,
  isView,
  isNew,
  query,
  accessKey,
}) => {
  const [options, setOptions] = useState<Option[]>([]);
  const [isFetching, setIsFetching] = useState(false);

  const { data, loading, fetchMore } = useQuery(query, {
    variables: { first: 20, after: null },
    fetchPolicy: "cache-and-network",
    skip: isNew,
  });

  React.useEffect(() => {
    if (!data || !data[accessKey] || !data[accessKey].edges) return;
    const newOptions = data[accessKey].edges.map((edge: any) => edge.node);
    setOptions((prev) => {
      const existingIds = new Set(prev.map((o) => o.id));
      const uniqueOptions = newOptions.filter((o: Option) => !existingIds.has(o.id));
      return [...prev, ...uniqueOptions];
    });
  }, [data]);

  const handleMenuScroll = React.useCallback(
    (event: React.UIEvent<HTMLElement>) => {
      if (isFetching || !data[accessKey]?.pageInfo?.hasNextPage) return;
      const { scrollTop, scrollHeight, clientHeight } = event.target as HTMLElement;
      if (scrollHeight - scrollTop <= clientHeight + 5) {
        setIsFetching(true);
        fetchMore({
          variables: { after: data[accessKey].pageInfo.endCursor },
          updateQuery: (prev, { fetchMoreResult }) => {
            if (!fetchMoreResult) return prev;
            return {
              [accessKey]: {
                __typename: prev[accessKey].__typename,
                edges: [...prev[accessKey].edges, ...fetchMoreResult[accessKey].edges],
                pageInfo: fetchMoreResult[accessKey].pageInfo,
              },
            };
          },
        })
          .catch((error) => {
            console.error("Error fetching more:", error);
          })
          .finally(() => setIsFetching(false));
      }
    },
    [data, isFetching, fetchMore],
  );

  return (
    <FormControl fullWidth>
      <Select
        value={connectionData?.dataIndex?.id || ""}
        sx={{ width: "100%", minWidth: "350px" }}
        disabled={isView}
        onChange={(event) => {
          const id = event.target.value;
          const name = options.find((item) => item.id === id)?.name || "";
          setConnectionData((prevData: any) => ({
            ...prevData,
            dataIndex: { id, name },
          }));
        }}
        MenuProps={{
          PaperProps: {
            onScroll: handleMenuScroll,
          },
        }}
        displayEmpty
      >
        {options.map((item) => (
          <MenuItem key={item.id} value={item.id}>
            {item.name}
          </MenuItem>
        ))}
        {(loading || isFetching) && (
          <MenuItem disabled>
            <CircularProgress size={24} />
          </MenuItem>
        )}
      </Select>
    </FormControl>
  );
};
