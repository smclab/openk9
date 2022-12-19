import React from "react";
import { gql } from "@apollo/client";
import { useDeletePluginDriverMutation, usePluginDriversQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const PluginDriversQuery = gql`
  query PluginDrivers($searchText: String, $cursor: String) {
    pluginDrivers(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          type
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
  mutation DeletePluginDriver($id: ID!) {
    deletePluginDriver(pluginDriverId: $id) {
      id
      name
    }
  }
`;

export function PluginDrivers() {
  const pluginDriversQuery = usePluginDriversQuery();
  const showToast = useToast();
  const [deletePluginDriverMutate] = useDeletePluginDriverMutation({
    refetchQueries: [PluginDriversQuery],
    onCompleted(data) {
      if (data.deletePluginDriver?.id) {
        showToast({ displayType: "success", title: "Plugin drivers deleted", content: data.deletePluginDriver.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Plungi drivers nerror", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: pluginDriversQuery,
        field: (data) => data?.pluginDrivers,
      }}
      onCreatePath="/plugin-drivers/new"
      onDelete={(pluginDriver) => {
        if (pluginDriver?.id) deletePluginDriverMutate({ variables: { id: pluginDriver.id } });
      }}
      columns={[
        { header: "Name", content: (pluginDriver) => formatName(pluginDriver) },
        { header: "Description", content: (pluginDriver) => pluginDriver?.description },
        { header: "Type", content: (pluginDriver) => pluginDriver?.type },
      ]}
    />
  );
}
