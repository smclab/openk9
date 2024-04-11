import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import {
  AnnotatorType,
  Fuzziness,
  useAnnotatorQuery,
  useBindDocTypeFieldToDataSourceMutation,
  useCreateOrUpdateAnnotatorMutation,
  useDocTypeFieldOptionsQuery,
  useDocTypeFieldValueQuery,
  useUnbindDocTypeFieldToDataSourceMutation,
  useAddAnnotatorExtraParamMutation,
} from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, EnumSelect, NumberInput, SearchSelect, CustomButtom } from "./Form";
import { AnnotatorsQuery } from "./Annotators";
import { useToast } from "./ToastProvider";
import { ContainerFluid } from "@clayui/layout";

const AnnotatorQuery = gql`
  query Annotator($id: ID!) {
    annotator(id: $id) {
      id
      fuziness
      size
      type
      description
      name
      fieldName
      docTypeField {
        id
      }
      extraParams {
        key
        value
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateAnnotator(
    $id: ID
    $fieldName: String!
    $fuziness: Fuzziness!
    $type: AnnotatorType!
    $description: String
    $size: Int
    $name: String!
  ) {
    annotator(
      id: $id
      annotatorDTO: { fieldName: $fieldName, fuziness: $fuziness, size: $size, type: $type, description: $description, name: $name }
    ) {
      entity {
        id
        name
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

gql`
  mutation addAnnotatorExtraParam($id: ID!, $key: String, $value: String!) {
    addAnnotatorExtraParam(id: $id, key: $key, value: $value) {
      name
    }
  }
