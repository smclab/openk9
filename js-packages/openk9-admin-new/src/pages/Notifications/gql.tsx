import { gql } from "@apollo/client";

export const FailureQuery = gql`
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
export const ErrorQuery = gql`
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

export const SchedulerQuery = gql`
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
