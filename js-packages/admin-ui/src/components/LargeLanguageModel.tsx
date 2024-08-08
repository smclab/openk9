import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useToast } from "./ToastProvider";
import { useLargeLanguageModelQuery } from "../graphql-generated";
import { ContainerFluid, MainTitle, TextInput } from "./Form";
import React from "react";

export const LargeLanguageModelQ = gql`
  query LargeLanguageModel($id: ID!) {
    largeLanguageModel(id: $id) {
      name
      description
      apiUrl
      apiKey
      jsonConfig
    }
  }
`;

export function LargeLanguageModelE() {
  const { largeLanguagesModelsId = "new" } = useParams();
  const languageQuery = useLargeLanguageModelQuery({
    variables: { id: largeLanguagesModelsId as string },
    skip: !largeLanguagesModelsId || largeLanguagesModelsId === "new",
  });
  if (languageQuery.loading) return null;
  const { name, description, apiKey, apiUrl } = languageQuery.data?.largeLanguageModel || {};
  return (
    <ContainerFluid>
      {largeLanguagesModelsId !== "new" && <MainTitle title="Language Model" />}
      <form className="sheet">
        <TextInput
          label="Name"
          disabled={true}
          id={largeLanguagesModelsId}
          onChange={() => {}}
          validationMessages={[]}
          value={name || ""}
        />
        <TextInput
          label="Description"
          disabled={true}
          id={largeLanguagesModelsId}
          onChange={() => {}}
          validationMessages={[]}
          value={description || ""}
        />
        <TextInput
          label="Api Key"
          disabled={true}
          id={largeLanguagesModelsId}
          onChange={() => {}}
          validationMessages={[]}
          value={apiKey || ""}
        />
        <TextInput
          label="Api Url"
          disabled={true}
          id={largeLanguagesModelsId}
          onChange={() => {}}
          validationMessages={[]}
          value={apiUrl || ""}
        />
        <div className="sheet-footer"></div>
      </form>
    </ContainerFluid>
  );
}
