import { gql } from "@apollo/client";

gql`
  query Schedulers {
    schedulers(searchText: "FAILURE") {
      edges {
        node {
          scheduleId
          datasource {
            id
            name
          }
          status
        }
      }
    }
  }
`;

gql`
  query schedulersError {
    schedulers(searchText: "ERROR") {
      edges {
        node {
          scheduleId
          createDate
          datasource {
            id
            name
          }
          status
        }
      }
    }
  }
`;

gql`
  query Scheduler($id: ID!) {
    scheduler(id: $id) {
      scheduleId
      createDate
      modifiedDate
      lastIngestionDate
      status
      errorDescription
    }
  }
`;
