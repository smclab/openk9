import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useToast } from "./ToastProvider";
import { useEmbeddingModelQuery } from "../graphql-generated";
import { ContainerFluid, MainTitle, TextInput } from "./Form";
import React from "react";

export const EmbeddingModelQuery = gql`
  query EmbeddingModel($id: ID!) {
    embeddingModel(id: $id) {
      name
      description
      apiUrl
      apiKey
    }
  }
`;

export function EmbeddingModel() {
  const { embeddingModelsId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const modelQuery = useEmbeddingModelQuery({
    variables: { id: embeddingModelsId as string },
    skip: !embeddingModelsId || embeddingModelsId === "new",
  });
  if (modelQuery.loading) return null;
  const { name, description, apiKey, apiUrl } = modelQuery.data?.embeddingModel || {};
  return (
    <ContainerFluid>
      {embeddingModelsId !== "new" && <MainTitle title="Embedding Model" />}
      <form className="sheet">
        <TextInput label="Name" disabled={true} id={embeddingModelsId} onChange={() => {}} validationMessages={[]} value={name || ""} />
        <TextInput
          label="Description"
          disabled={true}
          id={embeddingModelsId}
          onChange={() => {}}
          validationMessages={[]}
          value={description || ""}
        />
        <TextInput
          label="Api Key"
          disabled={true}
          id={embeddingModelsId}
          onChange={() => {}}
          validationMessages={[]}
          value={apiKey || ""}
        />
        <TextInput
          label="Api Url"
          disabled={true}
          id={embeddingModelsId}
          onChange={() => {}}
          validationMessages={[]}
          value={apiUrl || ""}
        />
      </form>
    </ContainerFluid>
  );
}
