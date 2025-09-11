import {
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  fromFieldValidators,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { GenerateDynamicFieldsMemo } from "@components/Form/Form/GenerateDynamicFields";
import useTemplate, { createJsonString } from "@components/Form/Hook/Template";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCharFilterQuery, useCreateOrUpdateCharFilterMutation } from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { CharFilters } from "./gql";

export function SaveCharFilter() {
  const { charFilterId = "new", view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Char Filter",
    body: "Are you sure you want to edit this Char Filter?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/char-filter/${charFilterId}`);
    }
  };

  const charFilterQuery = useCharFilterQuery({
    variables: { id: charFilterId as string },
    skip: !charFilterId || charFilterId === "new",
  });
  const [page, setPage] = React.useState(0);
  const toast = useToast();
  const { template, typeSelected, changeType, changeValueKey } = useTemplate({
    templateSelected: CharFilters,
    jsonConfig: charFilterQuery.data?.charFilter?.jsonConfig,
    type: charFilterQuery.data?.charFilter?.type,
  });
  const [createOrUpdateCharFilterMutate, createOrUpdateCharFilterMutation] = useCreateOrUpdateCharFilterMutation({
    refetchQueries: ["CharFilter", "Charfilters"],
    onCompleted(data) {
      if (data.charFilter?.entity) {
        const isNew = charFilterId === "new" ? "created" : "updated";
        toast({
          title: `Char Filter ${isNew}`,
          content: `Char Filter has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/char-filters/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.charFilter?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = charFilterId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Char Filter`,
        displayType: "error",
      });
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        jsonConfig: "{}",
        type: "",
      }),
      [],
    ),
    originalValues: charFilterQuery.data?.charFilter,
    isLoading: charFilterQuery.loading || createOrUpdateCharFilterMutation.loading,
    onSubmit(data) {
      const jsonConfig = createJsonString({ template: template?.value, type: typeSelected });
      createOrUpdateCharFilterMutate({
        variables: {
          id: charFilterId !== "new" ? charFilterId : undefined,
          ...data,
          type: typeSelected,
          jsonConfig,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateCharFilterMutation.data?.charFilter?.fieldValidators),
  });
  const isRecap = page === 1;
  if (charFilterQuery.loading) {
    return <div></div>;
  }

  return (
    <>
      <ContainerFluid>
        <>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <TitleEntity
              nameEntity="Char Filters"
              description="Create or Edit an Char Filter to definire a specific character analysis logic to apply to fields. 
            You can choose between pre-built Char Filters choosing prefer type."
              id={charFilterId}
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
              id={charFilterId}
              pathBack="/char-filters/"
              setPage={setPage}
              haveConfirmButton={view ? false : true}
              informationSuggestion={[
                {
                  content: (
                    <>
                      <TextInput label="Name" {...form.inputProps("name")} disabled={isRecap} />
                      <TextArea label="Description" {...form.inputProps("description")} disabled={isRecap} />
                      <GenerateDynamicFieldsMemo
                        templates={CharFilters}
                        type={typeSelected}
                        template={template}
                        setType={changeType}
                        isRecap={isRecap}
                        changeValueKey={changeValueKey}
                      />
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
        </>
        <ConfirmModal />
      </ContainerFluid>
    </>
  );
}
