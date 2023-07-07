import React from "react";
import { gql } from "@apollo/client";
import { useCreateOrUpdateDataSourceMutation, useDataSourcesQuery, useDeleteDataSourceMutation } from "../graphql-generated";
import { formatDate, formatName, Table } from "./Table";
import { useReindexMutation, useTriggerSchedulerMutation } from "./DataSource";
import { useToast } from "./ToastProvider";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";

export const DataSourcesQuery = gql`
  query DataSources($searchText: String, $cursor: String) {
    datasources(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          schedulable
          lastIngestionDate
          scheduling
          jsonConfig
          description
          reindex
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
  mutation DeleteDataSource($id: ID!) {
    deleteDatasource(datasourceId: $id) {
      id
      name
    }
  }
`;

export function DataSources() {
  const dataSourcesQuery = useDataSourcesQuery();
  const showToast = useToast();
  const [deleteDataSourceMutate] = useDeleteDataSourceMutation({
    refetchQueries: [DataSourcesQuery],
    onCompleted(data) {
      if (data.deleteDatasource?.id) {
        showToast({ displayType: "success", title: "Data source deleted", content: data.deleteDatasource.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Data source error", content: error.message ?? "" });
    },
  });
  const triggerSchedulerMutation = useTriggerSchedulerMutation();
  const reindexMutation = useReindexMutation();
  const [updateDataSourceMutate] = useCreateOrUpdateDataSourceMutation({
    refetchQueries: [DataSourcesQuery],
  });
  return (
    <Table
      data={{
        queryResult: dataSourcesQuery,
        field: (data) => data?.datasources,
      }}
      onCreatePath="/data-sources/new"
      onDelete={(dataSource) => {
        if (dataSource?.id) deleteDataSourceMutate({ variables: { id: dataSource.id } });
      }}
      columns={[
        { header: "Name", content: (dataSource) => formatName(dataSource) },
        {
          header: "Schedulable",
          content: (dataSource) => (
            <React.Fragment>
              <ClayToggle
                toggled={dataSource?.schedulable ?? false}
                onToggle={(schedulable) => {
                  if (dataSource && dataSource.id && dataSource.name && dataSource.scheduling)
                    updateDataSourceMutate({
                      variables: {
                        id: dataSource.id,
                        schedulable,
                        name: dataSource.name,
                        scheduling: dataSource.scheduling,
                        jsonConfig: dataSource.jsonConfig ?? "{}",
                        description: dataSource.description ?? "",
                        reindex: dataSource.reindex || false,
                      },
                    });
                }}
              />
              <style type="text/css">{StyleToggle}</style>
            </React.Fragment>
          ),
        },
        { header: "Last Ingestion Date", content: (dataSource) => formatDate(dataSource?.lastIngestionDate) },
        { header: "Scheduling", content: (dataSource) => dataSource?.scheduling },
      ]}
      rowActions={(dataSource) => {
        const dataSourceId = dataSource?.id;
        if (dataSourceId) {
          return [
            {
              label: "Trigger Scheduler",
              icon: "",
              onClick() {
                triggerSchedulerMutation.mutate(dataSourceId);
              },
            },
            {
              label: "Reindex",
              icon: "",
              onClick() {
                reindexMutation.mutate(dataSourceId);
              },
            },
          ];
        } else return [];
      }}
    />
  );
}
