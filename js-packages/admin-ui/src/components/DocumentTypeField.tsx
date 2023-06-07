import { gql } from "@apollo/client";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  FieldType,
  useAnalyzerOptionsQuery,
  useAnalyzerValueQuery,
  useBindAnalyzerToDocTypeFieldMutation,
  useCreateOrUpdateDocumentTypeFieldMutation,
  useDocumentTypeFieldQuery,
  useUnbindnAlyzerToDocTypeFieldMutation,
} from "../graphql-generated";
import {
  BooleanInput,
  ContainerFluid,
  CustomButtom,
  EnumSelect,
  fromFieldValidators,
  NumberInput,
  SearchSelect,
  TextArea,
  TextInput,
  useForm,
} from "./Form";
import { ClayButtonWithIcon } from "@clayui/button";
import { Link } from "react-router-dom";
import ClayToolbar from "@clayui/toolbar";
import { CodeInput } from "./CodeInput";
import { DocumentTypeFieldsQuery } from "./SubFieldsDocumentType";
import { useToast } from "./ToastProvider";
import { ClassNameButton } from "../App";

const DocumentTypeFieldQuery = gql`
  query DocumentTypeField($id: ID!) {
    docTypeField(id: $id) {
      id
      name
      description
      fieldType
      boost
      searchable
      exclude
      fieldName
      jsonConfig
      sortable
      analyzer {
        id
      }
    }
  }
`;

gql`
  mutation createOrUpdateDocumentTypeSubFields(
    $parentDocTypeFieldId: ID!
    $name: String!
    $fieldName: String!
    $jsonConfig: String
    $searchable: Boolean!
    $boost: Float
    $fieldType: FieldType!
    $sortable: Boolean!
  ) {
    createSubField(
      parentDocTypeFieldId: $parentDocTypeFieldId
      docTypeFieldDTO: {
        name: $name
        fieldName: $fieldName
        jsonConfig: $jsonConfig
        searchable: $searchable
        boost: $boost
        fieldType: $fieldType
        sortable: $sortable
      }
    ) {
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

export function DocumentTypeField() {
  const { documentTypeId, documentTypeFieldId = "new" } = useParams();
  const navigate = useNavigate();
  const documentTypeFieldQuery = useDocumentTypeFieldQuery({
    variables: { id: documentTypeFieldId as string },
    skip: !documentTypeFieldId || documentTypeFieldId === "new",
  });
  const showToast = useToast();
  const [createOrUpdateDocumentTypeFieldMutate, createOrUpdateDocumentTypeFieldMutation] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [DocumentTypeFieldQuery, DocumentTypeFieldsQuery],
    onCompleted(data) {
      if (data.docTypeField?.entity) {
        if (documentTypeFieldId === "new") {
          showToast({ displayType: "success", title: "Document Type Fields created", content: "" });
          navigate(`/document-types/${documentTypeId}/document-type-fields`, { replace: true });
        } else {
          showToast({ displayType: "info", title: "Document Type Fields update", content: "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        fieldName: "",
        description: "",
        fieldType: FieldType.Text,
        boost: 1,
        searchable: false,
        exclude: false,
        jsonConfig: "{}",
        sortable: false,
      }),
      []
    ),
    originalValues: documentTypeFieldQuery.data?.docTypeField,
    isLoading: documentTypeFieldQuery.loading || createOrUpdateDocumentTypeFieldMutation.loading,
    onSubmit(data) {
      createOrUpdateDocumentTypeFieldMutate({
        variables: {
          documentTypeId: documentTypeId as string,
          documentTypeFieldId: documentTypeFieldId !== "new" ? documentTypeFieldId : undefined,
          ...data,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateDocumentTypeFieldMutation.data?.docTypeField?.fieldValidators),
  });
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item>
              <Link to={`/document-types/${documentTypeId}/document-type-fields`}>
                <ClayButtonWithIcon aria-label="" className={` ${ClassNameButton} `} symbol="angle-left" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluid>
      </ClayToolbar>
      <ContainerFluid>
        <form
          className="sheet"
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("name")} />
          <TextInput
            label="Field Name"
            {...form.inputProps("fieldName")}
            description="Name used to retrive field mapping, composed of Document Type and the name of indexed field"
          />
          <TextArea label="Description" {...form.inputProps("description")} />
          <EnumSelect
            label="Field Type"
            dict={FieldType}
            {...form.inputProps("fieldType")}
            description={"Type associated to field. See Elasticsearch documentation for field data types"}
          />
          <NumberInput
            label="Boost"
            {...form.inputProps("boost")}
            description="Define how much score is boosted in case of match on this field"
          />
          {documentTypeFieldId !== "new" && (
            <form
              onSubmit={(event) => {
                event.preventDefault();
              }}
            >
              <SearchSelect
                label="Analyzer"
                value={documentTypeFieldQuery.data?.docTypeField?.analyzer?.id}
                useValueQuery={useAnalyzerValueQuery}
                useOptionsQuery={useAnalyzerOptionsQuery}
                useChangeMutation={useBindAnalyzerToDocTypeFieldMutation}
                mapValueToMutationVariables={(analayzerId) => ({ documentTypeFieldId: documentTypeFieldId, analyzerId: analayzerId })}
                useRemoveMutation={useUnbindnAlyzerToDocTypeFieldMutation}
                mapValueToRemoveMutationVariables={() => ({ documentTypeFieldId })}
                invalidate={() => documentTypeFieldQuery.refetch()}
                description={"Analyzer associated to this Document Type Field"}
              />
            </form>
          )}
          <BooleanInput label="Searchable" {...form.inputProps("searchable")} description="If field is searchable or not" />
          <BooleanInput
            label="Exclude"
            {...form.inputProps("exclude")}
            description="If field need to be excluded from search response or not"
          />
          <BooleanInput label="Sortable" {...form.inputProps("sortable")} description="If field is searchable or not" />
          <CodeInput language="json" label="Configuration" {...form.inputProps("jsonConfig")} />
          <div className="sheet-footer">
            <CustomButtom
              nameButton={documentTypeFieldId === "new" ? "Create" : "Update"}
              canSubmit={!form.canSubmit}
              typeSelectet="submit"
            />
          </div>
        </form>
      </ContainerFluid>
    </React.Fragment>
  );
}

gql`
  query AnalyzerOptions($searchText: String, $cursor: String) {
    options: analyzers(searchText: $searchText, first: 5, after: $cursor) {
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
  query AnalyzerValue($id: ID!) {
    value: analyzer(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindAnalyzerToDocTypeField($documentTypeFieldId: ID!, $analyzerId: ID!) {
    bindAnalyzerToDocTypeField(docTypeFieldId: $documentTypeFieldId, analyzerId: $analyzerId) {
      left {
        id
        analyzer {
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
  mutation UnbindnAlyzerToDocTypeField($documentTypeFieldId: ID!) {
    unbindAnalyzerFromDocTypeField(docTypeFieldId: $documentTypeFieldId) {
      left {
        id
        analyzer {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
