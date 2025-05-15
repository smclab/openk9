import React from "react";
import {
  BooleanInput,
  ContainerFluid,
  CronInput,
  CustomButtom,
  fromFieldValidators,
  NumberInput,
  TextInput,
  useForm,
} from "../components/Form";
import { gql } from "@apollo/client";
import { DataSourcesQuery } from "../components/DataSources";
import { useNavigate } from "react-router-dom";
import { useTriggerSchedulerMutation } from "../components/DataSource";
import { useWizardPluginDriverBinding } from "../components/PluginDriver";
import { useCreateWebCrawlerDataSourceMutation } from "../graphql-generated";

gql`
  mutation CreateSitemapDataSource(
    $name: String!
    $description: String
    $schedulable: Boolean!
    $scheduling: String!
    $jsonConfig: String
    $reindexRate: Int!
  ) {
    datasource(
      datasourceDTO: {
        name: $name
        description: $description
        schedulable: $schedulable
        scheduling: $scheduling
        jsonConfig: $jsonConfig
        reindexRate: $reindexRate
      }
    ) {
      entity {
        id
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;
export function DropBoxWizard() {
  const navigate = useNavigate();
  const triggerSchedulerMutation = useTriggerSchedulerMutation();
  const bindPluginDriver = useWizardPluginDriverBinding("github");
  const [createWebCrawlerDataSourceMutate, createWebCrawlerDataSourceMutation] = useCreateWebCrawlerDataSourceMutation({
    refetchQueries: [DataSourcesQuery],
    onCompleted(data) {
      if (data.datasource?.entity) {
        navigate(`/data-sources/${data.datasource?.entity?.id}`, { replace: true });
      }
      if (data.datasource?.entity?.id) {
        bindPluginDriver(data.datasource.entity.id);
      }
      if (form.inputProps("reindex").value && data.datasource?.entity?.id) {
        triggerSchedulerMutation.mutate({ datasourceId: data.datasource.entity.id });
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        scheduling: "0 0 * ? * *",
        token: "",
        reindex: true,
      }),
      []
    ),
    originalValues: undefined,
    isLoading: createWebCrawlerDataSourceMutation.loading,
    onSubmit(data) {
      createWebCrawlerDataSourceMutate({
        variables: {
          name: data.name,
          scheduling: data.scheduling,
          description: "",
          schedulable: true,
          reindexRate: 0,
          jsonConfig: JSON.stringify(
            {
              token: data.token,
            },
            null,
            2
          ),
        },
      });
    },
    getValidationMessages: fromFieldValidators(createWebCrawlerDataSourceMutation.data?.datasource?.fieldValidators),
  });
  return (
    <ContainerFluid>
      <form
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextInput label="Token" {...form.inputProps("token")} />
        <CronInput label="Scheduling" {...form.inputProps("scheduling")} />
        <div className="sheet-footer">
          <CustomButtom nameButton={"Create"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