`;

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

export function Annotator() {
  const { annotatorId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const annotatorQuery = useAnnotatorQuery({
    variables: { id: annotatorId as string },
    skip: !annotatorId || annotatorId === "new",
  });
  const [createOrUpdateAnnotatorMutate, createOrUpdateannotatorMutation] = useCreateOrUpdateAnnotatorMutation({
    refetchQueries: [AnnotatorQuery, AnnotatorsQuery],
    onCompleted(data) {
      if (data.annotator?.entity) {
        if (annotatorId === "new") {
          navigate(`/annotators/`, { replace: true });
          showToast({ displayType: "success", title: "Annotator created", content: data.annotator.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Annotator updated", content: data.annotator.entity.name ?? "" });
        }
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Annotator error", content: error.message ?? "" });
    },
  });
  const [addExtraParamMutate, addExtraParamMutation] = useAddAnnotatorExtraParamMutation({
    refetchQueries: [AnnotatorsQuery],
  });

  const boostValue = annotatorQuery.data?.annotator?.extraParams?.find((element: any) => element.key === "boost")?.value;
  const valuesQueryTypeValue = annotatorQuery.data?.annotator?.extraParams?.find(
    (element: any) => element.key === "valuesQueryType"
  )?.value;
  const globalQueryTypeValue = annotatorQuery.data?.annotator?.extraParams?.find(
    (element: any) => element.key === "globalQueryType"
  )?.value;
  const annotatorTypeInitialValue = annotatorQuery.data?.annotator?.type;

  const originalValues = {
    fieldName: annotatorQuery.data?.annotator?.fieldName,
    fuziness: annotatorQuery.data?.annotator?.fuziness,
    type: annotatorQuery.data?.annotator?.type,
    description: annotatorQuery.data?.annotator?.description,
    size: annotatorQuery.data?.annotator?.size,
    name: annotatorQuery.data?.annotator?.name,
    boost: boostValue,
    valuesQueryType: valuesQueryTypeValue,
    globalQueryType: globalQueryTypeValue,
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
        boost: boostDefaultValue,
        valuesQueryType: valuesQueryTypeDefaultValue,
        globalQueryType: globalQueryTypeDefaultValue,
      }),
      []
    ),
    originalValues: originalValues,
    isLoading: annotatorQuery.loading || createOrUpdateannotatorMutation.loading,
    onSubmit(data) {
      createOrUpdateAnnotatorMutate({ variables: { id: annotatorId !== "new" ? annotatorId : undefined, ...data } });

      if (
        data.type === AnnotatorType.Autocomplete ||
        data.type === AnnotatorType.NerAutocomplete ||
        data.type === AnnotatorType.KeywordAutocomplete ||
        data.type === AnnotatorType.Ner ||
        data.type === AnnotatorType.Aggregator
      ) {
        addExtraParamMutate({
          variables: {
            id: annotatorId,
            key: "boost",
            value: data.boost ? data.boost : boostDefaultValue,
          },
        });
        addExtraParamMutate({
          variables: {
            id: annotatorId,
            key: "valuesQueryType",
            value: data.valuesQueryType ? data.valuesQueryType : valuesQueryTypeDefaultValue,
          },
        });
        addExtraParamMutate({
          variables: {
            id: annotatorId,
            key: "globalQueryType",
            value: data.globalQueryType ? data.globalQueryType : globalQueryTypeDefaultValue,
          },
        });
      }
    },
    getValidationMessages: fromFieldValidators(createOrUpdateannotatorMutation.data?.annotator?.fieldValidators),
  });
  return (
    <ContainerFluid>
      <form
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextInput label="Field Name" {...form.inputProps("fieldName")} description="Field name used by annotator to get result" />
        <EnumSelect
          label="Fuziness"
          dict={Fuzziness}
          {...form.inputProps("fuziness")}
          description="Fuzziness used by annotator to search result"
        />
        <EnumSelect
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
              form.inputProps("boost").onChange(boostValue ? boostValue : boostDefaultValue);
              form.inputProps("valuesQueryType").onChange(valuesQueryTypeValue ? valuesQueryTypeValue : valuesQueryTypeDefaultValue);
              form.inputProps("globalQueryType").onChange(globalQueryTypeValue ? globalQueryTypeValue : globalQueryTypeDefaultValue);
            }
          }}
        />
        <TextArea label="Description" {...form.inputProps("description")} />
        <NumberInput label="Size" {...form.inputProps("size")} description="Size for result retrieved by annotator" />
        {annotatorId !== "new" && (
          <form
            onSubmit={(event) => {
              event.preventDefault();
            }}
          >
            <SearchSelect
              label="Document Type Field"
              value={annotatorQuery.data?.annotator?.docTypeField?.id}
              useValueQuery={useDocTypeFieldValueQuery}
              useOptionsQuery={({ variables }) => (useDocTypeFieldOptionsQuery as any)({ variables: { ...variables, annotatorId } })}
              useChangeMutation={useBindDocTypeFieldToDataSourceMutation}
              mapValueToMutationVariables={(documentTypeFieldId) => ({ annotatorId, documentTypeFieldId })}
              useRemoveMutation={useUnbindDocTypeFieldToDataSourceMutation}
              mapValueToRemoveMutationVariables={() => ({
                annotatorId,
                documentTypeFieldId: annotatorQuery.data?.annotator?.docTypeField?.id!,
              })}
              invalidate={() => annotatorQuery.refetch()}
            />
          </form>
        )}
        {(form.inputProps("type").value === AnnotatorType.Autocomplete ||
          form.inputProps("type").value === AnnotatorType.NerAutocomplete ||
          form.inputProps("type").value === AnnotatorType.KeywordAutocomplete ||
          form.inputProps("type").value === AnnotatorType.Ner ||
          form.inputProps("type").value === AnnotatorType.Aggregator) && (
          <div>
            <TextInput label="boost" {...form.inputProps("boost")} />
            <EnumSelect label="valuesQueryType" dict={valuesQueryType} {...form.inputProps("valuesQueryType")} />
            <EnumSelect label="globalQueryType" dict={globalQueryType} {...form.inputProps("globalQueryType")} />
          </div>
        )}
        <div className="sheet-footer">
          <CustomButtom nameButton={annotatorId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
gql`
  query DocTypeFieldOptions($searchText: String, $cursor: String, $annotatorId: ID!) {
    options: docTypeFieldNotInAnnotator(annotatorId: $annotatorId, searchText: $searchText, first: 5, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;
gql`
  query DocTypeFieldValue($id: ID!) {
    value: docTypeField(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindDocTypeFieldToDataSource($documentTypeFieldId: ID!, $annotatorId: ID!) {
    bindAnnotatorToDocTypeField(docTypeFieldId: $documentTypeFieldId, id: $annotatorId) {
      left {
        id
        docTypeField {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindDocTypeFieldToDataSource($documentTypeFieldId: ID!, $annotatorId: ID!) {
    unbindAnnotatorFromDocTypeField(docTypeFieldId: $documentTypeFieldId, id: $annotatorId) {
      left {
        id
      }
    }
  }
`;
