import {
  CodeInput,
  ContainerFluid,
  CustomSelect,
  CustomSelectRelationsOneToOne,
  NumberInput,
  TitleEntity,
} from "@components/Form";
import { useRestClient } from "@components/queryClient";
import {
  Box,
  Button,
  Checkbox,
  FormControl,
  FormControlLabel,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useOptions } from "@pages/SuggestionCategories";
import React, { useCallback, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { ChunkType, useDataSourcesQuery, useDocumentTypesQuery } from "../../../graphql-generated";
import { DataindexData } from "../SaveDataindex";
import { AutocompleteDropdown } from "@components/Form/Select/AutocompleteDropdown";
import { useDocTypeOptions } from "../../../utils/RelationOneToOne";

export function CreateDataindex({
  dataindexData,
  setDataindexData,
  verifyData,
  setVerifyData,
  isReadOnly,
}: {
  dataindexData: DataindexData | null | undefined;
  setDataindexData: React.Dispatch<React.SetStateAction<DataindexData | null | undefined>>;
  verifyData: string;
  setVerifyData: React.Dispatch<React.SetStateAction<string | undefined>>;
  isReadOnly: boolean;
}) {
  const saveAndContinueDataindex = () => {
    setVerifyData("editView");
  };

  const datasourcesQuery = useDataSourcesQuery();
  const documentTypesQuery = useDocumentTypesQuery();
  const { OptionDocType, docTypesQuery } = useOptions();
  const { datasourcersQuery, OptionDataSourceType } = useOptionsDataSource();

  const documentTypes = useMemo(
    () =>
      documentTypesQuery?.data?.docTypes?.edges?.map(
        (edge) => dataindexData?.docTypeIds?.includes(Number(edge?.node?.id)) || false,
      ) || [],
    [documentTypesQuery.data, dataindexData?.docTypeIds],
  );

  const [documentTypesSelected, setDocumentTypesSelected] = useState(documentTypes);
  const [step, setStep] = React.useState<"configureStandart" | "configureJson">("configureStandart");

  const docsId = useMemo(() => dataindexData?.docTypeIds, [dataindexData?.docTypeIds]);

  const isLoading = datasourcesQuery.loading || documentTypesQuery.loading;

  const resetOptionalFields = () => {
    setDataindexData((prevData) =>
      prevData
        ? {
            ...prevData,
            chunkWindowSize: null,
            embeddingJsonConfig: null,
            chunkType: null,
          }
        : null,
    );
  };

  const loadMoreOptionsDataSource = async (): Promise<{ value: string; label: string }[]> => {
    if (!datasourcersQuery.data?.datasources?.pageInfo?.hasNextPage) return [];

    try {
      const response = await datasourcersQuery.fetchMore({
        variables: {
          after: datasourcersQuery.data.datasources.pageInfo.endCursor,
        },
      });

      const newEdges = response.data?.datasources?.edges || [];
      const newPageInfo = response.data?.datasources?.pageInfo;

      if (!newEdges.length || !newPageInfo) {
        console.warn("No new data fetched or pageInfo is missing.");
        return [];
      }

      datasourcersQuery.updateQuery((prev) => ({
        ...prev,
        datasources: {
          __typename: "DefaultConnection_Datasource",
          ...prev.datasources,
          edges: [...(prev.datasources?.edges || []), ...newEdges],
          pageInfo: newPageInfo,
        },
      }));

      return newEdges
        .map((item) => ({
          value: item?.node?.id || "",
          label: item?.node?.name || "",
        }))
        .filter((option) => option.value && option.label);
    } catch (error) {
      console.error("Error loading more options for datasource:", error);
      return [];
    }
  };

  const loadMoreOptions = async (): Promise<{ value: string; label: string }[]> => {
    if (!docTypesQuery.data?.docTypeFields?.pageInfo?.hasNextPage) return [];

    try {
      const response = await docTypesQuery.fetchMore({
        variables: {
          after: docTypesQuery.data.docTypeFields.pageInfo.endCursor,
        },
      });

      const newEdges = response.data?.docTypeFields?.edges || [];
      const newPageInfo = response.data?.docTypeFields?.pageInfo;

      if (!newEdges.length || !newPageInfo) {
        console.warn("No new data fetched or pageInfo is missing.");
        return [];
      }

      docTypesQuery.updateQuery((prev) => ({
        ...prev,
        docTypeFields: {
          ...prev.docTypeFields,
          edges: [...(prev.docTypeFields?.edges || []), ...newEdges],
          pageInfo: newPageInfo,
        },
      }));

      return newEdges
        .map((item) => ({
          value: item?.node?.id || "",
          label: item?.node?.name || "",
        }))
        .filter((option) => option.value && option.label);
    } catch (error) {
      console.error("Error loading more options:", error);
      return [];
    }
  };

  return (
    <ContainerFluid>
      {isLoading ? (
        <Typography>Loading...</Typography>
      ) : (
        <>
          <TitleEntity nameEntity="Data Index" description="" id={dataindexData?.dataindexId || ""} />
          {step === "configureStandart" && (
            <div>
              <FormControl fullWidth margin="normal">
                <Box sx={{ marginBottom: 1 }}>
                  <Typography variant="subtitle1" component="label" htmlFor={"name-create-data-index"}>
                    {"Name"}
                  </Typography>
                </Box>
                <TextField
                  placeholder="Insert text here"
                  id="name-create-data-index"
                  disabled={isReadOnly}
                  value={dataindexData?.name || ""}
                  onChange={(e) =>
                    setDataindexData((prevData) =>
                      prevData
                        ? {
                            ...prevData,
                            name: e.target.value,
                          }
                        : null,
                    )
                  }
                />
                <Box sx={{ marginBottom: 1 }}>
                  <Typography variant="subtitle1" component="label" htmlFor={"description-create-data-index"}>
                    {"Description"}
                  </Typography>
                </Box>
                <TextField
                  placeholder="Insert description here"
                  id="description-create-data-index"
                  disabled={isReadOnly}
                  value={dataindexData?.description || ""}
                  onChange={(e) =>
                    setDataindexData((prevData) =>
                      prevData
                        ? {
                            ...prevData,
                            description: e.target.value,
                          }
                        : null,
                    )
                  }
                />

                <FormControl fullWidth margin="normal">
                  <Box sx={{ marginBottom: 1 }}>
                    <Typography variant="subtitle1" component="label">
                      {"Associate Datasource"}
                    </Typography>
                  </Box>
                  <CustomSelectRelationsOneToOne
                    options={OptionDataSourceType}
                    label=""
                    onChange={(val) => {
                      setDataindexData((prevData) =>
                        prevData
                          ? {
                              ...prevData,
                              datasourceId: { id: val.id, name: val.name },
                            }
                          : null,
                      );
                    }}
                    value={{
                      id: dataindexData?.datasourceId?.id || "",
                      name: dataindexData?.datasourceId?.name || "",
                    }}
                    disabled={isReadOnly}
                    loadMoreOptions={{
                      response: loadMoreOptionsDataSource,
                      hasNextPage: datasourcersQuery.data?.datasources?.pageInfo?.hasNextPage || false,
                    }}
                  />
                </FormControl>
              </FormControl>
              <Box sx={{ marginBottom: 1 }}>
                <Typography variant="subtitle1" component="label">
                  {"Document types"}
                </Typography>
              </Box>

              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell></TableCell>
                      <TableCell>Name</TableCell>
                      <TableCell>Description</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {documentTypesQuery.data?.docTypes?.edges?.map((item, index) => (
                      <TableRow key={item?.node?.id}>
                        <TableCell>
                          <FormControlLabel
                            control={
                              <Checkbox
                                checked={documentTypesSelected[index]}
                                disabled={isReadOnly}
                                onChange={() => {
                                  const updatedSelected = [...documentTypesSelected];
                                  updatedSelected[index] = !documentTypesSelected[index];
                                  setDocumentTypesSelected(updatedSelected);

                                  const updatedDocTypeIds = [...(dataindexData?.docTypeIds || [])];
                                  const docTypeId = Number(item?.node?.id);
                                  if (updatedSelected[index]) {
                                    updatedDocTypeIds.push(docTypeId);
                                  } else {
                                    const removeIndex = updatedDocTypeIds.indexOf(docTypeId);
                                    if (removeIndex > -1) updatedDocTypeIds.splice(removeIndex, 1);
                                  }
                                  setDataindexData((prevData) =>
                                    prevData
                                      ? {
                                          ...prevData,
                                          docTypeIds: updatedDocTypeIds,
                                        }
                                      : null,
                                  );
                                }}
                              />
                            }
                            label=""
                          />
                        </TableCell>
                        <TableCell>{item?.node?.name}</TableCell>
                        <TableCell>{item?.node?.description}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              <Box sx={{ marginTop: 3 }}>
                <FormControl fullWidth margin="normal">
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={dataindexData?.knnIndex || false}
                        disabled={isReadOnly}
                        onChange={(e) => {
                          if (!e.target.checked) {
                            resetOptionalFields();
                          }
                          setDataindexData((prevData) =>
                            prevData
                              ? {
                                  ...prevData,
                                  knnIndex: e.target.checked,
                                }
                              : null,
                          );
                        }}
                      />
                    }
                    label="Enable KNN Index"
                  />
                </FormControl>

                <Box sx={{ marginBottom: 1 }}>
                  <CustomSelect
                    label="Chunk Type"
                    id="chunk-type-select"
                    dict={Object.fromEntries(Object.entries(ChunkType).filter(([key]) => key !== "Unrecognized"))}
                    disabled={isReadOnly || !dataindexData?.knnIndex}
                    value={(dataindexData?.chunkType as ChunkType) || ChunkType.ChunkTypeCharacterTextSplitter}
                    onChange={(e: string) =>
                      setDataindexData((prevData) =>
                        prevData
                          ? {
                              ...prevData,
                              chunkType: e as ChunkType,
                            }
                          : null,
                      )
                    }
                    validationMessages={[]}
                  />
                </Box>
                <NumberInput
                  label="Chunk Window Size"
                  disabled={isReadOnly || !dataindexData?.knnIndex || false}
                  id="chunk-window-size"
                  validationMessages={[]}
                  value={dataindexData?.chunkWindowSize || 0}
                  onChange={(e) =>
                    setDataindexData((prevData) =>
                      prevData
                        ? {
                            ...prevData,
                            chunkWindowSize: Number(e),
                          }
                        : null,
                    )
                  }
                />
              </Box>
              <CodeInput
                id="code-input"
                label="Embedding Json Config"
                height="200px"
                disabled={isReadOnly || !dataindexData?.knnIndex}
                readonly={isReadOnly || !dataindexData?.knnIndex}
                language="json"
                onChange={(event) => {
                  setDataindexData((prevData) =>
                    prevData
                      ? {
                          ...prevData,
                          embeddingJsonConfig: event,
                        }
                      : null,
                  );
                }}
                value={dataindexData?.embeddingJsonConfig || "{}"}
                validationMessages={[]}
              />
              <AutocompleteDropdown
                label="Doc Type"
                onChange={(val) => {
                  setDataindexData((prevData) =>
                    prevData
                      ? {
                          ...prevData,
                          embeddingDocTypeFieldId: { id: val.id, name: val.name },
                        }
                      : null,
                  );
                }}
                value={{
                  id: dataindexData?.embeddingDocTypeFieldId?.id || "",
                  name: dataindexData?.embeddingDocTypeFieldId?.name || "",
                }}
                disabled={isReadOnly}
                onClear={() =>
                  setDataindexData((prevData) =>
                    prevData
                      ? {
                          ...prevData,
                          embeddingDocTypeFieldId: { id: "", name: "" },
                        }
                      : null,
                  )
                }
                useOptions={useDocTypeOptions}
              />

              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  marginTop: "20px",
                }}
              >
                <Link to={"/dataindices"}>
                  <Button variant="contained" color="secondary">
                    BACK
                  </Button>
                </Link>
                <Button
                  variant="contained"
                  color="error"
                  disabled={
                    !dataindexData?.name || !dataindexData?.datasourceId || dataindexData?.docTypeIds?.length === 0
                  }
                  onClick={() => {
                    setStep("configureJson");
                  }}
                >
                  Next Step
                </Button>
              </div>
            </div>
          )}
          {step === "configureJson" && (
            <Configuration
              docTypeIds={docsId || []}
              setStep={setStep}
              saveAndContinueDataindex={saveAndContinueDataindex}
              setDataIndex={setDataindexData}
              isReadOnly={isReadOnly}
              dataIndex={dataindexData}
            />
          )}
        </>
      )}
    </ContainerFluid>
  );
}

function Configuration({
  docTypeIds,
  setStep,
  saveAndContinueDataindex,
  setDataIndex,
  isReadOnly,
  dataIndex,
}: {
  docTypeIds: number[];
  isReadOnly: boolean;
  setStep: React.Dispatch<React.SetStateAction<"configureStandart" | "configureJson">>;
  saveAndContinueDataindex: () => void;
  setDataIndex: React.Dispatch<React.SetStateAction<DataindexData | null | undefined>>;
  dataIndex: DataindexData | null | undefined;
}) {
  const restClient = useRestClient();
  const [settings, setSettings] = useState<string>("{}");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getInfo = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await restClient.dataIndexResource.postApiDatasourceV1DataIndexGetSettingsFromDocTypes({
        docTypeIds,
      });
      setSettings(JSON.stringify(response, null, 2));
    } catch (error) {
      setError("An error occurred while fetching settings.");
    } finally {
      setLoading(false);
    }
  }, [docTypeIds]);

  const previousDocTypeIds = useRef<number[]>([]);

  React.useEffect(() => {
    if (JSON.stringify(previousDocTypeIds.current) !== JSON.stringify(docTypeIds)) {
      if (docTypeIds.length > 0) {
        getInfo();
      }
      previousDocTypeIds.current = docTypeIds;
    }
  }, [docTypeIds, getInfo]);

  React.useEffect(() => {
    setDataIndex((prevData) => {
      if (!prevData) return null;
      if (prevData?.settings !== settings) {
        return { ...prevData, settings: settings };
      }
      return prevData;
    });
  }, [settings, setDataIndex]);

  if (loading) {
    return <Typography>Loading...</Typography>;
  }

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  return (
    <div>
      {!isReadOnly && <Typography variant="h6">Modify Data Index Settings</Typography>}
      {settings && (
        <CodeInput
          id="settings-code-input"
          readonly={isReadOnly}
          label="Settings"
          value={settings}
          onChange={(value) => setSettings(value)}
          language="json"
          validationMessages={[]}
          disabled={false}
          height="400px"
        />
      )}
      <Box display={"flex"} justifyContent={"space-between"}>
        <Button
          variant="contained"
          color="primary"
          onClick={() => {
            setStep("configureStandart");
          }}
          style={{ marginTop: "16px" }}
          aria-label="Go back to the previous step"
        >
          Back
        </Button>
        {!isReadOnly && (
          <Button
            variant="contained"
            color="primary"
            onClick={saveAndContinueDataindex}
            style={{ marginTop: "16px" }}
            aria-label="Save the changes and continue to the next step"
          >
            Save and Continue
          </Button>
        )}
      </Box>
    </div>
  );
}

export default Configuration;

export const useOptionsDataSource = () => {
  const datasourcers = useDataSourcesQuery();

  const getOptions = (data: any, key: "datasources") => {
    return (
      data?.[key]?.edges?.map((item: { node: { id: string; name: string } }) => ({
        value: item?.node?.id || "",
        label: item?.node?.name || "",
      })) || []
    );
  };

  const OptionDataSourceType = getOptions(datasourcers.data, "datasources");

  return {
    datasourcersQuery: datasourcers,
    OptionDataSourceType,
  };
};
