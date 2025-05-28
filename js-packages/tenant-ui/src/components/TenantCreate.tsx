import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { Box, Button, CircularProgress, Container, FormControlLabel, Switch, Paper, Typography } from "@mui/material";
import { useForm, fromFieldValidators, TextInput } from "./Form";
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
      showToast({ displayType: "error", title: "Tenant error", content: error.message ?? "failed to create Tenant" });
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
    <Container maxWidth="sm" sx={{ mt: 4, position: "relative" }}>
      {loading && (
        <Box
          sx={{
            position: "absolute",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            bgcolor: "rgba(0,0,0,0.3)",
            zIndex: 2,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <CircularProgress />
          <Typography sx={{ mt: 3, letterSpacing: 2 }}>LOADING...</Typography>
        </Box>
      )}

      <Paper elevation={3} sx={{ p: 4 }}>
        <form
          onSubmit={(event: any) => {
            event.preventDefault();
            if (ability) {
              form.submit();
            }
          }}
        >
          <TextInput label="VirtualHost" {...form.inputProps("virtualHost")} />
          <Box sx={{ pt: 2 }}>
            <FormControlLabel
              control={<Switch checked={ability} onChange={() => setAbility(!ability)} color="primary" />}
              label="Configure all fields"
            />
          </Box>
          <TextInput label="Real Name" {...form.inputProps("realmName")} />
          <TextInput label="Schema Name" {...form.inputProps("schemaName")} />
          <TextInput label="Liquid Base Schema Name" {...form.inputProps("liquibaseSchemaName")} />
          <TextInput label="Client Id" {...form.inputProps("clientId")} />
          <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 3 }}>
            <Button
              variant="contained"
              color="primary"
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
                        showToast({ displayType: "error", title: "Tenant not created", content: "" });
                      }
                    }
                  : () => {}
              }
            >
              {tenantId === "new" ? "Create" : "Update"}
            </Button>
          </Box>
        </form>
      </Paper>
    </Container>
  );
}
