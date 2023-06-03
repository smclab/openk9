import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import { useCreateOrUpdateSuggestionCategoryMutation, useSuggestionCategoryQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, NumberInput, BooleanInput, CustomButtom } from "./Form";
import { SuggestionCategoriesQuery } from "./SuggestionCategories";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";

const SuggestionCategoryQuery = gql`
  query SuggestionCategory($id: ID!) {
    suggestionCategory(id: $id) {
      id
      name
      description
      priority
      multiSelect
    }
  }
`;

gql`
  mutation CreateOrUpdateSuggestionCategory($id: ID, $name: String!, $description: String, $priority: Float!, $multiSelect: Boolean!) {
    suggestionCategory(
      id: $id
      suggestionCategoryDTO: { name: $name, description: $description, priority: $priority, multiSelect: $multiSelect }
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
        multiSelect: false,
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
        <NumberInput
          label="Priority"
          {...form.inputProps("priority")}
          description="Define priority according to which suggestion cateogories are
        orderder by search frontend during rendering"
        />
        <BooleanInput
          label="Multi Select"
          {...form.inputProps("multiSelect")}
          description="If currente Suggestion Category is rendered as multi label filter or not"
        />
        <div className="sheet-footer">
          <CustomButtom
            nameButton={suggestionCategoryId === "new" ? "Create" : "Update"}
            canSubmit={!form.canSubmit}
            typeSelectet="submit"
          />
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
