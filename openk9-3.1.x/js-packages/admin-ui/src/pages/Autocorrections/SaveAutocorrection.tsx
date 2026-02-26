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
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import { AutocompleteDropdown } from "@pages/SuggestionCategories/AutocompleateOptionList";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  BooleanInput,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  NumberInput,
  TextInput,
  TitleEntity,
  useForm,
} from "../../components/Form";
import {
  SortType,
  SuggestMode,
  useAutocorrectionValueQuery,
  useDocTypeFieldsQuery,
  useSaveAutocorrectionMutation,
} from "../../graphql-generated";
import { makeUseOptionsHook, UseOptionsHook } from "../../utils/RelationOneToOne";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { autocorrectionsConfigOptions, autocorrectionValue } from "./gql";

export function SaveAutocorrection({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { autocorrectionId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = autocorrectionId === "new" ? "create" : "update";
  const autocorrectionQuery = useAutocorrectionValueQuery({
    variables: { id: autocorrectionId as string },
    skip: !autocorrectionId || autocorrectionId === "new",
    fetchPolicy: "network-only",
  });
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Autocorrection",
    body: "Are you sure you want to edit this Autocorrection?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/autocorrection/${autocorrectionId}`);
    }
  };
  const toast = useToast();

  const [createOrUpdateTabMutate, createOrUpdateTabMutation] = useSaveAutocorrectionMutation({
    refetchQueries: [autocorrectionValue, autocorrectionsConfigOptions],
    onCompleted(data) {
      try {
        const parentId = data.autocorrection?.entity?.id;

        if (!parentId) {
          throw new Error("Name is invalid");
        }
        if (parentId) {
          toast({
            content: "Autocorrection has been created successfully",
            displayType: "success",
            title: "Autocorrection Created",
          });
          navigate(`/autocorrections`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: `An unexpected error occurred`,
          content: `Impossible to ${err.message} Autocorrection`,
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Autocorrection`,
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        suggestMode: SuggestMode.Missing,
        sort: SortType.Score,
        prefixLength: 1,
        minWordLength: 4,
        maxEdit: 2,
        enableSearchWithCorrection: false,
        autocorrectionDocTypeFieldId: { id: undefined, name: undefined },
        docTypeFields: {
          id: autocorrectionQuery.data?.autocorrection?.autocorrectionDocTypeField?.id,
          name: autocorrectionQuery.data?.autocorrection?.autocorrectionDocTypeField?.name,
        },
      }),
      [autocorrectionQuery],
    ),
    originalValues: autocorrectionQuery.data?.autocorrection,
    isLoading: autocorrectionQuery.loading || createOrUpdateTabMutation.loading,
    onSubmit(data) {
      createOrUpdateTabMutate({
        variables: {
          id: autocorrectionId !== "new" ? autocorrectionId : undefined,
          ...data,
          autocorrectionDocTypeFieldId: form?.inputProps("docTypeFields")?.value?.id,
        },
      });
    },
    getValidationMessages: fromFieldValidators([]),
  });

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "prefixLength", label: "Prefix Lenght" },
          { key: "minWordLength", label: "Min Word Length" },
          { key: "maxEdit", label: "Max Edits" },
          { key: "sort" },
          { key: "suggestMode", label: "Suggest Mode" },
          { key: "enableSearchWithCorrection", label: "Search With Correction" },
          { key: "docTypeFields", label: "Document Type Field" },
        ],
        label: "Recap Autocorrection",
      },
    ],
    valueOverride: {
      docTypeFields: form.inputProps("docTypeFields").value.name || "",
    },
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Autocorrection"
            description="Create or Edit a Autocorrection and add to it Token Tabs to create yoy personalized search to perform by tab."
            id={autocorrectionId}
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
            id={autocorrectionId}
            pathBack="/autocorrections/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <NumberInput label="Prefix Lenght" {...form.inputProps("prefixLength")} />
                    <NumberInput label="Min Word Length" {...form.inputProps("minWordLength")} />
                    <NumberInput label="Max edits" {...form.inputProps("maxEdit")} />
                    <CustomSelect
                      label={"Sort"}
                      value={form.inputProps("sort").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={SortType}
                      id={"HybridSearch"}
                      onChange={(e: SortType) => form.inputProps("sort").onChange(e)}
                    />
                    <CustomSelect
                      label={"Suggest Mode"}
                      value={form.inputProps("suggestMode").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={SuggestMode}
                      id={"HybridSearch"}
                      onChange={(e: SuggestMode) => form.inputProps("suggestMode").onChange(e)}
                    />
                    <BooleanInput label="Search with correction" {...form.inputProps("enableSearchWithCorrection")} />
                    <AutocompleteDropdown
                      label="Doc type fields"
                      onChange={(val) => form.inputProps("docTypeFields").onChange({ id: val.id, name: val.name })}
                      value={
                        !form?.inputProps("docTypeFields")?.value?.id
                          ? undefined
                          : {
                              id: form?.inputProps("docTypeFields")?.value?.id || "",
                              name: form?.inputProps("docTypeFields")?.value?.name || "",
                            }
                      }
                      onClear={() => form.inputProps("docTypeFields").onChange({ id: undefined, name: undefined })}
                      disabled={page === 1}
                      useOptions={useOptionAutocomplete}
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

export const useOptionAutocomplete: UseOptionsHook = makeUseOptionsHook({
  useQuery: useDocTypeFieldsQuery,
  connectionKey: "docTypeFields",
  first: 20,
});

