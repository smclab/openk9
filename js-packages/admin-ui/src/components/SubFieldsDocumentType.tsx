import { gql } from "@apollo/client";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { FieldType, useCreateOrUpdateDocumentTypeSubFieldsMutation, useDocumentTypeFieldQuery } from "../graphql-generated";
import {
  BooleanInput,
  ContainerFluid,
  ContainerFluidWithoutView,
  CustomButtom,
  EnumSelect,
  fromFieldValidators,
  NumberInput,
  TextArea,
  TextInput,
  useForm,
} from "./Form";
import { ClayButtonWithIcon } from "@clayui/button";
import { Link } from "react-router-dom";
import ClayToolbar from "@clayui/toolbar";
import { CodeInput } from "./CodeInput";
import { ClassNameButton } from "../App";
import { DocumentTypeFieldsParentQuery } from "./DocumentTypeFields";

export const DocumentTypeFieldsQuery = gql`
  query DocumentTypeFields($documentTypeId: ID!, $searchText: String, $cursor: String, $parentId: BigInteger!) {
    docTypeFieldsFromDocTypeByParent(docTypeId: $documentTypeId, searchText: $searchText, first: 25, after: $cursor, parentId: $parentId) {
      edges {
        node {
          id
          name
          description
          fieldType
          boost
          searchable
          exclude
          fieldName
          sortable
          subFields {
            edges {
              node {
                id
                name
                description
                fieldType
                boost
                searchable
                exclude
                fieldName
                sortable
              }
            }
          }
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
  mutation CreateOrUpdateDocumentTypeField(
    $documentTypeId: ID!
    $documentTypeFieldId: ID
    $name: String!
    $fieldName: String!
    $description: String
    $fieldType: FieldType!
    $boost: Float
    $searchable: Boolean!
    $exclude: Boolean
    $jsonConfig: String
    $sortable: Boolean!
  ) {
    docTypeField(
      docTypeId: $documentTypeId
      docTypeFieldId: $documentTypeFieldId
      docTypeFieldDTO: {
        name: $name
        description: $description
        fieldType: $fieldType
        boost: $boost
        searchable: $searchable
        exclude: $exclude
        fieldName: $fieldName
        jsonConfig: $jsonConfig
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

gql`
  mutation createDocumentTypeSubFields(
    $parentDocTypeFieldId: ID!
    $name: String!
    $fieldName: String!
    $jsonConfig: String
    $searchable: Boolean!
    $boost: Float
    $fieldType: FieldType!
    $description: String
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
        description: $description
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

export function SubFieldsDocumentType() {
  const { documentTypeId, ParentId = "new", subFieldID = "new" } = useParams();
  const navigate = useNavigate();
  const documentTypeFieldQuery = useDocumentTypeFieldQuery({
    variables: { id: subFieldID as string },
    skip: !subFieldID || subFieldID === "new",
  });
  const [createOrUpdateDocumentTypeSubFieldMutate, createOrUpdateDocumentTypeSubFieldMutation] =
    useCreateOrUpdateDocumentTypeSubFieldsMutation({
      refetchQueries: [DocumentTypeFieldsQuery, DocumentTypeFieldsParentQuery],
      onCompleted(data) {
        if (data.createSubField?.entity) {
          navigate(`/document-types/${documentTypeId}/document-type-fields`, { replace: true });
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
    isLoading: documentTypeFieldQuery.loading || createOrUpdateDocumentTypeSubFieldMutation.loading,
    onSubmit(data) {
      createOrUpdateDocumentTypeSubFieldMutate({
        variables: {
          parentDocTypeFieldId: ParentId as string,
          ...data,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateDocumentTypeSubFieldMutation.data?.createSubField?.fieldValidators),
  });
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluidWithoutView>
          <ClayToolbar.Nav>
            <ClayToolbar.Item>
              <Link to={`/document-types/${documentTypeId}/document-type-fields`}>
                <ClayButtonWithIcon className={ClassNameButton} aria-label="" symbol="angle-left" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
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
          <TextInput label="Field Name" {...form.inputProps("fieldName")} />
          <TextArea label="Description" {...form.inputProps("description")} />
          <EnumSelect label="Field Type" dict={FieldType} {...form.inputProps("fieldType")} />
          <NumberInput label="Boost" {...form.inputProps("boost")} />
          <BooleanInput label="Searchable" {...form.inputProps("searchable")} />
          <BooleanInput label="Exclude" {...form.inputProps("exclude")} />
          <BooleanInput label="Sortable" {...form.inputProps("sortable")} />
          <CodeInput language="json" label="Configuration" {...form.inputProps("jsonConfig")} />
          <div className="sheet-footer">
            <CustomButtom nameButton={subFieldID === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
          </div>
        </form>
      </ContainerFluid>
    </React.Fragment>
  );
}
