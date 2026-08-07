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
import { useQuery } from "@apollo/client";
import {
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
} from "@components/Form";
import DataCardManager from "@components/Form/Association/MultiLinkedAssociation/DataCardManager";
import { useToast } from "@components/Form/Form/ToastProvider";
import RefreshOptionsLayout from "@components/Form/Inputs/CheckboxOptionsLayout";
import { TooltipDescription } from "@components/Form/utils";
import { useRestClient } from "@components/queryClient";
import CloseIcon from "@mui/icons-material/Close";
import EditIcon from "@mui/icons-material/Edit";
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Slider,
  TextField,
  Typography,
  useTheme,
} from "@mui/material";
import {
  DynamicFormArray,
  GenerateDynamicForm,
  Template,
} from "@pages/datasources/components/Sections/DataSource/DynamicForm";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import { useMutation } from "@tanstack/react-query";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateSearchConfigMutation, useSearchConfigQuery } from "../../graphql-generated";
import { CombinationTechnique, HybridSearchPipelineDto, NormalizationTechnique } from "../../openapi-generated";
import { sxControl } from "../../utils/styleConfig";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { QueryParserConfig } from "./gql";

interface ConfigureHybridSearchInterface {
  searchConfigId: string;
  normalizationTechnique: NormalizationTechnique | undefined;
  combinationTechnique: CombinationTechnique | undefined;
  weights?: Array<number>;
}

// Runtime option maps for the selects (the generated enums are type-only string unions).
const normalizationTechniqueDict: Record<NormalizationTechnique, NormalizationTechnique> = {
  MIN_MAX: "MIN_MAX",
  L2: "L2",
};
const combinationTechniqueDict: Record<CombinationTechnique, CombinationTechnique> = {
  ARITHMETIC_MEAN: "ARITHMETIC_MEAN",
  GEOMETRIC_MEAN: "GEOMETRIC_MEAN",
  ARMONIC_MEAN: "ARMONIC_MEAN",
};

export function useConfigureHybridSearchMutation({
  searchConfigId,
  normalizationTechnique,
  combinationTechnique,
  weights,
}: ConfigureHybridSearchInterface) {
  const restClient = useRestClient();
  return useMutation(
    async ({
      searchConfigId,
      normalizationTechnique,
      combinationTechnique,
      weights,
    }: ConfigureHybridSearchInterface) => {
      const hybridSearchPipelineDTO: HybridSearchPipelineDto = {
        normalizationTechnique,
        combinationTechnique,
        weights,
      };

      await restClient.searchConfigResource.postApiDatasourceV1SearchConfigConfigureHybridSearch(
        Number(searchConfigId),
        hybridSearchPipelineDTO,
      );
    },
  );
}

