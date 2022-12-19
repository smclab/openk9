import { gql } from "@apollo/client";
import { Link, NavLink, useParams } from "react-router-dom";
import {
  useAddTokenFilterToAnalyzerMutation,
  useAnalyzerTokenFiltersQuery,
  useRemoveTokenFilterToAnalyzerMutation,
} from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query AnalyzerTokenFilters($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    analyzer(id: $parentId) {
      id
      tokenFilters(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
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
  mutation AddTokenFilterToAnalyzer($childId: ID!, $parentId: ID!) {
    addTokenFilterToAnalyzer(tokenFilterId: $childId, id: $parentId) {
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
  mutation RemoveTokenFilterToAnalyzer($childId: ID!, $parentId: ID!) {
    removeTokenFilterFromAnalyzer(tokenFilterId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function AnalyzerTokenFilters() {
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
                  <span className="navbar-text-truncate">{"Attributes"}</span>
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to={`/analyzers/${analyzerId}/char-filters`}>
                  <span className="navbar-text-truncate">{"Char Filters"}</span>
                </Link>
              </li>
              <li className="nav-item">
                <NavLink className={({ isActive }) => "nav-link " + `${isActive ? "active" : ""}`} to={""} end={true}>
                  <span className="navbar-text-truncate ">{"Token Filters"}</span>
                </NavLink>
              </li>
            </ul>
          </div>
        </div>
      )}
      <AssociatedEntities
        label="Associate Token Filter"
        parentId={analyzerId}
        list={{
          useListQuery: useAnalyzerTokenFiltersQuery,
          field: (data) => data?.analyzer?.tokenFilters,
        }}
        useAddMutation={useAddTokenFilterToAnalyzerMutation}
        useRemoveMutation={useRemoveTokenFilterToAnalyzerMutation}
      />
    </>
  );
}
