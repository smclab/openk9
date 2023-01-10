import React from "react";
import { gql } from "@apollo/client";
import ClayForm from "@clayui/form";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateEnrichPipelineMutation, useEnrichPipelineQuery } from "../graphql-generated";
import { EnrichPipelinesQuery } from "./EnrichPipelines";
import { fromFieldValidators, TextArea, TextInput, useForm } from "./Form";
import ClayButton from "@clayui/button";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { ClassNameButton } from "../App";

const EnrichPipelineQuery = gql`
  query EnrichPipeline($id: ID!) {
    enrichPipeline(id: $id) {
      id
      name
      description
    }
  }
`;

gql`
  mutation CreateOrUpdateEnrichPipeline($id: ID, $name: String!, $description: String) {
    enrichPipeline(id: $id, enrichPipelineDTO: { name: $name, description: $description }) {
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

export function EnrichPipeline() {
  const { enrichPipelineId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const enrichPipelineQuery = useEnrichPipelineQuery({
    variables: { id: enrichPipelineId as string },
    skip: !enrichPipelineId || enrichPipelineId === "new",
  });
  const [createOrUpdateEnrichPipelineMutate, createOrUpdateEnrichPipelineMutation] = useCreateOrUpdateEnrichPipelineMutation({
    refetchQueries: [EnrichPipelineQuery, EnrichPipelinesQuery],
    onCompleted(data) {
      if (data.enrichPipeline?.entity) {
        if (enrichPipelineId === "new") {
          navigate(`/enrich-pipelines/`, { replace: true });
          showToast({ displayType: "success", title: "Enrich pipelines created", content: data.enrichPipeline.entity.name ?? "" });
        } else {
          navigate(`/enrich-pipelines/`, { replace: true });
          showToast({ displayType: "info", title: "Enrich pipelines updated", content: data.enrichPipeline.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
      }),
      []
    ),
    originalValues: enrichPipelineQuery.data?.enrichPipeline,
    isLoading: enrichPipelineQuery.loading || createOrUpdateEnrichPipelineMutation.loading,
    onSubmit(data) {
      createOrUpdateEnrichPipelineMutate({ variables: { id: enrichPipelineId !== "new" ? enrichPipelineId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateEnrichPipelineMutation.data?.enrichPipeline?.fieldValidators),
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
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {enrichPipelineId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
