import React from "react";
import { gql } from "@apollo/client";
import ClayForm from "@clayui/form";
import { useNavigate, useParams } from "react-router-dom";
import { EnrichItemType, useCreateOrUpdateEnrichItemMutation, useEnrichItemQuery } from "../graphql-generated";
import { EnrichItemsQuery } from "./EnrichItems";
import { EnumSelect, fromFieldValidators, TextArea, TextInput, useForm } from "./Form";
import { CodeInput } from "./CodeInput";
import ClayButton from "@clayui/button";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { AssociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQuery } from "./EnrichPipelineEnrichItems";
import { ClassNameButton } from "../App";

const EnrichItemQuery = gql`
  query EnrichItem($id: ID!) {
    enrichItem(id: $id) {
      id
      name
      description
      type
      serviceName
      jsonConfig
      validationScript
    }
  }
`;

gql`
  mutation CreateOrUpdateEnrichItem(
    $id: ID
    $name: String!
    $description: String
    $type: EnrichItemType!
    $serviceName: String!
    $jsonConfig: String
    $validationScript: String
  ) {
    enrichItem(
      id: $id
      enrichItemDTO: {
        name: $name
        description: $description
        type: $type
        serviceName: $serviceName
        jsonConfig: $jsonConfig
        validationScript: $validationScript
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

export function EnrichItem() {
  const { enrichItemId = "new", name } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const enrichItemQuery = useEnrichItemQuery({
    variables: { id: enrichItemId as string },
    skip: !enrichItemId || enrichItemId === "new",
  });
  const [createOrUpdateEnrichItemMutate, createOrUpdateEnrichItemMutation] = useCreateOrUpdateEnrichItemMutation({
    refetchQueries: [
      EnrichItemQuery,
      EnrichItemsQuery,
      AssociatedEnrichPipelineEnrichItemsQuery,
      UnassociatedEnrichPipelineEnrichItemsQuery,
    ],
    onCompleted(data) {
      if (data.enrichItem?.entity) {
        if (enrichItemId === "new") {
          navigate(`/enrich-items/`, { replace: true });
          showToast({ displayType: "success", title: "Enrich items created", content: data.enrichItem.entity.name ?? "" });
        } else {
          navigate(`/enrich-items/`, { replace: true });
          showToast({ displayType: "info", title: "Enrich items updated", content: data.enrichItem.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: name ?? "",
        description: "",
        type: EnrichItemType.Async,
        serviceName: name ?? "",
        jsonConfig: "{}",
        validationScript: "",
      }),
      []
    ),
    originalValues: enrichItemQuery.data?.enrichItem,
    isLoading: enrichItemQuery.loading || createOrUpdateEnrichItemMutation.loading,
    onSubmit(data) {
      createOrUpdateEnrichItemMutate({
        variables: { id: enrichItemId !== "new" ? enrichItemId : undefined, ...data, validationScript: data.validationScript || undefined },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateEnrichItemMutation.data?.enrichItem?.fieldValidators),
  });
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
        <EnumSelect label="Type" dict={EnrichItemType} {...form.inputProps("type")} />
        <TextInput label="Service Name" {...form.inputProps("serviceName")} />
        <CodeInput language="json" label="Configuration" {...form.inputProps("jsonConfig")} />
        <CodeInput language="javascript" label="Validation Script" {...form.inputProps("validationScript")} />
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {enrichItemId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
