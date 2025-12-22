import {
  CodeInput,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  combineErrorMessages,
  fromFieldValidators,
  useForm,
  useToast,
} from "@components/Form";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  BehaviorMergeType,
  BehaviorOnError,
  EnrichItemType,
  useCreateOrUpdateEnrichItemMutation,
  useEnrichItemQuery,
} from "../../graphql-generated";
import { Box, Button } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveEnrichItem({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { enrichItemId = "new", name, view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Enrich Item",
    body: "Are you sure you want to edit this Enrich Item?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/enrich-item/${enrichItemId}`);
    }
  };
  const enrichItemQuery = useEnrichItemQuery({
    variables: { id: enrichItemId as string },
    skip: !enrichItemId || enrichItemId === "new",
    fetchPolicy: "network-only",
  });
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = enrichItemId === "new";
  const toast = useToast();
  const [createOrUpdateEnrichItemMutate, createOrUpdateEnrichItemMutation] = useCreateOrUpdateEnrichItemMutation({
    refetchQueries: ["EnrichItem", "EnrichItems"],
    onCompleted(data) {
      if (data.enrichItem?.entity) {
        const isNew = enrichItemId === "new" ? "created" : "updated";
        toast({
          title: `Enrich Item ${isNew}`,
          content: `Enrich Item has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/enrich-items/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.enrichItem?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = enrichItemId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Enrich Item`,
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: name ?? "",
        description: "",
        type: EnrichItemType.HttpAsync,
        serviceName: name ?? "",
        jsonConfig: "{}",
        script: "",
        behaviorMergeType: BehaviorMergeType.Merge,
        jsonPath: "",
        requestTimeout: 1000,
        behaviorOnError: BehaviorOnError.Skip,
      }),
      [],
    ),
    originalValues: enrichItemQuery.data?.enrichItem,
    isLoading: enrichItemQuery.loading || createOrUpdateEnrichItemMutation.loading,
    onSubmit(data) {
      createOrUpdateEnrichItemMutate({
        variables: {
          id: enrichItemId !== "new" ? enrichItemId : undefined,
          ...data,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateEnrichItemMutation.data?.enrichItem?.fieldValidators),
  });

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "type" },
          { key: "serviceName" },
          { key: "jsonPath" },
          { key: "requestTimeout" },
          { key: "behaviorMergeType" },
          { key: "behaviorOnError" },
        ],
        label: "Recap Enrich Item",
      },
    ],
  });

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
        <TitleEntity
          nameEntity="Enrich Item"
          description="Create or Edit a Enrich Item to define enrichment steps to perform on ingested data.
          Choose between the possibility of hook up external sync/async service or configure internal Groovys script enrichment."
          id={enrichItemId}
        />
        {view === "view" && (
          <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
            Edit
          </Button>
        )}
      </Box>
      <form style={{ borderStyle: "unset", padding: 0 }}>
        <CreateDataEntity
          form={form}
          page={page}
          id={enrichItemId}
          pathBack="/enrich-items/"
          setPage={setPage}
          haveConfirmButton={view ? false : true}
          informationSuggestion={[
            {
              content: (
                <>
                  <ContainerFluid flexColumn>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <NumberInput
                      label="Request Timeout - Milliseconds"
                      {...form.inputProps("requestTimeout")}
                      description={"the value is expressed in milliseconds"}
                    />
                    <TextInput
                      label="Service Name"
                      {...form.inputProps("serviceName")}
                      description={"Url where enrich service listen"}
                    />
                    <TextInput
                      label="Json Path"
                      {...form.inputProps("jsonPath")}
                      description={"Json Path for merging result. To merge entire Json response set $"}
                    />
                    <CustomSelect
                      label="Type"
                      dict={EnrichItemType}
                      {...form.inputProps("type")}
                      description={
                        "Enrich Type. Set Sync/Async for externale Openk9 compatible service or Groovy Script for simple script enrich."
                      }
                    />
                    <CustomSelect
                      label="Behavior Merge Type"
                      dict={BehaviorMergeType}
                      {...form.inputProps("behaviorMergeType")}
                      description={"If merge or replace original message with enrich response"}
                    />
                    <CustomSelect
                      label="Behavior On Error"
                      dict={BehaviorOnError}
                      {...form.inputProps("behaviorOnError")}
                      description={
                        "Behavior in case of error. If Fail, retry and erroo handling flow is performed for message. If Skip message go to next step, ignoring the error"
                      }
                    />
                  </ContainerFluid>
                  <ContainerFluid size="md">
                    <CodeInput
                      language="json"
                      label="Configuration"
                      {...form.inputProps("jsonConfig")}
                      readonly={view === "view" || page === 1}
                      description={
                        "Json configuration for enrich. Insert here specific configuration in case os Sync/Async enrich"
                      }
                    />
                    <CodeInput
                      language="javascript"
                      label="Script"
                      {...form.inputProps("script")}
                      readonly={view === "view" || page === 1}
                      description={
                        "Use it to insert script to be executed in case of Groovy Script enrich, or use it for validation in case of Sync/Async enrich"
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
    </>
  );
}
