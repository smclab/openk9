import { DocumentNode, useQuery } from "@apollo/client";
import { CodeInput, ModalConfirm } from "@components/Form";
import { useSideNavigation } from "@components/sideNavigationContext";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import {
  Autocomplete,
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormHelperText,
  IconButton,
  Link,
  ListItemText,
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
import { DataIndicesQuery } from "@pages/dataindices/gql";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useEnrichItemsQuery } from "../../../../../graphql-generated";
import { BoxArea } from "../../BoxArea";
import { DateTimeSection } from "./DateTimeSection";
import { GenerateDynamicForm, Template } from "./DynamicForm";
import { tabsType } from "../../../datasourceType";
import { ConnectionData } from "../../../types";

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
  changeValueTemplate: (fieldName: string, newValue: string | number | Array<string> | boolean) => void;
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
    if (!connectionData?.linkedEnrichItems) return;

    const updatedEnrichItems = [...connectionData.linkedEnrichItems];

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
      linkedEnrichItems: updatedEnrichItems,
    }));
  };

  const handleEnrichItemLink = (item: any) => {
    const weight = (connectionData?.linkedEnrichItems?.length || 0) + 1;
    const enrichItem = {
      id: item?.node?.id,
      name: item?.node?.name,
      description: item?.node?.description,
      weight,
    };
    setConnectionData((prevData) => ({
      ...prevData,
      linkedEnrichItems: [...(prevData?.linkedEnrichItems || []), enrichItem],
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
            value={connectionData?.enrichPipeline?.name}
            onChange={(e) => {
              if (e.currentTarget.value !== null && e.currentTarget.value !== undefined)
                setConnectionData((d) => ({
                  ...d,
                  enrichPipeline: {
                    ...d.enrichPipeline,
                    name: e?.currentTarget?.value,
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
                    const isLinked = connectionData?.linkedEnrichItems?.some(
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
              {connectionData?.linkedEnrichItems
                ?.sort((a, b) => a.weight - b.weight)
                .map((item, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      <IconButton onClick={() => handleOrderChange(index, "up")} disabled={index === 0}>
                        <ArrowUpwardIcon />
                      </IconButton>
                      <IconButton
                        onClick={() => handleOrderChange(index, "down")}
                        disabled={index === (connectionData?.linkedEnrichItems || [])?.length - 1}
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
                            const filteredItems = connectionData?.linkedEnrichItems?.filter(
                              (element) => element.id !== item.id,
                            );
                            setConnectionData((prevData) => ({
                              ...prevData,
                              linkedEnrichItems: filteredItems,
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

function Form({
  formCustom,
  setFormCustom,
  disabled,
}: {
  formCustom: CustomForm[] | undefined;
  setFormCustom: React.Dispatch<CustomForm[] | undefined>;
  disabled: boolean;
}) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormCustom(
      formCustom?.map((form) =>
        form.name === name
          ? {
              ...form,
              values: form.values.map((val: any) => (val.isDefault ? { ...val, value } : val)),
            }
          : form,
      ),
    );
  };

  const handleSelectChange = (e: any) => {
    const { name, value } = e.target;
    setFormCustom(
      formCustom?.map((form) =>
        form.name === name
          ? {
              ...form,
              values: form.values.map((val: any) => (val.isDefault ? { ...val, value } : val)),
            }
          : form,
      ),
    );
  };

  const handleListChange = (name: string, value: string[]) => {
    setFormCustom(
      formCustom?.map((form) =>
        form.name === name
          ? {
              ...form,
              values: value.map((val) => {
                const existingVal = form.values.find((v: any) => v.value === val);
                return existingVal ? { ...existingVal } : { value: val, isDefault: false };
              }),
            }
          : form,
      ),
    );
  };

  const handleRemoveItem = (formName: string, itemValue: string) => {
    setFormCustom(
      formCustom?.map((form) =>
        form.name === formName
          ? {
              ...form,
              values: form.values.filter((val: any) => val.value !== itemValue),
            }
          : form,
      ),
    );
  };

  const handleRemoveAllItems = (formName: string) => {
    setFormCustom(
      formCustom?.map((form) =>
        form.name === formName
          ? {
              ...form,
              values: [],
            }
          : form,
      ),
    );
    handleListChange(formName, []);
  };

  return (
    <>
      <Box>
        {formCustom?.map((form) => (
          <Box key={form?.name} sx={{ marginBottom: "1rem" }}>
            <FormControl fullWidth required={form?.required}>
              {form?.type === "text" || form?.type === "number" || form?.type === "password" ? (
                <>
                  <Box sx={{ display: "flex", gap: "3px" }}>
                    <Typography variant="body1">{form?.label || form?.name}</Typography>
                    {form?.required && <FormHelperText sx={{ color: "red" }}>*</FormHelperText>}
                  </Box>
                  {form?.info && <FormHelperText>{form?.info}</FormHelperText>}
                  <TextField
                    id={form?.name}
                    disabled={disabled}
                    name={form?.name}
                    type={form?.type}
                    required={form?.required}
                    value={form?.values.find((val: any) => val.isDefault)?.value || ""}
                    onChange={handleChange}
                  />
                </>
              ) : form?.type === "select" ? (
                <>
                  <Box sx={{ display: "flex", gap: "3px" }}>
                    <Typography variant="body1">{form?.name}</Typography>
                    {form?.required && <FormHelperText sx={{ color: "red" }}>*</FormHelperText>}
                  </Box>
                  <Select
                    id={form?.name}
                    disabled={disabled}
                    name={form?.name}
                    value={form?.values.find((val: any) => val.isDefault)?.value || ""}
                    label={form?.label}
                    onChange={handleSelectChange}
                  >
                    {form?.values.map((value: any) => (
                      <MenuItem key={value.value} value={value.value}>
                        {value.value}
                      </MenuItem>
                    ))}
                  </Select>
                </>
              ) : form?.type === "list" ? (
                <>
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      gap: "8px",
                    }}
                  >
                    <Typography variant="body1">{form?.name}</Typography>
                    <Button
                      variant="outlined"
                      color="secondary"
                      disabled={disabled}
                      onClick={() => handleRemoveAllItems(form.name)}
                    >
                      Remove all
                    </Button>
                  </Box>
                  <Autocomplete
                    freeSolo
                    disabled={disabled}
                    multiple
                    options={[]}
                    sx={{ minWidth: "230px" }}
                    value={form.values?.map((val: any) => val.value) || []}
                    onChange={(event, value) => handleListChange(form.name, value as string[])}
                    renderTags={(value: string[], getTagProps) =>
                      value?.map((option: string, index: number) => (
                        <Chip
                          label={option}
                          {...getTagProps({ index })}
                          onDelete={() => handleRemoveItem(form.name, option)}
                        />
                      ))
                    }
                    renderInput={(params) => (
                      <TextField {...params} variant="outlined" name={form?.name} placeholder="Add a domain" />
                    )}
                  />
                </>
              ) : form?.type === "checkbox" ? (
                <MenuItem value="*">
                  <Checkbox
                    checked={form.values[0].value}
                    disabled={disabled}
                    onChange={() =>
                      handleSelectChange({
                        target: {
                          name: form?.name || form?.label,
                          value: !form.values[0]?.value,
                        },
                      })
                    }
                  />
                  <ListItemText primary="Every month" />
                </MenuItem>
              ) : null}
            </FormControl>
          </Box>
        ))}
      </Box>
    </>
  );
}

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
