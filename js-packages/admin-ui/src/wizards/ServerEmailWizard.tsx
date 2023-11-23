import React from "react";
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
import { DataSourcesQuery } from "../components/DataSources";
import { useCreateWebCrawlerDataSourceMutation } from "../graphql-generated";
import { useNavigate } from "react-router-dom";
import { useTriggerSchedulerMutation } from "../components/DataSource";
import { useWizardPluginDriverBinding } from "../components/PluginDriver";

export function ServerEmailWizard() {
  const navigate = useNavigate();
  const triggerSchedulerMutation = useTriggerSchedulerMutation();
  const bindPluginDriver = useWizardPluginDriverBinding("email-server");
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
        mailServer: "",
        port: "",
        username: "",
        password: "",
        folder: "INBOX",
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
              mailServer: data.mailServer,
              port: data.port,
              username: data.username,
              password: data.password,
              folder: data.folder,
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
        <TextInput label="Server email" {...form.inputProps("mailServer")} />
        <CustomFormGroup className="form-group-autofit">
          <TextInput label="Username" {...form.inputProps("username")} item />
          <TextInput label="Password" {...form.inputProps("password")} item />
        </CustomFormGroup>
        <TextInput label="Folder" {...form.inputProps("folder")} />
        <CronInput label="Scheduling" {...form.inputProps("scheduling")} />
        <div className="sheet-footer">
          <CustomButtom nameButton={"Create"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
