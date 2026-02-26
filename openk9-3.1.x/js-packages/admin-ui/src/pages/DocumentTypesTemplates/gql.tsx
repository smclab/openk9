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
  query DocumentTypeTemplates($searchText: String, $after: String) {
    docTypeTemplates(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

gql`
  mutation DeleteDocumentTypeTemplate($id: ID!) {
    deleteDocTypeTemplate(docTypeTemplateId: $id) {
      id
      name
    }
  }
`;

export const DocumentTypesQuery = gql`
  query DocumentTypes($searchText: String, $cursor: String) {
    docTypes(searchText: $searchText, first: 20, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

gql`
  query DocumentTypeTemplate($id: ID!) {
    docTypeTemplate(id: $id) {
      id
      name
      description
      templateType
      source
      compiled
    }
  }
`;

gql`
  mutation CreateOrUpdateDocumentTypeTemplate(
    $id: ID
    $name: String!
    $description: String
    $templateType: TemplateType!
    $source: String!
    $compiled: String!
  ) {
    docTypeTemplate(
      id: $id
      docTypeTemplateDTO: {
        name: $name
        description: $description
        templateType: $templateType
        source: $source
        compiled: $compiled
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

