import React from "react";
import { QueryResult, gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import {
  useForm,
  fromFieldValidators,
  TextInput,
  TextArea,
  SearchSelect,
  MainTitle,
  BooleanInput,
  CustomButtom,
  ContainerFluid,
  QueryHook,
  MutationHook,
  CustomFormGroup,
  InformationField,
  ClayListComponents,
} from "./Form";
import { useToast } from "./ToastProvider";
import {
  BucketLanguagesQuery,
  Exact,
  InputMaybe,
  useBindLanguageToBucketMutation,
  useBindQueryAnalysisToBucketMutation,
  useBindSearchConfigToBucketMutation,
  useBucketLanguagesQuery,
  useBucketQuery,
  useCreateOrUpdateBucketMutation,
  useLanguageQuery,
  useLanguageValueQuery,
  useLanguagesOptionsQuery,
  useQueryAnalysisOptionsQuery,
  useQueryAnalysisValueQuery,
  useSearchConfigOptionsQuery,
  useSearchConfigValueQuery,
  useUnbindLanguageFromBucketMutation,
  useUnbindQueryAnalysisFromBucketMutation,
  useUnbindSearchConfigFromBucketMutation,
} from "../graphql-generated";
import { BucketsQuery } from "./Buckets";
import useDebounced from "./useDebounced";
import ClayModal, { useModal } from "@clayui/modal";
import { ClayInput } from "@clayui/form";
import ClayButton, { ClayButtonWithIcon } from "@clayui/button";
import { Virtuoso } from "react-virtuoso";
import ClayList from "@clayui/list";

const BucketQuery = gql`
  query Bucket($id: ID!) {
    bucket(id: $id) {
      id
      name
      description
      enabled
      handleDynamicFilters
      queryAnalysis {
        id
      }
      searchConfig {
        id
      }
      language {
        id
      }
    }
  }
`;
gql`
  mutation EnableBucket($id: ID!) {
    enableBucket(id: $id) {
      id
      name
    }
  }
`;
gql`
  mutation CreateOrUpdateBucket($id: ID, $name: String!, $description: String, $handleDynamicFilters: Boolean!) {
    bucket(id: $id, bucketDTO: { name: $name, description: $description, handleDynamicFilters: $handleDynamicFilters }) {
      entity {
        id
        name
        enabled
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function Bucket() {
  const { bucketId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const bucketQuery = useBucketQuery({
    variables: { id: bucketId as string },
    skip: !bucketId || bucketId === "new",
  });
  const [createOrUpdateBucketMutate, createOrUpdateBucketMutation] = useCreateOrUpdateBucketMutation({
    refetchQueries: [BucketQuery, BucketsQuery],
    onCompleted(data) {
      if (data.bucket?.entity) {
        if (bucketId === "new") {
          navigate(`/buckets/`, { replace: true });
          showToast({ displayType: "success", title: "Bucket created", content: data.bucket.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Bucket updated", content: data.bucket.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        enable: false,
        handleDynamicFilters: false,
      }),
      []
    ),
    originalValues: bucketQuery.data?.bucket,
    isLoading: bucketQuery.loading || createOrUpdateBucketMutation.loading,
    onSubmit(data) {
      createOrUpdateBucketMutate({ variables: { id: bucketId !== "new" ? bucketId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateBucketMutation.data?.bucket?.fieldValidators),
  });
  return (
    <ContainerFluid>
      {bucketId !== "new" && <MainTitle title="Attribute" />}
      <form
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextArea label="Description" {...form.inputProps("description")} />
        {bucketId !== "new" && (
          <React.Fragment>
            <SearchSelect
              label="Query Analyzer"
              value={bucketQuery.data?.bucket?.queryAnalysis?.id}
              useValueQuery={useQueryAnalysisValueQuery}
              useOptionsQuery={useQueryAnalysisOptionsQuery}
              useChangeMutation={useBindQueryAnalysisToBucketMutation}
              mapValueToMutationVariables={(queryAnalysis) => ({ bucketId, queryAnalysis })}
              useRemoveMutation={useUnbindQueryAnalysisFromBucketMutation}
              mapValueToRemoveMutationVariables={() => ({ bucketId })}
              invalidate={() => bucketQuery.refetch()}
              description={"Query Analysis configuration for current bucket"}
            />

            <SearchSelect
              label="Search Config"
              value={bucketQuery.data?.bucket?.searchConfig?.id}
              useValueQuery={useSearchConfigValueQuery}
              useOptionsQuery={useSearchConfigOptionsQuery}
              useChangeMutation={useBindSearchConfigToBucketMutation}
              mapValueToMutationVariables={(searchConfigId) => ({ bucketId, searchConfigId })}
              useRemoveMutation={useUnbindSearchConfigFromBucketMutation}
              mapValueToRemoveMutationVariables={() => ({ bucketId })}
              invalidate={() => bucketQuery.refetch()}
              description={"Search Configuration for current bucket"}
            />
            <SearchSelectLanguage
              label="Language"
              value={bucketQuery.data?.bucket?.language?.id}
              useValueQuery={useLanguageValueQuery}
              list={{
                useListQuery: useBucketLanguagesQuery,
                field: (data) => data?.bucket?.languages,
              }}
              parentId={bucketId}
              useChangeMutation={useBindLanguageToBucketMutation}
              mapValueToMutationVariables={(languageId) => ({ bucketId, languageId })}
              useRemoveMutation={useUnbindLanguageFromBucketMutation}
              mapValueToRemoveMutationVariables={() => ({ bucketId })}
              invalidate={() => bucketQuery.refetch()}
              description={"Language for current bucket"}
            />
          </React.Fragment>
        )}
        {bucketId !== "new" && (
          <BooleanInput
            label="Dynamic Filters"
            description=" Allow to handle filter in dynamic way. Filters will change base to current query."
            {...form.inputProps("handleDynamicFilters")}
          />
        )}
        <div className="sheet-footer">
          <CustomButtom nameButton={bucketId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}

gql`
  query QueryAnalysisOptions($searchText: String, $cursor: String) {
    options: queryAnalyses(searchText: $searchText, first: 5, after: $cursor) {
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
  query QueryAnalysisValue($id: ID!) {
    value: queryAnalysis(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindQueryAnalysisToBucket($bucketId: ID!, $queryAnalysis: ID!) {
    bindQueryAnalysisToBucket(bucketId: $bucketId, queryAnalysisId: $queryAnalysis) {
      left {
        id
        queryAnalysis {
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
  mutation UnbindQueryAnalysisFromBucket($bucketId: ID!) {
    unbindQueryAnalysisFromBucket(bucketId: $bucketId) {
      right {
        id
      }
    }
  }
`;

gql`
  query SearchConfigOptions($searchText: String, $cursor: String) {
    options: searchConfigs(searchText: $searchText, first: 5, after: $cursor) {
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
  query SearchConfigValue($id: ID!) {
    value: searchConfig(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindSearchConfigToBucket($bucketId: ID!, $searchConfigId: ID!) {
    bindSearchConfigToBucket(bucketId: $bucketId, searchConfigId: $searchConfigId) {
      left {
        id
        searchConfig {
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
  mutation UnbindSearchConfigFromBucket($bucketId: ID!) {
    unbindSearchConfigFromBucket(bucketId: $bucketId) {
      right {
        id
      }
    }
  }
`;

gql`
  query LanguagesOptions($searchText: String, $cursor: String) {
    options: languages(searchText: $searchText, after: $cursor) {
      edges {
        node {
          id
          name
          value
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
  query LanguageValue($id: ID!) {
    value: language(id: $id) {
      id
      name
      value
    }
  }
`;
gql`
  mutation BindLanguageToBucket($bucketId: ID!, $languageId: ID!) {
    bindLanguageToBucket(bucketId: $bucketId, languageId: $languageId) {
      left {
        id
        language {
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
  mutation UnbindLanguageFromBucket($bucketId: ID!) {
    unbindLanguageFromBucket(bucketId: $bucketId) {
      right {
        id
      }
    }
  }
`;

function SearchSelectLanguage<Q, Value, Change extends Record<string, any>, Remove extends Record<string, any>>({
  label,
  value,
  useValueQuery,
  list: { useListQuery, field },
  useChangeMutation,
  mapValueToMutationVariables,
  useRemoveMutation,
  mapValueToRemoveMutationVariables,
  invalidate,
  description,
  parentId,
}: {
  label: string;
  value: Value | null | undefined;
  parentId: string;
  description?: string;
  useValueQuery: QueryHook<{ value?: { id?: string | null; name?: string | null; description?: string | null } | null }, { id: Value }>;
  list: {
    useListQuery: QueryHook<Q, { parentId: string; unassociated: boolean; searchText?: string | null; cursor?: string | null }>;
    field(data: Q | undefined):
      | {
          edges?: Array<{ node?: { id?: string | null; name?: string | null; description?: string | null } | null } | null> | null;
          pageInfo?: { hasNextPage: boolean; endCursor?: string | null } | null;
        }
      | null
      | undefined;
  };
  mapValueToMutationVariables(id: string): Change;
  useChangeMutation: MutationHook<any, Change>;
  mapValueToRemoveMutationVariables(): Remove;
  useRemoveMutation: MutationHook<any, Remove>;
  invalidate(): void;
}) {
  const [searchText, setSearchText] = React.useState("");
  const searchTextDebounced = useDebounced(searchText);
  const valueQuery = useValueQuery({ variables: { id: value as Value }, skip: !value });
  const optionsQuery = useListQuery({ variables: { parentId, unassociated: false, searchText: searchTextDebounced } });
  const [changeMutate, changeMutation] = useChangeMutation({});
  const { observer, onOpenChange, open } = useModal();
  const scrollerRef = React.useRef<HTMLElement>();
  const [removeMutate, removeMutation] = useRemoveMutation({});
  return (
    <React.Fragment>
      <CustomFormGroup>
        <label>{label}</label>
        {description && InformationField(description)}
        <ClayInput.Group>
          <ClayInput.GroupItem>
            <ClayInput
              type="text"
              className="form-control"
              style={{ backgroundColor: "#f1f2f5" }}
              readOnly
              disabled={!value}
              value={valueQuery.data?.value?.name ?? ""}
            />
          </ClayInput.GroupItem>
          <ClayInput.GroupItem append shrink>
            <ClayButton.Group>
              <ClayButton
                displayType="secondary"
                style={{
                  border: "1px solid #393B4A",
                  borderRadius: "3px",
                }}
                onClick={() => onOpenChange(true)}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Change
                </span>
              </ClayButton>
              <ClayButton
                displayType="secondary"
                disabled={typeof valueQuery.data?.value?.name === "string" ? false : true}
                style={{ marginLeft: "10px", border: "1px solid #393B4A", borderRadius: "3px" }}
                onClick={() => {
                  if (!changeMutation.loading && !removeMutation.loading)
                    removeMutate({
                      variables: mapValueToRemoveMutationVariables(),
                      onCompleted() {
                        invalidate();
                      },
                    });
                }}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Remove
                </span>
              </ClayButton>
            </ClayButton.Group>
          </ClayInput.GroupItem>
        </ClayInput.Group>
      </CustomFormGroup>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{label}</ClayModal.Header>
          <ClayModal.Body>
            <CustomFormGroup>
              <ClayInput
                type="search"
                placeholder="search"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </CustomFormGroup>
            <Virtuoso
              totalCount={field(optionsQuery.data)?.edges?.length}
              scrollerRef={(element) => (scrollerRef.current = element as any)}
              style={{ height: "400px" }}
              components={ClayListComponents}
              itemContent={(index) => {
                const row = field(optionsQuery.data)?.edges?.[index]?.node ?? undefined;
                return (
                  <React.Fragment>
                    <ClayList.ItemField expand>
                      <ClayList.ItemTitle>{row?.name || "..."}</ClayList.ItemTitle>
                      <ClayList.ItemText>{"..."}</ClayList.ItemText>
                    </ClayList.ItemField>
                    <ClayList.ItemField>
                      <ClayList.QuickActionMenu>
                        {!changeMutation.loading && !removeMutation.loading && (
                          <ClayList.QuickActionMenu.Item
                            onClick={() => {
                              if (row?.id) {
                                changeMutate({
                                  variables: mapValueToMutationVariables(row.id),
                                  onCompleted() {
                                    onOpenChange(false);
                                  },
                                });
                              }
                            }}
                            symbol="play"
                          />
                        )}
                      </ClayList.QuickActionMenu>
                    </ClayList.ItemField>
                  </React.Fragment>
                );
              }}
              isScrolling={(isScrolling) => {
                if (scrollerRef.current) {
                  if (isScrolling) {
                    scrollerRef.current.style.pointerEvents = "none";
                  } else {
                    scrollerRef.current.style.pointerEvents = "auto";
                  }
                }
              }}
            />
          </ClayModal.Body>
          <ClayModal.Footer
            first={
              <ClayButton displayType="secondary" onClick={() => onOpenChange(false)}>
                Cancel
              </ClayButton>
            }
          />
        </ClayModal>
      )}
    </React.Fragment>
  );
}
