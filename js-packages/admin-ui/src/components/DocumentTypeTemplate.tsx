import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import { TemplateType, useCreateOrUpdateDocumentTypeTemplateMutation, useDocumentTypeTemplateQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, EnumSelect } from "./Form";
import { CodeInput } from "./CodeInput";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { DocumentTypeTemplatesQuery } from "./DocumentTypeTemplates";
import { transform, availablePresets } from "@babel/standalone";
import { ClassNameButton } from "../App";

const DocumentTypeTemplateQuery = gql`
  query DocumentTypeTemplate($id: ID!) {
    docTypeTemplate(id: $id) {
      id
      name
      description
      templateType
      source
      compiled
    }
  }
`;

gql`
  mutation CreateOrUpdateDocumentTypeTemplate(
    $id: ID
    $name: String!
    $description: String
    $templateType: TemplateType!
    $source: String!
    $compiled: String!
  ) {
    docTypeTemplate(
      id: $id
      docTypeTemplateDTO: { name: $name, description: $description, templateType: $templateType, source: $source, compiled: $compiled }
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

export function DocumentTypeTemplate() {
  const { documentTypeTemplateId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const documentTypeTemplateQuery = useDocumentTypeTemplateQuery({
    variables: { id: documentTypeTemplateId as string },
    skip: !documentTypeTemplateId || documentTypeTemplateId === "new",
  });
  const [createOrUpdateDocumentTypeTemplateMutate, createOrUpdateDocumentTypeTempalteMutation] =
    useCreateOrUpdateDocumentTypeTemplateMutation({
      refetchQueries: [DocumentTypeTemplateQuery, DocumentTypeTemplatesQuery],
      onCompleted(data) {
        if (data.docTypeTemplate?.entity) {
          if (documentTypeTemplateId === "new") {
            navigate(`/document-type-templates/`, { replace: true });
            showToast({ displayType: "success", title: "Template created", content: data.docTypeTemplate.entity.name ?? "" });
          } else {
            showToast({ displayType: "info", title: "Template updated", content: data.docTypeTemplate.entity.name ?? "" });
          }
        }
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
      []
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
            return { source: data.source, compiled: transpile(data.source) ?? data.compiled };
          }
          default:
            throw new Error();
        }
      })();
      createOrUpdateDocumentTypeTemplateMutate({
        variables: { id: documentTypeTemplateId !== "new" ? documentTypeTemplateId : undefined, ...data, source, compiled },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateDocumentTypeTempalteMutation.data?.docTypeTemplate?.fieldValidators),
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
        <EnumSelect label="Template Type" dict={TemplateType} {...form.inputProps("templateType")} />
        {(() => {
          switch (form.inputProps("templateType").value) {
            case TemplateType.TypescriptSource: {
              return <CodeInput label="Source" language="typescript-react" height="80vh" {...form.inputProps("source")} />;
            }
            case TemplateType.JavascriptSource: {
              return <CodeInput label="Source" language="javascript-react" height="80vh" {...form.inputProps("source")} />;
            }
            case TemplateType.JavascriptCompiled: {
              return <CodeInput label="Compiled" language="javascript" height="80vh" {...form.inputProps("compiled")} />;
            }
          }
        })()}
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {documentTypeTemplateId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
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
