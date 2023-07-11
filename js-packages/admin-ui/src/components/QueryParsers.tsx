import React from "react";
import { gql } from "@apollo/client";
import { useDeleteQueryParserMutation, useQueryParserConfigsQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useParams } from "react-router-dom";
import { useToast } from "./ToastProvider";

export const QueryParserConfigsQuery = gql`
  query QueryParserConfigs($queryParserConfigId: ID!, $searchText: String, $cursor: String) {
    queryParserConfigs(searchConfigId: $queryParserConfigId, searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          type
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
  mutation DeleteQueryParser($searchConfigId: ID!, $queryParserConfigId: ID!) {
    removeQueryParserConfig(queryParserConfigId: $queryParserConfigId, searchConfigId: $searchConfigId) {
      left {
        id
      }
    }
  }
`;

export function QueryParsers() {
  const { searchConfigId } = useParams();
  const queryParserConfigsQuery = useQueryParserConfigsQuery({
    variables: { queryParserConfigId: searchConfigId! },
    skip: !searchConfigId,
  });

  const showToast = useToast();
  const [deleteQueryParserMutate] = useDeleteQueryParserMutation({
    onCompleted(data) {
      if (data.removeQueryParserConfig?.left?.id) {
        queryParserConfigsQuery.refetch();
        showToast({ displayType: "success", title: "Query Parser deleted", content: "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Data source error", content: error.message ?? "" });
    },
  });

  if (!searchConfigId) throw new Error();
  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: queryParserConfigsQuery,
          field: (data) => data?.queryParserConfigs,
        }}
        label="Query Parsaers"
        onCreatePath="new"
        onDelete={(queryparser) => {
          if (queryparser?.id) {
            deleteQueryParserMutate({ variables: { searchConfigId: searchConfigId!, queryParserConfigId: queryparser.id } });
          }
        }}
        columns={[
          { header: "Name", content: (queryParserConfig) => formatName(queryParserConfig) },
          { header: "Description", content: (queryParserConfig) => queryParserConfig?.description },
          { header: "Type", content: (queryParserConfig) => queryParserConfig?.type },
        ]}
      />
    </React.Fragment>
  );
}
