import { gql } from "@apollo/client";

gql`
  query Analyzers($searchText: String, $after: String) {
    analyzers(searchText: $searchText, first: 20, after: $after) {
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

export const analyzerConfigOptions = gql`
  query AnalyzerOptions($searchText: String, $cursor: String) {
    options: analyzers(searchText: $searchText, first: 5, after: $cursor) {
      edges {
        node {
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
  query analyzerValue($id: ID!) {
    value: analyzer(id: $id) {
      id
      name
      description
      type
    }
  }
`;

gql`
  mutation BindAnalyzerToDocTypeField($analyzerId: ID!, $docTypeFieldId: ID!) {
    bindAnalyzerToDocTypeField(analyzerId: $analyzerId, docTypeFieldId: $docTypeFieldId) {
      left {
        id
        docType {
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
  mutation UnbindQueryAnalysisFromDocTypeField($docTypeFieldId: ID!) {
    unbindAnalyzerFromDocTypeField(docTypeFieldId: $docTypeFieldId) {
      right {
        id
      }
    }
  }
`;

export const searchConfigOptions = gql`
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
  mutation BindTokenizerToAnalyzer($analyzerId: ID!, $tokenizerId: ID!) {
    bindTokenizerToAnalyzer(analyzerId: $analyzerId, tokenizerId: $tokenizerId) {
      left {
        id
        tokenizer {
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
  mutation UnbindTokenizerFromAnalyzer($analyzerId: ID!) {
    unbindTokenizerFromAnalyzer(analyzerId: $analyzerId) {
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

export const Analyzer = gql`
  query Analyzer($id: ID!) {
    analyzer(id: $id) {
      id
      name
      description
      type
      jsonConfig
      tokenizer {
        id
        name
      }
    }
  }
`;

gql`
  mutation DeleteAnalyzer($id: ID!) {
    deleteAnalyzer(analyzerId: $id) {
      id
      name
    }
  }
`;

export const CreateOrUpdateAnalyzer = gql`
  mutation CreateOrUpdateAnalyzer(
    $id: ID
    $name: String!
    $description: String
    $type: String!
    $tokenFilterIds: [BigInteger]
    $charFilterIds: [BigInteger]
    $tokenizerId: BigInteger
    $jsonConfig: String
  ) {
    analyzerWithLists(
      id: $id
      analyzerWithListsDTO: {
        name: $name
        type: $type
        description: $description
        tokenFilterIds: $tokenFilterIds
        charFilterIds: $charFilterIds
        tokenizerId: $tokenizerId
        jsonConfig: $jsonConfig
      }
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

export const AnalyzersAssociations = gql`
  query AnalyzersAssociations($parentId: ID!, $unassociated: Boolean!) {
    analyzer(id: $parentId) {
      id
      charFilters(notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }
      }
      tokenFilters(notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }
      }
    }
  }
`;

export interface TemplateValue {
  name: string;
  value: string | number | Array<string | number> | boolean;
  description?: string;
  type: "string" | "number" | "select" | "multi-select" | "boolean";
  options?: Array<{ label: string; value: string }>;
}

export interface TemplateType {
  title: string;
  description: string;
  type: string;
  value: TemplateValue[];
}

export const TemplateAnalyzers: TemplateType[] = [
  {
    title: "custom",
    description: "",
    type: "custom",
    value: [],
  },
  {
    title: "fingerprint",
    description:
      "The fingerprint analyzer implements a fingerprinting algorithm which is used by the OpenRefine project to assist in clustering.",
    type: "fingerprint",
    value: [
      {
        name: "separator",
        value: " ",
        type: "string",
        description: "The character to use to concatenate the terms. Defaults to a space.",
      },
      {
        name: "max_output_size",
        value: 255,
        type: "number",
        description: "The maximum token size to emit. Defaults to 255. Tokens larger than this size will be discarded.",
      },
      {
        name: "stopwords",
        value: ["a", "e", "i", "o", "u"],
        type: "multi-select",
        description:
          "A pre-defined stop words list like _english_ or an array containing a list of stop words. Defaults to _none_.",
      },
    ],
  },
  {
    title: "keyword",
    description: "The keyword analyzer is a “noop” analyzer which returns the entire input string as a single token.",
    type: "keyword",
    value: [],
  },
  {
    title: "italian",
    description: "Built-in italian analyzer aimed at analyzing italian language.",
    type: "italian",
    value: [],
  },
  {
    title: "english",
    description: "Built-in english analyzer aimed at analyzing english language.",
    type: "english",
    value: [],
  },
  {
    title: "french",
    description: "Built-in french analyzer aimed at analyzing french language.",
    type: "french",
    value: [],
  },
  {
    title: "german",
    description: "Built-in german analyzer aimed at analyzing german language.",
    type: "german",
    value: [],
  },
  {
    title: "spanish",
    description: "Built-in spanish analyzer aimed at analyzing spanish language.",
    type: "spanish",
    value: [],
  },
  {
    title: "portuguese",
    description: "Built-in portuguese analyzer aimed at analyzing portuguese language.",
    type: "portuguese",
    value: [],
  },
  {
    title: "simple",
    description:
      "The simple analyzer breaks text into tokens at any non-letter character, such as numbers, spaces, hyphens and apostrophes, discards non-letter characters, and changes uppercase to lowercase.",
    type: "simple",
    value: [],
  },
  {
    title: "standard",
    description: "The standard analyzer is the default analyzer which is used if none is specified.",
    type: "standard",
    value: [
      {
        name: "max_token_length",
        value: 255,
        type: "number",
        description:
          "The maximum token length. If a token is seen that exceeds this length then it is split at max_token_length intervals. Defaults to 255.",
      },
      {
        name: "stopwords",
        value: ["a", "e", "i", "o", "u"],
        type: "multi-select",
        description:
          "A pre-defined stop words list like _english_ or an array containing a list of stop words. Defaults to _none_.",
      },
    ],
  },
  {
    title: "stop",
    description:
      "The stop analyzer is the same as the simple analyzer but adds support for removing stop words. It defaults to using the _english_ stop words.",
    type: "stop",
    value: [
      {
        name: "stopwords",
        value: ["a", "e", "i", "o", "u"],
        type: "multi-select",
        description:
          "A pre-defined stop words list like _english_ or an array containing a list of stop words. Defaults to _none_.",
      },
    ],
  },
  {
    title: "whitespace",
    description: "The whitespace analyzer breaks text into terms whenever it encounters a whitespace character.",
    type: "whitespace",
    value: [],
  },
];
