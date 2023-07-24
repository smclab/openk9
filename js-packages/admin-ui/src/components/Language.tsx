import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useToast } from "./ToastProvider";
import { useBucketQuery, useCreateOrUpdateBucketMutation, useCreateOrUpdateLanguageMutation, useLanguageQuery } from "../graphql-generated";
import { LanguagesQuery } from "./Languages";
import { ContainerFluid, CustomButtom, MainTitle, TextInput, fromFieldValidators, useForm } from "./Form";
import React from "react";

const LanguageQuery = gql`
  query Language($id: ID!) {
    language(id: $id) {
      id
      name
      value
    }
  }
`;

gql`
  mutation CreateOrUpdateLanguage($id: ID, $name: String!, $value: String) {
    language(id: $id, languageDTO: { name: $name, value: $value }) {
      entity {
        id
        name
        value
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function Language() {
  const { languageId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const languageQuery = useLanguageQuery({
    variables: { id: languageId as string },
    skip: !languageId || languageId === "new",
  });
  const [createOrUpdateLanguageMutate, createOrUpdateLanguageMutation] = useCreateOrUpdateLanguageMutation({
    refetchQueries: [LanguageQuery, LanguagesQuery],
    onCompleted(data) {
      if (data.language?.entity) {
        if (languageId === "new") {
          navigate(`/languages/`, { replace: true });
          showToast({ displayType: "success", title: "Language created", content: data.language.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Language updated", content: data.language.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        value: "",
      }),
      []
    ),
    originalValues: languageQuery.data?.language,
    isLoading: languageQuery.loading || createOrUpdateLanguageMutation.loading,
    onSubmit(data) {
      createOrUpdateLanguageMutate({ variables: { id: languageId !== "new" ? languageId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateLanguageMutation.data?.language?.fieldValidators),
  });
  return (
    <ContainerFluid>
      {languageId !== "new" && <MainTitle title="Attribute" />}
      <form
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextInput label="value" {...form.inputProps("value")} />
        <div className="sheet-footer">
          <CustomButtom nameButton={languageId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
