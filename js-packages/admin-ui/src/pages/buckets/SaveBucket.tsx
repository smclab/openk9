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
import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  BucketDataSourcesQuery,
  RagType,
  RetrieveType,
  useBucketDataSourcesQuery,
  useBucketQuery,
  useCreateOrUpdateBucketMutation,
  useDataSourcesQuery,
  useLanguagesQuery,
  useQueryAnalysesQuery,
  useSearchConfigsQuery,
  useSuggestionCategoriesQuery,
  useTabsQuery,
  useUnboundRagConfigurationsByBucketQuery,
} from "../../graphql-generated";
import { AssociatedUnassociated, formatQueryToBE, formatQueryToFE } from "../../utils";
import useOptions from "../../utils/getOptions";

import RefreshOptionsLayout from "@components/Form/Inputs/CheckboxOptionsLayout";
import { useConfirmModal } from "../../utils/useConfirmModal";

const associationTabs: Array<{ label: string; id: string; tooltip?: string }> = [
  { label: "datasource", id: "datasourceIds", tooltip: "Datasources associated to current bucket" },
  {
    label: "suggestion category",
    id: "suggestionCategoryIds",
    tooltip: "Suggestion Categories associated to current bucket",
  },
  { label: "tabs", id: "tabIds", tooltip: "Tabs associated to current bucket" },
];

const sxCheckbox = {
  p: 0.5,
  "& .MuiSvgIcon-root": {
    fontSize: 16,
  },
};

const sxControl = {
  m: 0,
  mr: 0.5,
  ml: 1,
  cursor: "pointer",
  "& .MuiFormControlLabel-label": {
    fontSize: "0.875rem",
  },
};

