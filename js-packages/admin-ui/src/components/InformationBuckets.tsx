import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useDataIndexSingleInformationQuery } from "../graphql-generated";
import { DetailGraph } from "./Graph";

const DataIndexInformation = gql`
  query dataIndexSingleInformation($id: ID!) {
    bucket(id: $id) {
      datasources {
        edges {
          node {
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
      }
    }
  }
`;

export function InformationBuckets() {
  const { bucketId } = useParams();
  const bucketInformationQuery = useDataIndexSingleInformationQuery({
    variables: { id: bucketId as string },
  });
  const recoveryDocsDeleted = bucketInformationQuery.data?.bucket?.datasources?.edges
    ?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsDeleted)
    .filter((arr) => arr != null && arr.length > 0);
  const countDeleted = recoveryDocsDeleted?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);
  const recoveryDocsCount = bucketInformationQuery.data?.bucket?.datasources?.edges
    ?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsCount)
    .filter((arr) => arr != null && arr.length > 0);
  const countDocsCount = recoveryDocsCount?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);
  const recoveryStoreSize = bucketInformationQuery.data?.bucket?.datasources?.edges
    ?.map((datasource) => datasource?.node?.dataIndex?.cat?.priStoreSize)
    .filter((arr) => arr != null && arr.length > 0);
  const countByteStoresize = recoveryStoreSize?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  return (
    <DetailGraph
      dataGraph={data}
      secondDataGraph={dataTwo}
      firstCardNumber={countDocsCount || 0}
      secondCardNumber={countDeleted || 0}
      thirdCardNumber={countByteStoresize || 0}
      firstCardLabel={"document counts"}
      secondCardLabel={"document deleted"}
      thirdCardLabel={"store size"}
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
