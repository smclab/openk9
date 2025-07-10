import {
  CodeInput,
  ContainerFluid,
  CreateDataEntity,
  TextArea,
  TextInput,
  TitleEntity,
  combineErrorMessages,
  useForm,
  TooltipDescription,
  NumberInput,
  BooleanInput,
} from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateLargeLanguageModelMutation, useLargeLanguageModelQuery } from "../../graphql-generated";
import { LargeLanguageModelCreate, LargeLanguageModelQ, LargeLanguageModelsQuery } from "./gql";
import { Box, Button, Select, MenuItem, Typography } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { maskApiKey } from "@pages/EmbeddingModels";

const PROVIDER_OPTIONS = [
  { value: "openai", label: "OpenAI" },
  { value: "ollama", label: "Ollama" },
  { value: "hugging-face-custom", label: "Hugging Face Custom" },
  { value: "watsonx", label: "IBM WatsonX" },
  { value: "chat_vertex_ai", label: "Chat Vertex AI" },
  { value: "chat_vertex_ai_model_garden", label: "Chat Vertex AI Model Garden" },
];

export function SaveLargeLanguageModel() {
  const { LargeLanguageModelId = "new", view } = useParams();
  const navigate = useNavigate();
  const [isCleaning, setIsCleaning] = React.useState(false);
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Large Language Model",
    body: "Are you sure you want to edit this Large Language Model?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/large-language-model/${LargeLanguageModelId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const embeddingModelQuery = useLargeLanguageModelQuery({
    variables: { id: LargeLanguageModelId as string },
    skip: !LargeLanguageModelId || LargeLanguageModelId === "new",
  });
  const toast = useToast();
  const [createOrUpdateLargeLanguageModelMutate, createOrUpdateLargeLanguageModelMutation] =
    useCreateOrUpdateLargeLanguageModelMutation({
      refetchQueries: [LargeLanguageModelCreate, LargeLanguageModelsQuery, LargeLanguageModelQ],
      onCompleted(data) {
        if (data.largeLanguageModel?.entity) {
          const isNew = LargeLanguageModelId === "new" ? "created" : "updated";
          toast({
            title: `Large Language Model ${isNew}`,
            content: `Large Language Model has been ${isNew} successfully`,
            displayType: "success",
          });
          navigate(`/large-languages-model/`, { replace: true });
        } else {
          toast({
            title: `Error`,
            content: combineErrorMessages(data.largeLanguageModel?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = LargeLanguageModelId === "new" ? "create" : "update";
        toast({
          title: `Error ${isNew}`,
          content: `Impossible to ${isNew} Large Language Model`,
          displayType: "error",
        });
      },
    });

  const [providerModel, setProviderModel] = React.useState<{
    provider: string | null | undefined;
    model: string | null | undefined;
  }>({ provider: "", model: "" });

  React.useEffect(() => {
    setProviderModel({
      provider: embeddingModelQuery.data?.largeLanguageModel?.providerModel?.provider || "openai",
      model: embeddingModelQuery.data?.largeLanguageModel?.providerModel?.model || "",
    });
  }, [embeddingModelQuery]);

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        apiKey: "",
        apiUrl: "",
        contextWindow: 0,
        retrieveCitations: false,
        description: "",
        jsonConfig: "{}",
        provider: "openai",
        model: "",
      }),
      [],
    ),
    originalValues: embeddingModelQuery.data?.largeLanguageModel
      ? {
          ...embeddingModelQuery.data.largeLanguageModel,
          provider: embeddingModelQuery.data.largeLanguageModel.providerModel?.provider || "openai",
          model: embeddingModelQuery.data.largeLanguageModel.providerModel?.model || "",
        }
      : undefined,
    isLoading: embeddingModelQuery.loading || createOrUpdateLargeLanguageModelMutation.loading,
    onSubmit(data) {
      createOrUpdateLargeLanguageModelMutate({
        variables: {
          id: LargeLanguageModelId !== "new" ? LargeLanguageModelId : undefined,
          providerModel: { provider: providerModel.provider || "", model: providerModel.model || "" },
          ...data,
        },
      });
    },
  });
  const viewMaskApiKey = React.useMemo(() => {
    return !!((view || LargeLanguageModelId !== "new") && form.inputProps("apiKey").value);
  }, [view, LargeLanguageModelId, form.inputProps("apiKey").value]);

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
        <TitleEntity
          nameEntity="Large Language Modal"
          description="Create or Edit a Large Languae Model to define hookup to a chat LLM service.
          Define url to service or specify api key in caso of use of services like OpenAi."
          id={LargeLanguageModelId}
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
          id={LargeLanguageModelId}
          pathBack="/large-languages-model/"
          setPage={setPage}
          haveConfirmButton={view ? false : true}
          informationSuggestion={[
            {
              content: (
                <>
                  <ContainerFluid flexColumn>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
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
                    <TooltipDescription informationDescription="Api url in case of service hosted on on premise service">
                      <TextInput label="Api url" {...form.inputProps("apiUrl")} />
                    </TooltipDescription>
                    <NumberInput label="Context Window" {...form.inputProps("contextWindow")} />
                    <BooleanInput label="Retrieve Citations" {...form.inputProps("retrieveCitations")} />
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
                        form.inputProps("model").onChange(s);
                      }}
                      validationMessages={[]}
                      value={providerModel.model || ""}
                      disabled={view ? true : false}
                    />
                  </ContainerFluid>
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
                        <TooltipDescription informationDescription="Json config to set up Large Language Model settings" />
                      }
                    />
                  </ContainerFluid>
                </>
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
      <ConfirmModal />
    </>
  );
}
