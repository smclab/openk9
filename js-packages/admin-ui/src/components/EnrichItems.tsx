import React from "react";
import { gql } from "@apollo/client";
import { useDeleteEnrichItemMutation, useEnrichItemsQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";
import { AssociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQuery } from "./EnrichPipelineEnrichItems";

export const EnrichItemsQuery = gql`
  query EnrichItems($searchText: String, $cursor: String) {
    enrichItems(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          type
          serviceName
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
  mutation DeleteEnrichItem($id: ID!) {
    deleteEnrichItem(enrichItemId: $id) {
      id
      name
    }
  }
`;

export function EnrichItems() {
  const enrichItemsQuery = useEnrichItemsQuery();
  const showToast = useToast();
  const [deleteEnrichItemMutate] = useDeleteEnrichItemMutation({
    refetchQueries: [EnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQuery],
    onCompleted(data) {
      if (data.deleteEnrichItem?.id) {
        showToast({ displayType: "success", title: "Enrich items deleted", content: data.deleteEnrichItem.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Enrich items error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: enrichItemsQuery,
        field: (data) => data?.enrichItems,
      }}
      onCreatePath="/enrich-items/new"
      onDelete={(enrichItem) => {
        if (enrichItem?.id) deleteEnrichItemMutate({ variables: { id: enrichItem.id } });
      }}
      columns={[
        { header: "Name", content: (enrichItem) => formatName(enrichItem) },
        { header: "Description", content: (enrichItem) => enrichItem?.description },
        { header: "Type", content: (enrichItem) => enrichItem?.type },
        { header: "Service Name", content: (enrichItem) => enrichItem?.serviceName },
      ]}
    />
  );
}
