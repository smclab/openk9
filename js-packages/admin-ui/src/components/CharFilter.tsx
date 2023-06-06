import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import { useCharFilterQuery, useCreateOrUpdateCharFilterMutation } from "../graphql-generated";
import {
  useForm,
  fromFieldValidators,
  TextInput,
  TextArea,
  KeyValue,
  MultiSelectForDinamicFields,
  CreateFieldDinamically,
  CustomButtom,
  ContainerFluid,
} from "./Form";
import { useToast } from "./ToastProvider";
import { CharFiltersQuery } from "./CharFilters";

const CharFilterQuery = gql`
  query CharFilter($id: ID!) {
    charFilter(id: $id) {
      id
      name
      description
      jsonConfig
    }
  }
`;

gql`
  mutation CreateOrUpdateCharFilter($id: ID, $name: String!, $description: String, $jsonConfig: String) {
    charFilter(id: $id, charFilterDTO: { name: $name, description: $description, jsonConfig: $jsonConfig }) {
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

export function CharFilter() {
  const { charFilterId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const charFilterQuery = useCharFilterQuery({
    variables: { id: charFilterId as string },
    skip: !charFilterId || charFilterId === "new",
  });
  const [templateChoice, setTemplateChoice] = React.useState<KeyValue>(JSON.parse(charFilterQuery.data?.charFilter?.jsonConfig || `{}`));
  const [createOrUpdateCharFilterMutate, createOrUpdateCharFilterMutation] = useCreateOrUpdateCharFilterMutation({
    refetchQueries: [CharFilterQuery, CharFiltersQuery],
    onCompleted(data) {
      if (data.charFilter?.entity) {
        if (charFilterId === "new") {
          navigate(`/char-filters/`, { replace: true });
          showToast({ displayType: "success", title: "Char filter created", content: data.charFilter.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "char filter updated", content: data.charFilter.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: Filters[0].title,
        description: Filters[0].description,
        jsonConfig: "{}",
      }),
      []
    ),
    originalValues: charFilterQuery.data?.charFilter,
    isLoading: charFilterQuery.loading || createOrUpdateCharFilterMutation.loading,
    onSubmit(data) {
      createOrUpdateCharFilterMutate({ variables: { id: charFilterId !== "new" ? charFilterId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateCharFilterMutation.data?.charFilter?.fieldValidators),
  });
  React.useEffect(() => {
    if (JSON.stringify(templateChoice) !== "{}") {
      form.inputProps("jsonConfig").onChange(JSON.stringify(templateChoice));
    }
  }, [templateChoice]);

  if (charFilterId === "new" && JSON.stringify(templateChoice) === "{}") {
    try {
      const value = JSON.parse(Filters[0].Json);
      setTemplateChoice(value);
    } catch (error) {}
  }
  if (charFilterId !== "new") {
    Filters.forEach((filter) => {
      if (filter.title === templateChoice.type) {
        filter.visible = "" + true;
      }
    });
  }
  if (charFilterId === "new") {
    Filters[0].visible = "" + true;
  }
  if (charFilterQuery.loading) {
    return <div></div>;
  }
  if (charFilterId !== "new" && JSON.stringify(templateChoice) === "{}") {
    try {
      const value = JSON.parse(form.inputProps("jsonConfig").value);
      setTemplateChoice(value);
    } catch (error) {}
  }
  return (
    <ContainerFluid>
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
          id={charFilterId}
          setTitle={form.inputProps("name").onChange}
          templates={Filters}
          onChangeDescription={form.inputProps("description").onChange}
          templateChoice={templateChoice}
          setTemplateChoice={setTemplateChoice}
        />
        <CreateFieldDinamically templates={Filters} setTemplateChoice={setTemplateChoice} templateChoice={templateChoice} />
        <div className="sheet-footer">
          <CustomButtom nameButton={charFilterId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </ClayForm>
    </ContainerFluid>
  );
}

const Filters = [
  {
    title: "html_strip",
    description: "Strips HTML elements from a text and replaces HTML entities with their decoded value.",
    Json: `
    {
      "type":"html_strip",
      "escaped_tags": ["p"]
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter",
      "escaped_tags":"(Optional, array of strings) Array of HTML elements without enclosing angle brackets (< >). The filter skips these HTML elements when stripping HTML from the text. For example, a value of [ 'p' ] skips the <p> HTML element."
    }`,
    visible: "false",
  },
  {
    title: "mapping",
    description:
      "The mapping character filter accepts a map of keys and values. Whenever it encounters a string of characters that is the same as a key, it replaces them with the value associated with that key.",
    Json: `
    {
      "type":"mapping",
      "mappings": ["key => value"],
      "mappings_path": ["key => value"]
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter",
      "mappings":"Either this or the mappings_path parameter must be specified.",
      "mappings_path":"This path must be absolute or relative to the config location, and the file must be UTF-8 encoded. Each mapping in the file must be separated by a line break."
    }`,
    visible: "false",
  },
  {
    title: "pattern_replace",
    description:
      "The pattern_replace character filter uses a regular expression to match characters which should be replaced with the specified replacement string. The replacement string can refer to capture groups in the regular expression.",
    Json: `
    {
      "type":"pattern_replace",
      "pattern": "",
      "replacement": "",
      "flags":""
    }`,
    descriptionAttribute: `
    {
      "type":"type of filter",
      "pattern":"A Java regular expression. Required.",
      "replacement":"The replacement string, which can reference capture groups using the $1..$9 syntax, as explained here.",
      "flags":"Java regular expression flags. Flags should be pipe-separated, eg 'CASE_INSENSITIVE|COMMENTS'."
    }`,
    visible: "false",
  },
];
