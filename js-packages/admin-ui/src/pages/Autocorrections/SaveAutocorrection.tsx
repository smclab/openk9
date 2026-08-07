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
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();
  const { autocorrectionId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = autocorrectionId === "new";
  const autocorrectionQuery = useAutocorrectionValueQuery({
    variables: { id: autocorrectionId as string },
    skip: !autocorrectionId || autocorrectionId === "new",
    fetchPolicy: "network-only",
  });
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.autocorrections.edit-autocorrection"),
    body: t("pages.autocorrections.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
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
            content: t("pages.autocorrections.autocorrection-has-been-created-successfully"),
            displayType: "success",
            title: t("pages.autocorrections.autocorrection-created"),
          });
          navigate(`/autocorrections`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: t("pages.autocorrections.unexpected-error"),
          content: t("pages.autocorrections.impossible-to-action", { action: err.message }),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      toast({
        title: isNew
          ? t("pages.autocorrections.create-error-title")
          : t("pages.autocorrections.update-error-title"),
        content: isNew
          ? t("pages.autocorrections.create-error-content")
          : t("pages.autocorrections.update-error-content"),
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
          { key: "prefixLength", label: t("fields.prefix-lenght") },
          { key: "minWordLength", label: t("fields.min-word-length") },
          { key: "maxEdit", label: t("pages.autocorrections.max-edits") },
          { key: "sort" },
          { key: "suggestMode", label: t("pages.autocorrections.suggest-mode") },
          { key: "enableSearchWithCorrection", label: t("pages.autocorrections.search-with-correction") },
          { key: "docTypeFields", label: t("pages.autocorrections.document-type-field") },
        ],
        label: t("pages.autocorrections.recap-label"),
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
            nameEntity={t("pages.autocorrections.entity-name")}
            description={t("pages.autocorrections.create-or-edit-a-autocorrection-and-add")}
            id={autocorrectionId}
          />
          {view === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              {t("common.edit")}
            </Button>
          )}
        </Box>
        <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
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
                    <TextInput
                      label={t("common.name")}
                      {...form.inputProps("name")}
                      description={t("pages.autocorrections.unique-identifier-of-the-autocorrection-configuration")}
                    />
                    <NumberInput
                      label={t("fields.prefix-lenght")}
                      {...form.inputProps("prefixLength")}
                      description={t("pages.autocorrections.number-of-initial-characters-of-the-input")}
                    />
                    <NumberInput
                      label={t("fields.min-word-length")}
                      {...form.inputProps("minWordLength")}
                      description={t("pages.autocorrections.the-minimum-length-a-suggestion-must-be")}
                    />
                    <NumberInput
                      label={t("fields.max-edits")}
                      {...form.inputProps("maxEdit")}
                      description={t("pages.autocorrections.maximum-levenshtein-edit-distance-allowed-between-the")}
                    />
                    <CustomSelect
                      label={t("pages.autocorrections.sort")}
                      value={form.inputProps("sort").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={SortType}
                      id={"HybridSearch"}
                      onChange={(e: SortType) => form.inputProps("sort").onChange(e)}
                      description={t("pages.autocorrections.order-in-which-candidate-suggestions-are-returned")}
                    />
                    <CustomSelect
                      label={t("pages.autocorrections.suggest-mode")}
                      value={form.inputProps("suggestMode").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={SuggestMode}
                      id={"HybridSearch"}
                      onChange={(e: SuggestMode) => form.inputProps("suggestMode").onChange(e)}
                      description={t("pages.autocorrections.when-suggestions-are-produced-missing-only-for")}
                    />
                    <BooleanInput
                      label={t("fields.search-with-correction")}
                      {...form.inputProps("enableSearchWithCorrection")}
                      description={t("pages.autocorrections.if-enabled-when-a-correction-is-found")}
                    />
                    <AutocompleteDropdown
                      label={t("fields.doc-type-field")}
                      description={t("pages.autocorrections.document-type-field-used-by-the-suggester")}
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
          submitLabel: isNew ? t("entity.create") : t("entity.update"),
          backLabel: t("common.back"),
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
