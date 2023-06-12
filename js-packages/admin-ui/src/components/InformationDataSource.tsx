import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useDataSourceInformationQuery } from "../graphql-generated";
import { DetailGraph } from "./Graph";

const DataSourceInformation = gql`
  query DataSourceInformation($id: ID!) {
    datasource(id: $id) {
      dataIndex {
        cat {
          docsCount
          docsDeleted
          health
          index
          pri
          priStoreSize
          rep
          status
          storeSize
          uuid
        }
      }
    }
  }
`;

export function InformationDataSource() {
  const { datasourceId } = useParams();
  const dataSourceInformationQuery = useDataSourceInformationQuery({
    variables: { id: datasourceId as string },
  });
  const docsCount = dataSourceInformationQuery.data?.datasource?.dataIndex?.cat?.docsCount;
  const docsDeleted = dataSourceInformationQuery.data?.datasource?.dataIndex?.cat?.docsDeleted;
  const docsStoreSize = dataSourceInformationQuery.data?.datasource?.dataIndex?.cat?.priStoreSize;
  return (
    <DetailGraph
      dataGraph={data}
      secondDataGraph={dataTwo}
      firstCardNumber={parseFloat(docsCount || "0") || 0}
      secondCardNumber={parseFloat(docsDeleted || "0") || 0}
      thirdCardNumber={docsStoreSize || 0}
      firstCardLabel={"Document counts"}
      secondCardLabel={"Document deleted"}
      thirdCardLabel={"Store size"}
      thirdCardUnity={"byte"}
    />
  );
}

const data = [
  { name: "2/8", query: 100 },
  { name: "2/9", query: 350 },
  { name: "2/10", query: 500 },
  { name: "2/11", query: 250 },
  { name: "2/12", query: 500 },
  { name: "2/13", query: 300 },
  { name: "2/14", query: 200 },
];

const dataTwo = [
  { name: "10/3", query: 100 },
  { name: "10/7", query: 350 },
  { name: "10/10", query: 1500 },
  { name: "10/11", query: 2050 },
  { name: "10/12", query: 1500 },
  { name: "10/13", query: 800 },
  { name: "10/24", query: 500 },
];
