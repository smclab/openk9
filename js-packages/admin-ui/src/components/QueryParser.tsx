import { gql } from "@apollo/client";
import ClayForm, { ClaySelect, ClayToggle } from "@clayui/form";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateQueryParserConfigMutation, useQueryParserConfigQuery } from "../graphql-generated";
import { fromFieldValidators, InformationField, KeyValue, TextArea, TextInput, useForm } from "./Form";
import ClayButton from "@clayui/button";
import ClayLayout from "@clayui/layout";
import { ClayButtonWithIcon } from "@clayui/button";
import { Link } from "react-router-dom";
import ClayToolbar from "@clayui/toolbar";
import { QueryParserConfigsQuery } from "./QueryParsers";
import ClayPanel from "@clayui/panel";

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
        if (queryParserConfigId === "new") navigate(`/search-configs/${searchConfigId}/query-parsers`, { replace: true });
        else navigate(`/search-configs/${searchConfigId}/query-parsers/${data.queryParserConfig.entity.id}`, { replace: true });
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

  if (queryParserConfigId !== "new") {
    TemplateQueryParser.forEach((filter) => {
      if (filter.title === type) {
        filter.visible = "" + true;
      }
    });
  }
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ClayLayout.ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item>
              <Link to={`/search-configs/${searchConfigId}/query-parsers`}>
                <ClayButtonWithIcon aria-label="" symbol="angle-left" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ClayLayout.ContainerFluid>
      </ClayToolbar>
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
          <ClayPanel displayTitle="Type" displayType="secondary">
            <ClayPanel.Body>
              <ClayForm.Group>
                <div className="form-group-item">
                  <ClaySelect
                    defaultValue={queryParserConfigId === "new" ? "" : type}
                    onChange={(event) => {
                      form.inputProps("type").onChange(event.currentTarget.value);

                      TemplateQueryParser.map((element) => {
                        element.visible = "false";
                        if (element.title === event.currentTarget.value) {
                          element.visible = "true";
                          setTemplateChoice(JSON.parse(element.Json));
                          return true;
                        }
                      });
                      const dataSelect = TemplateQueryParser.find((element) => element.title === event.currentTarget.value);
                      form.inputProps("description").onChange(dataSelect!.description);
                    }}
                  >
                    {TemplateQueryParser.map((filter, index) => (
                      <ClaySelect.Option key={index} label={filter.title} value={filter.title} />
                    ))}
                  </ClaySelect>
                </div>
              </ClayForm.Group>
            </ClayPanel.Body>
          </ClayPanel>
          {TemplateQueryParser.map((template) => {
            if (template.visible === "true") {
              const keysOfFields = Object.keys(JSON.parse(template.Json));
              const descriptionsFields = JSON.parse(template.descriptionAttribute);
              let fields: Array<any> = [];
              let i = 0;
              while (i < keysOfFields.length) {
                let t = i;
                if (keysOfFields[i] !== "type" && typeof templateChoice?.[keysOfFields[i]] === "string") {
                  if (
                    keysOfFields[i] === "valuesQueryType" ||
                    keysOfFields[i] === "globalQueryType" ||
                    keysOfFields[i] === "queryCondition"
                  ) {
                    fields.push(
                      <div className="form-group-item" key={keysOfFields[i]}>
                        <label id={keysOfFields[i] + i} style={{ paddingTop: "18px" }}>
                          {keysOfFields[i]}
                        </label>
                        {InformationField(descriptionsFields[keysOfFields[i]])}
                        <ClaySelect
                          defaultValue={templateChoice?.[keysOfFields[i]]}
                          aria-label="Select Label"
                          id="mySelectId"
                          onChange={(event) => {
                            setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: event.currentTarget.value });
                          }}
                        >
                          <ClaySelect.Option label={"MUST"} value={"MUST"} />
                          <ClaySelect.Option label={"SHOULD"} value={"SHOULD"} />
                          <ClaySelect.Option label={"MIN_SHOULD_1"} value={"MIN_SHOULD_1"} />
                          <ClaySelect.Option label={"MIN_SHOULD_2"} value={"MIN_SHOULD_2"} />
                          <ClaySelect.Option label={"MIN_SHOULD_3"} value={"MIN_SHOULD_3"} />
                          <ClaySelect.Option label={"MUST_NOT"} value={"MUST_NOT"} />
                          <ClaySelect.Option label={"FILTER"} value={"FILTER"} />
                        </ClaySelect>
                      </div>
                    );
                  } else {
                    fields.push(
                      <div className="form-group-item" key={keysOfFields[i]}>
                        <label id={keysOfFields[i]} style={{ paddingTop: "18px" }}>
                          {keysOfFields[i]}
                        </label>
                        {InformationField(descriptionsFields[keysOfFields[i]])}
                        <input
                          type="text"
                          id={keysOfFields[i] + i}
                          className="form-control"
                          value={templateChoice?.[keysOfFields[i]]}
                          onChange={(event) => {
                            setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: event.currentTarget.value });
                          }}
                        ></input>
                      </div>
                    );
                  }
                }
                if (typeof templateChoice?.[keysOfFields[i]] == "number") {
                  fields.push(
                    <div className="form-group-item" key={keysOfFields[i]}>
                      <label id={keysOfFields[i] + i} style={{ paddingTop: "18px" }}>
                        {keysOfFields[i]}
                      </label>
                      {InformationField(descriptionsFields[keysOfFields[i]])}
                      <input
                        type="number"
                        id={keysOfFields[i] + i}
                        className="form-control"
                        value={templateChoice?.[keysOfFields[i]]}
                        onChange={(event) => {
                          setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: event.currentTarget.value });
                        }}
                      ></input>
                    </div>
                  );
                }
                if (typeof templateChoice?.[keysOfFields[i]] == "boolean") {
                  fields.push(
                    <div className="form-group" style={{ paddingTop: "18px" }} key={keysOfFields[i]}>
                      <ClayToggle
                        label={keysOfFields[i]}
                        id={keysOfFields[i] + i}
                        toggled={templateChoice?.[keysOfFields[i]]}
                        onToggle={(event) => {
                          setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: !templateChoice?.[keysOfFields[t]] });
                        }}
                      />
                      {InformationField(descriptionsFields[keysOfFields[i]])}
                    </div>
                  );
                }
                if (typeof templateChoice?.[keysOfFields[i]] == "object") {
                  const values = Object.values(templateChoice?.[keysOfFields[i]]);
                  fields.push(
                    <div className="form-group-item" key={keysOfFields[i]}>
                      <label id={keysOfFields[i] + i} style={{ paddingTop: "18px" }}>
                        {keysOfFields[i]}
                      </label>
                      {InformationField(descriptionsFields[keysOfFields[i]])}
                      <ClaySelect
                        aria-label="Select Label"
                        id="mySelectId"
                        onChange={(event) => {
                          setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: event.currentTarget.value });
                        }}
                      >
                        {values?.map((item: any) => (
                          <ClaySelect.Option key={item} label={item} value={item} />
                        ))}
                      </ClaySelect>
                    </div>
                  );
                }
                i++;
              }
              return fields;
            }
          })}
          <div className="sheet-footer">
            <ClayButton type="submit" disabled={!form.canSubmit}>
              {queryParserConfigId === "new" ? "Create" : "Update"}
            </ClayButton>
          </div>
        </ClayForm>
      </ClayLayout.ContainerFluid>
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
     "boost": 50
  }`,
    descriptionAttribute: `
  {
    "scale": "string",
    "boost": "long"
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
      "boost": 50,
       "queryCondition": "string",
       "manageEntityName": true
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "boost": "long", 
      "queryCondition": "string",
      "manageEntityName": "boolean"
    }`,
  },

  {
    title: "TEXT ",
    description: "",
    Json: `
    {
      "boost": 50,
       "valuesQueryType": {"key":"MUST","key2":"SHOULD","key3":"MIN_SHOULD_1","key4":"MIN_SHOULD_2","key5":"MIN_SHOULD_3","key6":"MUST_NOT","key7":"FILTER"}, 
       "globalQueryType": {"key":"MUST","key2":"SHOULD","key3":"MIN_SHOULD_1","key4":"MIN_SHOULD_2","key5":"MIN_SHOULD_3","key6":"MUST_NOT","key7":"FILTER"}
    }`,
    visible: "false",
    descriptionAttribute: `
    {
      "boost": "50",
      "valuesQueryType": "string", 
      "globalQueryType": "string"
    }`,
  },
];
