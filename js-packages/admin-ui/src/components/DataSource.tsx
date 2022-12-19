import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import {
  useDataSourceQuery,
  useCreateOrUpdateDataSourceMutation,
  usePluginDriverValueQuery,
  useDataIndexValueQuery,
  useEnrichPipelineValueQuery,
  usePluginDriverOptionsQuery,
  useDataIndexOptionsQuery,
  useEnrichPipelineOptionsQuery,
  useBindPluginDriverToDataSourceMutation,
  useBindDataIndexToDataSourceMutation,
  useBindEnrichPipelineToDataSourceMutation,
  useUnbindPluginDriverFromDataSourceMutation,
  useUnbindDataIndexToDataSourceMutation,
  useUnbindEnrichPipelineToDataSourceMutation,
} from "../graphql-generated";
import { BooleanInput, CronInput, fromFieldValidators, SearchSelect, TextArea, TextInput, useForm } from "./Form";
import { CodeInput } from "./CodeInput";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import { DataSourcesQuery } from "./DataSources";
import ClayLayout from "@clayui/layout";
import ClayToolbar from "@clayui/toolbar";
import { useRestClient } from "./queryClient";
import { useMutation } from "@tanstack/react-query";
import { useToast } from "./ToastProvider";

const DataSourceQuery = gql`
  query DataSource($id: ID!) {
    datasource(id: $id) {
      id
      name
      description
      schedulable
      scheduling
      jsonConfig
      pluginDriver {
        id
      }
      dataIndex {
        id
      }
      enrichPipeline {
        id
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateDataSource(
    $id: ID
    $name: String!
    $description: String
    $schedulable: Boolean
    $scheduling: String!
    $jsonConfig: String
  ) {
    datasource(
      id: $id
      datasourceDTO: { name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig }
    ) {
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

export function DataSource() {
  const { datasourceId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const datasourceQuery = useDataSourceQuery({
    variables: { id: datasourceId as string },
    skip: !datasourceId || datasourceId === "new",
  });
  const [createOrUpdateDataSourceMutate, createOrUpdateDataSourceMutation] = useCreateOrUpdateDataSourceMutation({
    refetchQueries: [DataSourceQuery, DataSourcesQuery],
    onCompleted(data) {
      if (data.datasource?.entity) {
        if (datasourceId === "new") {
          navigate(`/data-sources/`, { replace: true });
          showToast({ displayType: "success", title: "Data source created", content: data.datasource.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Data source updated", content: data.datasource.entity.name ?? "" });
        }
      }
    },
  });
  const triggerSchedulerMutation = useTriggerSchedulerMutation();
  const reindexMutation = useReindexMutation();
  const generateDocumentTypesMutation = useGenerateDocumentTypesMutation();
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        schedulable: false,
        jsonConfig: "{}",
        scheduling: "",
      }),
      []
    ),
    originalValues: datasourceQuery.data?.datasource,
    isLoading: datasourceQuery.loading || createOrUpdateDataSourceMutation.loading,
    onSubmit(data) {
      createOrUpdateDataSourceMutate({ variables: { id: datasourceId !== "new" ? datasourceId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateDataSourceMutation.data?.datasource?.fieldValidators),
  });
  if (!datasourceId) return null;
  return (
    <React.Fragment>
      {datasourceId !== "new" && (
        <ClayToolbar light>
          <ClayLayout.ContainerFluid>
            <ClayToolbar.Nav className="justify-content-end">
              <ClayToolbar.Item>
                <ClayButton.Group>
                  <ClayButton
                    displayType="secondary"
                    disabled={generateDocumentTypesMutation.isLoading}
                    onClick={() => generateDocumentTypesMutation.mutate(datasourceId)}
                  >
                    Generate Document Types
                  </ClayButton>
                  <ClayButton
                    displayType="secondary"
                    disabled={triggerSchedulerMutation.isLoading}
                    onClick={() => triggerSchedulerMutation.mutate(datasourceId)}
                  >
                    Trigger Scheduler
                  </ClayButton>
                  <ClayButton
                    displayType="secondary"
                    disabled={reindexMutation.isLoading}
                    onClick={() => reindexMutation.mutate(datasourceId)}
                  >
                    Reindex
                  </ClayButton>
                </ClayButton.Group>
              </ClayToolbar.Item>
            </ClayToolbar.Nav>
          </ClayLayout.ContainerFluid>
        </ClayToolbar>
      )}
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
          <BooleanInput label="Schedulable" {...form.inputProps("schedulable")} />
          <CronInput label="Scheduling" {...form.inputProps("scheduling")} />
          {datasourceId !== "new" && (
            <ClayForm
              onSubmit={(event) => {
                event.preventDefault();
              }}
            >
              <SearchSelect
                label="Plugin Driver"
                value={datasourceQuery.data?.datasource?.pluginDriver?.id}
                useValueQuery={usePluginDriverValueQuery}
                useOptionsQuery={usePluginDriverOptionsQuery}
                useChangeMutation={useBindPluginDriverToDataSourceMutation}
                mapValueToMutationVariables={(pluginDriverId) => ({ datasourceId, pluginDriverId })}
                useRemoveMutation={useUnbindPluginDriverFromDataSourceMutation}
                mapValueToRemoveMutationVariables={() => ({ datasourceId })}
                invalidate={() => datasourceQuery.refetch()}
              />
              <SearchSelect
                label="Data Index"
                value={datasourceQuery.data?.datasource?.dataIndex?.id}
                useValueQuery={useDataIndexValueQuery}
                useOptionsQuery={useDataIndexOptionsQuery}
                useChangeMutation={useBindDataIndexToDataSourceMutation}
                mapValueToMutationVariables={(dataIndexId) => ({ datasourceId, dataIndexId })}
                useRemoveMutation={useUnbindDataIndexToDataSourceMutation}
                mapValueToRemoveMutationVariables={() => ({ datasourceId })}
                invalidate={() => datasourceQuery.refetch()}
              />
              <SearchSelect
                label="Enrich Pipeline"
                value={datasourceQuery.data?.datasource?.enrichPipeline?.id}
                useValueQuery={useEnrichPipelineValueQuery}
                useOptionsQuery={useEnrichPipelineOptionsQuery}
                useChangeMutation={useBindEnrichPipelineToDataSourceMutation}
                mapValueToMutationVariables={(enrichPipelineId) => ({ datasourceId, enrichPipelineId })}
                useRemoveMutation={useUnbindEnrichPipelineToDataSourceMutation}
                mapValueToRemoveMutationVariables={() => ({ datasourceId })}
                invalidate={() => datasourceQuery.refetch()}
              />
            </ClayForm>
          )}
          <CodeInput language="json" label="Configuration" {...form.inputProps("jsonConfig")} />

          <div className="sheet-footer">
            <ClayButton type="submit" disabled={!form.canSubmit}>
              {datasourceId === "new" ? "Create" : "Update"}
            </ClayButton>
          </div>
        </ClayForm>
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}

gql`
  query PluginDriverOptions($searchText: String, $cursor: String) {
    options: pluginDrivers(searchText: $searchText, first: 5, after: $cursor) {
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
  query PluginDriverValue($id: ID!) {
    value: pluginDriver(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindPluginDriverToDataSource($datasourceId: ID!, $pluginDriverId: ID!) {
    bindPluginDriverToDatasource(datasourceId: $datasourceId, pluginDriverId: $pluginDriverId) {
      left {
        id
        pluginDriver {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindPluginDriverFromDataSource($datasourceId: ID!) {
    unbindPluginDriverToDatasource(datasourceId: $datasourceId) {
      pluginDriver {
        id
      }
    }
  }
`;

gql`
  query EnrichPipelineOptions($searchText: String, $cursor: String) {
    options: enrichPipelines(searchText: $searchText, first: 5, after: $cursor) {
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
  query EnrichPipelineValue($id: ID!) {
    value: enrichPipeline(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindEnrichPipelineToDataSource($datasourceId: ID!, $enrichPipelineId: ID!) {
    bindEnrichPipelineToDatasource(datasourceId: $datasourceId, enrichPipelineId: $enrichPipelineId) {
      left {
        id
        enrichPipeline {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindEnrichPipelineToDataSource($datasourceId: ID!) {
    unbindEnrichPipelineToDatasource(datasourceId: $datasourceId) {
      enrichPipeline {
        id
      }
    }
  }
`;

gql`
  query DataIndexOptions($searchText: String, $cursor: String) {
    options: dataIndices(searchText: $searchText, first: 5, after: $cursor) {
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
  query DataIndexValue($id: ID!) {
    value: dataIndex(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindDataIndexToDataSource($datasourceId: ID!, $dataIndexId: ID!) {
    bindDataIndexToDatasource(datasourceId: $datasourceId, dataIndexId: $dataIndexId) {
      left {
        id
        dataIndex {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindDataIndexToDataSource($datasourceId: ID!) {
    unbindDataIndexFromDatasource(datasourceId: $datasourceId) {
      dataIndex {
        id
      }
    }
  }
`;

export function useTriggerSchedulerMutation() {
  const restClient = useRestClient();
  const showToast = useToast();
  return useMutation(
    async (datasourceId: string) => {
      await restClient.triggerResource.postV1Trigger({ datasourceIds: [Number(datasourceId)] });
    },
    {
      onSuccess(data, variables, context) {
        showToast({ displayType: "info", title: "Scheduler triggered", content: "" });
      },
    }
  );
}

export function useReindexMutation() {
  const restClient = useRestClient();
  const showToast = useToast();
  return useMutation(
    async (datasourceId: string) => {
      await restClient.reindexResource.postV1IndexReindex({ datasourceIds: [Number(datasourceId)] });
    },
    {
      onSuccess(data, variables, context) {
        showToast({ displayType: "info", title: "Reindex started", content: "" });
      },
    }
  );
}

export function useGenerateDocumentTypesMutation() {
  const restClient = useRestClient();
  const showToast = useToast();
  return useMutation(
    async (datasourceId: string) => {
      await restClient.dataIndexResource.postV1DataIndexAutoGenerateDocTypes({ datasourceId: Number(datasourceId) });
    },
    {
      onSuccess(data, variables, context) {
        showToast({ displayType: "info", title: "Auto-generated document types", content: "" });
      },
    }
  );
}
