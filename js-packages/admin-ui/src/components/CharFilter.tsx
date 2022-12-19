import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import { useCharFilterQuery, useCreateOrUpdateCharFilterMutation } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea } from "./Form";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { CodeInput } from "./CodeInput";
import { CharFiltersQuery } from "./CharFilters";

const CharFilterQuery = gql`
  query CharFilter($id: ID!) {
    charFilter(id: $id) {
      id
      name
      description
      jsonConfig
    }
  }
`;

gql`
  mutation CreateOrUpdateCharFilter($id: ID, $name: String!, $description: String, $jsonConfig: String) {
    charFilter(id: $id, charFilterDTO: { name: $name, description: $description, jsonConfig: $jsonConfig }) {
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

export function CharFilter() {
  const { charFilterId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const charFilterQuery = useCharFilterQuery({
    variables: { id: charFilterId as string },
    skip: !charFilterId || charFilterId === "new",
  });
  const [createOrUpdateCharFilterMutate, createOrUpdateCharFilterMutation] = useCreateOrUpdateCharFilterMutation({
    refetchQueries: [CharFilterQuery, CharFiltersQuery],
    onCompleted(data) {
      if (data.charFilter?.entity) {
        if (charFilterId === "new") {
          navigate(`/char-filters/`, { replace: true });
          showToast({ displayType: "success", title: "Char filter created", content: data.charFilter.entity.name ?? "" });
        } else {
          navigate(`/char-filters/`, { replace: true });
          showToast({ displayType: "info", title: "char filter updated", content: data.charFilter.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        jsonConfig: "{}",
      }),
      []
    ),
    originalValues: charFilterQuery.data?.charFilter,
    isLoading: charFilterQuery.loading || createOrUpdateCharFilterMutation.loading,
    onSubmit(data) {
      createOrUpdateCharFilterMutate({ variables: { id: charFilterId !== "new" ? charFilterId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateCharFilterMutation.data?.charFilter?.fieldValidators),
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
        <CodeInput language="json" label="Configuration" {...form.inputProps("jsonConfig")} />

        <div className="sheet-footer">
          <ClayButton type="submit" disabled={!form.canSubmit}>
            {charFilterId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
