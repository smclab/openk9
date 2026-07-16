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
  CustomSelect,
  fromFieldValidators,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
} from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import { AutocompleteDropdown } from "@components/Form/Select/AutocompleteDropdown";
import { Box, Button } from "@mui/material";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { SortingType, useCreateOrUpdateSortingMutation, useSortingQuery } from "../../graphql-generated";
import { isValidId, useDocTypeOptions } from "../../utils/RelationOneToOne";
import { useConfirmModal } from "../../utils/useConfirmModal";

export function SaveSorting({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { sortingId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Sorting",
    body: "Are you sure you want to edit this Sorting?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/sorting/${sortingId}`);
    }
  };
  const isRecap = page === 1;
  const isNew = sortingId === "new";
  const disabled = isRecap || view === "view";
  const sortingQuery = useSortingQuery({
    variables: { id: sortingId as string },
    skip: !sortingId || sortingId === "new",
  });

  const toast = useToast();

  const [createOrUpdateSortingMutate, createOrUpdateSortingMutation] = useCreateOrUpdateSortingMutation({
    refetchQueries: ["Sorting", "Sortings"],
    onCompleted(data) {
      if (data.sortingWithDocTypeField?.entity) {
        const action = sortingId === "new" ? "created" : "updated";
        toast({
          title: `Sorting ${action}`,
          content: `Sorting has been ${action} successfully`,
          displayType: "success",
        });
        navigate(`/sortings/`, {
          replace: true,
        });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.sortingWithDocTypeField?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError() {
      const action = sortingId === "new" ? "create" : "update";
      toast({
        title: `Error ${action}`,
        content: `Impossible to ${action} Sorting`,
        displayType: "error",
      });
    },
  });

  const originalValues = {
    name: sortingQuery.data?.sorting?.name,
    description: sortingQuery.data?.sorting?.description,
    priority: sortingQuery.data?.sorting?.priority,
    type: sortingQuery.data?.sorting?.type,
    defaultSort: sortingQuery.data?.sorting?.defaultSort,
    docTypeFieldId: {
      id: String(sortingQuery.data?.sorting?.docTypeField?.id),
      name: sortingQuery.data?.sorting?.docTypeField?.name || "",
    },
  };

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        priority: 0,
        type: SortingType.Asc,
        defaultSort: false,
        docTypeFieldId: isValidId(sortingQuery.data?.sorting?.docTypeField),
      }),
      [sortingQuery.data?.sorting?.docTypeField],
    ),
    originalValues: originalValues,
    isLoading: sortingQuery.loading || createOrUpdateSortingMutation.loading,
    onSubmit(data) {
      const { docTypeFieldId, ...cleanData } = data;
      const docTypeId = docTypeFieldId?.id;

      createOrUpdateSortingMutate({
        variables: {
          sortingId: sortingId !== "new" ? sortingId : undefined,

          ...cleanData,

          ...(data.docTypeFieldId?.id !== "undefined" && data.docTypeFieldId?.id ? { docTypeFieldId: docTypeId } : {}),
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateSortingMutation.data?.sortingWithDocTypeField?.fieldValidators,
    ),
  });

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "priority" },
          { key: "type", label: "Type" },
          { key: "defaultSort", label: "Default Sort" },
          { key: "docTypeFieldId", label: "Document Type Field" },
        ],
        label: "Recap Sorting",
      },
    ],
    valueOverride: {
      docTypeFieldId: form.inputProps("docTypeFieldId").value || "",
    },
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Sorting"
            description="Create or Edit a Sorting to define how search results are ordered, then add it to a Tab to use it."
            id={sortingId}
          />

          {view === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              Edit
            </Button>
          )}
        </Box>

        <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
          <CreateDataEntity
            form={form}
            page={page}
            id={sortingId}
            pathBack="/sortings/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <NumberInput
                      label="Priority"
                      {...form.inputProps("priority")}
                      description="Priority according to which sortings are ordered"
                    />
                    <CustomSelect
                      label="Type"
                      dict={SortingType}
                      {...form.inputProps("type")}
                      disabled={disabled}
                      description="Sorting direction applied to the results"
                    />
                    <AutocompleteDropdown
                      label="DocType Field"
                      onChange={(val) => form.inputProps("docTypeFieldId").onChange({ id: val.id, name: val.name })}
                      value={
                        !form?.inputProps("docTypeFieldId")?.value?.id
                          ? undefined
                          : {
                            id: form?.inputProps("docTypeFieldId")?.value?.id || "",
                            name: form?.inputProps("docTypeFieldId")?.value?.name || "",
                          }
                      }
                      onClear={() => form.inputProps("docTypeFieldId").onChange(undefined)}
                      disabled={page === 1}
                      useOptions={useDocTypeOptions}
                    />
                    <BooleanInput label="Default Sort" {...form.inputProps("defaultSort")} disabled={disabled} />
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
