import { gql } from "@apollo/client";

gql`
  query Preconfigurations {
    preconfigurations {
      name
      configs {
        apiGroup
        authScheme
      }
    }
  }
`;
