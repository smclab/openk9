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
  query QueryAnalysesRules($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    queryAnalysis(id: $parentId) {
      id
      rules(searchText: $searchText, notEqual: $unassociated, first: 20, after: $cursor) {
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
  }
`;

gql`
  mutation AddRulesToQueryAnalyses($childId: ID!, $parentId: ID!) {
    addRuleToQueryAnalysis(ruleId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation RemoveRuleFromQueryAnalyses($childId: ID!, $parentId: ID!) {
    removeRuleFromQueryAnalysis(ruleId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

