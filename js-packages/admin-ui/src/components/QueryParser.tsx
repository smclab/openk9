import { gql } from "@apollo/client";
import { ClayToggle } from "@clayui/form";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateQueryParserConfigMutation, useQueryParserConfigQuery } from "../graphql-generated";
import {
  ContainerFluid,
  CreateField,
  CreateFieldDinamically,
  CustomFormGroup,
  FormatString,
  fromFieldValidators,
  InformationField,
  KeyValue,
  MultiSelectDynamicField,
  StyleToggle,
  TemplateQueryComponent,
  TextArea,
  TextInput,
  useForm,
} from "./Form";
import ClayButton from "@clayui/button";
import { ClayButtonWithIcon } from "@clayui/button";
import { Link } from "react-router-dom";
import ClayToolbar from "@clayui/toolbar";
import { QueryParserConfigsQuery } from "./QueryParsers";
import { useToast } from "./ToastProvider";
import { ClassNameButton } from "../App";

const QueryParserConfigQuery = gql`
  query QueryParserConfig($id: ID!) {
    queryParserConfig(id: $id) {
      id
      name
      description
      type
      jsonConfig
    }
  }
`;

gql`
  mutation CreateOrUpdateQueryParserConfig(
    $queryParserConfigId: ID
    $searchConfigId: ID!
    $name: String!
    $description: String
    $type: String!
    $jsonConfig: String
  ) {
    queryParserConfig(
      searchConfigId: $searchConfigId
      queryParserConfigId: $queryParserConfigId
      queryParserConfigDTO: { name: $name, description: $description, type: $type, jsonConfig: $jsonConfig }
    ) {
      entity {
        id
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function QueryParserConfig() {
  const { searchConfigId, queryParserConfigId = "new" } = useParams();
  const navigate = useNavigate();
  const Toast = useToast();
  const queryParserQuery = useQueryParserConfigQuery({
    variables: { id: queryParserConfigId as string },
    skip: !queryParserConfigId || queryParserConfigId === "new",
  });
  const [templateChoice, setTemplateChoice] = React.useState<KeyValue>(
    JSON.parse(queryParserQuery.data?.queryParserConfig?.jsonConfig || `{}`)
  );
  const [IsEmpty, setIsEmpty] = React.useState(false);
  const [createOrUpdateQueryParserConfigMutate, createOrUpdateQueryParserConfigMutation] = useCreateOrUpdateQueryParserConfigMutation({
    refetchQueries: [QueryParserConfigQuery, QueryParserConfigsQuery],
    onCompleted(data) {
      if (data.queryParserConfig?.entity) {
        if (queryParserConfigId === "new") {
          Toast({ content: "", displayType: "success", title: "Creat query parser" });
          navigate(`/search-configs/${searchConfigId}/query-parsers`, { replace: true });
        } else {
          Toast({ content: "", displayType: "success", title: "Update query parser" });
          navigate(`/search-configs/${searchConfigId}/query-parsers/${data.queryParserConfig.entity.id}`, { replace: true });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        type: "",
        jsonConfig: "{}",
      }),
      []
    ),
    originalValues: queryParserQuery.data?.queryParserConfig,
    isLoading: queryParserQuery.loading || queryParserQuery.loading,
    onSubmit(data) {
      createOrUpdateQueryParserConfigMutate({
        variables: {
          searchConfigId: searchConfigId as string,
          queryParserConfigId: queryParserConfigId !== "new" ? queryParserConfigId : undefined,
          ...data,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateQueryParserConfigMutation.data?.queryParserConfig?.fieldValidators),
  });
  const [type, setType] = React.useState("{}");
  React.useEffect(() => {
    if (JSON.stringify(templateChoice) !== "{}") {
      form.inputProps("jsonConfig").onChange(JSON.stringify(templateChoice));
    }
  }, [templateChoice]);
  if (queryParserQuery.loading) {
    return <div></div>;
  }
  if (!queryParserQuery.loading && queryParserConfigId !== "new" && type === "{}") {
    setType(form.inputProps("type").value);
  }
  if (queryParserConfigId !== "new" && JSON.stringify(templateChoice) === "{}" && IsEmpty === false) {
    try {
      const value = JSON.parse(form.inputProps("jsonConfig").value);
      setTemplateChoice(value);
      setIsEmpty(true);
    } catch (error) {}
  }
  TemplateQueryParser.map(
    (template) => (template.title = FormatString({ howFormat: ["upperFirstLetter", "withoutSpace"], words: template.title }))
  );
  if (queryParserConfigId !== "new") {
    TemplateQueryParser.forEach((filter) => {
      if (filter.title === type) {
        filter.visible = "" + true;
      }
    });
  }
  if (queryParserConfigId === "new" && JSON.stringify(templateChoice) === "{}") {
    TemplateQueryParser[0].visible = "true";
    setTemplateChoice(JSON.parse(TemplateQueryParser[0].Json));
  }
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item>
              <Link to={`/search-configs/${searchConfigId}/query-parsers`}>
                <ClayButtonWithIcon className={ClassNameButton} aria-label="" symbol="angle-left" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluid>
      </ClayToolbar>
      <ContainerFluid>
        <form
          className="sheet"
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("name")} />
          <TextArea label="Description" {...form.inputProps("description")} />
          {/* 
          <MultiSelectDynamicField
            id={queryParserConfigId}
            setTitle={form.inputProps("name").onChange}
            templates={TemplateQueryParser}
            onChangeDescription={form.inputProps("description").onChange}
            templateChoice={templateChoice}
            setTemplateChoice={setTemplateChoice}
            form={form}
            excludeType={false}
            query={queryParserQuery}
          />
          <CreateField templates={TemplateQueryParser} setTemplateChoice={setTemplateChoice} templateChoice={templateChoice} /> */}
          <TemplateQueryComponent TemplateQueryParser={TemplateQueryParser} />
          <div className="sheet-footer">
            <ClayButton type="submit" className={ClassNameButton} disabled={!form.canSubmit}>
              {queryParserConfigId === "new" ? "Create" : "Update"}
            </ClayButton>
          </div>
        </form>
      </ContainerFluid>
    </React.Fragment>
  );
}

const TemplateQueryParser = [
  {
    title: "DATE_ORDER",
    description: "DATE_ORDER  template.",
    Json: `
  {
    "scale": "",
     "boost": 50.0
  }`,
    descriptionAttribute: `
  {
    "scale": "Scale used to order by date",
    "boost": "Boost applied by this query parser to order result"
  }`,
    visible: "false",
  },
  {
    title: "DATE",
    description: "Date Template",
    Json: `
    {
     
    }`,
    descriptionAttribute: `
    {
     
    }`,
    visible: "false",
  },
  {
    title: "DOCTYPE",
    description: "doctype Template",
    Json: `
    {
    }`,
    descriptionAttribute: `
    {
    }`,
    visible: "false",
  },
  {
    title: "TYPE",
    description: "type Template",
    Json: `
    {
    }`,
    descriptionAttribute: `
    {
    }`,
    visible: "false",
  },
  {
    title: "ENTITY",
    description: "",
    Json: `
    {
      "boost": 50.0,
       "queryCondition": "string",
       "manageEntityName": true
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "boost": "Boost applied in case of match of Entity query parser", 
      "queryCondition": "Query Type applied in case of match of Entity query parser",
      "manageEntityName": "sample"
    }`,
  },
  {
    title: "TEXT",
    description: "",
    Json: `
    {
       "boost": 50.0,
       "valuesQueryType": "MUST",
       "globalQueryType": "MUST"
    }`,
    multiselect: `
    {
       "valuesQueryType": ["MUST","SHOULD","MIN_SHOULD_1","MIN_SHOULD_2","MIN_SHOULD_3","MUST_NOT","FILTER"], 
       "globalQueryType": ["MUST","SHOULD","MIN_SHOULD_1","MIN_SHOULD_2","MIN_SHOULD_3","MUST_NOT","FILTER"]
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "boost": "Boost applied in case of match of Text query parser",
      "valuesQueryType": "Query type applied in case of match inside values array", 
      "globalQueryType": "Query type applied in case of match betweem different Text query parser"
    }`,
  },
];
