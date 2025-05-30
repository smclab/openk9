import { gql } from "@apollo/client";
import { Box, Button, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import { useTenantsQuery } from "../../graphql-generated";
import { Table } from "../Table";

gql`
  query Tenants($searchText: String, $cursor: String) {
    tenants(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          virtualHost
          createDate
          modifiedDate
        }
      }
    }
  }
`;

export function Tenants() {
  const bucketsQuery = useTenantsQuery();
  const navigate = useNavigate();

  return (
    <Box padding={2}>
      <Box display="flex" justifyContent="space-between" alignItems="flex-end" mb={2}>
        <Box sx={{ width: "50%" }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Tenant area
          </Typography>
          <Typography variant="body1">
            In information technology, a tenant refers to a logically separate and isolated instance within a shared computing environment,
            typically in cloud computing or multi-tenant architectures. Each tenant operates independently and may represent an individual
            user, a customer organization, or a business unit, with its own data, configurations, and user management.
          </Typography>
        </Box>
        <Box>
          <Link to="tenant-create" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary" aria-label="create new analyzer">
              Create New Tenants
            </Button>
          </Link>
        </Box>
      </Box>
      <Table
        data={{
          queryResult: bucketsQuery,
          field: (data) => data?.tenants,
        }}
        onCreatePath="/buckets/new"
        columns={[
          { header: "Virtual Host", content: (tenant) => tenant?.virtualHost },
          { header: "Create Date", content: (tenant) => tenant?.createDate },
          { header: "Modify Date", content: (tenant) => tenant?.modifiedDate },
        ]}
        onDelete={() => {}}
        rowActions={[
          {
            label: "View",
            action: (tenant) => {
              if (tenant?.id) navigate(`${tenant.id}`);
            },
          },
        ]}
      />
    </Box>
  );
}
