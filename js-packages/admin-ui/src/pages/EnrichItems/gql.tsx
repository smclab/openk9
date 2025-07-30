import { gql } from "@apollo/client";

gql`
  query UnboundEnrichPipelines($itemId: BigInteger!) {
    unboundEnrichPipelines(itemId: $itemId) {
      name
      id
    }
  }
`;

gql`
  mutation DeleteEnrichItem($id: ID!) {
    deleteEnrichItem(enrichItemId: $id) {
      id
      name
    }
  }
`;

gql`
  query EnrichItem($id: ID!) {
    enrichItem(id: $id) {
      id
      name
      description
      type
      serviceName
      jsonConfig
      script
      behaviorMergeType
      jsonPath
      behaviorOnError
      requestTimeout
    }
  }
`;

gql`
  mutation CreateOrUpdateEnrichItem(
    $id: ID
    $name: String!
    $description: String
    $type: EnrichItemType!
    $serviceName: String!
    $jsonConfig: String
    $script: String
    $behaviorMergeType: BehaviorMergeType!
    $jsonPath: String!
    $behaviorOnError: BehaviorOnError!
    $requestTimeout: BigInteger!
  ) {
    enrichItem(
      id: $id
      enrichItemDTO: {
        name: $name
        description: $description
        type: $type
        serviceName: $serviceName
        jsonConfig: $jsonConfig
        script: $script
        behaviorMergeType: $behaviorMergeType
        jsonPath: $jsonPath
        behaviorOnError: $behaviorOnError
        requestTimeout: $requestTimeout
      }
    ) {
      entity {
        id
        name
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;
