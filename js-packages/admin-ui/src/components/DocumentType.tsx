import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import {
  useBindDocumentTypeTemplateToDocumentTypeMutation,
  useCreateOrUpdateDocumentTypeMutation,
  useDocumentTypeQuery,
  useDocumentTypeTemplateOptionsQuery,
  useDocumentTypeTemplateValueQuery,
  useUnbindDocumentTypeTemplateFromDocumentTypeMutation,
} from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, SearchSelect } from "./Form";
import { DocumentTypesQuery } from "./DocumentTypes";
import ClayLayout from "@clayui/layout";

const DocumentTypeQuery = gql`
  query DocumentType($id: ID!) {
    docType(id: $id) {
      id
      name
      description
      docTypeTemplate {
        id
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateDocumentType($id: ID, $name: String!, $description: String) {
    docType(id: $id, docTypeDTO: { name: $name, description: $description }) {
      entity {
        id
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function DocumentType() {
  const { documentTypeId = "new" } = useParams();
  const navigate = useNavigate();
  const documentTypeQuery = useDocumentTypeQuery({
    variables: { id: documentTypeId as string },
    skip: !documentTypeId || documentTypeId === "new",
  });
  const [createOrUpdateDocumentTypeMutate, createOrUpdateDocumentTypeMutation] = useCreateOrUpdateDocumentTypeMutation({
    refetchQueries: [DocumentTypeQuery, DocumentTypesQuery],
    onCompleted(data) {
      if (data.docType?.entity) {
        if (documentTypeId === "new") navigate(`/document-types/`, { replace: true });
        else navigate(`/document-types/${data.docType?.entity?.id}`, { replace: true });
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
      }),
      []
    ),
    originalValues: documentTypeQuery.data?.docType,
    isLoading: documentTypeQuery.loading || createOrUpdateDocumentTypeMutation.loading,
    onSubmit(data) {
      createOrUpdateDocumentTypeMutate({
        variables: { id: documentTypeId !== "new" ? documentTypeId : undefined, ...data },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateDocumentTypeMutation.data?.docType?.fieldValidators),
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
        <TextArea label="Description" {...form.inputProps("description")} />
        {documentTypeId !== "new" && (
          <ClayForm
            onSubmit={(event) => {
              event.preventDefault();
            }}
          >
            <SearchSelect
              label="Document Type Template"
              value={documentTypeQuery.data?.docType?.docTypeTemplate?.id}
              useValueQuery={useDocumentTypeTemplateValueQuery}
              useOptionsQuery={useDocumentTypeTemplateOptionsQuery}
              useChangeMutation={useBindDocumentTypeTemplateToDocumentTypeMutation}
              mapValueToMutationVariables={(documentTypeTemplateId) => ({ documentTypeId, documentTypeTemplateId })}
              useRemoveMutation={useUnbindDocumentTypeTemplateFromDocumentTypeMutation}
              mapValueToRemoveMutationVariables={() => ({ documentTypeId })}
              invalidate={() => documentTypeQuery.refetch()}
            />
          </ClayForm>
        )}
        <div className="sheet-footer">
          <ClayButton type="submit" disabled={!form.canSubmit}>
            {documentTypeId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}

gql`
  query DocumentTypeTemplateOptions($searchText: String, $cursor: String) {
    options: docTypeTemplates(searchText: $searchText, first: 5, after: $cursor) {
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
  query DocumentTypeTemplateValue($id: ID!) {
    value: docTypeTemplate(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindDocumentTypeTemplateToDocumentType($documentTypeId: ID!, $documentTypeTemplateId: ID!) {
    bindDocTypeToDocTypeTemplate(docTypeId: $documentTypeId, docTypeTemplateId: $documentTypeTemplateId) {
      left {
        id
        docTypeTemplate {
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
  mutation UnbindDocumentTypeTemplateFromDocumentType($documentTypeId: ID!) {
    unbindDocTypeTemplateFromDocType(docTypeId: $documentTypeId) {
      id
      docTypeTemplate {
        id
      }
    }
  }
`;
