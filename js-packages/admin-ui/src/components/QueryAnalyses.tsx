import React from "react";
import { gql } from "@apollo/client";
import { formatName, Table } from "./Table";
import { useDeleteQueryAnalysisMutation, useQueryAnalysesQuery } from "../graphql-generated";
import { useToast } from "./ToastProvider";

export const QueryAnalysesQuery = gql`
  query QueryAnalyses($searchText: String, $cursor: String) {
    queryAnalyses(searchText: $searchText, first: 25, after: $cursor) {
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
  mutation DeleteQueryAnalysis($id: ID!) {
    deleteQueryAnalysis(queryAnalysisId: $id) {
      id
      name
    }
  }
`;

export function QueryAnalyses() {
  const queryAnalysesQuery = useQueryAnalysesQuery();
  const showToast = useToast();
  const [deleteQueryAnalysisMutate] = useDeleteQueryAnalysisMutation({
    refetchQueries: [QueryAnalysesQuery],
    onCompleted(data) {
      if (data.deleteQueryAnalysis?.id) {
        showToast({ displayType: "success", title: "Query analyses deleted", content: data.deleteQueryAnalysis.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Query analyses error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: queryAnalysesQuery,
        field: (data) => data?.queryAnalyses,
      }}
      onCreatePath="/query-analyses/new"
      onDelete={(queryAnalysis) => {
        if (queryAnalysis?.id) deleteQueryAnalysisMutate({ variables: { id: queryAnalysis.id } });
      }}
      columns={[
        { header: "Name", content: (queryAnalysis) => formatName(queryAnalysis) },
        { header: "Description", content: (queryAnalysis) => queryAnalysis?.description },
        // { header: "Stop Words", content: (queryAnalysis) => queryAnalysis?.stopwords },
      ]}
    />
  );
}
