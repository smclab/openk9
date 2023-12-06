import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm, { ClayToggle } from "@clayui/form";
import ClayButton from "@clayui/button";
import ClayLoadingIndicator from "@clayui/loading-indicator";
import { useForm, fromFieldValidators, TextInput, StyleToggle, ClassNameButton } from "./Form";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { useCreateOrUpdateTenantMutation, useTenantQuery } from "../graphql-generated";
import { useRestClient } from "./queryClient";
import "./Spinner.css";
gql`
  mutation CreateOrUpdateTenant(
    $id: ID
    $virtualHost: String!
    $schemaName: String!
    $liquibaseSchemaName: String!
    $clientId: String!
    $realmName: String!
  ) {
    tenant(
      id: $id
      tenantDTO: {
        virtualHost: $virtualHost
        schemaName: $schemaName
        liquibaseSchemaName: $liquibaseSchemaName
        clientId: $clientId
        realmName: $realmName
      }
    ) {
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function TenantCreate() {
  const navigate = useNavigate();
  const { tenantId = "new" } = useParams();
  const restClient = useRestClient();
  const showToast = useToast();
  const [loading, setLoading] = React.useState(false);
  const [ability, setAbility] = React.useState(false);
  const tenantQuery = useTenantQuery({
    variables: { id: tenantId as string },
    skip: !tenantId || tenantId === "new",
  });
  const [createOrUpdateTenantMutate, createOrUpdtennatMutation] = useCreateOrUpdateTenantMutation({
    refetchQueries: [],
    onCompleted(data) {
      if (data.tenant) {
        navigate(`/tenants/`, { replace: true });
        showToast({ displayType: "success", title: "Tenant created", content: "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Tenant error", content: error.message ?? "failed to create Tenant" });
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        virtualHost: "",
        schemaName: "",
        liquibaseSchemaName: "",
        clientId: "",
        realmName: "",
      }),
      []
    ),
    originalValues: tenantQuery.data?.tenant,
    isLoading: createOrUpdtennatMutation.loading,
    getValidationMessages: fromFieldValidators(createOrUpdtennatMutation?.data?.tenant?.fieldValidators),
    onSubmit(data) {
      createOrUpdateTenantMutate({ variables: { id: undefined, ...data } });
    },
  });

  return (
    <ClayLayout.ContainerFluid view>
      {loading && (
        <div
          style={{
            position: "absolute",
            top: "0",
            left: "0",
            width: "100%",
            height: "100%",
            background: "rgb(0 0 0 / 0.3)",
            zIndex: "2",
          }}
        >
          <div style={{ position: "absolute", top: "50%", left: "55%" }}>
            <div className="spinner">
              <div></div>
              <div></div>
              <div></div>
              <div></div>
              <div></div>
              <div></div>
            </div>
            <div style={{ marginTop: "25px" }}>
              <div className="spinner-word">
                <span>L</span>
                <span>O</span>
                <span>A</span>
                <span>D</span>
                <span>I</span>
                <span>N</span>
                <span>G</span>
                <span>.</span>
                <span>.</span>
                <span>.</span>
              </div>
            </div>
          </div>
        </div>
      )}

      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          if (ability) {
            form.submit();
          }
        }}
      >
        <TextInput label="VirtualHost" {...form.inputProps("virtualHost")} />
        <div className="form-group" style={{ paddingTop: "18px" }}>
          <ClayToggle
            label={"Configure all fields"}
            toggled={ability}
            onToggle={() => {
              setAbility(!ability);
            }}
          />
          <style type="text/css">{StyleToggle}</style>
        </div>
        <TextInput label="Real Name" ability={ability} {...form.inputProps("realmName")} />
        <TextInput label="Schema Name" ability={ability} {...form.inputProps("schemaName")} />
        <TextInput label="Liquid Base Schema Name" ability={ability} {...form.inputProps("liquibaseSchemaName")} />
        <TextInput label="Client Id" ability={ability} {...form.inputProps("clientId")} />
        <div className="sheet-footer">
          <ClayButton
            className={ClassNameButton}
            type="submit"
            disabled={!form.canSubmit}
            onClick={
              !ability
                ? async () => {
                    setLoading(true);
                    try {
                      const stato = await restClient.tenantManagerResource.postApiTenantManagerTenantManagerTenant({
                        virtualHost: form.inputProps("virtualHost").value,
                      });
                      if (stato) {
                        showToast({ displayType: "success", title: "Tenant created", content: "" });
                        setLoading(false);
                        navigate(`/tenants/`, { replace: true });
                      }
                    } catch (error) {
                      setLoading(false);
                      showToast({ displayType: "danger", title: "Tenant not created", content: "" });
                    }
                  }
                : () => {}
            }
          >
            {tenantId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
