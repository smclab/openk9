import { gql } from "@apollo/client";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import DomainIcon from "@mui/icons-material/Domain";
import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import IconButton from "@mui/material/IconButton";
import TextField from "@mui/material/TextField";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
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
              value={new Date(tenant?.createDate).toLocaleDateString()}
              InputProps={{ readOnly: true }}
              variant="outlined"
              fullWidth
            />
            <TextField
              label="Modify Date"
              value={new Date(tenant?.modifiedDate).toLocaleDateString()}
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
