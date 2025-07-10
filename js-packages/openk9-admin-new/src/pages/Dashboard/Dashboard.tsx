import { gql } from "@apollo/client";
import { Box, Card, CardContent, CardHeader, Container, Typography } from "@mui/material";
import { useDataIndexInformationQuery } from "../../graphql-generated";
import { DetailGraph } from "./Graph";

export const DataIndexInformation = gql`
  query dataIndexInformation {
    buckets {
      edges {
        node {
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
    }
  }
`;

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

export function bytesToMegabytes(bytes: number) {
  const megabytes = bytes / (1024 * 1024);
  return parseFloat(megabytes.toFixed(4));
}

export function DashBoard() {
  const dashboardQuery = useDataIndexInformationQuery();

  const recoveryDocsDeleted = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsDeleted))
    .filter((arr) => arr != null && arr.length > 0);

  const documentDeleted = recoveryDocsDeleted
    ?.flat()
    .reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  const docsCount = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsCount))
    .filter((arr) => arr != null && arr.length > 0);

  const docCount = docsCount?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  const recoveryByteCount = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.priStoreSize))
    .filter((arr) => arr != null && arr.length > 0);

  const byteCount = recoveryByteCount?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  return (
    <Container style={{ marginTop: "25px" }}>
      <Box display="flex" padding="0 24px" gap="23px">
        <Presentation user={""} />
      </Box>
      <Box marginY={4}>
        <Typography variant="h5" fontWeight="600" marginLeft="24px">
          Your active bucket
        </Typography>
      </Box>
      <DetailGraph
        dataGraph={data}
        secondDataGraph={dataTwo}
        firstCardNumber={docCount || 0}
        secondCardNumber={documentDeleted || 0}
        thirdCardNumber={bytesToMegabytes(byteCount) || 0}
        firstCardLabel={"Document counts"}
        secondCardLabel={"Document deleted"}
        thirdCardLabel={"Store size megabyte"}
        thirdCardUnity={""}
      />
    </Container>
  );
}

function Presentation({ user }: { user: any }) {
  return (
    <Card
      style={{
        maxHeight: "307px",
        borderRadius: "10px",
        position: "relative",
      }}
    >
      <CardHeader title={`Welcome ${user}`} />
      <CardContent>
        <Typography variant="body2" color="textSecondary" component="p">
          OpenK9 is a complete Cognitive Enterprise Search solution that fits all your needs. Powerful, Modern and
          Flexible, it employs Machine Learning to enrich your data and give the best experience possible.
        </Typography>
      </CardContent>
    </Card>
  );
}