export function SaveSearchConfig({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const { searchConfigId = "new", view } = useParams();
  const navigate = useNavigate();
  const [types, setTypes] = React.useState<Array<{ itemLabel: string; itemLabelId: string }>>([]);
  const [activeType, setActiveType] = React.useState<string | undefined | null>();
  const openFormRef = React.useRef<(() => void) | null>(null);
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.search-configs.edit-search-config"),
    body: t("pages.search-configs.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/search-config/${searchConfigId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = searchConfigId === "new";
  const [isHybridSearch, setIsHybridSearch] = React.useState<boolean>(false);
  const [jsonConfigs, setJsonConfigs] = React.useState<string[]>([]);

  const searchConfigQuery = useSearchConfigQuery({
    variables: { id: searchConfigId as string },
    skip: !searchConfigId || searchConfigId === "new",
  });

  const queryParserConfig = useQuery(QueryParserConfig, { fetchPolicy: "cache-and-network" });

  const toast = useToast();
  const initialConfigValue: ConfigureHybridSearchInterface = {
    searchConfigId,
    normalizationTechnique: "MIN_MAX",
    combinationTechnique: "ARITHMETIC_MEAN",
    weights: [0.5, 0.5],
  };

  const [config, setConfig] = useState<ConfigureHybridSearchInterface>(initialConfigValue);
  const [createOrUpdateSearchConfigMutate, createOrUpdateSearchConfigMutation] = useCreateOrUpdateSearchConfigMutation({
    refetchQueries: ["SearchConfig", "Buckets"],
    onCompleted(data) {
      if (data.searchConfigWithQueryParsers?.entity) {
        const isNew = searchConfigId === "new" ? "created" : "updated";
        toast({
          title: isNew === "created" ? t("pages.search-configs.created-title") : t("pages.search-configs.updated-title"),
          content: isNew === "created" ? t("pages.search-configs.created-content") : t("pages.search-configs.updated-content"),
          displayType: "success",
        });
        navigate(`/search-configs/`, { replace: true });
      } else {
        toast({
          title: t("common.error"),
          content: combineErrorMessages(data.searchConfigWithQueryParsers?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = searchConfigId === "new" ? "create" : "update";
      toast({
        title: isNew === "create" ? t("pages.search-configs.create-error-title") : t("pages.search-configs.update-error-title"),
        content: isNew === "create" ? t("pages.search-configs.create-error-content") : t("pages.search-configs.update-error-content"),
        displayType: "error",
      });
    },
  });

  const handleCloseDialog = () => {
    setIsHybridSearch(false);
  };

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        minScore: 0.0,
        minScoreSuggestions: false,
        minScoreSearch: false,
        maxSearchPageFrom: 10000,
        maxSearchPageSize: 200,
        maxTextQueryLength: 0,
        jsonConfig: "",
        queryParserConfig: [],
      }),
      [],
    ),
    originalValues: searchConfigQuery.data?.searchConfig,
    isLoading: searchConfigQuery.loading || createOrUpdateSearchConfigMutation.loading,
    onSubmit(data) {
      createOrUpdateSearchConfigMutate({
        variables: {
          id: searchConfigId !== "new" ? searchConfigId : undefined,
          ...data,
          queryParsersConfig:
            types?.map((type, idx) => ({
              type: type.itemLabelId,
              name: type.itemLabelId,
              jsonConfig: jsonConfigs[idx] || "",
            })) || [],
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateSearchConfigMutation.data?.searchConfigWithQueryParsers?.fieldValidators,
    ),
  });

  const template = React.useMemo(() => {
    const raw = queryParserConfig?.data?.queryParserConfigFormConfigurations;
    if (!raw || searchConfigQuery.loading) return null;
    try {
      const parsed = JSON.parse(raw);
      const mappedType = (parsed as Array<{ type: string }>)?.map((parse) => ({
        itemLabel: parse?.type,
        itemLabelId: parse?.type,
      }));
      setTypes(mappedType);

      const template = parsed?.map((pars: { form: any }) => ({ ...pars?.form }));
      if (jsonConfigs.length === 0 && Array.isArray(template)) {
        const mappedData = template.map((item: Template, idx: number) => {
          const jsonObj: Record<string, any> = {};
          const edge = searchConfigQuery.data?.searchConfig?.queryParserConfigs?.edges?.find((e) => {
            return e?.node?.type?.toLowerCase() === mappedType[idx]?.itemLabelId?.toLowerCase();
          });

          let parsedJson: Record<string, any> = {};
          if (edge?.node?.jsonConfig) {
            try {
              parsedJson = JSON.parse(edge.node.jsonConfig);
            } catch {
              parsedJson = {};
            }
          }
          item.fields.forEach((field) => {
            const valueFromJson = parsedJson[field.name];
            jsonObj[field.name] =
              valueFromJson !== undefined && valueFromJson !== null && valueFromJson !== ""
                ? valueFromJson
                : field.values?.[0]?.value ?? "";
          });
          return JSON.stringify(jsonObj);
        });
        setJsonConfigs(mappedData);
      }

      return { template: template, jsonConfigs: jsonConfigs };
    } catch (e) {
      console.error("Failed to parse form config:", e);
      return null;
    }
  }, [queryParserConfig?.data, searchConfigQuery.loading, searchConfigQuery.data]);

  const DynamicsHook = DynamicFormArray({
    templates: template?.template,
    jsonConfigs: jsonConfigs,
    onChangeJsonConfig: (idx: number, newJson: string) => {
      setJsonConfigs((prev) => {
        const updated = [...prev];
        updated[idx] = newJson;
        return updated;
      });
    },
  });

  if (!searchConfigId && searchConfigQuery.loading) return null;
  const parsedQueryConfig = types.reduce((acc, type, i) => {
    const raw = jsonConfigs[i];
    if (!raw) return acc;

    try {
      const obj = JSON.parse(raw);
      acc[type.itemLabel] = obj;
    } catch {
      acc[type.itemLabel] = raw;
    }

    return acc;
  }, {} as Record<string, any>);

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "minScore", label: t("pages.search-configs.min-score") },
          { key: "minScoreSuggestions", label: t("pages.search-configs.min-score-suggestions") },
          { key: "minScoreSearch", label: t("pages.search-configs.min-score-search") },
          { key: "maxSearchPageFrom", label: t("fields.max-search-page-from") },
          { key: "maxSearchPageSize", label: t("fields.max-search-page-size") },
          { key: "maxTextQueryLength", label: t("fields.max-text-query-length") },
          // { key: "jsonConfig", label: t("fields.json-config") },
        ],
        label: t("fields.search-config"),
      },
      {
        cell: [{ key: "queryParserConfig", label: t("pages.search-configs.query-parser-config") }],
        label: t("pages.search-configs.query-parser"),
      },
      ...(searchConfigId !== "new"
        ? [
            {
              cell: [{ key: "HybridSearch", label: t("pages.search-configs.hybrid-search-config") }],
              label: t("pages.search-configs.hybrid-search"),
            },
          ]
        : []),
    ],
    valueOverride: {
      queryParserConfig: parsedQueryConfig || "",
      HybridSearch: config || "",
    },
  });

  return (
    <>
      <ContainerFluid size="md">
        <>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <TitleEntity
              nameEntity={t("pages.search-configs.entity-name")}
              description={t("pages.search-configs.create-or-edit-a-search-config-and")}
              id={searchConfigId}
            />
            {view === "view" && (
              <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
                {t("common.edit")}
              </Button>
            )}
          </Box>
          <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
            <CreateDataEntity
              form={form}
              page={page}
              id={searchConfigId}
              pathBack="/search-configs/"
              setPage={setPage}
              haveConfirmButton={view ? false : true}
              informationSuggestion={[
                {
                  content: (
                    <div>
                      <TextInput label={t("common.name")} {...form.inputProps("name")} />
                      <TextArea label={t("common.description")} {...form.inputProps("description")} />
                      {/* <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 2 }}> */}
                      <NumberInput
                        label={t("fields.minscore")}
                        {...form.inputProps("minScore")}
                        description={t("pages.search-configs.define-score-threshold-used-to-filter-results")}
                      />
                      <RefreshOptionsLayout title={t("pages.search-configs.min-score")}>
                        <BooleanInput
                          label={t("fields.suggestions")}
                          sxControl={sxControl}
                          {...form.inputProps("minScoreSuggestions")}
                          description={t("pages.search-configs.if-use-configured-min-score-to-filter")}
                        />
                        <BooleanInput
                          label={t("fields.search")}
                          sxControl={sxControl}
                          {...form.inputProps("minScoreSearch")}
                          description={t("pages.search-configs.if-use-configured-min-score-to-filter-2")}
                        />
                      </RefreshOptionsLayout>
                      <NumberInput
                        label={t("fields.max-search-page-from")}
                        {...form.inputProps("maxSearchPageFrom")}
                        description={t("pages.search-configs.maximum-value-allowed-for-the-from-pagination")}
                      />
                      <NumberInput
                        label={t("fields.max-search-page-size")}
                        {...form.inputProps("maxSearchPageSize")}
                        description={t("pages.search-configs.maximum-number-of-results-per-page")}
                      />
                      <NumberInput
                        label={t("fields.max-text-query-length")}
                        {...form.inputProps("maxTextQueryLength")}
                        description={t("pages.search-configs.maximum-length-allowed-for-text-search-queries")}
                      />
                      {/* </Box> */}
                      <TooltipDescription informationDescription="Set Hybrid Search after creation">
                        <Button
                          type="button"
                          color="primary"
                          disabled={searchConfigId === "new"}
                          onClick={() => setIsHybridSearch(true)}
                          children={"Set Hybrid Search"}
                          variant="outlined"
                          sx={{
                            marginTop: 1,
                            borderColor: searchConfigId === "new" ? "rgba(0, 0, 0, 0.26)" : "unset",
                          }}
                        />
                      </TooltipDescription>
                      <DataCardManager
                        options={[]}
                        config={{
                          title: t("pages.search-configs.set-query-parser"),
                          description: activeType ? `${activeType}` : "Query Parser Configuration",
                        }}
                        onAddField={() => {
                          // const remappedData= mappingDynamicForm.map((mapping)=>)
                        }}
                        // onReset={handleReset}
                        onInit={({ openForm }) => {
                          openFormRef.current = openForm;
                        }}
                        row={types.map((type) => ({
                          ...type,
                          customActions: [
                            {
                              icon: <EditIcon fontSize="small" />,
                              // label: t("pages.search-configs.modify"),
                              action: (id) => {
                                setActiveType(id);
                                openFormRef.current?.();
                              },
                            },
                          ],
                        }))}
                      >
                        <Box sx={{ width: "100%", display: "grid", gridColumn: "span 2" }}>
                          {activeType &&
                            (() => {
                              const index = types.findIndex((type) => type.itemLabel === activeType);
                              const dynamicObj = DynamicsHook[index !== -1 ? index : 0];
                              return (
                                <GenerateDynamicForm
                                  templates={dynamicObj?.dynamicTemplate ?? null}
                                  changeValueKey={dynamicObj?.changeValueTemplate ?? (() => {})}
                                  disabled={false}
                                />
                              );
                            })()}
                        </Box>
                      </DataCardManager>
                    </div>
                  ),
                  page: 0,
                  validation: view ? true : false,
                },
                {
                  validation: true,
                },
              ]}
              fieldsControll={["name"]}
            />
          </form>
        </>
        <ConfirmModal />
        <Recap
          recapData={recapSections}
          setExtraFab={setExtraFab}
          forceFullScreen={isRecap}
          actions={{
            onBack: () => setPage(0),
            onSubmit: () => form.submit(),
            submitLabel: isNew ? t("entity.create") : t("entity.update"),
            backLabel: t("common.back"),
          }}
        />
      </ContainerFluid>
      <CustomizedDialogs
        isHybridSearch={isHybridSearch}
        configuration={{ initialConfigValue, config, setConfig }}
        onClose={handleCloseDialog}
      />
    </>
  );
}

