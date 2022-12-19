import { gql } from "@apollo/client";
import { Link, NavLink, useParams } from "react-router-dom";
import {
  useAddCharFiltersToAnalyzerMutation,
  useAnalyzerCharFiltersQuery,
  useRemoveCharFiltersToAnalyzerMutation,
} from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query AnalyzerCharFilters($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    analyzer(id: $parentId) {
      id
      charFilters(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
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
  }
`;

gql`
  mutation AddCharFiltersToAnalyzer($childId: ID!, $parentId: ID!) {
    addCharFilterToAnalyzer(charFilterId: $childId, id: $parentId) {
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
  mutation RemoveCharFiltersToAnalyzer($childId: ID!, $parentId: ID!) {
    removeCharFilterFromAnalyzer(charFilterId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function AnalyzerCharFilters() {
  const { analyzerId } = useParams();
  if (!analyzerId) return null;
  return (
    <>
      {analyzerId !== "new" && (
        <div className="navbar navbar-underline navigation-bar navigation-bar-secondary navbar-expand-md" style={{ position: "sticky" }}>
          <div className="container-fluid container-fluid-max-xl">
            <ul className="navbar-nav ">
              <li className="nav-item">
                <Link className="nav-link" to={`/analyzers/${analyzerId}/`}>
                  <span className="navbar-text-truncate active">{"Attributes"}</span>
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link active" to={`/analyzers/${analyzerId}/char-filters`}>
                  <span className="navbar-text-truncate ">{"Char Filters"}</span>
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to={`/analyzers/${analyzerId}/token-filters`}>
                  <span className="navbar-text-truncate ">{"Token Filters"}</span>
                </Link>
              </li>
            </ul>
          </div>
        </div>
      )}
      <AssociatedEntities
        label="Associate Char Filter"
        parentId={analyzerId}
        list={{
          useListQuery: useAnalyzerCharFiltersQuery,
          field: (data) => data?.analyzer?.charFilters,
        }}
        useAddMutation={useAddCharFiltersToAnalyzerMutation}
        useRemoveMutation={useRemoveCharFiltersToAnalyzerMutation}
      />
    </>
  );
}
