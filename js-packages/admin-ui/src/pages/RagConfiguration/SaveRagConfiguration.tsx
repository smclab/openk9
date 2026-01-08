import {
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { NumberInput, TextArea, TextInput } from "@components/Form/Inputs";
import { Box, Button, Checkbox, FormControlLabel } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  RagType,
  useCreateRagConfigMutation,
  useRagConfigurationQuery,
  useUpdateRagConfigurationMutation,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { RagConfigurationQuery } from "./gql";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveRagConfiguration({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { ragConfigId = "new", view } = useParams();
  const [page, setPage] = React.useState<number>(0);
  const isRecap = page === 1;
  const isNew = ragConfigId === "new";
  const navigate = useNavigate();
  const toast = useToast();

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit RAG Configuration",
    body: "Are you sure you want to edit this RAG Configuration?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/rag-configuration/${ragConfigId}`);
    }
  };

  const ragConfigQuery = useRagConfigurationQuery({
    variables: { id: ragConfigId as string },
    skip: !ragConfigId || ragConfigId === "new",
    fetchPolicy: "network-only",
  });

  const [createRagConfigMutate, resultCreateRagConfigMutation] = useCreateRagConfigMutation({
    refetchQueries: ["RagConfigurations", RagConfigurationQuery, "Buckets"],
    onCompleted(data) {
      if (data.createRAGConfiguration?.entity) {
        const isNew = ragConfigId === "new" ? "created" : "updated";
        toast({
          title: `RAG Configuration ${isNew}`,
          content: `RAG Configuration has been ${isNew} successfully`,
          displayType: "success",
        });
        const redirectPath = `/rag-configurations/`;
        navigate(redirectPath, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: fromFieldValidators(data.createRAGConfiguration?.fieldValidators)("") || "Validation error",
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = ragConfigId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} RAG Configuration`,
        displayType: "error",
      });
    },
  });

  const [updateRagConfigMutate, resultUpdateRagConfigMutation] = useUpdateRagConfigurationMutation({
    refetchQueries: ["RagConfigurations", RagConfigurationQuery],
    onCompleted(data) {
      if (data.updateRAGConfiguration?.entity) {
        toast({
          title: `RAG Configuration Updated`,
          content: `RAG Configuration has been updated successfully`,
          displayType: "success",
        });
        const redirectPath = `/rag-configurations/`;
        navigate(redirectPath, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: fromFieldValidators(data.updateRAGConfiguration?.fieldValidators)("") || "Validation error",
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: `Error Update`,
        content: `Impossible to Update RAG Configuration`,
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: ragConfigQuery.data?.ragConfiguration?.name || "",
        description: ragConfigQuery.data?.ragConfiguration?.description || "",
        type: ragConfigQuery.data?.ragConfiguration?.type || RagType.ChatRag,
        reformulate: ragConfigQuery.data?.ragConfiguration?.reformulate || false,
        chunkWindow: ragConfigQuery.data?.ragConfiguration?.chunkWindow || 0,
        rephrasePrompt: ragConfigQuery.data?.ragConfiguration?.rephrasePrompt || "",
        prompt: ragConfigQuery.data?.ragConfiguration?.prompt || "",
        jsonConfig: ragConfigQuery.data?.ragConfiguration?.jsonConfig || "{}",
        ragToolDescription: ragConfigQuery.data?.ragConfiguration?.ragToolDescription || "",
        promptNoRag: ragConfigQuery.data?.ragConfiguration?.promptNoRag || "",
      }),
      [ragConfigQuery.data?.ragConfiguration],
    ),
    originalValues: ragConfigQuery.data?.ragConfiguration,
    isLoading: ragConfigQuery.loading || resultCreateRagConfigMutation.loading || resultUpdateRagConfigMutation.loading,
    onSubmit(data) {
      const isNew = ragConfigId === "new";
      isNew
        ? createRagConfigMutate({
            variables: {
              ...data,
            },
          })
        : updateRagConfigMutate({
            variables: {
              id: ragConfigId,
              ...data,
            },
          });
    },
    getValidationMessages:
      fromFieldValidators(resultCreateRagConfigMutation.data?.createRAGConfiguration?.fieldValidators) ||
      fromFieldValidators(resultUpdateRagConfigMutation.data?.updateRAGConfiguration?.fieldValidators),
  });

  if (ragConfigQuery.loading) return null;

  const selectedType = form.inputProps("type").value;

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "type" },
          { key: "reformulate" },
          { key: "chunkWindow" },
          { key: "rephrasePrompt", label: "Rephrase Prompt" },
          { key: "prompt" },
          { key: "jsonConfig", label: "JSON Config" },
          { key: "ragToolDescription", label: "RAG Tool Description" },
          { key: "promptNoRag", label: "Prompt No RAG" },
        ],
        label: "Recap RAG Configuration",
      },
    ],
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="RAG Configuration"
            description="Create or Edit a RAG Configuration to define how your RAG system will behave."
            id={ragConfigId}
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
            id={ragConfigId}
            pathBack="/rag-configurations/"
            setPage={setPage}
            haveConfirmButton={!view}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput label="Name" {...form.inputProps("name")} disabled={view === "view"} />
                    <TextArea label="Description" {...form.inputProps("description")} disabled={view === "view"} />
                    {ragConfigId === "new" && (
                      <CustomSelect
                        label="Type"
                        dict={RagType}
                        {...form.inputProps("type")}
                        disabled={view === "view" || page === 1}
                        description="Type of RAG Configuration"
                      />
                    )}
                    <>
                      <TextArea
                        label="Prompt"
                        {...form.inputProps("prompt")}
                        disabled={view === "view" || page === 1}
                        description="The main prompt for the RAG system"
                      />
                      <FormControlLabel
                        control={
                          <Checkbox
                            name="Reformulate"
                            checked={form.inputProps("reformulate").value}
                            onChange={(e, checked) => form.inputProps("reformulate").onChange(checked)}
                            disabled={view === "view" || page === 1}
                          />
                        }
                        sx={{ marginLeft: "0", marginRight: "0", marginBottom: "16px" }}
                        label="Reformulate"
                        labelPlacement="start"
                      />
                      <TextArea
                        label="Rephrase Prompt"
                        {...form.inputProps("rephrasePrompt")}
                        disabled={view === "view" || page === 1}
                        description="Prompt used for rephrasing"
                      />

                      <NumberInput
                        label="Chunk Window"
                        {...form.inputProps("chunkWindow")}
                        disabled={view === "view" || page === 1}
                        description="Number of chunks to consider"
                      />

                      <TextArea
                        label="JSON Config"
                        {...form.inputProps("jsonConfig")}
                        disabled={view === "view" || page === 1}
                        description="JSON configuration for the RAG system"
                      />

                      {selectedType === RagType.ChatRagTool && (
                        <>
                          <TextArea
                            label="RAG Tool Description"
                            {...form.inputProps("ragToolDescription")}
                            disabled={view === "view" || page === 1}
                            description="Description of the RAG tool"
                          />

                          <TextArea
                            label="Prompt No RAG"
                            {...form.inputProps("promptNoRag")}
                            disabled={view === "view" || page === 1}
                            description="Prompt to use when RAG is not available"
                          />
                        </>
                      )}
                    </>
                  </>
                ),
                page: 0,
                validation: !!view,
              },
              {
                validation: true,
              },
            ]}
            fieldsControll={["name", "type"]}
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
