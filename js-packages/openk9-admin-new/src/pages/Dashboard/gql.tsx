import { gql } from "@apollo/client";

export const scheduler = gql`
  query SchedulersFaiulure($searchText: String) {
    schedulers(searchText: $searchText) {
      edges {
        node {
          id
          modifiedDate
          errorDescription
          lastIngestionDate
          status
          datasource {
            id
            name
          }
          newDataIndex {
            id
            name
          }
        }
      }
    }
  }
`;
