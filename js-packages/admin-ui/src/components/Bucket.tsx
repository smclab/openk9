import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";

import { useForm, fromFieldValidators, TextInput, TextArea, SearchSelect, MainTitle } from "./Form";
import ClayLayout from "@clayui/layout";
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
import { ClassNameButton } from "../App";

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
  mutation CreateOrUpdateBucket($id: ID, $name: String!, $description: String) {
    bucket(id: $id, bucketDTO: { name: $name, description: $description }) {
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
    <ClayLayout.ContainerFluid view>
      {bucketId !== "new" && <MainTitle title="Attribute" />}
      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextArea label="Description" {...form.inputProps("description")} />
        {bucketId !== "new" && (
          <ClayForm
            onSubmit={(event) => {
              event.preventDefault();
            }}
          >
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
          </ClayForm>
        )}
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {bucketId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
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
