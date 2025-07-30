import { gql } from "@apollo/client";
import { TemplateType } from "@pages/Analyzer/gql";

 gql`
  query Charfilters($searchText: String, $after: String) {
    charFilters(searchText: $searchText, first: 20, after: $after) {
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
  mutation DeleteCharFilters($id: ID!) {
    deleteCharFilter(charFilterId: $id) {
      id
      name
    }
  }
`;

gql`
  query UnboundAnalyzersByCharFilter($charFilterId: BigInteger!) {
    unboundAnalyzersByCharFilter(charFilterId: $charFilterId) {
      name
      id
    }
  }
`;

gql`
  mutation AddCharFiltersToAnalyzer($childId: ID!, $parentId: ID!) {
    addCharFilterToAnalyzer(charFilterId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

 gql`
  query CharFilter($id: ID!) {
    charFilter(id: $id) {
      id
      name
      description
      jsonConfig
      type
    }
  }
`;

gql`
  mutation CreateOrUpdateCharFilter(
    $id: ID
    $name: String!
    $description: String
    $jsonConfig: String
    $type: String!
  ) {
    charFilter(
      id: $id
      charFilterDTO: { name: $name, description: $description, jsonConfig: $jsonConfig, type: $type }
    ) {
      entity {
        id
        name
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export const CharFilters: TemplateType[] = [
  {
    title: "html_strip",
    description: "Strips HTML elements from a text and replaces HTML entities with their decoded value.",
    type: "html_strip",
    value: [
      {
        name: "escaped_tags",
        value: ["p"],
        type: "multi-select",
        description:
          "(Optional, array of strings) Array of HTML elements without enclosing angle brackets (< >). The filter skips these HTML elements when stripping HTML from the text. For example, a value of [ 'p' ] skips the <p> HTML element.",
      },
    ],
  },
  {
    title: "mapping",
    description:
      "The mapping character filter accepts a map of keys and values. Whenever it encounters a string of characters that is the same as a key, it replaces them with the value associated with that key.",
    type: "mapping",
    value: [
      {
        name: "mappings",
        value: ["key => value"],
        type: "multi-select",
        description: "Either this or the mappings_path parameter must be specified.",
      },
      {
        name: "mappings_path",
        value: "key => value",
        type: "string",
        description:
          "This path must be absolute or relative to the config location, and the file must be UTF-8 encoded. Each mapping in the file must be separated by a line break.",
      },
    ],
  },
  {
    title: "pattern_replace",
    description:
      "The pattern_replace character filter uses a regular expression to match characters which should be replaced with the specified replacement string. The replacement string can refer to capture groups in the regular expression.",
    type: "pattern_replace",
    value: [
      { name: "pattern", value: "", type: "string", description: "A Java regular expression. Required." },
      {
        name: "replacement",
        value: "",
        type: "string",
        description:
          "The replacement string, which can reference capture groups using the $1..$9 syntax, as explained here.",
      },
    ],
  },
];
