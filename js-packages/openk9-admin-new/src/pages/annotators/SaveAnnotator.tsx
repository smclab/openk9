import {
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  CustomSelectRelationsOneToOne,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  combineErrorMessages,
  fromFieldValidators,
  useForm,
  useToast,
} from "@components/Form";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  AnnotatorType,
  Fuzziness,
  useAnnotatorQuery,
  useCreateOrUpdateAnnotatorMutation,
  useDocTypeFieldOptionsAnnotatorsQuery,
} from "../../graphql-generated";
import { AnnotatorQuery, AnnotatorsQuery, DocValuQuery } from "./gql";
import { Box, Button } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";

export function SaveAnnotator() {
  const { annotatorId = "new", view } = useParams();
  const annotatorQuery = useAnnotatorQuery({
    variables: { id: annotatorId as string },
    skip: !annotatorId || annotatorId === "new",
  });
  const { DocTypeQuery, OptionSearchConfig } = useOptions();

  const navigate = useNavigate();
  const [page, setPage] = React.useState(0);
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
    refetchQueries: [AnnotatorQuery, AnnotatorsQuery, DocValuQuery],
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
        docTypeFieldId: {
          id: annotatorQuery.data?.annotator?.docTypeField?.id,
          name: annotatorQuery.data?.annotator?.docTypeField?.name,
        },
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
        docTypeFieldId: data.docTypeFieldId.id === "-1" ? null : data.docTypeFieldId.id,
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

  const loadMoreOptions = async (): Promise<{ value: string; label: string }[]> => {
    if (!DocTypeQuery.data?.options?.pageInfo?.hasNextPage) return [];

    try {
      const response = await DocTypeQuery.fetchMore({
        variables: {
          first: 20,
          cursor: DocTypeQuery.data.options.pageInfo.endCursor,
        },
      });

      const newEdges = response.data?.options?.edges || [];
      const newPageInfo = response.data?.options?.pageInfo;

      if (!newEdges.length || !newPageInfo) {
        console.warn("No new data fetched or pageInfo is missing.");
        return [];
      }

      DocTypeQuery.updateQuery((prev) => ({
        ...prev,
        options: {
          ...prev.options,
          edges: [...(prev.options?.edges || []), ...newEdges],
          pageInfo: newPageInfo,
        },
      }));

      return newEdges
        .map((item) => ({
          value: item?.node?.id || "",
          label: item?.node?.name || "",
        }))
        .filter((option) => option.value && option.label);
    } catch (error) {
      console.error("Error loading more options:", error);
      return [];
    }
  };

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
                    <CustomSelectRelationsOneToOne
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
                    />
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

const useOptions = (): {
  DocTypeQuery: ReturnType<typeof useDocTypeFieldOptionsAnnotatorsQuery>;
  OptionSearchConfig: { value: string; label: string }[];
} => {
  const DocTypeQuery = useDocTypeFieldOptionsAnnotatorsQuery();

  const getOptions = (data: typeof DocTypeQuery.data, key: "options"): { value: string; label: string }[] => {
    return (
      data?.[key]?.edges?.map((item) => ({
        value: item?.node?.id || "",
        label: item?.node?.name || "",
      })) || []
    );
  };

  const OptionSearchConfig = getOptions(DocTypeQuery.data, "options");

  return {
    DocTypeQuery,
    OptionSearchConfig,
  };
};
