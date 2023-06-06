import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import { useCreateOrUpdateTabMutation, useTabQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, NumberInput, MainTitle, CustomButtom, ContainerFluid } from "./Form";
import { TabsQuery } from "./Tabs";
import { useToast } from "./ToastProvider";

const TabQuery = gql`
  query Tab($id: ID!) {
    tab(id: $id) {
      id
      name
      description
      priority
    }
  }
`;

gql`
  mutation CreateOrUpdateTab($id: ID, $name: String!, $description: String, $priority: Int!) {
    tab(id: $id, tabDTO: { name: $name, description: $description, priority: $priority }) {
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

export function Tab() {
  const { tabId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const tabQuery = useTabQuery({
    variables: { id: tabId as string },
    skip: !tabId || tabId === "new",
  });
  const [createOrUpdateTabMutate, createOrUpdateTabMutation] = useCreateOrUpdateTabMutation({
    refetchQueries: [TabQuery, TabsQuery],
    onCompleted(data) {
      if (data.tab?.entity) {
        if (tabId === "new") {
          navigate(`/tabs/`, { replace: true });
          showToast({ displayType: "success", title: "Tab created", content: data.tab.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Tab updated", content: data.tab.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        priority: 0,
      }),
      []
    ),
    originalValues: tabQuery.data?.tab,
    isLoading: tabQuery.loading || createOrUpdateTabMutation.loading,
    onSubmit(data) {
      createOrUpdateTabMutate({
        variables: { id: tabId !== "new" ? tabId : undefined, ...data },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTabMutation.data?.tab?.fieldValidators),
  });
  return (
    <ContainerFluid>
      {tabId !== "new" && <MainTitle title="Attributes" />}
      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} description="Name used to render tab in search frontend" />
        <TextArea label="Description" {...form.inputProps("description")} />
        <NumberInput
          label="Priority"
          {...form.inputProps("priority")}
          description="Define priority according to which tabs are
        orderder by search frontend during rendering"
        />
        <div className="sheet-footer">
          <CustomButtom nameButton={tabId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </ClayForm>
    </ContainerFluid>
  );
}