interface CustomizedDialogsProps {
  isHybridSearch: boolean;
  configuration: {
    initialConfigValue: ConfigureHybridSearchInterface;
    config: ConfigureHybridSearchInterface;
    setConfig: React.Dispatch<React.SetStateAction<ConfigureHybridSearchInterface>>;
  };
  onClose: () => void; // Funzione per chiudere la modale
}

const CustomizedDialogs: React.FC<CustomizedDialogsProps> = ({ isHybridSearch, configuration, onClose }) => {
  const { t } = useTranslation();
  const theme = useTheme();
  const color = theme.palette.primary.main;
  const { initialConfigValue, config, setConfig } = configuration;

  const [configValidation, setConfigValidation] = useState<{
    normalizationTechnique: string;
    combinationTechnique: string;
  }>({ normalizationTechnique: "", combinationTechnique: "" });

  const handleClose = () => {
    setConfig(initialConfigValue);
    setConfigValidation({ normalizationTechnique: "", combinationTechnique: "" });
    onClose();
  };

  const handleSelectChange = (field: string, e: unknown) => {
    setConfig((prevConfig) => ({
      ...prevConfig,
      [field]: e,
    }));
  };

  const handleSliderChange = (
    event: Event,
    newValue: number | number[], // Il nuovo valore dello slider
  ) => {
    if (typeof newValue === "number") {
      const updatedWeights = [newValue, 1 - newValue]; // Imposta il peso complementare
      setConfig((prevConfig) => ({
        ...prevConfig,
        weights: updatedWeights, // Aggiorna l'array dei pesi
      }));
    }
  };

  // Funzione per gestire il cambiamento del valore nell'input del slider
  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    let newValue = parseFloat(event.target.value);

    // Se il valore Ã¨ un numero valido, aggiorna lo stato
    if (!isNaN(newValue)) {
      // Assicurati che il valore rimanga nel range [0, 1]
      newValue = Math.max(0, Math.min(1, newValue));
      const updatedWeights = [newValue, 1 - newValue]; // Imposta il peso complementare
      setConfig((prevConfig) => ({
        ...prevConfig,
        weights: updatedWeights, // Aggiorna l'array dei pesi
      }));
    }
  };

  const { mutate: configureHybridSearch } = useConfigureHybridSearchMutation(config);
  const toast = useToast();
  const handleSettings = () => {
    const isValid = config.combinationTechnique && config.normalizationTechnique && config.searchConfigId;

    if (isValid) {
      try {
        configureHybridSearch(config, {
          onSuccess: () => {
            toast({
              title: t("pages.search-configs.hybrid-search"),
              content: t("pages.search-configs.hybrid-search-configuration-updated-successfully"),
              displayType: "success",
            });
            handleClose();
          },
          onError: () => {
            toast({
              title: t("common.error"),
              content: t("pages.search-configs.impossible-to-update-hybrid-search-configuration"),
              displayType: "error",
            });
          },
        });
        handleClose();
      } catch (error) {
        showErrorToast("Impossible to set Hybrid Search Config");
        console.error("Error configuring hybrid search:", error);
      }
    } else {
      showErrorToast("Complete all fields");
      validateFields();
    }
  };

  const showErrorToast = (message: string) => {
    toast({
      title: t("common.error"),
      content: message,
      displayType: "error",
    });
  };

  const validateFields = () => {
    const validationMessages: Partial<Record<keyof typeof config, string>> = {};

    if (config.combinationTechnique === undefined) {
      validationMessages.combinationTechnique = "Select combination technique";
    }
    if (config.normalizationTechnique === undefined) {
      validationMessages.normalizationTechnique = "Select normalization technique";
    }

    if (Object.keys(validationMessages).length > 0) {
      setConfigValidation((prevConfig) => ({ ...prevConfig, ...validationMessages }));
    }
  };

  return (
    <>
      {isHybridSearch && (
        <Dialog
          onClose={handleClose}
          aria-labelledby="customized-dialog-title"
          open // Mostra la modale se isHybridSearch Ã¨ true
        >
          <DialogTitle
            sx={{ m: 0, p: 2 }}
            borderRadius="10px 10px 0 0"
            color="white"
            bgcolor={color}
            id="customized-dialog-title"
            fontSize={"unset"}
          >
            {t("pages.search-configs.hybrid-search-config")}
            <IconButton
              aria-label={t("common.close")}
              onClick={handleClose} // Chiudi la modale quando clicchi sull'icona
              sx={(theme) => ({
                position: "absolute",
                right: 8,
                top: 8,
              })}
            >
              <CloseIcon />
            </IconButton>
          </DialogTitle>
          <DialogContent dividers>
            <Box display={"flex"} gap={3} flexDirection={"column"} minWidth={"400px"}>
              <CustomSelect
                label={t("pages.search-configs.normalization-technique")}
                value={config.normalizationTechnique}
                disabled={false}
                validationMessages={[configValidation.normalizationTechnique]}
                dict={normalizationTechniqueDict}
                id={"HybridSearch"}
                onChange={(e) => handleSelectChange("normalizationTechnique", e)}
              />
              <CustomSelect
                label={t("pages.search-configs.combination-technique")}
                value={config.combinationTechnique}
                disabled={false}
                validationMessages={[configValidation.combinationTechnique]}
                dict={combinationTechniqueDict}
                id={"HybridSearch"}
                onChange={(e) => handleSelectChange("combinationTechnique", e)}
              />
              <SliderWithTooltip
                value={(config?.weights && config?.weights[0]) || 0.0} // Usa il primo peso dal state
                onSliderChange={handleSliderChange} // Gestisce il cambio dello slider
                onInputChange={handleInputChange} // Gestisce il cambio dell'input
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button variant="outlined" autoFocus onClick={() => handleSettings()}>
              {t("pages.search-configs.set-changes")}
            </Button>
          </DialogActions>
        </Dialog>
      )}
    </>
  );
};

