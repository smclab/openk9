import { gql } from "@apollo/client";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import DomainIcon from "@mui/icons-material/Domain";
import { Box, Button, Container, FormControlLabel, IconButton, Switch, Toolbar, Typography } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateTenantMutation, useTenantQuery } from "../graphql-generated";
import { fromFieldValidators, TextInput, useForm } from "./Form";
import { LoadingOverlay } from "./Loading";
import { useRestClient } from "./queryClient";
import "./Spinner.css";
import { useToast } from "./ToastProvider";
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
    <React.Fragment>
      <Toolbar>
        <IconButton edge="start" color="inherit" aria-label="back" onClick={() => navigate(`/tenants/`, { replace: true })} size="large">
          <ArrowBackIcon />
        </IconButton>
      </Toolbar>
      <Container maxWidth="lg">
        {loading && <LoadingOverlay />}

        <Box sx={{ px: 4, py: 3 }}>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              mb: 6,
              borderBottom: "2px solid",
              borderColor: "primary.main",
              pb: 2,
            }}
          >
            <DomainIcon sx={{ fontSize: 40, mr: 2, color: "primary.main" }} />
            <Typography variant="h4" component="h1" color="primary">
              {tenantId === "new" ? "Create Tenant" : "Edit Tenant"}
            </Typography>
          </Box>

          <Box
            component="form"
            onSubmit={(event: any) => {
              event.preventDefault();
              if (ability) {
                form.submit();
              }
            }}
            sx={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: 4,
              marginBottom: 2,

              "& .MuiTextField-root": {
                "& .MuiOutlinedInput-root": {
                  backgroundColor: "background.default",
                  transition: "all 0.2s",
                  "&:hover:not(.Mui-disabled)": {
                    backgroundColor: "background.paper",
                  },
                  "&.Mui-focused": {
                    backgroundColor: "background.paper",
                  },
                  "&.Mui-disabled": {
                    backgroundColor: "action.disabledBackground",
                    "& .MuiOutlinedInput-input": {
                      color: "text.disabled",
                    },
                  },
                },
              },
            }}
          >
            <TextInput label="Virtual Host" {...form.inputProps("virtualHost")} />
            <FormControlLabel
              control={<Switch checked={ability} onChange={() => setAbility(!ability)} color="primary" />}
              label={<Typography sx={{ fontWeight: 500 }}>Configure all fields</Typography>}
            />
            <TextInput label="Real Name" {...form.inputProps("realmName")} disabled={!ability} />
            <TextInput label="Schema Name" {...form.inputProps("schemaName")} disabled={!ability} />
            <TextInput label="Liquid Base Schema Name" {...form.inputProps("liquibaseSchemaName")} disabled={!ability} />
            <TextInput label="Client Id" {...form.inputProps("clientId")} disabled={!ability} />
          </Box>

          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
            }}
          >
            <Button
              variant="contained"
              color="primary"
              type="submit"
              disabled={!form.canSubmit}
              sx={{
                px: 4,
                py: 1.5,
                borderRadius: 1,
                textTransform: "none",
                fontWeight: 600,
              }}
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
        </Box>
      </Container>
    </React.Fragment>
  );
}
