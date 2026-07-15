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
  query Highlights {
    highlights {
      id
      name
      description
      type
    }
  }
`;

gql`
  query Highlight($id: ID!) {
    highlight(id: $id) {
      id
      name
      description
      type
      boundaryScanner
      boundaryChars
      fragmenter
      fragmentSize
      numberOfFragments
      order
      fields {
        id
        name
      }
    }
  }
`;

gql`
  query DocTypeFieldsByOffsetSource($offsetSource: OffsetSourceType) {
    docTypeFieldsByOffsetSource(offsetSource: $offsetSource) {
      id
      name
    }
  }
`;

gql`
  mutation CreateOrUpdateHighlight(
    $id: ID
    $name: String!
    $description: String
    $type: HighlightType
    $fieldIds: [BigInteger]
    $boundaryScanner: BoundaryScannerType
    $boundaryChars: String
    $fragmenter: FragmenterType
    $fragmentSize: Int
    $numberOfFragments: Int
    $order: OrderType
    $patch: Boolean
  ) {
    highlight(
      id: $id
      patch: $patch
      highlightDTO: {
        name: $name
        description: $description
        type: $type
        fieldIds: $fieldIds
        boundaryScanner: $boundaryScanner
        boundaryChars: $boundaryChars
        fragmenter: $fragmenter
        fragmentSize: $fragmentSize
        numberOfFragments: $numberOfFragments
        order: $order
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

gql`
  mutation DeleteHighlight($id: ID!) {
    deleteHighlight(id: $id) {
      id
      name
    }
  }
`;
