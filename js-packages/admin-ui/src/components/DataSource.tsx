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
import {
  BooleanInput,
  ContainerFluid,
  CronInput,
  fromFieldValidators,
  SearchSelect,
  SimpleModal,
  TextArea,
  TextInput,
  useForm,
} from "./Form";
import { CodeInput } from "./CodeInput";
import ClayButton from "@clayui/button";
import { DataSourcesQuery } from "./DataSources";
import ClayToolbar from "@clayui/toolbar";
import { useRestClient } from "./queryClient";
import { useMutation } from "@tanstack/react-query";
import { useToast } from "./ToastProvider";
import { AddDataSourceToBucket, BucketsdataSources, RemoveDataSourceFromBucket } from "./BucketDataSource";
import { ClassNameButton } from "../App";
import { useModal } from "@clayui/core";
import ClayLoadingIndicator from "@clayui/loading-indicator";

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
    $schedulable: Boolean!
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
  const [loading, setLoading] = React.useState(false);
  const { observer: observerGenerate, onOpenChange: onOpenChangeGenerate, open: openGenerate } = useModal();
  const { observer: observerTrigger, onOpenChange: onOpenChangeTrigger, open: openTrigger } = useModal();
  const { observer: observerReindex, onOpenChange: onOpenChangeReindex, open: openReindex } = useModal();
  const datasourceQuery = useDataSourceQuery({
    variables: { id: datasourceId as string },
    skip: !datasourceId || datasourceId === "new",
  });
  const [createOrUpdateDataSourceMutate, createOrUpdateDataSourceMutation] = useCreateOrUpdateDataSourceMutation({
    refetchQueries: [DataSourceQuery, DataSourcesQuery, BucketsdataSources, AddDataSourceToBucket, RemoveDataSourceFromBucket],
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
      {loading && (
        <div
          style={{
            position: "absolute",
            top: "0",
            left: "0",
            width: "100%",
            height: "100%",
            background: "rgb(0 0 0 / 0.3)",
            zIndex: "2",
          }}
        >
          <ClayLoadingIndicator
            shape="squares"
            style={{
              position: "fixed",
              zIndex: "2",
              margin: "auto",
              top: "50%",
              left: "55%",
              cursor: "wait",
            }}
            displayType="primary"
            size="lg"
          />
        </div>
      )}
      {openGenerate && (
        <SimpleModal
          observer={observerGenerate}
          labelContinue={"yes"}
          labelCancel={"cancel"}
          actionContinue={() => {
            generateDocumentTypesMutation.mutate(datasourceId);
            onOpenChangeGenerate(false);
          }}
          actionCancel={() => {
            onOpenChangeGenerate(false);
          }}
          description="Are you sure you want to generate it?"
        />
      )}
      {openTrigger && (
        <SimpleModal
          observer={observerTrigger}
          labelContinue={"yes"}
          labelCancel={"cancel"}
          actionContinue={() => {
            triggerSchedulerMutation.mutate(datasourceId);
            onOpenChangeTrigger(false);
          }}
          actionCancel={() => {
            onOpenChangeTrigger(false);
          }}
          description="Are you sure you want to trigger it?"
        />
      )}
      {openReindex && (
        <SimpleModal
          observer={observerReindex}
          labelContinue={"yes"}
          labelCancel={"cancel"}
          actionContinue={() => {
            reindexMutation.mutate(datasourceId);
            onOpenChangeReindex(false);
            let count = 0;
            setLoading(true);
            const result = refetchDataIndex({
              dataIndex: datasourceQuery.data?.datasource?.dataIndex?.id,
              datasourceQuery,
              count,
              setLoading,
            });
            if (!result) {
              showToast({ displayType: "danger", title: "error reindex", content: "" });
            }
          }}
          actionCancel={() => {
            onOpenChangeReindex(false);
          }}
          description="Are you sure you want to reindex it?"
        />
      )}
      {datasourceId !== "new" && (
        <ClayToolbar light>
          <ContainerFluid>
            <ClayToolbar.Nav className="justify-content-end">
              <ClayToolbar.Item>
                <ClayButton.Group>
                  <ClayButton
                    displayType="secondary"
                    disabled={generateDocumentTypesMutation.isLoading}
                    onClick={() => onOpenChangeGenerate(true)}
                  >
                    Generate Document Types
                  </ClayButton>
                  <ClayButton
                    displayType="secondary"
                    disabled={triggerSchedulerMutation.isLoading}
                    onClick={() => onOpenChangeTrigger(true)}
                  >
                    Trigger Scheduler
                  </ClayButton>
                  <ClayButton displayType="secondary" disabled={reindexMutation.isLoading} onClick={() => onOpenChangeReindex(true)}>
                    Reindex
                  </ClayButton>
                </ClayButton.Group>
              </ClayToolbar.Item>
            </ClayToolbar.Nav>
          </ContainerFluid>
        </ClayToolbar>
      )}
      <ContainerFluid>
        <form
          className="sheet"
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("name")} />
          <TextArea label="Description" {...form.inputProps("description")} />
          <BooleanInput
            label="Schedulable"
            {...form.inputProps("schedulable")}
            description={"If datasource is automatically schedulable"}
          />
          <CronInput label="Scheduling" {...form.inputProps("scheduling")} />
          {datasourceId !== "new" && (
            <form
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
                description={"Plugin driver definition for external parser connection"}
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
                description={"Name of current data elasticsearch index"}
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
                description={"Definition of enrich pipeline applied to Datasource's data during processing"}
              />
            </form>
          )}
          <CodeInput
            language="json"
            label="Configuration"
            {...form.inputProps("jsonConfig")}
            description="Json configuration sended to corresponding external parser when execution start"
          />

          <div className="sheet-footer">
            <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
              {datasourceId === "new" ? "Create" : "Update"}
            </ClayButton>
          </div>
        </form>
      </ContainerFluid>
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
      await restClient.triggerResource.postApiDatasourceV1Trigger({ datasourceIds: [Number(datasourceId)] });
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
      await restClient.reindexResource.postApiDatasourceV1IndexReindex({ datasourceIds: [Number(datasourceId)] });
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
      await restClient.dataIndexResource.postApiDatasourceV1DataIndexAutoGenerateDocTypes({ datasourceId: Number(datasourceId) });
    },
    {
      onSuccess(data, variables, context) {
        showToast({ displayType: "info", title: "Auto-generated document types", content: "" });
      },
    }
  );
}

function refetchDataIndex({
  dataIndex,
  datasourceQuery,
  count,
  setLoading,
}: {
  dataIndex: string | null | undefined;
  datasourceQuery: any;
  count: number;
  setLoading: React.Dispatch<React.SetStateAction<boolean>>;
}): boolean {
  if (dataIndex && count < 3) {
    count++;
    window.setTimeout(() => datasourceQuery.refetch(), 1000);
    return refetchDataIndex({ dataIndex, datasourceQuery, count, setLoading });
  } else {
    if (count > 3) {
      setLoading(false);
      return false;
    }
    setLoading(false);
    return true;
  }
}
