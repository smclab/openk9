import {
  CodeInput,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  CustomSelectRelationsOneToOne,
  NumberInput,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
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
  Stack,
} from "@mui/material";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  ChunkType,
  useCreateDataIndexMutation,
  useDataIndexQuery,
  useDataSourcesQuery,
  useDocumentTypesQuery,
} from "../../graphql-generated";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import { AutocompleteDropdown } from "@components/Form/Select/AutocompleteDropdown";
import { useDocTypeOptions } from "../../utils/RelationOneToOne";
import { useOptions } from "@pages/SuggestionCategories";

export type DataindexData = {
  dataindexId: string;
  datasourceId: { id: string; name: string } | null;
  name: string;
  description: string;
  knnIndex: boolean;
  docTypeIds: number[];
  docTypeString: string[];
  chunkType: string | null;
  chunkWindowSize: number | null;
  embeddingDocTypeFieldId: { id: string; name: string } | null;
  settings: string | null;
  embeddingJsonConfig: string | null;
};

export const useOptionsDataSource = () => {
  const datasourcersQuery = useDataSourcesQuery();

  const OptionDataSourceType = useMemo(
    () =>
      datasourcersQuery.data?.datasources?.edges?.map((item) => ({
        value: item?.node?.id || "",
        label: item?.node?.name || "",
      })) || [],
    [datasourcersQuery.data],
  );

  return {
    datasourcersQuery,
    OptionDataSourceType,
  };
};

const DEFAULT_DATAINDEX_VALUES: DataindexData = {
  dataindexId: "new",
  datasourceId: null,
  name: "",
  description: "",
  knnIndex: false,
  docTypeIds: [],
  docTypeString: [],
  chunkType: ChunkType.ChunkTypeCharacterTextSplitter,
  chunkWindowSize: 2000,
  embeddingDocTypeFieldId: null,
  settings: "{}",
  embeddingJsonConfig: "{}",
};

