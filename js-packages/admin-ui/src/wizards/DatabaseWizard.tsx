import React from "react";
import ClayButton from "@clayui/button";
import {
  BooleanInput,
  ContainerFluid,
  CronInput,
  CustomButtom,
  CustomFormGroup,
  fromFieldValidators,
  TextInput,
  useForm,
} from "../components/Form";
import { gql } from "@apollo/client";
import { DataSourcesQuery } from "../components/DataSources";
import { useNavigate } from "react-router-dom";
import { useWizardPluginDriverBinding } from "../components/PluginDriver";
import { useTriggerSchedulerMutation } from "../components/DataSource";
import { useCreateWebCrawlerDataSourceMutation } from "../graphql-generated";
import { ClassNameButton } from "../App";

gql`
  mutation CreateSitemapDataSource(
    $name: String!
    $description: String
    $schedulable: Boolean!
    $scheduling: String!
    $jsonConfig: String
  ) {
    datasource(
      datasourceDTO: { name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig }
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

export function DatabaseWizard() {
  const navigate = useNavigate();
  const triggerSchedulerMutation = useTriggerSchedulerMutation();
  const bindPluginDriver = useWizardPluginDriverBinding("data-base");
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
        triggerSchedulerMutation.mutate(data.datasource.entity.id);
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        scheduling: "0 0 * ? * *",
        dialect: "",
        driver: "",
        user: "",
        password: "",
        host: "",
        port: "",
        db: "",
        table: "",
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
          jsonConfig: JSON.stringify(
            {
              dialect: data.dialect,
              driver: data.driver,
              user: data.user,
              password: data.password,
              host: data.host,
              port: data.port,
              db: data.db,
              table: data.table,
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
        <CustomFormGroup className="form-group-autofit">
          <TextInput label="Host" {...form.inputProps("host")} item />
          <TextInput label="Port" {...form.inputProps("port")} item />
          <TextInput label="Dialect" {...form.inputProps("dialect")} item />
          <TextInput label="Driver" {...form.inputProps("driver")} item />
        </CustomFormGroup>
        <CustomFormGroup className="form-group-autofit">
          <TextInput label="User" {...form.inputProps("user")} item />
          <TextInput label="Password" {...form.inputProps("password")} item />
        </CustomFormGroup>
        <CustomFormGroup className="form-group-autofit">
          <TextInput label="Database" {...form.inputProps("db")} item />
          <TextInput label="Table" {...form.inputProps("table")} item />
        </CustomFormGroup>
        <CronInput label="Scheduling" {...form.inputProps("scheduling")} />
        <BooleanInput label="Index on Create" {...form.inputProps("reindex")} />
        <div className="sheet-footer">
          <CustomButtom nameButton={"Create"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
