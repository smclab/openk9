import { gql } from "@apollo/client";
import {
  combineErrorMessages,
  ContainerFluid,
  fromFieldValidators,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
} from "@components/Form";
import { GenerateDynamicFieldsMemo } from "@components/Form/Form/GenerateDynamicFields";
import { useToast } from "@components/Form/Form/ToastProvider";
import useTemplate, { createJsonString, NavigationButtons } from "@components/Form/Hook/Template";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateTokenFilterMutation, useTokenFilterQuery } from "../../graphql-generated";
import { Filters } from "./gql";
import { Box, Button } from "@mui/material";
import React from "react";
import { useConfirmModal } from "../../utils/useConfirmModal";

export function SaveTokenFilter() {
  const { tokenFilterId = "new", view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Token Filter",
    body: "Are you sure you want to edit this Token Filter?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/token-filter/${tokenFilterId}`);
    }
  };

  const [page, setPage] = React.useState(0);
  const tokenFilterQuery = useTokenFilterQuery({
    variables: { id: tokenFilterId as string },
    skip: !tokenFilterId || tokenFilterId === "new",
  });

  const toast = useToast();
  const [createOrUpdateTokenFilterMutate, createOrUpdateTokenFilterMutation] = useCreateOrUpdateTokenFilterMutation({
    refetchQueries: ["TokenFilter", "TokenFilters"],
    onCompleted(data) {
      if (data.tokenFilter?.entity) {
        const isNew = tokenFilterId === "new" ? "created" : "updated";
        toast({
          title: `Token Filter ${isNew}`,
          content: `Token Filter has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/token-filters/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.tokenFilter?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = tokenFilterId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Token Filter`,
        displayType: "error",
      });
    },
  });
  const { template, typeSelected, changeType, changeValueKey } = useTemplate({
    templateSelected: Filters,
    jsonConfig: tokenFilterQuery.data?.tokenFilter?.jsonConfig,
    type: tokenFilterQuery.data?.tokenFilter?.type,
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        jsonConfig: "",
        type: "",
      }),
      [],
    ),
    originalValues: tokenFilterQuery.data?.tokenFilter,
    isLoading: tokenFilterQuery.loading || createOrUpdateTokenFilterMutation.loading,
    onSubmit(data) {
      const jsonConfig = createJsonString({ template: template?.value, type: typeSelected });
      createOrUpdateTokenFilterMutate({
        variables: {
          id: tokenFilterId !== "new" ? tokenFilterId : undefined,
          ...data,
          type: typeSelected,
          jsonConfig,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTokenFilterMutation.data?.tokenFilter?.fieldValidators),
  });
  const isRecap = page === 1;
  if (tokenFilterQuery.loading) {
    return <div></div>;
  }
  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Token Filter"
            description="Create or Edit an Token Filter to definire a specific token analysis logic to apply to fields. 
            You can choose between pre-built Token Filters choosing prefer type."
            id={tokenFilterId}
          />
          {view === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              Edit
            </Button>
          )}
        </Box>
        <form style={{ borderStyle: "unset", padding: "0 16px" }}>
          <TextInput label="Name" {...form.inputProps("name")} disabled={isRecap} />
          <TextArea label="Description" {...form.inputProps("description")} disabled={isRecap} />
          <GenerateDynamicFieldsMemo
            templates={Filters}
            type={typeSelected}
            template={template}
            setType={changeType}
            isRecap={isRecap}
            changeValueKey={changeValueKey}
          />
          <NavigationButtons
            isRecap={isRecap}
            submitForm={form.submit}
            goToRecap={() => setPage(1)}
            removeRecap={() => setPage(0)}
            pathBack="/filters"
          />
        </form>
      </>
      <ConfirmModal />
    </ContainerFluid>
  );
}