export function SaveDataindex({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { dataindexId = "new", mode, view } = useParams();
  const isNew = dataindexId === "new";
  const navigate = useNavigate();
  const toast = useToast();
  const restClient = useRestClient();

  const [verifyData, setVerifyData] = useState<string>(mode || "edit");
  const [page, setPage] = useState(0);
  const [step, setStep] = useState<"configureStandart" | "configureJson">("configureStandart");
  const [settings, setSettings] = useState<string>("{}");
  const [settingsLoading, setSettingsLoading] = useState(false);
  const [settingsError, setSettingsError] = useState<string | null>(null);
  const previousDocTypeIds = useRef<number[]>([]);

  const dataindexQuery = useDataIndexQuery({
    variables: { id: dataindexId },
    skip: isNew,
    errorPolicy: "ignore",
  });

  const { datasourcersQuery, OptionDataSourceType } = useOptionsDataSource();
  const documentTypesQuery = useDocumentTypesQuery();

  const chunkTypeDict = useMemo(
    () => Object.fromEntries(Object.entries(ChunkType).filter(([, value]) => value !== ChunkType.Unrecognized)),
    [],
  );

  const dataindexData = useMemo<DataindexData>(() => {
    if (isNew) {
      return { ...DEFAULT_DATAINDEX_VALUES, dataindexId };
    }

    const data = dataindexQuery.data?.dataIndex;
    if (!data) return { ...DEFAULT_DATAINDEX_VALUES, dataindexId };

    return {
      dataindexId,
      datasourceId: data.datasource ? { id: data.datasource.id || "", name: data.datasource.name || "" } : null,
      name: data.name || "",
      description: data.description || "",
      knnIndex: data.knnIndex || false,
      docTypeIds:
        data.docTypes?.edges
          ?.map((doc) => (doc?.node?.id ? Number(doc.node.id) : null))
          .filter((id): id is number => id !== null) || [],
      docTypeString: data.docTypes?.edges?.map((doc) => doc?.node?.name).filter((name): name is string => !!name) || [],
      chunkType: data.chunkType || ChunkType.ChunkTypeCharacterTextSplitter,
      chunkWindowSize: data.chunkWindowSize ?? 2000,
      embeddingDocTypeFieldId: data.embeddingDocTypeField
        ? { id: data.embeddingDocTypeField.id || "", name: data.embeddingDocTypeField.name || "" }
        : null,
      settings: data.settings || "{}",
      embeddingJsonConfig: data.embeddingJsonConfig || "{}",
    };
  }, [isNew, dataindexId, dataindexQuery.data]);

  useEffect(() => {
    setSettings(dataindexData.settings || "{}");
  }, [dataindexData.settings]);

  const [createOrUpdateDataIndexModelMutate, createOrUpdateDataIndexModel] = useCreateDataIndexMutation({
    onCompleted(data) {
      if (data.dataIndex?.entity) {
        toast({
          title: "Data Index creato con successo",
          content: "",
          displayType: "success",
        });
        navigate("/dataindices/");
      } else {
        toast({
          title: "Errore",
          content: "",
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error(error);
      toast({
        title: "Errore nella creazione",
        content: "Impossibile creare il Data Index Model",
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: useMemo(
      () => ({
        name: dataindexData.name,
        description: dataindexData.description,
        datasourceId: dataindexData.datasourceId,
        docTypeIds: dataindexData.docTypeIds,
        knnIndex: dataindexData.knnIndex,
        chunkType: dataindexData.chunkType,
        chunkWindowSize: dataindexData.chunkWindowSize,
        embeddingJsonConfig: dataindexData.embeddingJsonConfig,
        embeddingDocTypeFieldId: dataindexData.embeddingDocTypeFieldId,
        settings: dataindexData.settings,
      }),
      [dataindexData],
    ),
    originalValues: dataindexData,
    isLoading: dataindexQuery.loading || createOrUpdateDataIndexModel.loading,
    onSubmit(values) {
      const variables: any = {
        name: values.name,
        description: values.description,
        docTypeIds: values.docTypeIds,
        settings: values.settings,
        embeddingJsonConfig: values.embeddingJsonConfig,
        knnIndex: values.knnIndex,
      };

      if (values.datasourceId?.id && Number(values.datasourceId.id) > 0) {
        variables.datasourceId = Number(values.datasourceId.id);
      }

      if (values.embeddingDocTypeFieldId?.id) {
        variables.embeddingDocTypeFieldId = values.embeddingDocTypeFieldId.id;
      }

      if (values.chunkType) {
        variables.chunkType = values.chunkType as ChunkType;
      }

      if (values.chunkWindowSize != null) {
        variables.chunkWindowSize = values.chunkWindowSize;
      }

      createOrUpdateDataIndexModelMutate({ variables });
    },
  });

  const documentTypesSelected = useMemo(
    () =>
      documentTypesQuery.data?.docTypes?.edges?.map((edge) =>
        form.inputProps("docTypeIds").value?.includes(Number(edge?.node?.id)),
      ) || [],
    [documentTypesQuery.data, form.inputProps("docTypeIds").value],
  );

  const resetOptionalFields = useCallback(() => {
    form.inputProps("chunkWindowSize").onChange(null);
    form.inputProps("embeddingJsonConfig").onChange(null);
    form.inputProps("chunkType").onChange(null);
  }, [form]);

  const loadMoreOptionsDataSource = useCallback(async (): Promise<{ value: string; label: string }[]> => {
    if (!datasourcersQuery.data?.datasources?.pageInfo?.hasNextPage) return [];

    try {
      const response = await datasourcersQuery.fetchMore({
        variables: {
          after: datasourcersQuery.data.datasources.pageInfo.endCursor,
        },
      });

      const newEdges = response.data?.datasources?.edges || [];
      const newPageInfo = response.data?.datasources?.pageInfo;

      if (!newEdges.length || !newPageInfo) return [];

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
      console.error("Errore nel caricamento delle opzioni datasource:", error);
      return [];
    }
  }, [datasourcersQuery]);

  const getInfo = useCallback(async () => {
    const docTypeIds = form.inputProps("docTypeIds").value;
    if (!docTypeIds?.length) return;

    try {
      setSettingsLoading(true);
      setSettingsError(null);
      const response = await restClient.dataIndexResource.postApiDatasourceV1DataIndexGetSettingsFromDocTypes({
        docTypeIds,
      });
      setSettings(JSON.stringify(response, null, 2));
    } catch (error) {
      setSettingsError("Errore nel recupero delle impostazioni.");
    } finally {
      setSettingsLoading(false);
    }
  }, [form, restClient]);

  useEffect(() => {
    const currentDocTypeIds = form.inputProps("docTypeIds").value || [];

    if (JSON.stringify(previousDocTypeIds.current) !== JSON.stringify(currentDocTypeIds)) {
      if (currentDocTypeIds.length > 0) {
        getInfo();
      }
      previousDocTypeIds.current = currentDocTypeIds;
    }
  }, [form.inputProps("docTypeIds").value, getInfo]);

  useEffect(() => {
    form.inputProps("settings").onChange(settings);
  }, [settings]);

  const recapSections = useMemo(
    () =>
      mappingCardRecap({
        form: form as any,
        sections: [
          {
            cell: [
              { key: "name" },
              { key: "description" },
              { key: "datasourceId", label: "Datasource" },
              { key: "docTypeIds", label: "Document Types" },
              { key: "knnIndex", label: "KNN Index" },
              ...(form.inputProps("knnIndex").value
                ? [
                    { key: "chunkType" },
                    { key: "chunkWindowSize" },
                    { key: "embeddingJsonConfig", jsonView: true },
                    { key: "embeddingDocTypeFieldId" },
                  ]
                : []),
            ],
            label: "Recap Data Index",
          },
        ],
        valueOverride: {
          datasourceId: form.inputProps("datasourceId").value?.name || "",
          embeddingDocTypeFieldId: form.inputProps("embeddingDocTypeFieldId").value?.name || "",
        },
      }),
    [form],
  );

  const isReadOnly = verifyData === "view";
  const isLoading = dataindexQuery.loading && !isNew;

  const hasDocTypeIds = (form.inputProps("docTypeIds").value ?? []).length > 0;

  const isNextStepDisabled = useMemo(() => {
    const name = !!form.inputProps("name").value;
    const datasourceId = !!form.inputProps("datasourceId").value;
    const docTypeIds = hasDocTypeIds;
    const knnIndex = form.inputProps("knnIndex").value === true;
    const embeddingDocTypeFieldId = !!form.inputProps("embeddingDocTypeFieldId").value;
    return !(name && datasourceId && docTypeIds && (!knnIndex || embeddingDocTypeFieldId));
  }, [
    form.inputProps("name").value,
    form.inputProps("datasourceId").value,
    form.inputProps("knnIndex").value,
    form.inputProps("embeddingDocTypeFieldId").value,
    hasDocTypeIds,
  ]);

  const handleDocTypeToggle = useCallback(
    (index: number, docTypeId: number, checked: boolean) => {
      const updatedDocTypeIds = [...(form.inputProps("docTypeIds").value || [])];

      if (checked) {
        updatedDocTypeIds.push(docTypeId);
      } else {
        const removeIndex = updatedDocTypeIds.indexOf(docTypeId);
        if (removeIndex > -1) {
          updatedDocTypeIds.splice(removeIndex, 1);
        }
      }

      form.inputProps("docTypeIds").onChange(updatedDocTypeIds);
    },
    [form],
  );

  if (isLoading) {
    return <Typography>Caricamento...</Typography>;
  }

  const DocumentTypeTable = () => (
    <TableContainer sx={{ margin: "16px 0" }} component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell />
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
                      checked={documentTypesSelected[index] || false}
                      disabled={isReadOnly}
                      onChange={(e) => handleDocTypeToggle(index, Number(item?.node?.id), e.target.checked)}
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
  );

  const renderConfigureStandard = () => (
    <>
      <TitleEntity nameEntity="Data Index" description="" id={dataindexData.dataindexId} />

      <form style={{ borderStyle: "unset", padding: "0 16px" }}>
        <CreateDataEntity
          form={form}
          page={page}
          id={dataindexId}
          pathBack="/dataindices/"
          setPage={setPage}
          isFooterButton={false}
          haveConfirmButton={view ? false : true}
          informationSuggestion={[
            {
              content: (
                <div>
                  <TextInput label="Name" {...form.inputProps("name")} />
                  <TextInput label="Description" {...form.inputProps("description")} />
                  <CustomSelectRelationsOneToOne
                    options={OptionDataSourceType}
                    label="Associate Datasource"
                    onChange={(val) => form.inputProps("datasourceId").onChange({ id: val.id, name: val.name })}
                    value={form.inputProps("datasourceId").value || { id: "", name: "" }}
                    disabled={isReadOnly}
                    loadMoreOptions={{
                      response: loadMoreOptionsDataSource,
                      hasNextPage: datasourcersQuery.data?.datasources?.pageInfo?.hasNextPage || false,
                    }}
                  />
                  <DocumentTypeTable />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={form.inputProps("knnIndex").value || false}
                        disabled={isReadOnly}
                        onChange={(e) => {
                          if (!e.target.checked) resetOptionalFields();
                          form.inputProps("knnIndex").onChange(e.target.checked);
                        }}
                      />
                    }
                    label="Enable KNN Index"
                  />

                  {form.inputProps("knnIndex").value && (
                    <>
                      <CustomSelect
                        label="Chunk Type"
                        id="chunk-type-select"
                        dict={chunkTypeDict}
                        disabled={isReadOnly}
                        value={
                          (form.inputProps("chunkType").value as ChunkType) || ChunkType.ChunkTypeCharacterTextSplitter
                        }
                        onChange={(e: string) => form.inputProps("chunkType").onChange(e as ChunkType)}
                        validationMessages={[]}
                      />
                      <NumberInput
                        label="Chunk Window Size"
                        disabled={isReadOnly}
                        id="chunk-window-size"
                        validationMessages={[]}
                        value={form.inputProps("chunkWindowSize").value || 0}
                        onChange={(e) => form.inputProps("chunkWindowSize").onChange(Number(e))}
                      />
                      <Box sx={{ mt: 2 }}>
                        <CodeInput
                          id="code-input"
                          label="Embedding Json Config"
                          height="200px"
                          disabled={isReadOnly}
                          readonly={isReadOnly}
                          language="json"
                          onChange={(event) => form.inputProps("embeddingJsonConfig").onChange(event)}
                          value={form.inputProps("embeddingJsonConfig").value || "{}"}
                          validationMessages={[]}
                        />
                      </Box>
                      <AutocompleteDropdown
                        label="Doc Type"
                        onChange={(val) =>
                          form.inputProps("embeddingDocTypeFieldId").onChange({ id: val.id, name: val.name })
                        }
                        value={form.inputProps("embeddingDocTypeFieldId").value || { id: "", name: "" }}
                        disabled={isReadOnly}
                        onClear={() => form.inputProps("embeddingDocTypeFieldId").onChange({ id: "", name: "" })}
                        useOptions={useDocTypeOptions}
                      />
                    </>
                  )}
                </div>
              ),
              page: 0,
              validation: view ? true : false,
            },
            {
              validation: true,
            },
          ]}
          fieldsControll={["name", "embeddingDocTypeFieldId"]}
        />
      </form>

      <Box display="flex" justifyContent="space-between" mt={3} mb={9}>
        <Button
          component={Link}
          to="/dataindices"
          className="btn btn-secondary"
          type="button"
          variant="outlined"
          color="primary"
        >
          Back
        </Button>
        <Button
          variant="contained"
          className={`btn${form.inputProps("name").value ? ` btn-name` : ""}${
            form.inputProps("docTypeIds").value ? " btn-danger" : ""
          }`}
          type="button"
          sx={{
            border: form.inputProps("name").value && form.inputProps("docTypeIds").value ? "1px solid" : "unset",
            borderColor:
              form.inputProps("name").value && form.inputProps("docTypeIds").value ? "rgba(0, 0, 0, 0.26)" : "unset",
          }}
          color="primary"
          disabled={isNextStepDisabled}
          onClick={() => setStep("configureJson")}
        >
          Next Step
        </Button>
      </Box>
    </>
  );

  const renderConfigureJson = () => {
    if (settingsLoading) return <Typography>Caricamento...</Typography>;
    if (settingsError) return <Typography color="error">{settingsError}</Typography>;

    return (
      <>
        {!isReadOnly && <Typography variant="h6">Modifica Impostazioni Data Index</Typography>}
        <CodeInput
          id="settings-code-input"
          readonly={isReadOnly}
          label="Settings"
          value={settings}
          onChange={setSettings}
          language="json"
          validationMessages={[]}
          disabled={false}
          height="400px"
        />
        <Box display="flex" justifyContent="space-between" mt={2}>
          <Button variant="contained" color="primary" onClick={() => setStep("configureStandart")}>
            Back
          </Button>
          {!isReadOnly && (
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                setVerifyData("editView");
                setPage(1);
              }}
            >
              Save and continue
            </Button>
          )}
        </Box>
      </>
    );
  };

  return (
    <ContainerFluid>
      {step === "configureStandart" && renderConfigureStandard()}
      {step === "configureJson" && renderConfigureJson()}
      <Recap
        recapData={recapSections}
        setExtraFab={setExtraFab}
        forceFullScreen={page === 1}
        actions={{
          onBack: () => setPage(0),
          onSubmit: () => form.submit(),
          submitLabel: isNew ? "Create entity" : "Update entity",
          backLabel: "Back",
        }}
      />
    </ContainerFluid>
  );
}
