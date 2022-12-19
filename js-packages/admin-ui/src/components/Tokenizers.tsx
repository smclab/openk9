import React from "react";
import { gql } from "@apollo/client";
import { useDeleteTokenizerMutation, useTokenizersQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const TokenizersQuery = gql`
  query Tokenizers($searchText: String, $cursor: String) {
    tokenizers(searchText: $searchText, first: 25, after: $cursor) {
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
  mutation DeleteTokenizer($id: ID!) {
    deleteTokenizer(tokenizerId: $id) {
      id
      name
    }
  }
`;

export function Tokenizers() {
  const showToast = useToast();
  const tokenizersQuery = useTokenizersQuery();
  const [deleteTokenizersMutate] = useDeleteTokenizerMutation({
    refetchQueries: [TokenizersQuery],
    onCompleted(data) {
      if (data.deleteTokenizer?.id) {
        showToast({ displayType: "success", title: "Tokenizer deleted", content: data.deleteTokenizer.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Rule error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: tokenizersQuery,
        field: (data) => data?.tokenizers,
      }}
      onCreatePath="/tokenizers/new"
      onDelete={(tokenizer) => {
        if (tokenizer?.id) deleteTokenizersMutate({ variables: { id: tokenizer.id } });
      }}
      columns={[
        { header: "Name", content: (tokenizer) => formatName(tokenizer) },
        { header: "Description", content: (tokenizer) => tokenizer?.description },
      ]}
    />
  );
}
