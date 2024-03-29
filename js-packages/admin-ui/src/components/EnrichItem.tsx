import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import {
  BehaviorMergeType,
  BehaviorOnError,
  EnrichItemType,
  useCreateOrUpdateEnrichItemMutation,
  useEnrichItemQuery,
} from "../graphql-generated";
import { EnrichItemsQuery } from "./EnrichItems";
import { ContainerFluid, CustomButtom, EnumSelect, fromFieldValidators, NumberInput, TextArea, TextInput, useForm } from "./Form";
import { CodeInput } from "./CodeInput";
import { useToast } from "./ToastProvider";
import { AssociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQuery } from "./EnrichPipelineEnrichItems";

const EnrichItemQuery = gql`
  query EnrichItem($id: ID!) {
    enrichItem(id: $id) {
      id
      name
      description
      type
      serviceName
      jsonConfig
      script
      behaviorMergeType
      jsonPath
      behaviorOnError
      requestTimeout
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
    $script: String
    $behaviorMergeType: BehaviorMergeType!
    $jsonPath: String!
    $behaviorOnError: BehaviorOnError!
    $requestTimeout: BigInteger!
  ) {
    enrichItem(
      id: $id
      enrichItemDTO: {
        name: $name
        description: $description
        type: $type
        serviceName: $serviceName
        jsonConfig: $jsonConfig
        script: $script
        behaviorMergeType: $behaviorMergeType
        jsonPath: $jsonPath
        behaviorOnError: $behaviorOnError
        requestTimeout: $requestTimeout
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
        type: EnrichItemType.HttpAsync,
        serviceName: name ?? "",
        jsonConfig: "{}",
        script: "",
        behaviorMergeType: BehaviorMergeType.Merge,
        jsonPath: "",
        requestTimeout: 0,
        behaviorOnError: BehaviorOnError.Skip,
      }),
      []
    ),
    originalValues: enrichItemQuery.data?.enrichItem,
    isLoading: enrichItemQuery.loading || createOrUpdateEnrichItemMutation.loading,
    onSubmit(data) {
      createOrUpdateEnrichItemMutate({
        variables: { id: enrichItemId !== "new" ? enrichItemId : undefined, ...data },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateEnrichItemMutation.data?.enrichItem?.fieldValidators),
  });
  return (
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
        <NumberInput
          label="Request Timeout"
          {...form.inputProps("requestTimeout")}
          description={"the value is expressed in milliseconds"}
        />
        <TextInput label="Service Name" {...form.inputProps("serviceName")} description={"Url where enrich service listen"} />
        <TextInput label="Json Path" {...form.inputProps("jsonPath")} />
        <EnumSelect label="Type" dict={EnrichItemType} {...form.inputProps("type")} />
        <EnumSelect label="Behavior Merge Type" dict={BehaviorMergeType} {...form.inputProps("behaviorMergeType")} />
        <EnumSelect label="Behavior On Error" dict={BehaviorOnError} {...form.inputProps("behaviorOnError")} />
        <CodeInput language="json" label="Configuration" {...form.inputProps("jsonConfig")} />
        <CodeInput language="javascript" label="Script" {...form.inputProps("script")} />
        <div className="sheet-footer">
          <CustomButtom nameButton={enrichItemId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
