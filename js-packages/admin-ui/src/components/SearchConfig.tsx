import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useSearchConfigQuery, useCreateOrUpdateSearchConfigMutation } from "../graphql-generated";
import { BooleanInput, fromFieldValidators, MainTitle, NumberInput, TextArea, TextInput, useForm } from "./Form";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { SearchConfigsQuery } from "./SearchConfigs";
import { ClassNameButton } from "../App";

const SearchConfigQuery = gql`
  query SearchConfig($id: ID!) {
    searchConfig(id: $id) {
      id
      name
      description
      minScore
      minScoreSuggestions
      minScoreSearch
    }
  }
`;

gql`
  mutation CreateOrUpdateSearchConfig(
    $id: ID
    $name: String!
    $description: String
    $minScore: Float!
    $minScoreSuggestions: Boolean!
    $minScoreSearch: Boolean!
  ) {
    searchConfig(
      id: $id
      searchConfigDTO: {
        name: $name
        description: $description
        minScore: $minScore
        minScoreSuggestions: $minScoreSuggestions
        minScoreSearch: $minScoreSearch
      }
    ) {
      entity {
        id
        name
        minScore
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function SearchConfig() {
  const { searchConfigId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const searchConfigQuery = useSearchConfigQuery({
    variables: { id: searchConfigId as string },
    skip: !searchConfigId || searchConfigId === "new",
  });
  const [createOrUpdateSearchConfigMutate, createOrUpdateSearchConfigMutation] = useCreateOrUpdateSearchConfigMutation({
    refetchQueries: [SearchConfigQuery, SearchConfigsQuery],
    onCompleted(data) {
      if (data.searchConfig?.entity) {
        if (searchConfigId === "new") {
          navigate(`/search-configs/`, { replace: true });
          showToast({ displayType: "success", title: "Search Config created", content: "" });
        } else {
          showToast({ displayType: "info", title: "Search Config updated", content: "" });
        }
      }
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        minScore: 0.0,
        minScoreSuggestions: false,
        minScoreSearch: false,
      }),
      []
    ),
    originalValues: searchConfigQuery.data?.searchConfig,
    isLoading: searchConfigQuery.loading || createOrUpdateSearchConfigMutation.loading,
    onSubmit(data) {
      createOrUpdateSearchConfigMutate({ variables: { id: searchConfigId !== "new" ? searchConfigId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateSearchConfigMutation.data?.searchConfig?.fieldValidators),
  });
  if (!searchConfigId) return null;
  return (
    <React.Fragment>
      <ClayLayout.ContainerFluid view>
        {searchConfigId !== "new" && <MainTitle title="Attributes" />}
        <ClayForm
          className="sheet"
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("name")} />
          <TextArea label="Description" {...form.inputProps("description")} />
          <NumberInput label="minScore" {...form.inputProps("minScore")} 
          description="Define score threshold used to filter results after query has been done"/>
          <BooleanInput label="min Score Suggestions" {...form.inputProps("minScoreSuggestions")} 
          description="If use configured min score to filter search results"/>
          <BooleanInput label="min Score Search" {...form.inputProps("minScoreSearch")} 
          description="If use configured min score to filter suggestions"/>

          <div className="sheet-footer">
            <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
              {searchConfigId === "new" ? "Create" : "Update"}
            </ClayButton>
          </div>
        </ClayForm>
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}
