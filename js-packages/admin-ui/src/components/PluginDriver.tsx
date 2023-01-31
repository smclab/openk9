import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm, { ClaySelect, ClayToggle } from "@clayui/form";
import ClayButton from "@clayui/button";
import {
  PluginDriverType,
  useBindPluginDriverToDataSourceMutation,
  useCreateOrUpdatePluginDriverMutation,
  usePluginDriverByNameQuery,
  usePluginDriverQuery,
} from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, EnumSelect, KeyValue, InformationField, StyleToggle } from "./Form";
import { PluginDriversQuery } from "./PluginDrivers";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import ClayPanel from "@clayui/panel";
import { ClassNameButton } from "../App";

const PluginDriverQuery = gql`
  query PluginDriver($id: ID!) {
    pluginDriver(id: $id) {
      id
      name
      description
      type
      jsonConfig
    }
  }
`;

gql`
  mutation CreateOrUpdatePluginDriver($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $jsonConfig: String) {
    pluginDriver(id: $id, pluginDriverDTO: { name: $name, description: $description, type: $type, jsonConfig: $jsonConfig }) {
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

export function PluginDriver() {
  const { pluginDriverId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const pluginDriverQuery = usePluginDriverQuery({
    variables: { id: pluginDriverId as string },
    skip: !pluginDriverId || pluginDriverId === "new",
  });
  const [templateChoice, setTemplateChoice] = React.useState<KeyValue>(
    JSON.parse(pluginDriverQuery.data?.pluginDriver?.jsonConfig || `{}`)
  );
  const [createOrUpdatePluginDriverMutate, createOrUpdatePluginDriverMutation] = useCreateOrUpdatePluginDriverMutation({
    refetchQueries: [PluginDriverQuery, PluginDriversQuery, PluginDriverByNameQuery],
    onCompleted(data) {
      if (data.pluginDriver?.entity) {
        if (pluginDriverId === "new") {
          navigate(`/plugin-drivers/`, { replace: true });
          showToast({ displayType: "success", title: "Plugin drivers created", content: data.pluginDriver.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Plugin drivers updated", content: data.pluginDriver.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        type: PluginDriverType.Http,
        jsonConfig: "{}",
      }),
      []
    ),
    originalValues: pluginDriverQuery.data?.pluginDriver,
    isLoading: pluginDriverQuery.loading || createOrUpdatePluginDriverMutation.loading,
    onSubmit(data) {
      createOrUpdatePluginDriverMutate({ variables: { id: pluginDriverId !== "new" ? pluginDriverId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdatePluginDriverMutation.data?.pluginDriver?.fieldValidators),
  });
  const [type, setType] = React.useState("" + form.inputProps("type").value);

  React.useEffect(() => {
    if (JSON.stringify(templateChoice) !== "{}") {
      form.inputProps("jsonConfig").onChange(JSON.stringify(templateChoice));
    }
  }, [templateChoice]);
  if (pluginDriverQuery.loading) {
    return <div></div>;
  }
  if (pluginDriverId !== "new" && JSON.stringify(templateChoice) === "{}") {
    try {
      const value = JSON.parse(form.inputProps("jsonConfig").value);
      setTemplateChoice(value);
    } catch (error) {}
  }

  if (pluginDriverId !== "new") {
    PluginDriverOptions.forEach((filter) => {
      if (filter.title === type) {
        filter.visible = "" + true;
      }
    });
  }

  return (
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
                  defaultValue={pluginDriverId === "new" ? "" : type}
                  onChange={(event) => {
                    switch (event.currentTarget.value) {
                      case "HTTP":
                        form.inputProps("type").onChange(PluginDriverType.Http);
                        break;
                      case "Custom":
                        setType("Custom");
                        break;
                    }
                    PluginDriverOptions.map((element) => {
                      element.visible = "false";
                      if (element.title === event.currentTarget.value) {
                        element.visible = "true";
                        setTemplateChoice(JSON.parse(element.Json));
                        return true;
                      }
                    });
                    const dataSelect = PluginDriverOptions.find((element) => element.title === event.currentTarget.value);
                    form.inputProps("description").onChange(dataSelect!.description);
                  }}
                >
                  {PluginDriverOptions.map((filter, index) => (
                    <ClaySelect.Option key={index} label={filter.title} value={filter.title} />
                  ))}
                </ClaySelect>
              </div>
            </ClayForm.Group>
          </ClayPanel.Body>
        </ClayPanel>
        {PluginDriverOptions.map((template) => {
          if (template.visible === "true") {
            const keysOfFields = Object.keys(JSON.parse(template.Json));
            const descriptionsFields = JSON.parse(template.descriptionAttribute);
            let fields: Array<any> = [];
            let i = 0;
            while (i < keysOfFields.length) {
              let t = i;
              if (keysOfFields[i] !== "type" && typeof templateChoice?.[keysOfFields[i]] === "string") {
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
                    <style type="text/css">{StyleToggle}</style>
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
              i++;
            }
            return fields;
          }
        })}
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {pluginDriverId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}

const PluginDriverByNameQuery = gql`
  query PluginDriverByName($name: String) {
    pluginDrivers(searchText: $name, first: 1) {
      edges {
        node {
          id
        }
      }
    }
  }
`;

export function useWizardPluginDriverBinding(name: string) {
  const pluginDriverByNameQuery = usePluginDriverByNameQuery({ variables: { name } });
  const [bindPluginDriverToDatasourceMutate] = useBindPluginDriverToDataSourceMutation();
  const pluginDriverId = pluginDriverByNameQuery.data?.pluginDrivers?.edges?.[0]?.node?.id;
  function bindPluginDriverToWizardDatasource(datasourceId: string) {
    if (pluginDriverId) bindPluginDriverToDatasourceMutate({ variables: { datasourceId, pluginDriverId } });
  }
  return bindPluginDriverToWizardDatasource;
}

const PluginDriverOptions = [
  {
    title: "Custom",
    description: "",
    Json: `
    {
    }
    `,

    descriptionAttribute: `
    {
    }
    `,
    visible: "false",
  },
  {
    title: "HTTP",
    description: "",
    Json: `
    {
      "host": "openk9-parser",
      "port": 5000,
      "secure": false,
      "path": "/execute",
      "method": "POST"
    }
    `,

    descriptionAttribute: `
    {
      "host": "",
      "port": "",
      "secure": "",
      "path": "",
      "method": ""
    }
    `,
    visible: "false",
  },
];
