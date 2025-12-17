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
  useAutocorrectionsOptionsQuery,
  useBucketDataSourcesQuery,
  useBucketQuery,
  useCreateOrUpdateBucketMutation,
  useDataSourcesQuery,
  useSuggestionCategoriesQuery,
  useTabsQuery,
  useUnboundRagConfigurationsByBucketQuery,
} from "../../graphql-generated";
import { AssociatedUnassociated, formatQueryToBE, formatQueryToFE } from "../../utils";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

import RefreshOptionsLayout from "@components/Form/Inputs/CheckboxOptionsLayout";
import { AutocompleteDropdown, AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import { useLanguages, useOptionSearchConfig, useQueryAnaylyses } from "../../../src/utils/RelationOneToOne";
import useOptions from "../../utils/getOptions";
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

export function SaveBucket({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
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

  const { OptionQuery: autocorrectionOption } = useOptions({
    queryKeyPath: "autocorrections.edges",
    useQuery: useAutocorrectionsOptionsQuery,
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
          { key: "refreshOnDate" },
          { key: "refreshOnQuery" },
          { key: "refreshOnSuggestionCategory" },
          { key: "refreshOnTab" },
          { key: "retrieveType" },
          { key: "datasourceIds" },
          { key: "suggestionCategoryIds" },
          { key: "tabIds" },
          { key: "queryAnalysisId" },
          { key: "defaultLanguageId" },
          { key: "searchConfigId" },
          { key: "ragConfigurationChatId" },
          { key: "ragConfigurationChatToolId" },
          { key: "ragConfigurationSimpleGenerateId" },
        ],
        label: "Recap Document Type",
      },
    ],
  });

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
                      <AutocompleteDropdown
                        label="Search Config"
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
                        label="Language"
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
                        label="Query analysis"
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
                        label="Chat Rag "
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
                        label="Chat Rag Tool"
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
                        label="Autocorrection"
                        onChange={(val) => form.inputProps("autocorrectionId").onChange({ id: val.id, name: val.name })}
                        value={{
                          id: form.inputProps("autocorrectionId").value.id,
                          name: form.inputProps("autocorrectionId").value.name || "",
                        }}
                        disabled={page === 1}
                      />
                      <AutocompleteDropdownWithOptions
                        label="Simple Generate"
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
                    <Recap recapData={recapSections} setExtraFab={setExtraFab} />
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
