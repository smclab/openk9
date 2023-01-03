import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import { useCreateOrUpdateTokenFilterMutation, useTokenFilterQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, KeyValue, MultiSelectForDinamicFields, CreateFieldDinamically } from "./Form";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { TokenFiltersQuery } from "./TokenFilters";

const TokenFilterQuery = gql`
  query TokenFilter($id: ID!) {
    tokenFilter(id: $id) {
      id
      name
      description
      jsonConfig
    }
  }
`;

gql`
  mutation CreateOrUpdateTokenFilter($id: ID, $name: String!, $description: String, $jsonConfig: String) {
    tokenFilter(id: $id, tokenFilterDTO: { name: $name, description: $description, jsonConfig: $jsonConfig }) {
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

export function TokenFilter() {
  const { tokenFilterId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const tokenFilterQuery = useTokenFilterQuery({
    variables: { id: tokenFilterId as string },
    skip: !tokenFilterId || tokenFilterId === "new",
  });
  const [templateChoice, setTemplateChoice] = React.useState<KeyValue>(JSON.parse(tokenFilterQuery.data?.tokenFilter?.jsonConfig || `{}`));
  const [createOrUpdateTokenFilterMutate, createOrUpdateTokenFilterMutation] = useCreateOrUpdateTokenFilterMutation({
    refetchQueries: [TokenFilterQuery, TokenFiltersQuery],
    onCompleted(data) {
      if (data.tokenFilter?.entity) {
        if (tokenFilterId === "new") {
          navigate(`/token-filters/`, { replace: true });
          showToast({ displayType: "success", title: "Tokenizer created", content: data.tokenFilter.entity.name ?? "" });
        } else {
          navigate(`/token-filters/`, { replace: true });
          showToast({ displayType: "info", title: "Tokenizer updated", content: data.tokenFilter.entity.name ?? "" });
        }
      }
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: Filters[0].description,
        jsonConfig: "",
      }),
      []
    ),
    originalValues: tokenFilterQuery.data?.tokenFilter,
    isLoading: tokenFilterQuery.loading || createOrUpdateTokenFilterMutation.loading,
    onSubmit(data) {
      createOrUpdateTokenFilterMutate({ variables: { id: tokenFilterId !== "new" ? tokenFilterId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTokenFilterMutation.data?.tokenFilter?.fieldValidators),
  });

  React.useEffect(() => {
    if (JSON.stringify(templateChoice) !== "{}") {
      form.inputProps("jsonConfig").onChange(JSON.stringify(templateChoice));
    }
  }, [templateChoice]);

  if (tokenFilterId !== "new" && JSON.stringify(templateChoice) === "{}") {
    try {
      const value = JSON.parse(form.inputProps("jsonConfig").value);
      setTemplateChoice(value);
    } catch (error) {}
  }
  if (tokenFilterId === "new" && JSON.stringify(templateChoice) === "{}") {
    try {
      const value = JSON.parse(Filters[0].Json);
      setTemplateChoice(value);
    } catch (error) {}
  }
  if (tokenFilterId !== "new") {
    Filters.forEach((filter) => {
      if (filter.title === templateChoice.type) {
        filter.visible = "" + true;
      }
    });
  }
  if (tokenFilterId === "new") {
    Filters[0].visible = "" + true;
  }
  if (tokenFilterQuery.loading) {
    return <div></div>;
  }
  return (
    <>
      <ClayLayout.ContainerFluid view>
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
            id={tokenFilterId}
            templates={Filters}
            onChangeDescription={form.inputProps("description").onChange}
            templateChoice={templateChoice}
            setTemplateChoice={setTemplateChoice}
          />
          <CreateFieldDinamically templates={Filters} setTemplateChoice={setTemplateChoice} templateChoice={templateChoice} />
          <div className="sheet-footer">
            <ClayButton type="submit" disabled={!form.canSubmit}>
              {tokenFilterId === "new" ? "Create" : "Update"}
            </ClayButton>
          </div>
        </ClayForm>
      </ClayLayout.ContainerFluid>
    </>
  );
}

const Filters = [
  {
    title: "apostrophe",
    description: "Strips all characters after an apostrophe, including the apostrophe itself.",
    Json: `
    {
      "type":"apostrophe"
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
    visible: "false",
  },
  {
    title: "classic",
    description: "Performs optional post-processing of terms generated by the classic tokenizer.",
    Json: `
    {
      "type":"classic"
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
    visible: "false",
  },
  {
    title: "trim",
    description:
      "Removes leading and trailing whitespace from each token in a stream. While this can change the length of a token, the trim filter does not change a token’s offsets.",
    Json: `
    {
      "type":"trim"
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
    visible: "false",
  },
  {
    title: "uppercase",
    description: "Changes token text to uppercase. For example, you can use the uppercase filter to change the Lazy DoG to THE LAZY DOG.",
    Json: `
    {
      "type":"uppercase"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
  },
  {
    title: "reverse",
    description: "Reverses each token in a stream. For example, you can use the reverse filter to change cat to tac.",
    Json: `
    {
      "type":"reverse"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
  },

  {
    title: "remove_duplicates",
    description: "Removes duplicate tokens in the same position.",
    Json: `
    {
      "type":"remove_duplicates"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
  },
  {
    title: "edge_ngram",
    description: "Forms an n-gram of a specified length from the beginning of a token.",
    Json: `
    {
        "type": "edge_ngram",
        "min_gram": 1,
        "max_gram": 2,
        "side":"front",
        "preserve_original":false
    }`,
    visible: "false",
    descriptionAttribute: `
    {
        "type":"type of filter",
        "min_gram": "Minimum character length of a gram. Defaults to 1.",
        "max_gram": "Maximum character length of a gram. For custom token filters, defaults to 2. For the built-in edge_ngram filter, defaults to 1.",
        "preserve_original": "Emits original token when set to true. Defaults to false.",
        "side":"Indicates whether to truncate tokens from the front or back. Defaults to front"
    }`,
  },
  {
    title: "ngram",
    description: "Forms n-grams of specified lengths from a token.",
    Json: `
    {
          "type": "ngram",
          "min_gram": 3,
          "max_gram": 5,
          "preserve_original":false  
     }`,
    visible: "false",
    descriptionAttribute: `
    {
          "type":"type of filter",
          "min_gram": "Minimum character length of a gram. Defaults to 1.",
          "max_gram": "Maximum character length of a gram. For custom token filters, defaults to 2. For the built-in edge_ngram filter, defaults to 1.",
          "preserve_original": "Emits original token when set to true. Defaults to false."
        }`,
  },
  {
    title: "shingle",
    description:
      "Add shingles, or word n-grams, to a token stream by concatenating adjacent tokens. By default, the shingle token filter outputs two-word shingles and unigrams.",
    Json: `
    {
      "type": "shingle",
      "min_shingle_size": 2,
      "max_shingle_size": 3,
      "output_unigrams": true
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type": "type of filter",
      "min_shingle_size": "Minimum number of tokens to concatenate when creating shingles. Defaults to 2.",
      "max_shingle_size": "Maximum number of tokens to concatenate when creating shingles. Defaults to 2.",
      "output_unigrams": "f true, the output includes the original input tokens. If false, the output only includes shingles; the original input tokens are removed. Defaults to true."
    }`,
  },
  {
    title: "snowball",
    description: "A filter that stems words using a Snowball-generated stemmer.",
    Json: `
    {
      "type": "snowball",
      "language": "English"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "language": "Language"
    }`,
  },
  {
    title: "stemmer",
    description: "Provides algorithmic stemming for several languages, some with additional variants.",
    Json: `
    {
      "type": "stemmer",
      "language": "light_german"
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "language": "Language"
    }`,
  },
  {
    title: "porter_stem",
    description: "Provides algorithmic stemming for the English language, based on the Porter stemming algorithm.",
    Json: `
    {
      "type":  "porter_stem" 
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter"
    }`,
  },
  {
    title: "truncate",
    description:
      "Truncates tokens that exceed a specified character limit. This limit defaults to 10 but can be customized using the length parameter.",
    Json: `
    {
      "type": "truncate",
      "length": 10
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "length":"Character limit for each token. Tokens exceeding this limit are truncated. Defaults to 10."

    }`,
  },
  {
    title: "unique",
    description: "Provides algorithmic stemming for the English language, based on the Porter stemming algorithm.",
    Json: `
    {
      "type": "unique",
      "only_on_same_position": true
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "only_on_same_position": " If true, only remove duplicate tokens in the same position. Defaults to false."

    }`,
  },
  {
    title: "pattern_replace",
    description: "Uses a regular expression to match and replace token substrings.",
    Json: `
    {
      "type": "pattern_replace",
          "pattern": "",
          "replacement": "",
          "all": true
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "pattern": "Regular expression, written in Java’s regular expression syntax. The filter replaces token substrings matching this pattern with the substring in the replacement parameter.",
      "replacement": " Replacement substring. Defaults to an empty substring.",
      "all": " If true, all substrings matching the pattern parameter’s regular expression are replaced. If false, the filter replaces only the first matching substring in each token. Defaults to true."
    }`,
  },
  {
    title: "limit",
    description:
      "Limits the number of output tokens. The limit filter is commonly used to limit the size of document field values based on token count.",
    Json: `
    {
      "type": "limit",
      "max_token_count": 10
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "max_token_count": "Maximum number of tokens to keep. Once this limit is reached, any remaining tokens are excluded from the output. Defaults to 1."

    }`,
  },
  {
    title: "lowercase",
    description: "Changes token text to lowercase. For example, you can use the lowercase filter to change THE Lazy DoG to the lazy dog.",
    Json: `
    {
      "type": "lowercase",
      "language": ""
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "type":"type of filter",
      "language": "Language-specific lowercase token filter to use. Example:greek,irish,turkish."

    }`,
  },
];
