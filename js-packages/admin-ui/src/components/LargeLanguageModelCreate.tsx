import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useToast } from "./ToastProvider";
import { useCreateOrUpdateLargeLanguageModelMutation, useLargeLanguageModelQuery } from "../graphql-generated";
import { ContainerFluid, CustomButtom, MainTitle, TextInput, fromFieldValidators, useForm } from "./Form";
import React from "react";
import { CodeInput } from "./CodeInput";
import { LargeLanguageModelsQuery } from "./LargeLanguageModels";
import { LargeLanguageModelQ } from "./LargeLanguageModel";

const create = gql`
  mutation CreateOrUpdateLargeLanguageModel(
    $id: ID
    $apiKey: String
    $apiUrl: String!
    $description: String!
    $name: String!
    $jsonConfig: String
  ) {
    largeLanguageModel(
      id: $id
      largeLanguageModelDTO: { name: $name, apiKey: $apiKey, apiUrl: $apiUrl, description: $description, jsonConfig: $jsonConfig }
    ) {
      entity {
        id
        name
      }
    }
  }
`;

export function LargeLanguageModel() {
  const { largeLanguageModelId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const modelQuery = useLargeLanguageModelQuery({
    variables: { id: largeLanguageModelId as string },
    skip: !largeLanguageModelId || largeLanguageModelId === "new",
  });
  const [createOrUpdateEmbeddingModelsMutate] = useCreateOrUpdateLargeLanguageModelMutation({
    refetchQueries: [create, LargeLanguageModelsQuery, LargeLanguageModelQ],
    onCompleted(data) {
      if (data.largeLanguageModel?.entity) {
        if (largeLanguageModelId === "new") {
          navigate(`/large-languages-models/`, { replace: true });
          showToast({ displayType: "success", title: "Models created", content: data.largeLanguageModel.entity.name ?? "" });
        } else {
          navigate(`/large-languages-models/`, { replace: true });
          showToast({ displayType: "info", title: "Models updated", content: data.largeLanguageModel.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        apiKey: "",
        apiUrl: "",
        description: "",
        json: "",
      }),
      []
    ),
    originalValues: modelQuery.data?.largeLanguageModel,
    isLoading: modelQuery.loading || modelQuery.loading,
    onSubmit(data) {
      createOrUpdateEmbeddingModelsMutate({
        variables: { id: largeLanguageModelId !== "new" ? largeLanguageModelId : undefined, ...data },
      });
    },
    getValidationMessages: fromFieldValidators(null),
  });
  return (
    <ContainerFluid>
      {largeLanguageModelId !== "new" && <MainTitle title="Attribute" />}
      <form
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextInput label="Description" {...form.inputProps("description")} />
        <TextInput label="Api Url" {...form.inputProps("apiKey")} />
        <TextInput label="Api Key" {...form.inputProps("apiUrl")} />
        <CodeInput language="json" label="Configuration" {...form.inputProps("json")} />

        <div className="sheet-footer">
          <CustomButtom
            nameButton={largeLanguageModelId === "new" ? "Create" : "Update"}
            canSubmit={!form.canSubmit}
            typeSelectet="submit"
          />
        </div>
      </form>
    </ContainerFluid>
  );
}
