import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useToast } from "./ToastProvider";
import { useCreateOrUpdateEmbeddingModelMutation, useEmbeddingModelQuery } from "../graphql-generated";
import { ContainerFluid, CustomButtom, MainTitle, TextInput, fromFieldValidators, useForm } from "./Form";
import React from "react";
import { EmbeddingModelQuery } from "./EmbeddingModel";
import { EmbeddingModelsQuery } from "./EmbeddingModels";

gql`
  mutation CreateOrUpdateEmbeddingModel($id: ID, $apiKey: String, $apiUrl: String!, $description: String!, $name: String!) {
    embeddingModel(id: $id, embeddingModelDTO: { name: $name, apiKey: $apiKey, apiUrl: $apiUrl, description: $description }) {
      entity {
        id
        name
      }
    }
  }
`;

export function EmbeddingModelCreate() {
  const { embeddingModelsId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const modelQuery = useEmbeddingModelQuery({
    variables: { id: embeddingModelsId as string },
    skip: !embeddingModelsId || embeddingModelsId === "new",
  });
  const [createOrUpdateEmbeddingModelsMutate] = useCreateOrUpdateEmbeddingModelMutation({
    refetchQueries: [EmbeddingModelsQuery, EmbeddingModelQuery],
    onCompleted(data) {
      if (data.embeddingModel?.entity) {
        if (embeddingModelsId === "new") {
          navigate(`/embedding-models/`, { replace: true });
          showToast({ displayType: "success", title: "Models created", content: data.embeddingModel.entity.name ?? "" });
        } else {
          navigate(`/embedding-models/`, { replace: true });
          showToast({ displayType: "info", title: "Models updated", content: data.embeddingModel.entity.name ?? "" });
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
      }),
      []
    ),
    originalValues: modelQuery.data?.embeddingModel,
    isLoading: modelQuery.loading || modelQuery.loading,
    onSubmit(data) {
      createOrUpdateEmbeddingModelsMutate({ variables: { id: embeddingModelsId !== "new" ? embeddingModelsId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(null),
  });
  return (
    <ContainerFluid>
      {embeddingModelsId !== "new" && <MainTitle title="Attribute" />}
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

        <div className="sheet-footer">
          <CustomButtom nameButton={embeddingModelsId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
