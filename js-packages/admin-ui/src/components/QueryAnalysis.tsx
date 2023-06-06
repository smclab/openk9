import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import { useCreateOrUpdateQueryAnalysisMutation, useQueryAnalysisQuery } from "../graphql-generated";
import { QueryAnalysesQuery } from "./QueryAnalyses";
import { useForm, fromFieldValidators, TextInput, TextArea, MainTitle, CustomButtom, ContainerFluid } from "./Form";
import { CodeInput } from "./CodeInput";
import { useToast } from "./ToastProvider";

const QueryAnalysisQuery = gql`
  query QueryAnalysis($id: ID!) {
    queryAnalysis(id: $id) {
      id
      name
      description
      stopWords
    }
  }
`;

gql`
  mutation CreateOrUpdateQueryAnalysis($id: ID, $name: String!, $description: String, $stopWords: String) {
    queryAnalysis(id: $id, queryAnalysisDTO: { name: $name, description: $description, stopWords: $stopWords }) {
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

export function QueryAnalysis() {
  const { queryAnalysisId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const queryAnalysisQuery = useQueryAnalysisQuery({
    variables: { id: queryAnalysisId as string },
    skip: !queryAnalysisId || queryAnalysisId === "new",
  });
  const [createOrUpdateQueryAnalysisMutate, createOrUpdateQueryAnalysisMutation] = useCreateOrUpdateQueryAnalysisMutation({
    refetchQueries: [QueryAnalysisQuery, QueryAnalysesQuery],
    onCompleted(data) {
      if (data.queryAnalysis?.entity) {
        if (queryAnalysisId === "new") {
          navigate(`/query-analyses/`, { replace: true });
          showToast({ displayType: "success", title: "Query analyses created", content: data.queryAnalysis.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Query analyses updated", content: data.queryAnalysis.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        stopWords: "",
      }),
      []
    ),
    originalValues: queryAnalysisQuery.data?.queryAnalysis,
    isLoading: queryAnalysisQuery.loading || createOrUpdateQueryAnalysisMutation.loading,
    onSubmit(data) {
      createOrUpdateQueryAnalysisMutate({ variables: { id: queryAnalysisId !== "new" ? queryAnalysisId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateQueryAnalysisMutation.data?.queryAnalysis?.fieldValidators),
  });
  return (
    <ContainerFluid>
      {queryAnalysisId !== "new" && <MainTitle title="Query Analysis" />}
      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextArea label="Description" {...form.inputProps("description")} />
        <CodeInput label="Stop Words" language="text" {...form.inputProps("stopWords")} />
        <div className="sheet-footer">
          <CustomButtom nameButton={queryAnalysisId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </ClayForm>
    </ContainerFluid>
  );
}
