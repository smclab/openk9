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
import {
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
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
import { isValidId } from "../../utils/RelationOneToOne";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { DocTypeFieldAutocompleteDropdown } from "./DocTypeFieldAutocompleteDropdown";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveSuggestionCategory({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { suggestionCategoryId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const isNew = suggestionCategoryId === "new";
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
  const isRecap = page === 1;

  const suggestionCategoryQuery = useSuggestionCategoryQuery({
    variables: { id: suggestionCategoryId as string },
    skip: !suggestionCategoryId || suggestionCategoryId === "new",
  });

  const toast = useToast();

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
        docTypeFieldId: isValidId(suggestionCategoryQuery.data?.suggestionCategory?.docTypeField),
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
          docTypeFieldId: isValidId(data.docTypeFieldId)?.id,
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateSuggestionCategoryMutation.data?.suggestionCategoryWithDocTypeField?.fieldValidators,
    ),
  });

  const docTypeField = form.inputProps("docTypeFieldId");
  const numericSuggestionCategoryId = Number(suggestionCategoryId);

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "priority" },
          { key: "multiSelect" },
          { key: "docTypeFieldId", label: "Search Config" },
        ],
        label: "Recap Suggestion Category",
      },
    ],
    valueOverride: {
      docTypeFieldId: form.inputProps("docTypeFieldId").value?.name || "",
    },
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
                    <DocTypeFieldAutocompleteDropdown
                      label="Search Config"
                      disabled={page === 1}
                      suggestionCategoryId={
                        Number.isNaN(numericSuggestionCategoryId) ? null : numericSuggestionCategoryId
                      }
                      value={
                        !docTypeField.value?.id
                          ? undefined
                          : {
                              id: docTypeField.value.id || "",
                              name: docTypeField.value.name || "",
                            }
                      }
                      onChange={(val) => docTypeField.onChange({ id: val.id, name: val.name })}
                      onClear={() => docTypeField.onChange(undefined)}
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

