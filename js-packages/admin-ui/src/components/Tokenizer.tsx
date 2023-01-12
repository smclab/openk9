import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import { useCreateOrUpdateTokenizerMutation, useTokenizerQuery } from "../graphql-generated";
import {
  useForm,
  fromFieldValidators,
  TextInput,
  TextArea,
  KeyValue,
  CreateFieldDinamically,
  MultiSelectForDinamicFields,
  MainTitle,
} from "./Form";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { TokenizersQuery } from "./Tokenizers";
import { ClassNameButton } from "../App";

const TokenizerQuery = gql`
  query Tokenizer($id: ID!) {
    tokenizer(id: $id) {
      id
      name
      description
      jsonConfig
    }
  }
`;

gql`
  mutation CreateOrUpdateTokenizer($id: ID, $name: String!, $description: String, $jsonConfig: String) {
    tokenizer(id: $id, tokenizerDTO: { name: $name, description: $description, jsonConfig: $jsonConfig }) {
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

export function Tokenizer() {
  const { tokenizerId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const tokenizerQuery = useTokenizerQuery({
    variables: { id: tokenizerId as string },
    skip: !tokenizerId || tokenizerId === "new",
  });
  const [templateChoice, setTemplateChoice] = React.useState<KeyValue>(JSON.parse(tokenizerQuery.data?.tokenizer?.jsonConfig || `{}`));
  const [createOrUpdateTokenizerMutate, createOrUpdateTokenizerMutation] = useCreateOrUpdateTokenizerMutation({
    refetchQueries: [TokenizerQuery, TokenizersQuery],
    onCompleted(data) {
      if (data.tokenizer?.entity) {
        if (tokenizerId === "new") {
          navigate(`/tokenizers/`, { replace: true });
          showToast({ displayType: "success", title: "Tokenizer created", content: data.tokenizer.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Tokenizer updated", content: data.tokenizer.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: TokenizerFilters[0].description,
        jsonConfig: "",
      }),
      []
    ),
    originalValues: tokenizerQuery.data?.tokenizer,
    isLoading: tokenizerQuery.loading || createOrUpdateTokenizerMutation.loading,
    onSubmit(data) {
      createOrUpdateTokenizerMutate({ variables: { id: tokenizerId !== "new" ? tokenizerId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTokenizerMutation.data?.tokenizer?.fieldValidators),
  });
  React.useEffect(() => {
    if (JSON.stringify(templateChoice) !== "{}") {
      form.inputProps("jsonConfig").onChange(JSON.stringify(templateChoice));
    }
  }, [templateChoice]);

  if (tokenizerId !== "new" && JSON.stringify(templateChoice) === "{}") {
    try {
      const value = JSON.parse(form.inputProps("jsonConfig").value);
      setTemplateChoice(value);
    } catch (error) {}
  }
  if (tokenizerId === "new" && JSON.stringify(templateChoice) === "{}") {
    try {
      const value = JSON.parse(TokenizerFilters[0].Json);
      setTemplateChoice(value);
    } catch (error) {}
  }
  if (tokenizerId !== "new") {
    TokenizerFilters.forEach((filter) => {
      if (filter.title === templateChoice.type) {
        filter.visible = "" + true;
      }
    });
  }
  if (tokenizerId === "new") {
    TokenizerFilters[0].visible = "" + true;
  }
  if (tokenizerQuery.loading) {
    return <div></div>;
  }
  return (
    <ClayLayout.ContainerFluid view>
      {tokenizerId !== "new" && <MainTitle title="Attributes" />}
      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextArea label="Description" {...form.inputProps("description")} />
        <MultiSelectForDinamicFields
          id={tokenizerId}
          templates={TokenizerFilters}
          onChangeDescription={form.inputProps("description").onChange}
          templateChoice={templateChoice}
          setTemplateChoice={setTemplateChoice}
        />
        <CreateFieldDinamically templates={TokenizerFilters} setTemplateChoice={setTemplateChoice} templateChoice={templateChoice} />
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {tokenizerId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}

const TokenizerFilters = [
  {
    title: "classic",
    description:
      "The classic tokenizer is a grammar based tokenizer that is good for English language documents. This tokenizer has heuristics for special treatment of acronyms, company names, email addresses, and internet host names.",
    Json: `
    {
      "type": "classic",
       "max_token_length": 255
    }
    `,
    descriptionAttribute: `
    {
      "type":"type of filter",
      "max_token_length": "The maximum token length. If a token is seen that exceeds this length then it is split at max_token_length intervals. Defaults to 255."
    }
    `,
    visible: "false",
  },
  {
    title: "edge_ngram",
    description: "Performs optional post-processing of terms generated by the classic tokenizer.",
    Json: `
    {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 2,
          "token_chars":["letter"]
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter",
          "min_gram": "Minimum length of characters in a gram. Defaults to 1.",
          "max_gram": "Maximum length of characters in a gram. Defaults to 2.",
          "custom_token_chars":"Character classes that should be included in a token. Elasticsearch will split on characters that don’t belong to the classes specified. Defaults to [] (keep all characters)."
    }`,
    visible: "false",
  },
  {
    title: "keyword ",
    description:
      "The keyword tokenizer is a “noop” tokenizer that accepts whatever text it is given and outputs the exact same text as a single term. It can be combined with token filters to normalise output, e.g. lower-casing email addresses.",
    Json: `
    {
      "type":"keyword"
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
    visible: "false",
  },
  {
    title: "letter",
    description:
      "The letter tokenizer breaks text into terms whenever it encounters a character which is not a letter. It does a reasonable job for most European languages, but does a terrible job for some Asian languages, where words are not separated by spaces.",
    Json: `
    {
      "type":"letter"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
  },
  {
    title: "lowercase",
    description:
      "The lowercase tokenizer, like the letter tokenizer breaks text into terms whenever it encounters a character which is not a letter, but it also lowercases all terms. It is functionally equivalent to the letter tokenizer combined with the lowercase token filter, but is more efficient as it performs both steps in a single pass.",
    Json: `
    {
      "type":"lowercase"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
  },
  {
    title: "N-gram",
    description:
      "The ngram tokenizer first breaks text down into words whenever it encounters one of a list of specified characters, then it emits N-grams of each word of the specified length.",
    Json: `
    { 
      "type": "ngram",
      "min_gram": 1,
      "max_gram": 2,
      "token_chars":["letter"],
      "custom_token_chars":"+-_"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "min_gram": "Minimum length of characters in a gram. Defaults to 1",
      "max_gram": "Maximum length of characters in a gram. Defaults to 2.",
      "token_chars":"Character classes that should be included in a token. Elasticsearch will split on characters that don’t belong to the classes specified. Defaults to [] (keep all characters).",
      "custom_token_chars":"Custom characters that should be treated as part of a token. For example, setting this to +-_ will make the tokenizer treat the plus, minus and underscore sign as part of a token."
    }`,
  },
  {
    title: "path_hierarchy",
    description:
      "The path_hierarchy tokenizer takes a hierarchical value like a filesystem path, splits on the path separator, and emits a term for each component in the tree.",
    Json: `
    {
      "type": "path_hierarchy",
      "delimiter": "/",
      "replacement": "-",
      "skip": 0,
      "buffer_size":"The number of characters read into the term buffer in a single pass. Defaults to 1024. The term buffer will grow by this size until all the text has been consumed. It is advisable not to change this setting."
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type": "path_hierarchy",
      "delimiter": "The character to use as the path separator. Defaults to /.",
      "replacement": "An optional replacement character to use for the delimiter. Defaults to the delimiter.",
      "skip": "The number of initial tokens to skip. Defaults to 0.",
      "buffer_size":1024
    }`,
  },
  {
    title: "pattern ",
    description:
      "The pattern tokenizer uses a regular expression to either split text into terms whenever it matches a word separator, or to capture matching text as terms.",
    Json: `
    {
          "type": "pattern ",
          "pattern": "\W+",
          "flags": " ",
          "group": 1
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type": "pattern ",
      "flags":"Java regular expression flags. Flags should be pipe-separated, ex: 'CASE_INSENSITIVE|COMMENTS'.",
      "pattern": "A Java regular expression, defaults to \W+.",
      "group": 1
    }`,
  },
  {
    title: "simple_pattern",
    description:
      "The simple_pattern tokenizer uses a regular expression to capture matching text as terms. The set of regular expression features it supports is more limited than the pattern tokenizer, but the tokenization is generally faster.",
    Json: `
    {
      "type": "simple_pattern",
      "pattern": ""
    }`,
    visible: "false",
    descriptionAttribute: `
    {
        "type": "type of filter ",
        "pattern": "Lucene regular expression, defaults to the empty string."
    }`,
  },
  {
    title: "simple_pattern_split",
    description:
      "The simple_pattern_split tokenizer uses a regular expression to split the input into terms at pattern matches. The set of regular expression features it supports is more limited than the pattern tokenizer, but the tokenization is generally faster.",
    Json: `
    {
      "type": "simple_pattern_split",
      "pattern": ""
    }`,
    visible: "false",
    descriptionAttribute: `
    {
        "type": "type of filter ",
        "pattern": "Lucene regular expression, defaults to the empty string."
    }`,
  },
  {
    title: "standard",
    description:
      "The standard tokenizer provides grammar based tokenization (based on the Unicode Text Segmentation algorithm, as specified in Unicode Standard Annex #29) and works well for most languages.",
    Json: `
    {
      "type": "standard",
      "max_token_length":255
    }`,
    visible: "false",
    descriptionAttribute: `
    {
        "type": "type of filter ",
        "max_token_length": "The maximum token length. If a token is seen that exceeds this length then it is split at max_token_length intervals. Defaults to 255."
    }`,
  },
  {
    title: "thai",
    description:
      "The thai tokenizer segments Thai text into words, using the Thai segmentation algorithm included with Java. Text in other languages in general will be treated the same as the standard tokenizer.",
    Json: `
    {
      "type": "thai"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
        "type": "type of filter "
    }`,
  },
  {
    title: "uax_url_email",
    description:
      "The uax_url_email tokenizer is like the standard tokenizer except that it recognises URLs and email addresses as single tokens.",
    Json: `
    {
      "type": "uax_url_email",
      "max_token_length":255
    }`,
    visible: "false",
    descriptionAttribute: `
    {
        "type": "type of filter ",
        "max_token_length": "The maximum token length. If a token is seen that exceeds this length then it is split at max_token_length intervals. Defaults to 255."
    }`,
  },
  {
    title: "whitespace",
    description: "The whitespace tokenizer breaks text into terms whenever it encounters a whitespace character.",
    Json: `
    {
      "type": "whitespace",
      "max_token_length":255
    }`,
    visible: "false",
    descriptionAttribute: `
    {
        "type": "type of filter ",
        "max_token_length": "The maximum token length. If a token is seen that exceeds this length then it is split at max_token_length intervals. Defaults to 255."
    }`,
  },
];
