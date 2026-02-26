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
  ContainerFluid,
  CreateDataEntity,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { AutocompleteDropdown } from "@components/Form/Select/AutocompleteDropdown";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateDocTypeWithTemplateMutation, useDocumentTypeQuery } from "../../graphql-generated";
import { isValidId, useDocTypesTemplates } from "../../utils/RelationOneToOne";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveDocumentType({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { documentTypeId = "new", view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Document Type",
    body: "Are you sure you want to edit this Document Type?",
    labelConfirm: "Edit",
  });
  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/document-type/${documentTypeId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = documentTypeId === "new";
  const documentTypeQuery = useDocumentTypeQuery({
    variables: { id: documentTypeId as string },
    skip: !documentTypeId || documentTypeId === "new",
  });
  const toast = useToast();
  const [createOrUpdateDocumentTypeMutate, createOrUpdateDocumentTypeMutation] =
    useCreateOrUpdateDocTypeWithTemplateMutation({
      refetchQueries: ["DocumentType", "DocumentTypes"],
      onCompleted(data) {
        if (data.docTypeWithTemplate?.entity) {
          if (documentTypeId === "new") {
            toast({ displayType: "success", title: "Document Type Create", content: "" });
          } else toast({ displayType: "info", title: "Document Type Update", content: "" });
          navigate(`/document-types/`, { replace: true });
        }
      },
    });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        docTypeTemplateId: isValidId(documentTypeQuery?.data?.docType?.docTypeTemplate, "doc Type Template"),
      }),
      [documentTypeQuery],
    ),
    originalValues: documentTypeQuery.data?.docType,
    isLoading: documentTypeQuery.loading || createOrUpdateDocumentTypeMutation.loading,
    onSubmit(data) {
      createOrUpdateDocumentTypeMutate({
        variables: {
          id: documentTypeId !== "new" ? documentTypeId : undefined,
          name: data.name,
          description: data.description,
          ...(data?.docTypeTemplateId?.id && {
            docTypeTemplateId: data?.docTypeTemplateId?.id,
          }),
        },
      });
    },
  });
  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [{ key: "name" }, { key: "description" }, { key: "docTypeTemplateId", label: "Document Type Template" }],
        label: "Recap Document Type",
      },
    ],
    valueOverride: {
      docTypeTemplateId: form.inputProps("docTypeTemplateId").value?.name || "",
    },
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Document Type"
            description="Create or Edit a Document Type to define hookup to a service exposing features to vectorize your data.
          Define url to service or specify api key in caso of use of services like OpenAi."
            id={documentTypeId}
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
            id={documentTypeId}
            pathBack="/document-types/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <AutocompleteDropdown
                      label="Document type template"
                      onChange={(val) =>
                        form
                          .inputProps("docTypeTemplateId")
                          .onChange({ title: "doc Type Template", id: val.id, name: val.name })
                      }
                      value={
                        !form?.inputProps("docTypeTemplateId")?.value?.id
                          ? undefined
                          : {
                              id: form?.inputProps("docTypeTemplateId")?.value?.id || "",
                              name: form?.inputProps("docTypeTemplateId")?.value?.name || "",
                            }
                      }
                      onClear={() => form.inputProps("docTypeTemplateId").onChange(undefined)}
                      disabled={page === 1}
                      useOptions={useDocTypesTemplates}
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
      <ConfirmModal />
    </ContainerFluid>
  );
}

