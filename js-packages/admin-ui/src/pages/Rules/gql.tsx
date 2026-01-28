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
  query Rules($searchText: String, $after: String) {
    rules(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          lhs
          rhs
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
  mutation DeleteRules($id: ID!) {
    deleteRule(ruleId: $id) {
      id
      name
    }
  }
`;

gql`
  query Rule($id: ID!) {
    rule: rule(id: $id) {
      id
      name
      description
      lhs
      rhs
    }
  }
`;

gql`
  mutation CreateOrUpdateRuleQuery($id: ID, $name: String!, $description: String, $lhs: String!, $rhs: String!) {
    rule(id: $id, ruleDTO: { name: $name, description: $description, lhs: $lhs, rhs: $rhs }) {
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

