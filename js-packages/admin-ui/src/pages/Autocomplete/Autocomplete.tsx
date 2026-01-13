import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  NumberInput,
  TextInput,
  TitleEntity,
  useForm,
} from "../../components/Form";
import {
  BooleanOperator,
  useAutocompleteQuery,
  useCreateOrUpdateAutocompleteMutation,
  useUnboundDocTypeFieldByAutocompleteQuery,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";

export function SaveAutocomplete() {
  const { autocompletId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const autocompleteQuery = useAutocompleteQuery({
    variables: { id: autocompletId as string },
    skip: !autocompletId || autocompletId === "new",
    fetchPolicy: "network-only",
  });

  const associationsQuery = useUnboundDocTypeFieldByAutocompleteQuery({
    variables: { autocompleteId: autocompletId || "0" },
    skip: !autocompletId || autocompletId === "new",
    fetchPolicy: "network-only",
  });

  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Autocomplete",
    body: "Are you sure you want to edit this Autocomplete?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/autocomplete/${autocompletId}`);
    }
  };
  const toast = useToast();

  const [createOrUpdateAutocompleteMutate, createOrUpdateAutocompleteMutation] = useCreateOrUpdateAutocompleteMutation({
    refetchQueries: ["autocomplete", "autocompletes"],
    onCompleted(data) {
      try {
        const parentId = data.autocomplete?.entity?.id;

        if (!parentId) {
          throw new Error("Name is invalid");
        }
        if (parentId) {
          toast({
            content: "Autocomplete has been created successfully",
            displayType: "success",
            title: "Autocomplete Created",
          });
          navigate(`/autocompletes`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: `An unexpected error occurred`,
          content: `Impossible to ${err.message} autocomplete`,
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      const isNew = autocompletId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Autocomplete`,
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        fuzziness: "",
        minimumShouldMatch: "1",
        name: "",
        operator: "AND" as BooleanOperator,
        resultSize: 10,
        fieldIds: [associationsQuery.data?.unboundDocTypeFieldByAutocomplete?.map((field) => Number(field?.id)) || []],
      }),
      [associationsQuery.data],
    ),
    originalValues: autocompleteQuery?.data?.autocomplete,
    isLoading: autocompleteQuery.loading || createOrUpdateAutocompleteMutation.loading,
    onSubmit(data) {
      createOrUpdateAutocompleteMutate({
        variables: {
          id: autocompletId !== "new" ? autocompletId : undefined,
          autocompleteDTO: {
            ...data,
            fieldIds: [],
          },
        },
      });
    },
    getValidationMessages: fromFieldValidators([]),
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Autocomplete"
            description="Create or Edit a Autocomplete and add to it Token Tabs to create yoy personalized search to perform by tab."
            id={autocompletId}
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
            id={autocompletId}
            pathBack="/autocompletes/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextInput label="Fuzziness" {...form.inputProps("fuzziness")} />
                    <TextInput label="Min Word Length" {...form.inputProps("minimumShouldMatch")} />
                    <NumberInput label="Result Size" {...form.inputProps("resultSize")} />
                    <CustomSelect
                      label={"Operator"}
                      value={form.inputProps("operator").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={BooleanOperator}
                      id={"HybridSearch"}
                      onChange={(e: BooleanOperator) => form.inputProps("operator").onChange(e)}
                    />
                    <MultiAssociationCustomQuery
                      label="Fields"
                      {...form.inputProps("fieldIds")}
                      list={{
                        unassociated: (associationsQuery?.data?.unboundDocTypeFieldByAutocomplete as number[]) ?? [],
                        isLoading: associationsQuery.loading,
                      }}
                      getOptionLabel={(option: any) => option.name || ""}
                      getOptionValue={(option: any) => option.id || ""}
                      disabled={false}
                      isRecap={false}
                      onSelect={() => {}}
                    />
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
  );
}
