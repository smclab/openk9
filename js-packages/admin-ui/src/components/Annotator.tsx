import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import {
  AnnotatorType,
  Fuzziness,
  useAnnotatorQuery,
  useBindDocTypeFieldToDataSourceMutation,
  useCreateOrUpdateAnnotatorMutation,
  useDocTypeFieldOptionsQuery,
  useDocTypeFieldValueQuery,
  useUnbindDocTypeFieldToDataSourceMutation,
} from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, EnumSelect, NumberInput, SearchSelect } from "./Form";
import { AnnotatorsQuery } from "./Annotators";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { ClassNameButton } from "../App";

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
          navigate(`/annotators/`, { replace: true });
          showToast({ displayType: "info", title: "Annotator updated", content: data.annotator.entity.name ?? "" });
        }
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Annotator error", content: error.message ?? "" });
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        fieldName: "",
        fuziness: Fuzziness.Auto,
        type: AnnotatorType.Stopword,
        description: "",
        size: 1,
        name: "",
      }),
      []
    ),
    originalValues: annotatorQuery.data?.annotator,
    isLoading: annotatorQuery.loading || createOrUpdateannotatorMutation.loading,
    onSubmit(data) {
      createOrUpdateAnnotatorMutate({ variables: { id: annotatorId !== "new" ? annotatorId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateannotatorMutation.data?.annotator?.fieldValidators),
  });
  return (
    <ClayLayout.ContainerFluid view>
      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextInput label="Field Name" {...form.inputProps("fieldName")} />
        <EnumSelect label="Fuziness" dict={Fuzziness} {...form.inputProps("fuziness")} />
        <EnumSelect label="Type" dict={AnnotatorType} {...form.inputProps("type")} />
        <TextArea label="Description" {...form.inputProps("description")} />
        <NumberInput label="Size" {...form.inputProps("size")} />
        {annotatorId !== "new" && (
          <ClayForm
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
          </ClayForm>
        )}
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {annotatorId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
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
