import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import Container from "@mui/material/Container";
import Toolbar from "@mui/material/Toolbar";
import IconButton from "@mui/material/IconButton";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import TextField from "@mui/material/TextField";
import Box from "@mui/material/Box";
import { useTenantQuery } from "../graphql-generated";

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

  const tenant = tenantQuery.data?.tenant;

  return (
    <React.Fragment>
      <Toolbar>
        <IconButton edge="start" color="inherit" aria-label="back" onClick={() => navigate(`/tenants/`, { replace: true })} size="large">
          <ArrowBackIcon />
        </IconButton>
      </Toolbar>
      <Container maxWidth="sm">
        <Box component="form" sx={{ mt: 2, display: "flex", flexDirection: "column", gap: 2 }}>
          <TextField label="Name" value={tenant?.virtualHost || ""} InputProps={{ readOnly: true }} variant="outlined" margin="normal" />
          <TextField label="Real Name" value={tenant?.realmName || ""} InputProps={{ readOnly: true }} variant="outlined" margin="normal" />
          <TextField
            label="Schema Name"
            value={tenant?.schemaName || ""}
            InputProps={{ readOnly: true }}
            variant="outlined"
            margin="normal"
          />
          <TextField
            label="Client Secret"
            value={tenant?.clientSecret || ""}
            InputProps={{ readOnly: true }}
            variant="outlined"
            margin="normal"
          />
          <TextField
            label="Create Date"
            value={tenant?.createDate || ""}
            InputProps={{ readOnly: true }}
            variant="outlined"
            margin="normal"
          />
          <TextField
            label="Modify Date"
            value={tenant?.modifiedDate || ""}
            InputProps={{ readOnly: true }}
            variant="outlined"
            margin="normal"
          />
        </Box>
      </Container>
    </React.Fragment>
  );
}