const SliderWithTooltip = ({
  value,
  onSliderChange,
  onInputChange,
}: {
  value: number;
  onSliderChange: (event: Event, newValue: number | number[]) => void;
  onInputChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
}) => {
  const { t } = useTranslation();
  const theme = useTheme();
  // const color = theme.palette.primary.main;
  return (
    <Box>
      <Typography variant="subtitle1" component="label" htmlFor={"weigth"}>
        {t("pages.search-configs.weigths")}
      </Typography>
      <Box display={"flex"} alignItems={"center"} flex={1} gap={2}>
        <Slider
          value={value}
          onChange={onSliderChange} // Gestore di cambiamento con i 3 parametri
          valueLabelDisplay="auto"
          valueLabelFormat={(value) => value.toFixed(2)} // formato con 2 decimali
          min={0}
          max={1}
          step={0.01}
          sx={
            {
              // color: color, // Colore personalizzato dello slider
            }
          }
        />
        <TextField
          type="number"
          value={value.toFixed(2)} // Mostra il valore con due decimali
          onChange={onInputChange} // Aggiorna lo stato quando l'utente cambia il numero
          inputProps={{
            step: 0.01,
            min: 0,
            max: 1,
          }}
        />
      </Box>
      <Box display={"flex"} flexDirection={"column"} style={{ marginTop: 10 }}>
        <strong>textual value:</strong> {value.toFixed(2)}
        <strong>vector value:</strong> {(1 - value).toFixed(2)}
      </Box>
    </Box>
  );
};
