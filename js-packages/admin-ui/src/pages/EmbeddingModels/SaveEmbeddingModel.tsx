import {
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
  TooltipDescription,
  NumberInput,
  CodeInput,
} from "@components/Form";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateEmbeddingModelMutation, useEmbeddingModelQuery } from "../../graphql-generated";
import { Box, Button, Select, MenuItem, Typography } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

const PROVIDER_OPTIONS = [
  { value: "openai", label: "OpenAI" },
  { value: "ollama", label: "Ollama" },
  { value: "watsonx", label: "IBM WatsonX" },
  { value: "chat_vertex_ai", label: "Chat Vertex AI" },
];

export function SaveEmbeddingModel({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { embeddingModelsId = "new", view } = useParams();
  const navigate = useNavigate();
  const [isCleaning, setIsCleaning] = React.useState(false);
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Embedding Model",
    body: "Are you sure you want to edit this Embedding Model?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/embedding-model/${embeddingModelsId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = embeddingModelsId === "new";
  const [providerModel, setProviderModel] = React.useState<{
    provider: string | null | undefined;
    model: string | null | undefined;
  }>({ provider: "", model: "" });

  const embeddingModelQuery = useEmbeddingModelQuery({
    variables: { id: embeddingModelsId as string },
    skip: !embeddingModelsId || embeddingModelsId === "new",
  });
  // const showModal = useModal();
  const toast = useToast();

  const [createOrUpdateEmbeddingModelsMutate, createOrUpdateEmbeddingModelsMutation] =
    useCreateOrUpdateEmbeddingModelMutation({
      refetchQueries: ["EmbeddingModel", "EmbeddingModels"],
      onCompleted(data) {
        if (data.embeddingModel?.entity) {
          const isNew = embeddingModelsId === "new" ? "created" : "updated";
          toast({
            title: `Embedding Model ${isNew}`,
            content: `Embedding Model has been ${isNew} successfully`,
            displayType: "success",
          });
          navigate(`/embedding-models/`, { replace: true });
        } else {
          toast({
            title: `Error`,
            content: combineErrorMessages(data.embeddingModel?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = embeddingModelsId === "new" ? "create" : "update";
        toast({
          title: `Error ${isNew}`,
          content: `Impossible to ${isNew} Embedding Model`,
          displayType: "error",
        });
      },
    });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        apiKey: "",
        apiUrl: "",
        description: "",
        provider: "openai",
        vectorSize: 1,
        model: "",
        jsonConfig: "{}",
      }),
      [],
    ),
    originalValues: embeddingModelQuery.data?.embeddingModel
      ? {
          ...embeddingModelQuery.data.embeddingModel,
          provider: embeddingModelQuery.data.embeddingModel.providerModel?.provider || "openai",
          model: embeddingModelQuery.data.embeddingModel.providerModel?.model || "",
        }
      : undefined,
    isLoading: embeddingModelQuery.loading || createOrUpdateEmbeddingModelsMutation.loading,
    onSubmit(data) {
      createOrUpdateEmbeddingModelsMutate({
        variables: {
          id: embeddingModelsId !== "new" ? embeddingModelsId : undefined,
          providerModel: { provider: providerModel.provider || "", model: providerModel.model || "" },
          ...data,
        },
      });
    },
  });

  React.useEffect(() => {
    setProviderModel({
      provider: embeddingModelQuery.data?.embeddingModel?.providerModel?.provider || "openai",
      model: embeddingModelQuery.data?.embeddingModel?.providerModel?.model || "",
    });
  }, [embeddingModelQuery]);

  const viewMaskApiKey = React.useMemo(() => {
    return !!((view || embeddingModelsId !== "new") && form.inputProps("apiKey").value);
  }, [view, embeddingModelsId, form.inputProps("apiKey").value]);

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "provider" },
          { key: "model" },
          { key: "apiKey", label: "API Key" },
          { key: "apiUrl", label: "API URL" },
          { key: "vectorSize", label: "Vector Size" },
          { key: "jsonConfig", label: "JSON Config" },
        ],
        label: "Recap Embedding Model",
      },
    ],
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Embedding Model"
            description="Create or Edit a Embedding Model to define hookup to a service exposing features to vectorize your data.
          Define url to service or specify api key in caso of use of services like OpenAi."
            id={embeddingModelsId}
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
            id={embeddingModelsId}
            pathBack="/embedding-models/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <NumberInput label="Vector Size" {...form.inputProps("vectorSize")} isNumber={false} />
                    <Typography variant="h4">Provider</Typography>
                    <Select
                      id="providerId"
                      onChange={(event) => {
                        setProviderModel((value) => ({ ...value, provider: event.target.value }));
                        form.inputProps("provider").onChange(event.target.value);
                      }}
                      value={providerModel.provider || "openai"}
                      disabled={view ? true : false}
                      fullWidth
                    >
                      {PROVIDER_OPTIONS.map((option) => (
                        <MenuItem key={option.value} value={option.value}>
                          {option.label}
                        </MenuItem>
                      ))}
                    </Select>
                    <TextInput
                      label="Model"
                      id="modelId"
                      onChange={(s) => {
                        setProviderModel((value) => ({ ...value, model: s }));
                      }}
                      validationMessages={[]}
                      value={providerModel.model || ""}
                      disabled={view ? true : false}
                    />
                    <TooltipDescription informationDescription="Api key in case of external api service">
                      <TextInput
                        label="Api key"
                        {...form.inputProps("apiKey")}
                        value={
                          viewMaskApiKey && !isCleaning
                            ? maskApiKey(form.inputProps("apiKey").value)
                            : form.inputProps("apiKey").value
                        }
                        disabled={viewMaskApiKey && !isCleaning ? true : false}
                        haveReset={{
                          isVisible: !view && viewMaskApiKey,
                          callback: () => {
                            setIsCleaning(true);
                          },
                        }}
                      />
                    </TooltipDescription>
                    <TooltipDescription informationDescription="Url where Embedding Module is hosted">
                      <TextInput label="Api url" {...form.inputProps("apiUrl")} />
                    </TooltipDescription>
                    <ContainerFluid size="md" style={{ marginRight: 0 }}>
                      <CodeInput
                        id="settings-code-input"
                        readonly={page === 1 || view === "view"}
                        label="Settings"
                        value={form.inputProps("jsonConfig").value}
                        onChange={(value) => form.inputProps("jsonConfig").onChange(value)}
                        language="json"
                        validationMessages={[]}
                        disabled={false}
                        height="400px"
                        tooltip={
                          <TooltipDescription informationDescription="Json config to set up Embedding Model settings" />
                        }
                      />
                    </ContainerFluid>
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
          submitLabel: isNew ? "Create entity" : "Update entity",
          backLabel: "Back",
        }}
      />
    </ContainerFluid>
  );
}

export const maskApiKey = (apiKey: string) => {
  if (!apiKey || apiKey.length <= 4) return apiKey;
  const firstTwo = apiKey.slice(0, 2);
  const lastTwo = apiKey.slice(-2);
  const masked = `${firstTwo}${"*".repeat(apiKey.length - 4)}${lastTwo}`;
  return masked;
};
