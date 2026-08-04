/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import {
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  CustomSelectRelationsOneToOne,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import AssociationsLayout from "@components/Form/Tabs/LayoutTab";
import { TooltipDescription } from "@components/Form/utils";
import { Box, Button } from "@mui/material";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import React, { useState } from "react";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import {
  BucketDataSourcesQuery,
  RagType,
  RetrieveType,
  useAutocompletesOptionsQuery,
  useAutocorrectionsOptionsQuery,
  useBucketDataSourcesQuery,
  useBucketQuery,
  useCreateOrUpdateBucketMutation,
  useDataSourcesQuery,
  useHighlightsQuery,
  useLanguagesQuery,
  useSuggestionCategoriesQuery,
  useTabsQuery,
  useUnboundRagConfigurationsByBucketQuery,
} from "../../graphql-generated";
import { AssociatedUnassociated, formatQueryToBE, formatQueryToFE } from "../../utils";

import RefreshOptionsLayout from "@components/Form/Inputs/CheckboxOptionsLayout";
import { AutocompleteDropdown, AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import { useLanguages, useOptionSearchConfig, useQueryAnaylyses } from "../../../src/utils/RelationOneToOne";
import useOptions from "../../utils/getOptions";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { sxCheckbox, sxControl } from "../../utils/styleConfig";

const getAssociationTabs = (t: TFunction): Array<{ label: string; id: string; tooltip?: string }> => [
  {
    label: t("pages.buckets.tab-datasource"),
    id: "datasourceIds",
    tooltip: t("pages.buckets.datasources-associated-to-current-bucket"),
  },
  {
    label: t("pages.buckets.tab-suggestion-category"),
    id: "suggestionCategoryIds",
    tooltip: t("pages.buckets.suggestion-categories-associated-to-current-bucket"),
  },
  { label: t("pages.buckets.tab-tabs"), id: "tabIds", tooltip: t("pages.buckets.tabs-associated-to-current-bucket") },
  {
    label: t("pages.buckets.tab-language"),
    id: "languageIds",
    tooltip: t("pages.buckets.languages-associated-to-current-bucket"),
  },
];

export function SaveBucket({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const associationTabs = React.useMemo(() => getAssociationTabs(t), [t]);
  const { bucketId = "new", view } = useParams();
  const [page, setPage] = React.useState<number>(0);
  const isRecap = page === 1;
  const isNew = bucketId === "new";
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.buckets.edit-bucket"),
    body: t("pages.buckets.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const [selectedAssociationTabs, setSelectedAssociationTabs] = useState<string>(associationTabs[0].id);
  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/bucket/${bucketId}`);
    }
  };

  const bucketQuery = useBucketQuery({
    variables: { id: bucketId as string },
    skip: !bucketId || bucketId === "new",
    fetchPolicy: "network-only",
  });

  const ragConfigurationChatRag = useUnboundRagConfigurationsByBucketQuery({
    variables: { bucketId: bucketId === "new" ? "0" : bucketId, ragType: RagType.ChatRag },
    fetchPolicy: "network-only",
  });

  const ragConfigurationChatRagTool = useUnboundRagConfigurationsByBucketQuery({
    variables: { bucketId: bucketId === "new" ? "0" : bucketId, ragType: RagType.ChatRagTool },
    fetchPolicy: "network-only",
  });

  const ragConfigurationSimpleGenerate = useUnboundRagConfigurationsByBucketQuery({
    variables: { bucketId: bucketId === "new" ? "0" : bucketId, ragType: RagType.SimpleGenerate },
    fetchPolicy: "network-only",
  });

  const bucketDataSources = useBucketDataSourcesQuery({
    variables: { parentId: bucketId as string, unassociated: true },
    skip: !bucketId || bucketId === "new",
    fetchPolicy: "network-only",
  });

  const bucketDataSourcesAssociated = useBucketDataSourcesQuery({
    variables: { parentId: bucketId as string, unassociated: false },
    skip: !bucketId || bucketId === "new",
    fetchPolicy: "network-only",
  });

  const { OptionQuery: autocorrectionOption } = useOptions({
    queryKeyPath: "autocorrections.edges",
    useQuery: useAutocorrectionsOptionsQuery,
    accessKey: "node",
    isNetworkOnly: true,
  });

  const { OptionQuery: autocompleteOption } = useOptions({
    queryKeyPath: "autocompletes.edges",
    useQuery: useAutocompletesOptionsQuery,
    accessKey: "node",
    isNetworkOnly: true,
  });

  const { OptionQuery: highlightOption } = useOptions({
    queryKeyPath: "highlights",
    useQuery: useHighlightsQuery,
    isNetworkOnly: true,
  });

  const toast = useToast();
  const [createOrUpdateBucketMutate, createOrUpdateBucketMutation] = useCreateOrUpdateBucketMutation({
    refetchQueries: ["Buckets", "BucketDataSources"],
    onCompleted(data) {
      if (data.bucketWithLists?.entity) {
        const isNew = bucketId === "new" ? "created" : "updated";
        toast({
          title: isNew === "created" ? t("pages.buckets.created-title") : t("pages.buckets.updated-title"),
          content: isNew === "created" ? t("pages.buckets.created-content") : t("pages.buckets.updated-content"),
          displayType: "success",
        });
        const redirectPath = `/buckets/`;
        navigate(redirectPath, { replace: true });
      } else {
        toast({
          title: t("common.error"),
          content: combineErrorMessages(data.bucketWithLists?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      const isNew = bucketId === "new" ? "create" : "update";
      toast({
        title: isNew === "create" ? t("pages.buckets.create-error-title") : t("pages.buckets.update-error-title"),
        content: isNew === "create" ? t("pages.buckets.create-error-content") : t("pages.buckets.update-error-content"),
        displayType: "error",
      });
    },
  });

  const { datasources, suggestionCategories, tabs, languages } = useBucketData({
    bucketId,
    bucketQuery: bucketDataSources.data,
    associatedBucketQuery: bucketDataSourcesAssociated.data,
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        enabled: false,
        refreshOnDate: false,
        refreshOnQuery: false,
        refreshOnSuggestionCategory: false,
        refreshOnTab: false,
        retrieveType: RetrieveType.Text,
        datasourceIds: datasources?.associated || [],
        suggestionCategoryIds: suggestionCategories?.associated || [],
        tabIds: tabs?.associated || [],
        languageIds: languages?.associated || [],
        queryAnalysisId: bucketQuery.data?.bucket?.queryAnalysis?.id
          ? {
            id: bucketQuery.data?.bucket?.queryAnalysis?.id,
            name: bucketQuery.data?.bucket?.queryAnalysis?.name,
          }
          : undefined,
        defaultLanguageId: bucketQuery.data?.bucket?.language?.id
          ? {
            id: bucketQuery.data?.bucket?.language?.id,
            name: bucketQuery.data?.bucket?.language?.name,
          }
          : undefined,
        searchConfigId: bucketQuery.data?.bucket?.searchConfig?.id
          ? {
            id: bucketQuery.data?.bucket?.searchConfig?.id,
            name: bucketQuery.data?.bucket?.searchConfig?.name,
          }
          : undefined,
        ragConfigurationChatId: bucketQuery.data?.bucket?.ragConfigurationChat?.id
          ? {
            id: bucketQuery.data?.bucket?.ragConfigurationChat?.id,
            name: bucketQuery.data?.bucket?.ragConfigurationChat?.name,
          }
          : undefined,
        ragConfigurationChatToolId: bucketQuery.data?.bucket?.ragConfigurationChatTool?.id
          ? {
            id: bucketQuery.data?.bucket?.ragConfigurationChatTool?.id,
            name: bucketQuery.data?.bucket?.ragConfigurationChatTool?.name,
          }
          : undefined,
        ragConfigurationSimpleGenerateId: bucketQuery.data?.bucket?.ragConfigurationSimpleGenerate?.id
          ? {
            id: bucketQuery.data?.bucket?.ragConfigurationSimpleGenerate?.id,
            name: bucketQuery.data?.bucket?.ragConfigurationSimpleGenerate?.name,
          }
          : undefined,
        autocorrectionId: {
          id: bucketQuery.data?.bucket?.autocorrection?.id || "-1",
          name: bucketQuery.data?.bucket?.autocorrection?.name || "",
        },
        autocompleteId: {
          id: bucketQuery.data?.bucket?.autocomplete?.id || "-1",
          name: bucketQuery.data?.bucket?.autocomplete?.name || "",
        },
        highlightId: {
          id: bucketQuery.data?.bucket?.highlight?.id || "-1",
          name: bucketQuery.data?.bucket?.highlight?.name || "",
        },
      }),
      [datasources, suggestionCategories, tabs, languages, bucketQuery],
    ),
    originalValues: bucketQuery.data?.bucket,
    isLoading: bucketQuery.loading || createOrUpdateBucketMutation.loading,
    onSubmit(data) {
      createOrUpdateBucketMutate({
        variables: {
          id: bucketId !== "new" ? bucketId : undefined,
          ...data,
          retrieveType: data.retrieveType,
          datasourceIds: formatQueryToBE({
            information: data.datasourceIds,
          }),
          suggestionCategoryIds: formatQueryToBE({
            information: data.suggestionCategoryIds,
          }),
          tabIds: formatQueryToBE({
            information: data.tabIds,
          }),
          languageIds: formatQueryToBE({
            information: data.languageIds,
          }),
          searchConfigId: data?.searchConfigId?.id ? data.searchConfigId.id : undefined,
          defaultLanguageId: data.defaultLanguageId?.id ? data?.defaultLanguageId?.id : undefined,
          queryAnalysisId: data?.queryAnalysisId?.id ? data?.queryAnalysisId?.id : undefined,
          ragConfigurationChat: data?.ragConfigurationChatId?.id ? data?.ragConfigurationChatId?.id : undefined,
          ragConfigurationChatTool: data.ragConfigurationChatToolId?.id
            ? data?.ragConfigurationChatToolId?.id
            : undefined,
          ragConfigurationSimpleGenerate: data?.ragConfigurationSimpleGenerateId?.id
            ? data.ragConfigurationSimpleGenerateId.id
            : undefined,
          autocorrection: data.autocorrectionId.id !== "-1" ? data.autocorrectionId.id : null,
          autocomplete: data.autocompleteId.id !== "-1" ? data.autocompleteId.id : null,
          highlightId: data.highlightId.id !== "-1" ? data.highlightId.id : null,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateBucketMutation.data?.bucketWithLists?.fieldValidators),
  });

  if (bucketQuery.loading) return null;

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "refreshOnDate", label: t("pages.buckets.refresh-on-date") },
          { key: "refreshOnQuery", label: t("pages.buckets.refresh-on-query") },
          { key: "refreshOnSuggestionCategory", label: t("pages.buckets.refresh-on-suggestion-category") },
          { key: "refreshOnTab", label: t("pages.buckets.refresh-on-tab") },
          { key: "retrieveType", label: t("pages.buckets.retriever-type") },
          { key: "datasourceIds", label: t("pages.buckets.datasources") },
          { key: "suggestionCategoryIds", label: t("pages.buckets.suggestion-categories") },
          { key: "tabIds", label: t("pages.buckets.tabs") },
          { key: "languageIds", label: t("pages.buckets.languages") },
          { key: "queryAnalysisId", label: t("pages.buckets.query-analysis") },
          { key: "defaultLanguageId", label: t("pages.buckets.default-language") },
          { key: "searchConfigId", label: t("pages.buckets.search-configuration") },
          { key: "ragConfigurationChatId", label: t("pages.buckets.rag-configuration-chat") },
          { key: "ragConfigurationChatToolId", label: t("pages.buckets.rag-configuration-chat-tool") },
          { key: "ragConfigurationSimpleGenerateId", label: t("pages.buckets.rag-configuration-simple-generate") },
          { key: "highlightId", label: t("pages.highlights.entity-name") },
        ],
        label: t("pages.buckets.recap-label"),
      },
    ],
    valueOverride: {
      searchConfigId: form.inputProps("searchConfigId").value?.name || "",
      defaultLanguageId: form.inputProps("defaultLanguageId").value?.name || "",
      queryAnalysisId: form.inputProps("queryAnalysisId").value?.name || "",
      ragConfigurationChatId: form.inputProps("ragConfigurationChatId").value?.name || "",
      ragConfigurationChatToolId: form.inputProps("ragConfigurationChatToolId").value?.name || "",
      autocorrectionId: form.inputProps("autocorrectionId").value?.name || "",
      ragConfigurationSimpleGenerateId: form.inputProps("ragConfigurationSimpleGenerateId").value?.name || "",
      highlightId: form.inputProps("highlightId").value?.name || "",
      datasourceIds: form.inputProps("datasourceIds").value?.map((ds, index) => ({ [index + 1]: ds.label })) || [],
      suggestionCategoryIds:
        form.inputProps("suggestionCategoryIds").value?.map((sc, index) => ({ [index + 1]: sc.label })) || [],
      tabIds: form.inputProps("tabIds").value?.map((tab, index) => ({ [index + 1]: tab.label })) || [],
      languageIds:
        form.inputProps("languageIds").value?.map((lang, index) => ({ [index + 1]: lang.label })) || [],
    },
  });

  return (
    <ContainerFluid style={{ width: "55%" }}>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity={t("pages.buckets.entity-name")}
            description={t("pages.buckets.create-or-edit-a-bucket-to-construct")}
            id={bucketId}
          />
          {view === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              Edit
            </Button>
          )}
        </Box>
        <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
          <CreateDataEntity
            form={form}
            page={page}
            id={bucketId}
            pathBack="/buckets/"
            setPage={setPage}
            haveConfirmButton={!view}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput label={t("common.name")} {...form.inputProps("name")} />
                    <TextArea label={t("common.description")} {...form.inputProps("description")} />
                    <RefreshOptionsLayout>
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label={t("fields.date")}
                        {...form.inputProps("refreshOnDate")}
                      />
                      <TooltipDescription informationDescription="Refresh filters when date filter is applied" />
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label={t("fields.query")}
                        {...form.inputProps("refreshOnQuery")}
                      />
                      <TooltipDescription informationDescription="Refresh filters when query search is performed" />
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label={t("fields.suggestioncategory")}
                        {...form.inputProps("refreshOnSuggestionCategory")}
                      />
                      <TooltipDescription informationDescription="Refresh filters when filters are applied" />
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label={t("fields.tab")}
                        {...form.inputProps("refreshOnTab")}
                      />
                      <TooltipDescription informationDescription="Refresh filters Tab is applied" />
                    </RefreshOptionsLayout>

                    <CustomSelect
                      label={t("fields.retriver-type")}
                      dict={RetrieveType}
                      {...form.inputProps("retrieveType")}
                      description={t("pages.buckets.retriever-type-used-to-search-data-if")}
                    />
                    <AssociationsLayout tabs={associationTabs} setTabsId={setSelectedAssociationTabs}>
                      <MultiAssociationCustomQuery
                        list={{
                          ...datasources,
                          associated: form.inputProps("datasourceIds").value,
                        }}
                        sx={selectedAssociationTabs === "datasourceIds" ? {} : { display: "none" }}
                        isLoading={datasources.isLoading}
                        disabled={isRecap || view === "view"}
                        isRecap={page === 1}
                        createPath={{ path: "/data-source/new/mode/create/landingTab/0", entity: "data-sources" }}
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("datasourceIds").value;

                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("datasourceIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("datasourceIds").onChange(updatedData);
                          }
                        }}
                      />
                      <MultiAssociationCustomQuery
                        list={{
                          ...suggestionCategories,
                          associated: form.inputProps("suggestionCategoryIds").value,
                        }}
                        sx={selectedAssociationTabs === "suggestionCategoryIds" ? {} : { display: "none" }}
                        isLoading={suggestionCategories.isLoading}
                        disabled={isRecap || view === "view"}
                        isRecap={page === 1}
                        createPath={{ path: "/suggestion-category/new", entity: "suggestion-categories" }}
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("suggestionCategoryIds").value;

                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("suggestionCategoryIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("suggestionCategoryIds").onChange(updatedData);
                          }
                        }}
                      />
                      <MultiAssociationCustomQuery
                        list={{
                          ...tabs,
                          associated: form.inputProps("tabIds").value,
                        }}
                        sx={selectedAssociationTabs === "tabIds" ? {} : { display: "none" }}
                        disabled={isRecap || view === "view"}
                        isLoading={tabs.isLoading}
                        createPath={{ path: "/tab/new", entity: "tabs" }}
                        isRecap={page === 1}
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("tabIds").value;

                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("tabIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("tabIds").onChange(updatedData);
                          }
                        }}
                      />
                      <MultiAssociationCustomQuery
                        list={{
                          ...languages,
                          associated: form.inputProps("languageIds").value,
                        }}
                        sx={selectedAssociationTabs === "languageIds" ? {} : { display: "none" }}
                        isLoading={languages.isLoading}
                        disabled={isRecap || view === "view"}
                        isRecap={page === 1}
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("languageIds").value;

                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("languageIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("languageIds").onChange(updatedData);
                          }
                        }}
                      />
                    </AssociationsLayout>
                    <Box display={"grid"} gridTemplateColumns={"1fr 1fr"} gap={"10px"} mt={"16px"}>
                      <AutocompleteDropdown
                        label={t("fields.search-config")}
                        onChange={(val) => form.inputProps("searchConfigId").onChange({ id: val.id, name: val.name })}
                        value={
                          !form?.inputProps("searchConfigId")?.value?.id
                            ? undefined
                            : {
                              id: form?.inputProps("searchConfigId")?.value?.id || "",
                              name: form?.inputProps("searchConfigId")?.value?.name || "",
                            }
                        }
                        onClear={() => form.inputProps("searchConfigId").onChange(undefined)}
                        disabled={page === 1}
                        useOptions={useOptionSearchConfig}
                      />
                      <AutocompleteDropdown
                        label={t("fields.language")}
                        onChange={(val) =>
                          form.inputProps("defaultLanguageId").onChange({ id: val.id, name: val.name })
                        }
                        value={
                          !form?.inputProps("defaultLanguageId")?.value?.id
                            ? undefined
                            : {
                              id: form?.inputProps("defaultLanguageId")?.value?.id || "",
                              name: form?.inputProps("defaultLanguageId")?.value?.name || "",
                            }
                        }
                        onClear={() => form.inputProps("defaultLanguageId").onChange(undefined)}
                        disabled={page === 1}
                        useOptions={useLanguages}
                      />
                      <AutocompleteDropdown
                        label={t("fields.query-analysis")}
                        onChange={(val) => form.inputProps("queryAnalysisId").onChange({ id: val.id, name: val.name })}
                        value={
                          !form?.inputProps("queryAnalysisId")?.value?.id
                            ? undefined
                            : {
                              id: form?.inputProps("queryAnalysisId")?.value?.id || "",
                              name: form?.inputProps("queryAnalysisId")?.value?.name || "",
                            }
                        }
                        onClear={() => form.inputProps("queryAnalysisId").onChange(undefined)}
                        disabled={page === 1}
                        useOptions={useQueryAnaylyses}
                      />
                      <AutocompleteDropdownWithOptions
                        label={t("fields.chat-rag")}
                        onChange={(val) =>
                          form.inputProps("ragConfigurationChatId").onChange({ id: val.id, name: val.name })
                        }
                        value={
                          !form?.inputProps("ragConfigurationChatId")?.value?.id
                            ? undefined
                            : {
                              id: form?.inputProps("ragConfigurationChatId")?.value?.id || "",
                              name: form?.inputProps("ragConfigurationChatId")?.value?.name || "",
                            }
                        }
                        onClear={() => form.inputProps("ragConfigurationChatId").onChange(undefined)}
                        disabled={page === 1}
                        optionsDefault={
                          ragConfigurationChatRag?.data?.unboundRAGConfigurationByBucket?.map((unbound) => ({
                            value: unbound?.id || "",
                            label: unbound?.name || "",
                          })) || []
                        }
                      />
                      <AutocompleteDropdownWithOptions
                        label={t("fields.chat-rag-tool")}
                        onChange={(val) =>
                          form.inputProps("ragConfigurationChatToolId").onChange({ id: val.id, name: val.name })
                        }
                        value={
                          !form?.inputProps("ragConfigurationChatToolId")?.value?.id
                            ? undefined
                            : {
                              id: form?.inputProps("ragConfigurationChatToolId")?.value?.id || "",
                              name: form?.inputProps("ragConfigurationChatToolId")?.value?.name || "",
                            }
                        }
                        onClear={() => form.inputProps("ragConfigurationChatToolId").onChange(undefined)}
                        disabled={page === 1}
                        optionsDefault={
                          ragConfigurationChatRagTool?.data?.unboundRAGConfigurationByBucket?.map((unbound) => ({
                            value: unbound?.id || "",
                            label: unbound?.name || "",
                          })) || []
                        }
                      />
                      <CustomSelectRelationsOneToOne
                        options={autocorrectionOption}
                        label={t("fields.autocorrection")}
                        onChange={(val) => form.inputProps("autocorrectionId").onChange({ id: val.id, name: val.name })}
                        value={{
                          id: form.inputProps("autocorrectionId").value.id,
                          name: form.inputProps("autocorrectionId").value.name || "",
                        }}
                        disabled={page === 1}
                      />
                      <CustomSelectRelationsOneToOne
                        options={autocompleteOption}
                        label={t("fields.autocomplete")}
                        onChange={(val) => form.inputProps("autocompleteId").onChange({ id: val.id, name: val.name })}
                        value={{
                          id: form.inputProps("autocompleteId").value.id,
                          name: form.inputProps("autocompleteId").value.name || "",
                        }}
                        disabled={page === 1}
                      />
                      <CustomSelectRelationsOneToOne
                        options={highlightOption}
                        label={t("pages.highlights.entity-name")}
                        description={t("pages.buckets.highlight-configuration-used-to-build-the")}
                        onChange={(val) => form.inputProps("highlightId").onChange({ id: val.id, name: val.name })}
                        value={{
                          id: form.inputProps("highlightId").value.id,
                          name: form.inputProps("highlightId").value.name || "",
                        }}
                        disabled={page === 1}
                      />
                      <AutocompleteDropdownWithOptions
                        label={t("fields.simple-generate")}
                        onChange={(val) =>
                          form.inputProps("ragConfigurationSimpleGenerateId").onChange({ id: val.id, name: val.name })
                        }
                        value={
                          !form?.inputProps("ragConfigurationSimpleGenerateId")?.value?.id
                            ? undefined
                            : {
                              id: form?.inputProps("ragConfigurationSimpleGenerateId")?.value?.id || "",
                              name: form?.inputProps("ragConfigurationSimpleGenerateId")?.value?.name || "",
                            }
                        }
                        onClear={() => form.inputProps("ragConfigurationSimpleGenerateId").onChange(undefined)}
                        disabled={page === 1}
                        optionsDefault={
                          ragConfigurationSimpleGenerate?.data?.unboundRAGConfigurationByBucket?.map((unbound) => ({
                            value: unbound?.id || "",
                            label: unbound?.name || "",
                          })) || []
                        }
                      />
                    </Box>
                    <Recap
                      recapData={recapSections}
                      setExtraFab={setExtraFab}
                      forceFullScreen={isRecap}
                      actions={{
                        onBack: () => setPage(0),
                        onSubmit: () => form.submit(),
                        submitLabel: isNew ? t("entity.create") : t("entity.update"),
                        backLabel: t("common.back"),
                      }}
                    />
                  </>
                ),
                page: 0,
                validation: !!view,
              },
              {
                validation: true,
              },
            ]}
            fieldsControll={["name"]}
          />
        </form>
      </>
      <ConfirmModal />
    </ContainerFluid>
  );
}

type ReturnUserBucketData = {
  datasources: AssociatedUnassociated;
  suggestionCategories: AssociatedUnassociated;
  tabs: AssociatedUnassociated;
  languages: AssociatedUnassociated;
};

const useBucketData = ({
  bucketId,
  bucketQuery,
  associatedBucketQuery,
}: {
  bucketId: string;
  bucketQuery: BucketDataSourcesQuery | undefined;
  associatedBucketQuery: BucketDataSourcesQuery | undefined;
}): ReturnUserBucketData => {
  const skipRecoveryAllInformation = bucketId !== "new";

  const datasourcesQuery = useDataSourcesQuery({
    skip: skipRecoveryAllInformation,
  });

  const suggestionCategoriesQuery = useSuggestionCategoriesQuery({
    skip: skipRecoveryAllInformation,
  });

  const tabsQuery = useTabsQuery({
    skip: skipRecoveryAllInformation,
  });

  const languagesQuery = useLanguagesQuery({
    skip: skipRecoveryAllInformation,
  });

  const data = React.useMemo(
    () => ({
      datasources: {
        unassociated: formatQueryToFE({
          informationId: datasourcesQuery.data?.datasources?.edges || bucketQuery?.bucket?.datasources?.edges,
        }),
        isLoading: datasourcesQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedBucketQuery?.bucket?.datasources?.edges,
        }),
      },
      suggestionCategories: {
        unassociated: formatQueryToFE({
          informationId:
            suggestionCategoriesQuery.data?.suggestionCategories?.edges ||
            bucketQuery?.bucket?.suggestionCategories?.edges,
        }),
        isLoading: suggestionCategoriesQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedBucketQuery?.bucket?.suggestionCategories?.edges,
        }),
      },
      tabs: {
        unassociated: formatQueryToFE({
          informationId: tabsQuery.data?.tabs?.edges || bucketQuery?.bucket?.tabs?.edges,
        }),
        isLoading: tabsQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedBucketQuery?.bucket?.tabs?.edges,
        }),
      },
      languages: {
        unassociated: formatQueryToFE({
          informationId: languagesQuery.data?.languages?.edges || bucketQuery?.bucket?.languages?.edges,
        }),
        isLoading: languagesQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedBucketQuery?.bucket?.languages?.edges,
        }),
      },
    }),
    [datasourcesQuery, suggestionCategoriesQuery, tabsQuery, languagesQuery, bucketQuery, associatedBucketQuery],
  );

  return { ...data };
};

