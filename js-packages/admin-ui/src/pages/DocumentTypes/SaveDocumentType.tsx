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

export function SaveDocumentType() {
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
        docTypeTemplateId: isValidId(documentTypeQuery?.data?.docType?.docTypeTemplate),
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
                      onChange={(val) => form.inputProps("docTypeTemplateId").onChange({ id: val.id, name: val.name })}
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
      </>
      <ConfirmModal />
    </ContainerFluid>
  );
}
