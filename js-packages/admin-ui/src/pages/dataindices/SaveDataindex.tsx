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
import {
  CodeInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  fromFieldValidators,
  NumberInput,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { AutocompleteDropdown, AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import { useRestClient } from "@components/queryClient";
import {
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Divider,
  FormControlLabel,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  ChunkType,
  CreateDataIndexMutationVariables,
  useAlignIndexMutation,
  useCreateDataIndexMutation,
  useDataIndexQuery,
  useDataSourcesQuery,
  useDocumentTypesQuery,
  useUpdateIndexSettingsMutation,
} from "../../graphql-generated";
import { AlignmentOutcomesModal } from "@components/IndexAlignment/AlignmentOutcomesModal";
import { useAlignmentRun } from "@components/IndexAlignment/useAlignmentRun";
import { useDataSources, useDocTypeOptions } from "../../utils/RelationOneToOne";

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
  customSettings: string | null;
  embeddingJsonConfig: string | null;
};

/**
 * Indents a JSON document for the editor, leaving it alone when it does not
 * parse: what a dataIndex recorded before this screen existed may be anything.
 */
function prettyJson(value: string | null | undefined): string {
  if (!value) return "{}";

  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}

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

const DATA_INDEX_NOTHING_TO_ALIGN = "This data index has no live index yet, so there was nothing to align.";

const DEFAULT_DATAINDEX_VALUES: DataindexData = {
  dataindexId: "new",
  datasourceId: null,
  name: "",
  description: "",
  knnIndex: false,
  docTypeIds: [],
  docTypeString: [],
  chunkType: ChunkType.ChunkTypeCharacterTextSplitter,
  chunkWindowSize: 0,
  embeddingDocTypeFieldId: null,
  settings: "{}",
  customSettings: "{}",
  embeddingJsonConfig: "{}",
};

export function SaveDataindex({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const { dataindexId = "new", mode, view } = useParams();
  const isNew = dataindexId === "new";
  const navigate = useNavigate();
  const toast = useToast();
  const restClient = useRestClient();

  const [verifyData, setVerifyData] = useState<string>(mode || "edit");
  const [page, setPage] = useState(0);
  const [step, setStep] = useState<"configureStandart" | "configureJson" | "configureMappings">("configureStandart");
  const [settings, setSettings] = useState<string>("{}");
  const [customSettings, setCustomSettings] = useState<string>("{}");
  const [settingsLoading, setSettingsLoading] = useState(false);
  const [settingsError, setSettingsError] = useState<string | null>(null);
  const [mappings, setMappings] = useState<string>("{}");
  const [mappingsLoading, setMappingsLoading] = useState(false);
  const [mappingsError, setMappingsError] = useState<string | null>(null);
  const previousDocTypeIds = useRef<number[]>([]);

  const dataindexQuery = useDataIndexQuery({
    variables: { id: dataindexId },
    skip: isNew,
    errorPolicy: "ignore",
  });

  const documentTypesQuery = useDocumentTypesQuery();

  const chunkTypeOptions = useMemo(
    () =>
      Object.entries(ChunkType)
        .filter(([, value]) => value !== ChunkType.Unrecognized)
        .map(([label, value]) => ({ value, label })),
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
      customSettings: data.customSettings || "{}",
      embeddingJsonConfig: data.embeddingJsonConfig || "{}",
    };
  }, [isNew, dataindexId, dataindexQuery.data]);

  useEffect(() => {
    setSettings(prettyJson(dataindexData.settings));
  }, [dataindexData.settings]);

  useEffect(() => {
    setCustomSettings(prettyJson(dataindexData.customSettings));
  }, [dataindexData.customSettings]);

  const [createOrUpdateDataIndexModelMutate, createOrUpdateDataIndexModel] = useCreateDataIndexMutation({
    onCompleted(data) {
      if (data.dataIndex?.entity) {
        toast({
          title: t("pages.data-indices.created-successfully"),
          content: "",
          displayType: "success",
        });
        navigate("/dataindices/");
      } else {
        toast({
          title: t("common.error"),
          content: combineErrorMessages(data.dataIndex?.fieldValidators),
          displayType: "error",
        });
        setPage(0);
        setStep("configureStandart");
      }
    },
    onError(error) {
      console.error(error);
      toast({
        title: t("pages.data-indices.creation-error"),
        content: t("pages.data-indices.impossible-to-create"),
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
        settings: dataindexData.customSettings,
      }),
      [dataindexData],
    ),
    originalValues: dataindexData,
    isLoading: dataindexQuery.loading || createOrUpdateDataIndexModel.loading,
    onSubmit(values) {
      const variables: CreateDataIndexMutationVariables = {
        // `datasourceId` e' obbligatorio per la mutation: prima veniva impostato
        // solo dentro una if e poteva mancare del tutto.
        datasourceId: String(values.datasourceId?.id ?? ""),
        name: values.name,
        description: values.description,
        docTypeIds: values.docTypeIds,
        settings: values.settings,
        embeddingJsonConfig: values.embeddingJsonConfig,
        knnIndex: values.knnIndex,
      };


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
    getValidationMessages: fromFieldValidators(createOrUpdateDataIndexModel.data?.dataIndex?.fieldValidators),
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

  const getInfo = useCallback(async () => {
    const docTypeIds = form.inputProps("docTypeIds").value;
    if (!docTypeIds?.length) return;

    // an existing dataIndex answers its own index template, which is what it
    // has; the document derived from the docTypes is all there is before one
    if (isNew) {
      try {
        setSettingsLoading(true);
        setSettingsError(null);
        const response = await restClient.dataIndexResource.postApiDatasourceV1DataIndexGetSettingsFromDocTypes({
          docTypeIds,
        });
        setSettings(JSON.stringify(response, null, 2));
      } catch (error) {
        setSettingsError(t("pages.data-indices.settings-fetch-error"));
      } finally {
        setSettingsLoading(false);
      }
    }

    try {
      setMappingsLoading(true);
      setMappingsError(null);
      const mappingsResponse = await restClient.dataIndexResource.postApiDatasourceV1DataIndexGetMappingsFromDocTypes({
        docTypeIds,
      });
      setMappings(JSON.stringify(mappingsResponse, null, 2));
    } catch (error) {
      setMappingsError(t("pages.data-indices.mappings-fetch-error"));
    } finally {
      setMappingsLoading(false);
    }
  }, [form, isNew, restClient, t]);

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
    form.inputProps("settings").onChange(customSettings);
  }, [customSettings]);

  const refetchDataIndex = dataindexQuery.refetch;

  const [updateIndexSettings] = useUpdateIndexSettingsMutation();
  const settingsRun = useAlignmentRun({
    // CodeInput emits its value only when the editor loses focus, so what is
    // sent here is whatever the last blur left. Clicking the button blurs the
    // editor first, which is what keeps this current: a path that applies
    // without moving the focus away would send the previous document.
    run: useCallback(
      async (closeIfNeeded: boolean) => {
        const result = await updateIndexSettings({
          variables: { dataIndexId: dataindexId, settings: customSettings, closeIfNeeded },
        });
        const outcome = result.data?.updateIndexSettings;
        return outcome ? [outcome] : [];
      },
      [updateIndexSettings, dataindexId, customSettings],
    ),
    onApplied: useCallback(() => {
      // the read-only document is the index template, which has just been rewritten
      refetchDataIndex();
    }, [refetchDataIndex]),
  });

  const [alignIndex] = useAlignIndexMutation();
  const alignRun = useAlignmentRun({
    run: useCallback(
      async (closeIfNeeded: boolean) => {
        const result = await alignIndex({ variables: { dataIndexId: dataindexId, closeIfNeeded } });
        const outcome = result.data?.alignIndex;
        return outcome ? [outcome] : [];
      },
      [alignIndex, dataindexId],
    ),
    onApplied: useCallback(() => {
      refetchDataIndex();
    }, [refetchDataIndex]),
  });

  // both write to the same index, so they take turns; the guard is not
  // `disabled`, which would move the focus away mid-action
  const isBusy = settingsRun.isRunning || alignRun.isRunning || settingsRun.isAsking || alignRun.isAsking;
  const hasSettingsOutcome = settingsRun.outcomes !== null || settingsRun.error !== null;

  const recapSections = useMemo(
    () =>
      mappingCardRecap({
        form,
        sections: [
          {
            cell: [
              { key: "name" },
              { key: "description" },
              { key: "datasourceId", label: t("pages.data-indices.datasource") },
              { key: "docTypeIds", label: t("pages.data-indices.document-types") },
              { key: "knnIndex", label: t("pages.data-indices.knn-index") },
              ...(form.inputProps("knnIndex").value
                ? [
                    { key: "chunkType" },
                    { key: "chunkWindowSize" },
                    { key: "embeddingJsonConfig", jsonView: true },
                    { key: "embeddingDocTypeFieldId" },
                  ]
                : []),
            ],
            label: t("pages.data-indices.recap-label"),
          },
          {
            cell: [{ key: "settings", label: t("fields.settings"), jsonView: true }],
            label: t("fields.settings"),
          },
        ],
        valueOverride: {
          datasourceId: form.inputProps("datasourceId").value?.name || "",
          embeddingDocTypeFieldId: form.inputProps("embeddingDocTypeFieldId").value?.name || "",
          mappings,
        },
      }),
    [form, mappings],
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
    return <Typography>{t("common.loading")}</Typography>;
  }

  const DocumentTypeTable = () => (
    <TableContainer sx={{ margin: "16px 0" }} component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell />
            <TableCell>{t("common.name")}</TableCell>
            <TableCell>{t("common.description")}</TableCell>
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
      <TitleEntity
        nameEntity={t("pages.data-indices.entity-name")}
        description=""
        id={dataindexData.dataindexId}
        readOnly={isReadOnly}
      />

      <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
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
                  <TextInput label={t("common.name")} {...form.inputProps("name")} disabled={isReadOnly} />
                  <TextInput
                    label={t("common.description")}
                    {...form.inputProps("description")}
                    disabled={isReadOnly}
                  />
                  <AutocompleteDropdown
                    label={t("fields.associate-datasource")}
                    onChange={(val) => form.inputProps("datasourceId").onChange({ id: val.id, name: val.name })}
                    value={form.inputProps("datasourceId").value || { id: "", name: "" }}
                    disabled={isReadOnly}
                    onClear={() => form.inputProps("datasourceId").onChange(null)}
                    useOptions={useDataSources}
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
                    label={t("fields.enable-knn-index")}
                  />

                  {form.inputProps("knnIndex").value && (
                    <>
                      <AutocompleteDropdownWithOptions
                        label={t("fields.chunk-type")}
                        allowClear={false}
                        disabled={isReadOnly}
                        optionsDefault={chunkTypeOptions}
                        value={(() => {
                          const current =
                            (form.inputProps("chunkType").value as ChunkType) ||
                            ChunkType.ChunkTypeCharacterTextSplitter;
                          const match = chunkTypeOptions.find((o) => o.value === current);
                          return { id: current, name: match?.label || current };
                        })()}
                        onChange={(val) => form.inputProps("chunkType").onChange(val.id as ChunkType)}
                      />
                      <NumberInput
                        label={t("fields.chunk-window-size")}
                        disabled={isReadOnly}
                        id="chunk-window-size"
                        validationMessages={[]}
                        value={form.inputProps("chunkWindowSize").value || 0}
                        onChange={(e) => form.inputProps("chunkWindowSize").onChange(Number(e))}
                      />
                      <Box sx={{ mt: 2 }}>
                        <CodeInput
                          id="code-input"
                          label={t("fields.embedding-json-config")}
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
                        label={t("fields.doc-type")}
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
          {t("common.back")}
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
          {t("common.next-step")}
        </Button>
      </Box>
    </>
  );

  const renderConfigureJson = () => {
    if (settingsLoading) return <Typography>{t("common.loading")}</Typography>;
    if (settingsError) return <Typography color="error">{settingsError}</Typography>;

    return (
      <>
        <Typography variant="h6" mb={2}>
          {t("pages.data-indices.settings-title")}
        </Typography>
        <CodeInput
          id="settings-code-input"
          readonly={false}
          label={t("pages.data-indices.custom-settings")}
          value={customSettings}
          onChange={setCustomSettings}
          language="json"
          validationMessages={[]}
          disabled={false}
          height="300px"
          description="The settings asked for on top of the ones the document types derive, which cannot be set here. The document is additive, the way OpenSearch merges one: a key it does not name is left alone, so deleting a line removes nothing. To put a setting back to its default set it to null."
        />
        {!isNew && (
          <Box display="flex" alignItems="center" gap="10px" mt={1}>
            <Button
              variant="contained"
              color="primary"
              aria-busy={settingsRun.isRunning}
              startIcon={
                settingsRun.isRunning ? (
                  <CircularProgress size={16} color="inherit" aria-label="Apply in progress" />
                ) : undefined
              }
              onClick={() => {
                if (!isBusy) settingsRun.start();
              }}
            >
              Apply settings
            </Button>
            {/* always rendered, so the change of content is announced */}
            <Typography variant="body2" color="text.secondary" role="status" aria-live="polite" sx={{ minHeight: 20 }}>
              {settingsRun.isRunning ? "Writing the settings to the index on OpenSearch…" : ""}
            </Typography>
          </Box>
        )}

        {!isNew && (
          <>
            <Divider sx={{ my: 3 }} />
            <Typography variant="subtitle1" fontWeight={600}>
              Align to the document types
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Writes to the index the mapping the document types of this data index derive. It is a separate action from
              the one above: it does not send the custom settings.
            </Typography>
            <Box display="flex" alignItems="center" gap="10px" mt={1}>
              <Button
                variant="outlined"
                color="primary"
                aria-busy={alignRun.isRunning}
                startIcon={
                  alignRun.isRunning ? (
                    <CircularProgress size={16} color="inherit" aria-label="Alignment in progress" />
                  ) : undefined
                }
                onClick={() => {
                  if (!isBusy) alignRun.start();
                }}
              >
                Align index
              </Button>
              <Typography
                variant="body2"
                color="text.secondary"
                role="status"
                aria-live="polite"
                sx={{ minHeight: 20 }}
              >
                {alignRun.isRunning ? "Writing the mapping to the index on OpenSearch…" : ""}
              </Typography>
            </Box>
            <Divider sx={{ my: 3 }} />
          </>
        )}
        {settingsRun.ConfirmClosing}
        {alignRun.ConfirmClosing}
        {/* the outcome waits for the closing question: two stacked dialogs would bury it */}
        {!settingsRun.isAsking && (
          <AlignmentOutcomesModal
            outcomes={settingsRun.outcomes}
            error={settingsRun.error}
            onClose={settingsRun.dismiss}
            title="Outcome of the custom settings"
            note="An applied outcome means OpenSearch took the settings, and they were recorded and written to the index template."
            emptyMessage={DATA_INDEX_NOTHING_TO_ALIGN}
          />
        )}
        {/* one outcome at a time, or the second dialog buries the first */}
        {!alignRun.isAsking && !hasSettingsOutcome && (
          <AlignmentOutcomesModal
            outcomes={alignRun.outcomes}
            error={alignRun.error}
            onClose={alignRun.dismiss}
            emptyMessage={DATA_INDEX_NOTHING_TO_ALIGN}
          />
        )}
        <Box mt={2}>
          <CodeInput
            id="settings-derived-code-input"
            readonly
            label={isNew ? "Settings derived from the document types" : "Settings of the index template"}
            value={settings}
            onChange={() => {}}
            language="json"
            validationMessages={[]}
            disabled={false}
            height="300px"
            description={
              isNew
                ? "What the selected document types derive. It is shown for reference and is not sent: the index template is built from it."
                : "What the index template declares: the derived settings with the custom ones laid over them. It is not the live index, which also carries what OpenSearch adds by default."
            }
          />
        </Box>
        <Box display="flex" justifyContent="space-between" mt={2}>
          <Button variant="contained" color="primary" onClick={() => setStep("configureStandart")}>
            {t("common.back")}
          </Button>
          {isReadOnly ? (
            <Button variant="contained" color="primary" onClick={() => setStep("configureMappings")}>
              {t("common.next-step")}
            </Button>
          ) : (
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                setVerifyData("editView");
                setPage(1);
              }}
            >
              {t("common.save-and-continue")}
            </Button>
          )}
        </Box>
      </>
    );
  };

  const renderConfigureMappings = () => {
    if (mappingsLoading) return <Typography>{t("common.loading")}</Typography>;
    if (mappingsError) return <Typography color="error">{mappingsError}</Typography>;

    return (
      <>
        <Typography variant="h6" mb={2}>
          {t("pages.data-indices.index-mapping")}
        </Typography>
        <CodeInput
          id="mappings-code-input"
          readonly
          label={t("fields.mappings")}
          value={mappings}
          onChange={() => {}}
          language="json"
          validationMessages={[]}
          disabled={false}
          height="400px"
        />
        <Box display="flex" justifyContent="space-between" mt={2}>
          <Button variant="contained" color="primary" onClick={() => setStep("configureJson")}>
            {t("common.back")}
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
              {t("common.save-and-continue")}
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
      {step === "configureMappings" && isReadOnly && renderConfigureMappings()}
      <Recap
        recapData={recapSections}
        setExtraFab={setExtraFab}
        forceFullScreen={page === 1}
        actions={{
          onBack: () => setPage(0),
          onSubmit: () => form.submit(),
          submitLabel: isNew ? t("entity.create") : t("entity.update"),
          backLabel: t("common.back"),
        }}
      />
    </ContainerFluid>
  );
}
