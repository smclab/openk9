import { gql } from "@apollo/client";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import DomainIcon from "@mui/icons-material/Domain";
import { Button, MenuItem } from "@mui/material";
import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import IconButton from "@mui/material/IconButton";
import TextField from "@mui/material/TextField";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTenantQuery } from "../graphql-generated";
import { ModalConfirm } from "./ModalConfirm";
import { useRestClient } from "./queryClient";
import { Preset } from "../openapi-generated";
import { useToast } from "./ToastProvider";

export const TenantQuery = gql`
  query Tenant($id: ID!) {
    tenant(id: $id) {
      id
      realmName
      schemaName
      modifiedDate
      virtualHost
      clientSecret
      createDate
    }
  }
`;

export function Tenant() {
  const { tenantId = "new" } = useParams();
  const navigate = useNavigate();
  const tenantQuery = useTenantQuery({
    variables: { id: tenantId as string },
    skip: !tenantId || tenantId === "new",
  });
  const [viewModal, setViewModal] = React.useState(false);
  const [viewModalInit, setViewModalInit] = React.useState(false);
  const [selectedConnector, setSelectedConnector] = React.useState<Preset | null>(null);
  const tenant = tenantQuery.data?.tenant;

  const restClient = useRestClient();
  const showToast = useToast();

  const formatDate = (date: string | undefined) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString();
  };

  return (
    <React.Fragment>
      {viewModal && (
        <ModalConfirm
          actionConfirm={async () => {
            try {
              if (selectedConnector) {
                await restClient.provisioningResource.postApiTenantManagerProvisioningConnector({
                  tenantName: tenant?.schemaName || "",
                  preset: selectedConnector,
                });
                showToast({
                  displayType: "success",
                  title: "Success",
                  content: `Connector ${selectedConnector} installed successfully`,
                });
              }
              setViewModal(false);
            } catch (error) {
              showToast({
                displayType: "error",
                title: "Error",
                content: "Failed to install connector",
              });
            }
          }}
          close={() => {
            setViewModal(false);
            setSelectedConnector(null);
          }}
          labelConfirm="Confirm"
          title="Select Preset"
          fullWidth
          type="error"
        >
          <Box sx={{ minWidth: 120, my: 2 }}>
            <TextField
              select
              fullWidth
              label="Select Connector"
              value={selectedConnector}
              onChange={(e) => setSelectedConnector(e.target.value as Preset)}
              variant="outlined"
            >
              {Object.values(Preset).map((preset) => (
                <MenuItem key={preset} value={preset}>
                  {preset}
                </MenuItem>
              ))}
            </TextField>
          </Box>
        </ModalConfirm>
      )}
      {viewModalInit && (
        <ModalConfirm
          actionConfirm={async () => {
            try {
              if (tenant?.schemaName) {
                await restClient.provisioningResource.postApiTenantManagerProvisioningInitTenant({
                  tenantName: tenant.schemaName,
                });
                showToast({
                  displayType: "success",
                  title: "Success",
                  content: "Tenant initialized successfully",
                });
              }
              setViewModalInit(false);
            } catch (error) {
              showToast({
                displayType: "error",
                title: "Error",
                content: "Failed to initialize tenant",
              });
            }
          }}
          close={() => {
            setViewModalInit(false);
          }}
          labelConfirm="Confirm"
          title="Confirm init"
          fullWidth
          type="error"
        >
          <Box>
            <Typography variant="body1" sx={{ mb: 2 }}>
              Are you sure you want to initialize this tenant ?
            </Typography>
          </Box>
        </ModalConfirm>
      )}
      <Toolbar sx={{ display: "flex", justifyContent: "space-between" }}>
        <IconButton edge="start" color="inherit" aria-label="back" onClick={() => navigate(`/tenants/`, { replace: true })} size="large">
          <ArrowBackIcon />
        </IconButton>
        <Box display={"flex"} gap={2}>
          <Button
            color="primary"
            aria-label="init connector"
            size="medium"
            variant="contained"
            onClick={() => {
              setViewModalInit(true);
            }}
          >
            Init
          </Button>
          <Button
            color="primary"
            aria-label="install connector"
            size="medium"
            variant="contained"
            onClick={() => {
              setViewModal(true);
            }}
          >
            Install Connector
          </Button>
        </Box>
      </Toolbar>
      <Container maxWidth="lg">
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
              Tenant Details
            </Typography>
          </Box>
          <Box
            component="form"
            sx={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: 4,
              "& .MuiTextField-root": {
                "& .MuiOutlinedInput-root": {
                  backgroundColor: "background.default",
                  transition: "all 0.2s",
                  "&:hover": {
                    backgroundColor: "background.paper",
                  },
                  "&.Mui-focused": {
                    backgroundColor: "background.paper",
                  },
                },
              },
            }}
          >
            <TextField label="Name" value={tenant?.virtualHost || ""} InputProps={{ readOnly: true }} variant="outlined" fullWidth />
            <TextField label="Real Name" value={tenant?.realmName || ""} InputProps={{ readOnly: true }} variant="outlined" fullWidth />
            <TextField label="Schema Name" value={tenant?.schemaName || ""} InputProps={{ readOnly: true }} variant="outlined" fullWidth />
            <TextField
              label="Client Secret"
              value={tenant?.clientSecret || ""}
              InputProps={{ readOnly: true }}
              variant="outlined"
              fullWidth
            />
            <TextField
              label="Create Date"
              value={formatDate(tenant?.createDate)}
              InputProps={{ readOnly: true }}
              variant="outlined"
              fullWidth
            />
            <TextField
              label="Modify Date"
              value={formatDate(tenant?.modifiedDate)}
              InputProps={{ readOnly: true }}
              variant="outlined"
              fullWidth
            />
          </Box>
        </Box>
      </Container>
    </React.Fragment>
  );
}
