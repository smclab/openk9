import { gql } from "@apollo/client";
import React from "react";
import { useLanguagesQuery } from "../../graphql-generated";

export const LanguagesQuery = gql`
  query Languages($searchText: String, $cursor: String) {
    languages(searchText: $searchText, first: 20, after: $cursor) {
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

export function Languages() {
  const dataLanguagesQuery = useLanguagesQuery();
}
