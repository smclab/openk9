import { gql } from "@apollo/client";

gql`
  query Preconfigurations {
    getPreconfigurations {
      name
      configs {
        apiGroup
        authScheme
      }
    }
  }
`;

gql`
  mutation CreateTenant($tenantRequestDTO: TenantRequestDTOInput!) {
    tenant(tenantRequestDTO: $tenantRequestDTO) {
      entity {
        id
        tenantName
        virtualHost
        securityConfiguration
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;
