import {
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  CustomSelectRelationsOneToOne,
  fromFieldValidators,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  useCreateOrUpdateSuggestionCategoryMutation,
  useDocTypeFieldsQuery,
  useSuggestionCategoryQuery,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import useOptionsSuggestionCategory from "./useOptionsSuggestionCategory";

export function SaveSuggestionCategory() {
  const { suggestionCategoryId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Filter",
    body: "Are you sure you want to edit this Filter?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/suggestion-category/${suggestionCategoryId}`);
    }
  };
  const suggestionCategoryQuery = useSuggestionCategoryQuery({
    variables: { id: suggestionCategoryId as string },
    skip: !suggestionCategoryId || suggestionCategoryId === "new",
  });
  const toast = useToast();
  const OptionDocType = useOptionsSuggestionCategory({ suggestionCategoryId: suggestionCategoryId });
  const [createOrUpdateSuggestionCategoryMutate, createOrUpdateSuggestionCategoryMutation] =
    useCreateOrUpdateSuggestionCategoryMutation({
      refetchQueries: ["SuggestionCategory", "SuggestionCategories"],
      onCompleted(data) {
        if (data.suggestionCategoryWithDocTypeField?.entity) {
          const isNew = suggestionCategoryId === "new" ? "created" : "updated";
          toast({
            title: `Filter ${isNew}`,
            content: `Filter has been ${isNew} successfully`,
            displayType: "success",
          });
          navigate(`/suggestion-categories/`, { replace: true });
        } else {
          toast({
            title: `Error`,
            content: combineErrorMessages(data.suggestionCategoryWithDocTypeField?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = suggestionCategoryId === "new" ? "create" : "update";
        toast({
          title: `Error ${isNew}`,
          content: `Impossible to ${isNew} Filter`,
          displayType: "error",
        });
      },
    });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        priority: 0,
        docTypeFieldId: {
          id: suggestionCategoryQuery.data?.suggestionCategory?.docTypeField?.id || "-1",
          name: suggestionCategoryQuery.data?.suggestionCategory?.docTypeField?.name || "",
        },
        multiSelect: false,
      }),
      [suggestionCategoryQuery.data],
    ),
    originalValues: suggestionCategoryQuery.data?.suggestionCategory,
    isLoading: suggestionCategoryQuery.loading || createOrUpdateSuggestionCategoryMutation.loading,
    onSubmit(data) {
      createOrUpdateSuggestionCategoryMutate({
        variables: {
          id: suggestionCategoryId !== "new" ? suggestionCategoryId : undefined,
          ...data,
          docTypeFieldId: data.docTypeFieldId.id !== "-1" ? data.docTypeFieldId.id : null,
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateSuggestionCategoryMutation.data?.suggestionCategoryWithDocTypeField?.fieldValidators,
    ),
  });
  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Filter"
            description="Create or Edit a Filter to define a search filter.
          Choose between a single o multi select filter and associate to it a specific field to retrieve option for the filter."
            id={suggestionCategoryId}
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
            id={suggestionCategoryId}
            pathBack="/suggestion-categories/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <NumberInput
                      label="Priority"
                      {...form.inputProps("priority")}
                      description="Define priority according to which suggestion cateogories are
        orderder by search frontend during rendering"
                    />
                    <BooleanInput
                      label="Multi Select"
                      {...form.inputProps("multiSelect")}
                      description="If currente Filter is rendered as multi label filter or not"
                    />
                    <CustomSelectRelationsOneToOne
                      options={OptionDocType}
                      label="Doc Type"
                      onChange={(val) => form.inputProps("docTypeFieldId").onChange({ id: val.id, name: val.name })}
                      value={{
                        id: form.inputProps("docTypeFieldId").value.id,
                        name: form.inputProps("docTypeFieldId").value.name || "",
                      }}
                      disabled={page === 1}
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

export const useOptions = (searchText?: string) => {
  const docTypes = useDocTypeFieldsQuery({ variables: { searchText } });

  const getOptions = (data: any, key: "docTypeFields") => {
    return (
      data?.[key]?.edges?.map((item: { node: { id: string; name: string } }) => ({
        value: item?.node?.id || "",
        label: item?.node?.name || "",
      })) || []
    );
  };

  const OptionDocType = getOptions(docTypes.data, "docTypeFields");

  return {
    docTypesQuery: docTypes,
    OptionDocType,
  };
};
