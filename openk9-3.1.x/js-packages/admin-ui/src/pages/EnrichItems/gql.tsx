/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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

