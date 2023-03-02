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
import { useForm, fromFieldValidators, TextInput, TextArea, SearchSelect, MainTitle } from "./Form";
import { DocumentTypesQuery } from "./DocumentTypes";
import ClayLayout from "@clayui/layout";
import { ClassNameButton } from "../App";
import { useToast } from "./ToastProvider";

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
  const showToast = useToast();
  const [createOrUpdateDocumentTypeMutate, createOrUpdateDocumentTypeMutation] = useCreateOrUpdateDocumentTypeMutation({
    refetchQueries: [DocumentTypeQuery, DocumentTypesQuery],
    onCompleted(data) {
      if (data.docType?.entity) {
        if (documentTypeId === "new") {
          navigate(`/document-types/`, { replace: true });
          showToast({ displayType: "success", title: "Document Type Create", content: "" });
        } else showToast({ displayType: "info", title: "Document Type Update", content: "" });
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
      {documentTypeId !== "new" && <MainTitle title="Attributes" />}
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
              description={"Javascript template for card and detail rendering associated to current Document Type"}
            />
          </ClayForm>
        )}
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
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
