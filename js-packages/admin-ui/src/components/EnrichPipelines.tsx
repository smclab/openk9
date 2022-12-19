import React from "react";
import { gql } from "@apollo/client";
import { useDeleteEnrichPipelineMutation, useEnrichPipelinesQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const EnrichPipelinesQuery = gql`
  query EnrichPipelines($searchText: String, $cursor: String) {
    enrichPipelines(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

gql`
  mutation DeleteEnrichPipeline($id: ID!) {
    deleteEnrichPipeline(enrichPipelineId: $id) {
      id
      name
    }
  }
`;

export function EnrichPipelines() {
  const enrichPipelinesQuery = useEnrichPipelinesQuery();
  const showToast = useToast();
  const [deleteEnrichPipelineMutate] = useDeleteEnrichPipelineMutation({
    refetchQueries: [EnrichPipelinesQuery],
    onCompleted(data) {
      if (data.deleteEnrichPipeline?.id) {
        showToast({ displayType: "success", title: "Enrich pipelines deleted", content: data.deleteEnrichPipeline.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Enrich pipelines error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: enrichPipelinesQuery,
        field: (data) => data?.enrichPipelines,
      }}
      onCreatePath="/enrich-pipelines/new"
      onDelete={(enrichPipeline) => {
        if (enrichPipeline?.id) deleteEnrichPipelineMutate({ variables: { id: enrichPipeline.id } });
      }}
      columns={[
        { header: "Name", content: (enrichPipeline) => formatName(enrichPipeline) },
        { header: "Description", content: (enrichPipeline) => enrichPipeline?.description },
      ]}
    />
  );
}
