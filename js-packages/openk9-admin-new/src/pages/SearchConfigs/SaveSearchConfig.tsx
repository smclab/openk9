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
import { useToast } from "@components/Form/Form/ToastProvider";
import { InformationField } from "@components/Form/utils/informationField";
import { useRestClient } from "@components/queryClient";
import CloseIcon from "@mui/icons-material/Close";
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
import { useMutation } from "@tanstack/react-query";
import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateSearchConfigMutation, useSearchConfigQuery } from "../../graphql-generated";
import { CombinationTechnique, HybridSearchPipelineDTO, NormalizationTechnique } from "../../openapi-generated";
import { SearchConfigQuery } from "./gql";
import { TooltipDescription } from "@components/Form/utils";
import { useConfirmModal } from "../../utils/useConfirmModal";

interface ConfigureHybridSearchInterface {
  searchConfigId: string;
  normalizationTechnique: NormalizationTechnique | undefined;
  combinationTechnique: CombinationTechnique | undefined;
  weights?: Array<number>;
}

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
      const hybridSearchPipelineDTO: HybridSearchPipelineDTO = {
        normalizationTechnique, //: NormalizationTechnique.MIN_MAX, // Scegli il valore appropriato dall'enum - mock
        combinationTechnique, //: CombinationTechnique.ARITHMETIC_MEAN, // Scegli il valore appropriato dall'enum - mock
        weights, //: [1.0, 2.0, 3.0], // Sostituisci con i pesi desiderati
      };

      await restClient.searchConfigResource.postApiDatasourceV1SearchConfigConfigureHybridSearch(
        Number(searchConfigId), // Passa l'ID come primo argomento
        hybridSearchPipelineDTO, // Passa l'oggetto DTO come secondo argomento
      );
    },
  );
}

export function SaveSearchConfig() {
  const { searchConfigId = "new", view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Search Config",
    body: "Are you sure you want to edit this Search Config?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/search-config/${searchConfigId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const [isHybridSearch, setIsHybridSearch] = React.useState<boolean>(false);

  const searchConfigQuery = useSearchConfigQuery({
    variables: { id: searchConfigId as string },
    skip: !searchConfigId || searchConfigId === "new",
  });
  const toast = useToast();
  const [createOrUpdateSearchConfigMutate, createOrUpdateSearchConfigMutation] = useCreateOrUpdateSearchConfigMutation({
    refetchQueries: [SearchConfigQuery],
    onCompleted(data) {
      if (data.searchConfig?.entity) {
        const isNew = searchConfigId === "new" ? "created" : "updated";
        toast({
          title: `Search Config ${isNew}`,
          content: `Search Config has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/search-configs/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.searchConfig?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = searchConfigId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Search Config`,
        displayType: "error",
      });
    },
  });

  const handleCloseDialog = () => {
    setIsHybridSearch(false); // Chiudi la modale
  };

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        minScore: 0.0,
        minScoreSuggestions: false,
        minScoreSearch: false,
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
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateSearchConfigMutation.data?.searchConfig?.fieldValidators),
  });

  if (!searchConfigId) return null;
  return (
    <>
      <ContainerFluid>
        <>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <TitleEntity
              nameEntity="Search config "
              description="Create or Edit a Search Config and define search behavior. 
          Configure specific search terms or how scores are handled and customize how search engine returns results."
              id={searchConfigId}
            />
            {view === "view" && (
              <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
                Edit
              </Button>
            )}
          </Box>
          <form style={{ borderStyle: "unset", padding: "0 16px" }}>
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
                      <TextInput label="Name" {...form.inputProps("name")} />
                      <TextArea label="Description" {...form.inputProps("description")} />
                      <NumberInput
                        label="minScore"
                        {...form.inputProps("minScore")}
                        description="Define score threshold used to filter results after query has been done"
                      />
                      <BooleanInput
                        label="min Score Suggestions"
                        {...form.inputProps("minScoreSuggestions")}
                        description="If use configured min score to filter search results"
                      />
                      <BooleanInput
                        label="min Score Search"
                        {...form.inputProps("minScoreSearch")}
                        description="If use configured min score to filter suggestions"
                      />
                      <TooltipDescription informationDescription="Set Hybrid Search after creation">
                        <Button
                          type="button"
                          color="primary"
                          disabled={searchConfigId === "new" ? true : false}
                          onClick={() => setIsHybridSearch(true)}
                          children={"Set Hybrid Search"}
                          variant="outlined"
                          sx={{
                            marginTop: 1,
                            borderColor: searchConfigId === "new" ? "rgba(0, 0, 0, 0.26)" : "unset",
                          }}
                        />
                      </TooltipDescription>
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
      </ContainerFluid>
      <CustomizedDialogs isHybridSearch={isHybridSearch} searchConfigId={searchConfigId} onClose={handleCloseDialog} />
    </>
  );
}

interface CustomizedDialogsProps {
  isHybridSearch: boolean;
  searchConfigId: string;
  onClose: () => void; // Funzione per chiudere la modale
}

const CustomizedDialogs: React.FC<CustomizedDialogsProps> = ({ isHybridSearch, searchConfigId, onClose }) => {
  const theme = useTheme();
  const color = theme.palette.primary.main;

  const initialConfigValue = {
    combinationTechnique: undefined,
    normalizationTechnique: undefined,
    searchConfigId: searchConfigId,
    weights: [0.5, 0.5],
  };
  const [config, setConfig] = useState<ConfigureHybridSearchInterface>(initialConfigValue);
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
    // Aggiorna lo stato per NormalizationTechnique o CombinationTechnique
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

    // Se il valore è un numero valido, aggiorna lo stato
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
    const isValid = config.combinationTechnique && config.normalizationTechnique && searchConfigId;

    if (isValid) {
      try {
        configureHybridSearch(config);
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
      title: "Error",
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
          open // Mostra la modale se isHybridSearch è true
        >
          <DialogTitle
            sx={{ m: 0, p: 2 }}
            borderRadius="10px 10px 0 0"
            color="white"
            bgcolor={color}
            id="customized-dialog-title"
            fontSize={"unset"}
          >
            Hybrid Search Config
            <IconButton
              aria-label="close"
              onClick={handleClose} // Chiudi la modale quando clicchi sull'icona
              sx={(theme) => ({
                position: "absolute",
                right: 8,
                top: 8,
                // color: "white",
              })}
            >
              <CloseIcon />
            </IconButton>
          </DialogTitle>
          {/* </Box> */}
          <DialogContent dividers>
            <Box display={"flex"} gap={3} flexDirection={"column"} minWidth={"400px"}>
              <CustomSelect
                label={"NormalizationTechnique"}
                value={config.normalizationTechnique}
                disabled={false}
                validationMessages={[configValidation.normalizationTechnique]}
                dict={NormalizationTechnique}
                id={"HybridSearch"}
                onChange={(e) => handleSelectChange("normalizationTechnique", e)}
              />
              <CustomSelect
                label={"CombinationTechnique"}
                value={config.combinationTechnique}
                disabled={false}
                validationMessages={[configValidation.combinationTechnique]}
                dict={CombinationTechnique}
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
              Set Changes
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
  const theme = useTheme();
  // const color = theme.palette.primary.main;
  return (
    <Box>
      <Typography variant="subtitle1" component="label" htmlFor={"weigth"}>
        Weigths
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
