import React from "react";
import { gql } from "@apollo/client";
import { useDeleteQueryParserMutation, useQueryParserConfigsQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useParams } from "react-router-dom";

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
  const [deleteQueryParserMutate] = useDeleteQueryParserMutation({
    refetchQueries: [{ query: QueryParserConfigsQuery, variables: { queryParserConfigId: searchConfigId! } }],
  });

  if (!searchConfigId) throw new Error();
  return (
    <Table
      data={{
        queryResult: queryParserConfigsQuery,
        field: (data) => data?.queryParserConfigs,
      }}
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
  );
}
