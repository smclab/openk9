import {
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  combineErrorMessages,
  fromFieldValidators,
  useForm,
  useToast,
} from "@components/Form";
import { AutocompleteDropdown } from "@components/Form/Select/AutocompleteDropdown";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { isValidId, useDocTypesAnnotators } from "../../utils/RelationOneToOne";
import {
  AnnotatorType,
  Fuzziness,
  useAnnotatorQuery,
  useCreateOrUpdateAnnotatorMutation,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveAnnotator({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { annotatorId = "new", view } = useParams();
  const annotatorQuery = useAnnotatorQuery({
    variables: { id: annotatorId as string },
    skip: !annotatorId || annotatorId === "new",
  });

  const navigate = useNavigate();
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const toast = useToast();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Annotator",
    body: "Are you sure you want to edit this annotator?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/annotator/${annotatorId}`);
    }
  };
  const [createOrUpdateAnnotatorMutate, createOrUpdateannotatorMutation] = useCreateOrUpdateAnnotatorMutation({
    refetchQueries: ["Annotator", "Annotators", "DocTypeFieldValue"],
    onCompleted(data) {
      if (data.annotatorWithDocTypeField?.entity) {
        const isNew = annotatorId === "new" ? "created" : "updated";

        toast({
          title: `Annotator ${isNew}`,
          content: `Annotator has been ${isNew} successfully`,
          displayType: "success",
        });

        navigate(`/annotators/`, { replace: true });
      } else {
        const errorMessage = combineErrorMessages(data.annotatorWithDocTypeField?.fieldValidators || []);
        toast({
          title: `Error`,
          content: errorMessage || "An unknown error occurred.",
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Error creating or updating annotator:", error);
      const isNew = annotatorId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: error.message || `Impossible to ${isNew} Annotator`,
        displayType: "error",
      });
    },
  });

  const extraParams = JSON.parse(annotatorQuery.data?.annotator?.extraParams || "{}") as {
    globalQueryType: string;
    valuesQueryType: string;
    boost: string;
  };

  const annotatorTypeInitialValue = annotatorQuery.data?.annotator?.type;

  const originalValues = {
    fieldName: annotatorQuery.data?.annotator?.fieldName,
    fuziness: annotatorQuery.data?.annotator?.fuziness,
    type: annotatorQuery.data?.annotator?.type,
    description: annotatorQuery.data?.annotator?.description,
    size: annotatorQuery.data?.annotator?.size,
    name: annotatorQuery.data?.annotator?.name,
  };

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        fieldName: "",
        fuziness: Fuzziness.Auto,
        type: AnnotatorType.Stopword,
        description: "",
        size: 1,
        name: "",
        boost: extraParams.boost || boostDefaultValue,
        valuesQueryType: extraParams.valuesQueryType || valuesQueryTypeDefaultValue,
        globalQueryType: extraParams.globalQueryType || globalQueryTypeDefaultValue,
        docTypeFieldId: isValidId(annotatorQuery.data?.annotator?.docTypeField),
      }),
      [annotatorQuery.data?.annotator?.docTypeField?.id, extraParams],
    ),
    originalValues: originalValues,
    isLoading: annotatorQuery.loading || createOrUpdateannotatorMutation.loading,
    onSubmit(data) {
      const isExtraParamsType = [
        AnnotatorType.Autocomplete,
        AnnotatorType.NerAutocomplete,
        AnnotatorType.KeywordAutocomplete,
        AnnotatorType.Ner,
        AnnotatorType.Aggregator,
      ].includes(data.type);

      const variables = {
        id: annotatorId !== "new" ? annotatorId : undefined,
        ...data,
        docTypeFieldId: data?.docTypeFieldId?.id ? data?.docTypeFieldId?.id : null,
        ...(isExtraParamsType
          ? {
              extraParams: JSON.stringify({
                globalQueryType: data.globalQueryType,
                valuesQueryType: data.valuesQueryType,
                boost: data.boost,
              }),
            }
          : {}),
      };

      createOrUpdateAnnotatorMutate({
        variables,
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateannotatorMutation.data?.annotatorWithDocTypeField?.fieldValidators,
    ),
  });

  const isDisabled = (inputName: formInput): boolean => {
    return view === "view" || page === 1 || form.inputProps(inputName).disabled;
  };

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "fieldName" },
          { key: "fuziness" },
          { key: "type" },
          { key: "description" },
          { key: "size" },
          { key: "boost" },
          { key: "valuesQueryType", label: "Values Query Type" },
          { key: "globalQueryType", label: "Global Query Type" },
          { key: "docTypeFieldId", label: "Document Type Field" },
        ],
        label: "Recap Annotator",
      },
    ],
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Annotator"
            description="Create or Edit an Annotator to definire a specific logic to annotates and recognizes user intents
        when a user query is analyzesd.
          You can choose between pre-built annotator types and link to them a specific document type field when allowed."
            id={annotatorId}
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
            id={annotatorId}
            pathBack="/annotators/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextInput
                      label="Field Name"
                      {...form.inputProps("fieldName")}
                      description="Field name used by annotator to get result"
                    />
                    <CustomSelect
                      label="Fuziness"
                      dict={Fuzziness}
                      {...form.inputProps("fuziness")}
                      description="Fuzziness used by annotator to search result"
                    />
                    <CustomSelect
                      label="Type"
                      dict={AnnotatorType}
                      {...form.inputProps("type")}
                      description="Annotator type. Read documentation for more information"
                      onChange={(annotatorType: AnnotatorType) => {
                        form.inputProps("type").onChange(annotatorType);
                        if (annotatorType !== annotatorTypeInitialValue) {
                          form.inputProps("boost").onChange(boostDefaultValue);
                          form.inputProps("valuesQueryType").onChange(valuesQueryTypeDefaultValue);
                          form.inputProps("globalQueryType").onChange(globalQueryTypeDefaultValue);
                        } else {
                          form.inputProps("boost").onChange(extraParams.boost ? extraParams.boost : boostDefaultValue);
                          form
                            .inputProps("valuesQueryType")
                            .onChange(
                              extraParams.valuesQueryType ? extraParams.valuesQueryType : valuesQueryTypeDefaultValue,
                            );
                          form
                            .inputProps("globalQueryType")
                            .onChange(
                              extraParams.globalQueryType ? extraParams.globalQueryType : globalQueryTypeDefaultValue,
                            );
                        }
                      }}
                    />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <NumberInput
                      label="Size"
                      {...form.inputProps("size")}
                      description="Size for result retrieved by annotator"
                    />
                    <AutocompleteDropdown
                      label="Doc type field"
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
                      useOptions={useDocTypesAnnotators}
                    />
                    {/* <CustomSelectRelationsOneToOne
                      options={OptionSearchConfig}
                      label="Doc type field"
                      onChange={(val) => form.inputProps("docTypeFieldId").onChange({ id: val.id, name: val.name })}
                      value={{
                        id: "" + form.inputProps("docTypeFieldId").value.id,
                        name: form.inputProps("docTypeFieldId").value.name || "",
                      }}
                      disabled={page === 1}
                      description="Search Configuration for current bucket"
                      loadMoreOptions={{
                        response: loadMoreOptions,
                        hasNextPage: DocTypeQuery.data?.options?.pageInfo?.hasNextPage || false,
                      }}
                    /> */}
                    {[
                      AnnotatorType.Autocomplete,
                      AnnotatorType.NerAutocomplete,
                      AnnotatorType.KeywordAutocomplete,
                      AnnotatorType.Ner,
                      AnnotatorType.Aggregator,
                    ].includes(form.inputProps("type").value) && (
                      <div>
                        <TextInput label="boost" {...form.inputProps("boost")} disabled={isDisabled("boost")} />
                        <CustomSelect
                          label="valuesQueryType"
                          dict={valuesQueryType}
                          {...form.inputProps("valuesQueryType")}
                          disabled={isDisabled("valuesQueryType")}
                        />
                        <CustomSelect
                          label="globalQueryType"
                          dict={globalQueryType}
                          {...form.inputProps("globalQueryType")}
                          disabled={isDisabled("globalQueryType")}
                        />
                      </div>
                    )}
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
    </ContainerFluid>
  );
}

const boostDefaultValue = "50";

enum valuesQueryType {
  MUST = "MUST",
  SHOULD = "SHOULD",
  MIN_SHOULD_1 = "MIN_SHOULD_1",
  MIN_SHOULD_2 = "MIN_SHOULD_2",
  MIN_SHOULD_3 = "MIN_SHOULD_3",
  MUST_NOT = "MUST_NOT",
  FILTER = "FILTER",
}

const valuesQueryTypeDefaultValue = valuesQueryType.MUST.toString();

enum globalQueryType {
  MUST = "MUST",
  SHOULD = "SHOULD",
  MIN_SHOULD_1 = "MIN_SHOULD_1",
  MIN_SHOULD_2 = "MIN_SHOULD_2",
  MIN_SHOULD_3 = "MIN_SHOULD_3",
  MUST_NOT = "MUST_NOT",
  FILTER = "FILTER",
}

const globalQueryTypeDefaultValue = globalQueryType.MUST.toString();

type formInput =
  | "fieldName"
  | "fuziness"
  | "type"
  | "description"
  | "size"
  | "name"
  | "boost"
  | "valuesQueryType"
  | "globalQueryType";
