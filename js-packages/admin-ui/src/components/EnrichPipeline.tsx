import React from "react";
import { gql } from "@apollo/client";
import ClayForm from "@clayui/form";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateEnrichPipelineMutation, useEnrichPipelineQuery } from "../graphql-generated";
import { EnrichPipelinesQuery } from "./EnrichPipelines";
import { ContainerFluid, CustomButtom, fromFieldValidators, TextArea, TextInput, useForm } from "./Form";
import { useToast } from "./ToastProvider";

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
    <ContainerFluid>
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
          <CustomButtom nameButton={enrichPipelineId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </ClayForm>
    </ContainerFluid>
  );
}
