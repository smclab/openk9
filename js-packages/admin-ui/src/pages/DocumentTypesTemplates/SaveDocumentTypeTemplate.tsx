import { availablePresets, transform } from "@babel/standalone";
import {
  CodeInput,
  combineErrorMessages,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Select as SelectMaterial,
  Typography,
} from "@mui/material";
import { EnrichPipelinesOptionsQuery } from "@pages/pipelines/gql";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  TemplateType,
  useAddEnrichItemToEnrichPipelineMutation,
  useCreateOrUpdateDocumentTypeTemplateMutation,
  useDocumentTypeTemplateQuery,
  useEnrichPipelinesValueOptionsQuery,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveDocumentTypeTemplate({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { documentTypeTemplateId = "new", name, view } = useParams();

  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Document Type Template",
    body: "Are you sure you want to edit this Document Type Template?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/document-type-template/${documentTypeTemplateId}`);
    }
  };
  const documentTypeTemplateQuery = useDocumentTypeTemplateQuery({
    variables: { id: documentTypeTemplateId as string },
    skip: !documentTypeTemplateId || documentTypeTemplateId === "new",
  });
  const toast = useToast();
  const [createOrUpdateDocumentTypeTemplateMutate, createOrUpdateDocumentTypeTempalteMutation] =
    useCreateOrUpdateDocumentTypeTemplateMutation({
      refetchQueries: ["DocumentTypeTemplate", "DocumentTypeTemplates"],
      onCompleted(data) {
        if (data.docTypeTemplate?.entity) {
          const isNew = documentTypeTemplateId === "new" ? "created" : "updated";
          toast({
            title: `Document Type Template ${isNew}`,
            content: `Document Type Template has been ${isNew} successfully`,
            displayType: "success",
          });
          navigate(`/document-type-templates/`, { replace: true });
        } else {
          toast({
            title: `Error`,
            content: combineErrorMessages(data.docTypeTemplate?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = documentTypeTemplateId === "new" ? "create" : "update";
        toast({
          title: `Error ${isNew}`,
          content: `Impossible to ${isNew} Document Type Template`,
          displayType: "error",
        });
      },
    });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        templateType: TemplateType.TypescriptSource,
        source: "",
        compiled: "",
      }),
      [],
    ),
    originalValues: documentTypeTemplateQuery.data?.docTypeTemplate,
    isLoading: documentTypeTemplateQuery.loading || createOrUpdateDocumentTypeTempalteMutation.loading,
    onSubmit(data) {
      const { source, compiled } = (() => {
        switch (data.templateType) {
          case TemplateType.JavascriptCompiled: {
            return { source: data.compiled, compiled: data.compiled };
          }
          case TemplateType.JavascriptSource:
          case TemplateType.TypescriptSource: {
            return {
              source: data.source,
              compiled: transpile(data.source) ?? data.compiled,
            };
          }
          default:
            throw new Error();
        }
      })();
      createOrUpdateDocumentTypeTemplateMutate({
        variables: {
          id: documentTypeTemplateId !== "new" ? documentTypeTemplateId : undefined,
          ...data,
          source,
          compiled,
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateDocumentTypeTempalteMutation.data?.docTypeTemplate?.fieldValidators,
    ),
  });

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "templateType", label: "Template Type" },
          ...(form.inputProps("templateType").value === "JAVASCRIPT_SOURCE" ||
          form.inputProps("templateType").value === "TYPESCRIPT_SOURCE"
            ? [{ key: "source" }]
            : [{ key: "compiled" }]),
        ],
        label: "Recap Document Type Template",
      },
    ],
    // valueOverride: {
    //   templateType: form.inputProps("templateType").value,
    // },
  });

  return (
    <Box sx={{ overflowX: "hidden" }}>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Document Type Template"
            description="Create or Edit a Document Type Template to define a render template for specific data. 
          Choose type and editing ti using Openk9 renderer components."
            id={documentTypeTemplateId}
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
            id={documentTypeTemplateId}
            pathBack="/document-type-templates/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      gap: "10px",
                      width: "100%",
                    }}
                  >
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <CustomSelect
                      label="Template Type"
                      dict={TemplateType}
                      {...form.inputProps("templateType")}
                      description={"If template is written in Typescript or Javascript"}
                    />
                    {(() => {
                      switch (form.inputProps("templateType").value) {
                        case TemplateType.TypescriptSource: {
                          return documentTypeTemplateQuery.data?.docTypeTemplate?.templateType ===
                            TemplateType.TypescriptSource ? (
                            <CodeInput
                              label="Source"
                              readonly={page === 1 || !(view === undefined)}
                              language="typescript-react"
                              height="80vh"
                              {...form.inputProps("source")}
                            />
                          ) : (
                            <CodeInput
                              label="Source"
                              readonly={page === 1 || !(view === undefined)}
                              language="typescript-react"
                              height="80vh"
                              {...form.inputProps("source")}
                            />
                          );
                        }
                        case TemplateType.JavascriptSource: {
                          return documentTypeTemplateQuery.data?.docTypeTemplate?.templateType ===
                            TemplateType.JavascriptCompiled ? (
                            <CodeInput
                              label="Source"
                              language="javascript-react"
                              height="80vh"
                              readonly={page === 1 || !(view === undefined)}
                              {...form.inputProps("source")}
                            />
                          ) : (
                            <CodeInput
                              label="Source"
                              language="javascript-react"
                              height="80vh"
                              readonly={page === 1 || !(view === undefined)}
                              {...form.inputProps("source")}
                            />
                          );
                        }
                        case TemplateType.JavascriptCompiled: {
                          return (
                            <CodeInput
                              label="Compiled"
                              readonly={true}
                              language="javascript"
                              height="80vh"
                              {...form.inputProps("compiled")}
                            />
                          );
                        }
                      }
                    })()}
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
      <Recap recapData={recapSections} setExtraFab={setExtraFab} forceFullScreen={isRecap} />
    </Box>
  );
}
export function ModalDocumentTypeAssociation({
  id,
  callbackClose,
  title,
}: {
  id: string | null | undefined;
  callbackClose?(): void;
  title?: string;
}) {
  const pipelinesQuery = useEnrichPipelinesValueOptionsQuery({
    variables: { id: Number(id) },
  });
  const loading = pipelinesQuery.loading;
  const navigate = useNavigate();
  const [open, setOpen] = React.useState<boolean>(true);
  const [item, setItem] = React.useState<string>("");

  const createData =
    pipelinesQuery?.data?.unboundEnrichPipelines?.map((enrich) => {
      return { label: enrich?.name || "", value: enrich?.id || "" };
    }) ?? [];

  const [addMutate, { loading: mutationLoading }] = useAddEnrichItemToEnrichPipelineMutation({
    refetchQueries: [
      "AssociatedEnrichPipelineEnrichItemsQuery",
      "UnassociatedEnrichPipelineEnrichItemsQuery",
      EnrichPipelinesOptionsQuery,
    ],
    onCompleted(data) {
      if (data.addEnrichItemToEnrichPipeline) {
        navigate(`/enrich-items/`, { replace: true });
        if (callbackClose) callbackClose();
      }
    },
  });

  const handleClose = () => {
    setOpen(false);
    if (callbackClose) callbackClose();
  };

  const handleSubmit = () => {
    if (id && item.length > 0) {
      addMutate({
        variables: {
          parentId: item,
          childId: id,
        },
      });
    } else {
      navigate(`/document-type-templates/`, { replace: true });
      if (callbackClose) callbackClose();
    }
  };

  return (
    <>
      {!loading && (
        <Dialog open={open} onClose={handleClose} fullWidth={true}>
          <DialogTitle>
            {title ? (
              title
            ) : (
              <Typography variant="h6">
                <span>Item successfully created</span>
              </Typography>
            )}
          </DialogTitle>
          <DialogContent>
            <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
              <SelectMaterial value={item} onChange={(event) => setItem(event.target.value)} displayEmpty fullWidth>
                {createData.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </SelectMaterial>
            </div>
          </DialogContent>
          <DialogActions>
            <Button color="error" onClick={handleSubmit} disabled={mutationLoading}>
              Associate
            </Button>
          </DialogActions>
        </Dialog>
      )}
    </>
  );
}

function transpile(code: string) {
  const transpiled = transform(code, {
    filename: "file.tsx",
    presets: [availablePresets.env, availablePresets.react, availablePresets.typescript],
  }).code;
  return `\
  var React = window.OpenK9.dependencies.React;
  var rendererComponents = OpenK9.dependencies.SearchFrontend.rendererComponents;
  export const exports = { template };
  ${transpiled}
  `;
}
