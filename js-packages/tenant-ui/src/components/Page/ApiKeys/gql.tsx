import { gql } from "@apollo/client";

gql`
  query GetApiKeys($tenantId: String!) {
    apiKeys(tenantId: $tenantId) {
      id
      tenantId
      name
      apiGroup
      status
      prefix
      suffix
      hash
      createDate
      expirationDate
    }
  }
`;

gql`
  query GetApiKey($id: ID!) {
    apiKey(id: $id) {
      id
      tenantId
      name
      apiGroup
      status
      prefix
      suffix
      hash
      createDate
      expirationDate
    }
  }
`;

gql`
  mutation CreateApiKey($createApiKeyRequest: CreateApiKeyRequestInput!) {
    createApiKey(createApiKeyRequest: $createApiKeyRequest) {
      id
      apiKey
    }
  }
`;

gql`
  mutation RevokeApiKey($id: ID!) {
    revokeApiKey(id: $id)
  }
`;
