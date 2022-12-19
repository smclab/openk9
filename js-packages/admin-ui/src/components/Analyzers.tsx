import React from "react";
import { gql } from "@apollo/client";
import { useAnalyzersQuery, useDeleteAnalyzersMutation } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const AnalyzersQuery = gql`
  query Analyzers($searchText: String, $cursor: String) {
    analyzers(searchText: $searchText, first: 25, after: $cursor) {
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
  mutation DeleteAnalyzers($id: ID!) {
    deleteAnalyzer(analyzerId: $id) {
      id
      name
    }
  }
`;

export function Analyzers() {
  const analyzersQuery = useAnalyzersQuery();
  const showToast = useToast();
  const [deleteAnalyzersMutate] = useDeleteAnalyzersMutation({
    refetchQueries: [AnalyzersQuery],
    onCompleted(data) {
      if (data.deleteAnalyzer?.id) {
        showToast({ displayType: "success", title: "Analyzers deleted", content: data.deleteAnalyzer.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Enrich items error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: analyzersQuery,
        field: (data) => data?.analyzers,
      }}
      onCreatePath="/analyzers/new"
      onDelete={(analyzer) => {
        if (analyzer?.id) deleteAnalyzersMutate({ variables: { id: analyzer.id } });
      }}
      columns={[
        { header: "Name", content: (analyzer) => formatName(analyzer) },
        { header: "Description", content: (analyzer) => analyzer?.description },
        { header: "Type", content: (analyzer) => analyzer?.type },
      ]}
    />
  );
}
