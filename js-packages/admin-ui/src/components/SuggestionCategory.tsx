import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import { useCreateOrUpdateSuggestionCategoryMutation, useSuggestionCategoryQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, NumberInput } from "./Form";
import { SuggestionCategoriesQuery } from "./SuggestionCategories";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { ClassNameButton } from "../App";

const SuggestionCategoryQuery = gql`
  query SuggestionCategory($id: ID!) {
    suggestionCategory(id: $id) {
      id
      name
      description
      priority
    }
  }
`;

gql`
  mutation CreateOrUpdateSuggestionCategory($id: ID, $name: String!, $description: String, $priority: Float!) {
    suggestionCategory(id: $id, suggestionCategoryDTO: { name: $name, description: $description, priority: $priority }) {
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

export function SuggestionCategory() {
  const { suggestionCategoryId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const suggestionCategoryQuery = useSuggestionCategoryQuery({
    variables: { id: suggestionCategoryId as string },
    skip: !suggestionCategoryId || suggestionCategoryId === "new",
  });
  const [createOrUpdateSuggestionCategoryMutate, createOrUpdateSuggestionCategoryMutation] = useCreateOrUpdateSuggestionCategoryMutation({
    refetchQueries: [SuggestionCategoryQuery, SuggestionCategoriesQuery],
    onCompleted(data) {
      if (data.suggestionCategory?.entity) {
        if (suggestionCategoryId === "new") {
          navigate(`/suggestion-categories/`, { replace: true });
          showToast({ displayType: "success", title: "Suggestion categorie created", content: data.suggestionCategory.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Suggestion categorie updated", content: data.suggestionCategory.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        priority: 0,
      }),
      []
    ),
    originalValues: suggestionCategoryQuery.data?.suggestionCategory,
    isLoading: suggestionCategoryQuery.loading || createOrUpdateSuggestionCategoryMutation.loading,
    onSubmit(data) {
      createOrUpdateSuggestionCategoryMutate({
        variables: { id: suggestionCategoryId !== "new" ? suggestionCategoryId : undefined, ...data },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateSuggestionCategoryMutation.data?.suggestionCategory?.fieldValidators),
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
        <NumberInput label="Priority" {...form.inputProps("priority")} />
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {suggestionCategoryId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
