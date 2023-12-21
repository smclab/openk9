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

export function MinioWizard() {
  const navigate = useNavigate();
  const triggerSchedulerMutation = useTriggerSchedulerMutation();
  const bindPluginDriver = useWizardPluginDriverBinding("minio");
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
        accessKey: "",
        host: "",
        port: "",
        secretKey: "",
        bucketName: "",
        reindexRate: 0,
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
          reindexRate: 0,
          schedulable: true,
          jsonConfig: JSON.stringify(
            {
              host: data.host,
              port: data.port,
              accessKey: data.accessKey,
              secretKey: data.secretKey,
              bucketName: data.bucketName
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
        </CustomFormGroup>
        <CustomFormGroup className="form-group-autofit">
          <TextInput label="Access Key" {...form.inputProps("accessKey")} item />
          <TextInput label="Secret Key" {...form.inputProps("secretKey")} item />
          <TextInput label="Bucket Name" {...form.inputProps("bucketName")} item />
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
