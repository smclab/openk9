import React from "react";
import ClayForm from "@clayui/form";
import ClayLayout from "@clayui/layout";
import ClayButton from "@clayui/button";
import { BooleanInput, CronInput, fromFieldValidators, StringListInput, TextInput, useForm } from "../components/Form";
import { gql } from "@apollo/client";
import { DataSourcesQuery } from "../components/DataSources";
import { useCreateWebCrawlerDataSourceMutation } from "../graphql-generated";
import { useNavigate } from "react-router-dom";
import { useTriggerSchedulerMutation } from "../components/DataSource";
import { useWizardPluginDriverBinding } from "../components/PluginDriver";

export function SiteMapWizard() {
  const navigate = useNavigate();
  const triggerSchedulerMutation = useTriggerSchedulerMutation();
  const bindPluginDriver = useWizardPluginDriverBinding("site-map");
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
        sitemapUrls: [] as Array<string>,
        allowedDomains: [] as Array<string>,
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
              sitemapUrls: data.sitemapUrls,
              allowedDomains: data.allowedDomains,
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
    <ClayLayout.ContainerFluid view>
      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <StringListInput label="Urls" {...form.inputProps("sitemapUrls")} />
        <StringListInput label="Allowed Domains" {...form.inputProps("allowedDomains")} />
        <CronInput label="Scheduling" {...form.inputProps("scheduling")} />
        <BooleanInput label="Index on Create" {...form.inputProps("reindex")} />
        <div className="sheet-footer">
          <ClayButton type="submit" disabled={!form.canSubmit}>
            Create
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
