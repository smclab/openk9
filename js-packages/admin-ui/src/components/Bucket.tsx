import React from "react";
import { gql } from "@apollo/client";
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
} from "./Form";
import { useToast } from "./ToastProvider";
import {
  useBindQueryAnalysisToBucketMutation,
  useBindSearchConfigToBucketMutation,
  useBucketQuery,
  useCreateOrUpdateBucketMutation,
  useQueryAnalysisOptionsQuery,
  useQueryAnalysisValueQuery,
  useSearchConfigOptionsQuery,
  useSearchConfigValueQuery,
  useUnbindQueryAnalysisFromBucketMutation,
  useUnbindSearchConfigFromBucketMutation,
} from "../graphql-generated";
import { BucketsQuery } from "./Buckets";

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

// gql`
//   query languagesOptions($searchText: String, $cursor: String) {
//     options: languages(searchText: $searchText, first: 5, after: $cursor) {
//       edges {
//         node {
//           id
//           name
//         }
//       }
//       pageInfo {
//         hasNextPage
//         endCursor
//       }
//     }
//   }
// `;
// gql`
//   query LanguagesValue($id: ID!) {
//     value: language(id: $id) {
//       id
//       name
//     }
//   }
// `;
// gql`
//   mutation BindLanguagesToBucket($bucketId: ID!, $languages: ID!) {
//     bindLanguageToBucket(bucketId: $bucketId, languagesId: $languages) {
//       left {
//         id
//         languages {
//           id
//         }
//       }
//       right {
//         id
//       }
//     }
//   }
// `;
// gql`
//   mutation UnbindLanguagesFromBucket($languageId: ID!) {
//     unbindLanguageFromBucket(languageId: $languageId) {
//       right {
//         id
//       }
//     }
//   }
// `;

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