export function SaveBucket() {
  const { bucketId = "new", view } = useParams();
  const [page, setPage] = React.useState<number>(0);
  const isRecap = page === 1;
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Bucket",
    body: "Are you sure you want to edit this bucket?",
    labelConfirm: "Edit",
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

  const { OptionQuery: optionSearchConfig } = useOptions({
    queryKeyPath: "searchConfigs.edges",
    useQuery: useSearchConfigsQuery,
    accessKey: "node",
  });
  const { OptionQuery: languageOptions } = useOptions({
    queryKeyPath: "languages.edges",
    useQuery: useLanguagesQuery,
    accessKey: "node",
  });
  const { OptionQuery: queryAnalysisOption } = useOptions({
    queryKeyPath: "queryAnalyses.edges",
    useQuery: useQueryAnalysesQuery,
    accessKey: "node",
  });
  const { OptionQuery: ragChatConfigurationOption } = useOptions({
    queryKeyPath: "unboundRAGConfigurationByBucket",
    data: ragConfigurationChatRag,
  });
  const { OptionQuery: ragChatSimpleGenerateConfigurationOption } = useOptions({
    queryKeyPath: "unboundRAGConfigurationByBucket",
    data: ragConfigurationSimpleGenerate,
  });
  const { OptionQuery: ragChatToolConfigurationOption } = useOptions({
    queryKeyPath: "unboundRAGConfigurationByBucket",
    data: ragConfigurationChatRagTool,
  });
  const toast = useToast();
  const [createOrUpdateBucketMutate, createOrUpdateBucketMutation] = useCreateOrUpdateBucketMutation({
    refetchQueries: ["Buckets", "BucketDataSources"],
    onCompleted(data) {
      if (data.bucketWithLists?.entity) {
        const isNew = bucketId === "new" ? "created" : "updated";
        toast({
          title: `Bucket ${isNew}`,
          content: `Bucket has been ${isNew} successfully`,
          displayType: "success",
        });
        const redirectPath = `/buckets/`;
        navigate(redirectPath, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.bucketWithLists?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      const isNew = bucketId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Bucket`,
        displayType: "error",
      });
    },
  });

  const { datasources, suggestionCategories, tabs } = useBucketData({
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
        retrieveType: RetrieveType.Hybrid,
        datasourceIds: datasources?.associated || [],
        suggestionCategoryIds: suggestionCategories?.associated || [],
        tabIds: tabs?.associated || [],
        queryAnalysisId: {
          id: bucketQuery.data?.bucket?.queryAnalysis?.id || "-1",
          name: bucketQuery.data?.bucket?.queryAnalysis?.name || "",
        },
        defaultLanguageId: {
          id: bucketQuery.data?.bucket?.language?.id || "-1",
          name: bucketQuery.data?.bucket?.language?.name || "",
        },
        searchConfigId: {
          id: bucketQuery.data?.bucket?.searchConfig?.id || "-1",
          name: bucketQuery.data?.bucket?.searchConfig?.name || "",
        },
        ragConfigurationChatId: {
          id: bucketQuery.data?.bucket?.ragConfigurationChat?.id || "-1",
          name: bucketQuery.data?.bucket?.ragConfigurationChat?.name || "",
        },
        ragConfigurationChatToolId: {
          id: bucketQuery.data?.bucket?.ragConfigurationChatTool?.id || "-1",
          name: bucketQuery.data?.bucket?.ragConfigurationChatTool?.name || "",
        },
        ragConfigurationSimpleGenerateId: {
          id: bucketQuery.data?.bucket?.ragConfigurationSimpleGenerate?.id || "-1",
          name: bucketQuery.data?.bucket?.ragConfigurationSimpleGenerate?.name || "",
        },
      }),
      [datasources, suggestionCategories, tabs, bucketQuery],
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
          searchConfigId: data.searchConfigId.id !== "-1" ? data.searchConfigId.id : null,
          defaultLanguageId: data.defaultLanguageId.id !== "-1" ? data.defaultLanguageId.id : null,
          queryAnalysisId: data.queryAnalysisId.id !== "-1" ? data.queryAnalysisId.id : null,
          ragConfigurationChat: data.ragConfigurationChatId.id !== "-1" ? data.ragConfigurationChatId.id : null,
          ragConfigurationChatTool:
            data.ragConfigurationChatToolId.id !== "-1" ? data.ragConfigurationChatToolId.id : null,
          ragConfigurationSimpleGenerate:
            data.ragConfigurationSimpleGenerateId.id !== "-1" ? data.ragConfigurationSimpleGenerateId.id : null,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateBucketMutation.data?.bucketWithLists?.fieldValidators),
  });

  if (bucketQuery.loading) return null;

  const RagConfigurationSelect = () => {
    return (
      <>
        <CustomSelectRelationsOneToOne
          options={ragChatConfigurationOption}
          label="Chat Rag"
          onChange={(val) => form.inputProps("ragConfigurationChatId").onChange({ id: val.id, name: val.name })}
          value={{
            id: form.inputProps("ragConfigurationChatId").value.id,
            name: form.inputProps("ragConfigurationChatId").value.name || "",
          }}
          disabled={page === 1}
          // description="Search Configuration for current bucket"
        />
        <CustomSelectRelationsOneToOne
          options={ragChatToolConfigurationOption}
          label="Chat Rag Tool"
          onChange={(val) => form.inputProps("ragConfigurationChatToolId").onChange({ id: val.id, name: val.name })}
          value={{
            id: form.inputProps("ragConfigurationChatToolId").value.id,
            name: form.inputProps("ragConfigurationChatToolId").value.name || "",
          }}
          disabled={page === 1}
          // description="Query Analysis for current bucket"
        />
        <CustomSelectRelationsOneToOne
          options={ragChatSimpleGenerateConfigurationOption}
          label="Simple Generate"
          onChange={(val) =>
            form.inputProps("ragConfigurationSimpleGenerateId").onChange({ id: val.id, name: val.name })
          }
          value={{
            id: form.inputProps("ragConfigurationSimpleGenerateId").value.id,
            name: form.inputProps("ragConfigurationSimpleGenerateId").value.name || "",
          }}
          disabled={page === 1}
          // description="Default Language for current bucket"
        />
      </>
    );
  };

  return (
    <ContainerFluid style={{ width: "55%" }}>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Bucket"
            description="Create or Edit a Bucket to construct your search engine configuration. 
          You can add or remove from it data sources, tabs or filters.
          Bind to it your default language or search configuration."
            id={bucketId}
          />
          {view === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              Edit
            </Button>
          )}
        </Box>
        <form style={{ borderStyle: "unset", padding: "0 16px" }}>
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
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <RefreshOptionsLayout>
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label="Date"
                        {...form.inputProps("refreshOnDate")}
                      />
                      <TooltipDescription informationDescription="Refresh filters when date filter is applied" />
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label="Query"
                        {...form.inputProps("refreshOnQuery")}
                      />
                      <TooltipDescription informationDescription="Refresh filters when query search is performed" />
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label="SuggestionCategory"
                        {...form.inputProps("refreshOnSuggestionCategory")}
                      />
                      <TooltipDescription informationDescription="Refresh filters when filters are applied" />
                      <BooleanInput
                        sxCheckbox={sxCheckbox}
                        sxControl={sxControl}
                        label="Tab"
                        {...form.inputProps("refreshOnTab")}
                      />
                      <TooltipDescription informationDescription="Refresh filters Tab is applied" />
                    </RefreshOptionsLayout>

                    <CustomSelect
                      label="Retriver Type"
                      dict={RetrieveType}
                      {...form.inputProps("retrieveType")}
                      description="Retriever Type used to search data. If match performs text search 
                    otherwise performs vector/hybrid search"
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
                    </AssociationsLayout>
                    <Box display={"grid"} gridTemplateColumns={"1fr 1fr"} gap={"10px"} mt={"16px"}>
                      <CustomSelectRelationsOneToOne
                        options={optionSearchConfig}
                        label="Search Config"
                        onChange={(val) => form.inputProps("searchConfigId").onChange({ id: val.id, name: val.name })}
                        value={{
                          id: form.inputProps("searchConfigId").value.id,
                          name: form.inputProps("searchConfigId").value.name || "",
                        }}
                        disabled={page === 1}
                        description="Search Configuration for current bucket"
                      />
                      <CustomSelectRelationsOneToOne
                        options={queryAnalysisOption}
                        label="Query Analyzer"
                        onChange={(val) => form.inputProps("queryAnalysisId").onChange({ id: val.id, name: val.name })}
                        value={{
                          id: form.inputProps("queryAnalysisId").value.id,
                          name: form.inputProps("queryAnalysisId").value.name || "",
                        }}
                        disabled={page === 1}
                        description="Query Analysis for current bucket"
                      />
                      <CustomSelectRelationsOneToOne
                        options={languageOptions}
                        label="Default Language"
                        onChange={(val) =>
                          form.inputProps("defaultLanguageId").onChange({ id: val.id, name: val.name })
                        }
                        value={{
                          id: form.inputProps("defaultLanguageId").value.id,
                          name: form.inputProps("defaultLanguageId").value.name || "",
                        }}
                        disabled={page === 1}
                        description="Default Language for current bucket"
                      />
                      <RagConfigurationSelect />
                    </Box>
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
    }),
    [datasourcesQuery, suggestionCategoriesQuery, tabsQuery, bucketQuery, associatedBucketQuery],
  );

  return { ...data };
};
