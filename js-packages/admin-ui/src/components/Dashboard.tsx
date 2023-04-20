import ClayCard from "@clayui/card";
import ClayIcon from "@clayui/icon";
import ClayLayout from "@clayui/layout";
import ClayList from "@clayui/list";
import { Link } from "react-router-dom";
import React from "react";
import { gql } from "@apollo/client";
import { useDataIndexInformationQuery } from "../graphql-generated";
import ClayCardBody from "@clayui/card/lib/Body";
import { DashBoardTable } from "./Table";
import { DetailGraph } from "./Graph";

const DataIndexInformation = gql`
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
export function DashBoard() {
  const dashboardQuery = useDataIndexInformationQuery();

  const recoveryDocsDeleted = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsDeleted))
    .filter((arr) => arr != null && arr.length > 0);

  const countSingleIndex = recoveryDocsDeleted?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  const docsCount = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsCount))
    .filter((arr) => arr != null && arr.length > 0);

  const docCount = docsCount?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  const recoveryByteCount = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.priStoreSize))
    .filter((arr) => arr != null && arr.length > 0);

  const byteCount = recoveryByteCount?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  return (
    <ClayLayout.ContainerFluid view>
      <ClayCard>
        <ClayCard.Body>
          <div style={{ display: "flex", gap: "23px", marginTop: "25px" }}>
            <Presentation />
            <WizardList />
          </div>
        </ClayCard.Body>
      </ClayCard>
      <DetailGraph
        dataGraph={data}
        secondDataGraph={dataTwo}
        firstCardNumber={countSingleIndex || 0}
        secondCardNumber={docCount || 0}
        thirdCardNumber={byteCount || 0}
        firstCardLabel={"document counts"}
        secondCardLabel={"document deleted"}
        thirdCardLabel={"store size"}
        thirdCardUnity={"byte"}
      />
      <ClayCard>
        <ClayCardBody>
          <DashBoardTable />
        </ClayCardBody>
      </ClayCard>
    </ClayLayout.ContainerFluid>
  );
}

function Presentation() {
  return (
    <React.Fragment>
      <Card
        title="Welcome To Openk9"
        subTitle="QuickStart guide"
        description="When working on a Machine learning project flexibility and reusability are very important to make your life easier while developing the solution. Find the best way to structure your project files can be difficult when you are a beginner or when the project becomes big. Sometime you may end up duplicate or rewrite some part of your project which is not professional as a Data Scientist or Machine learning Engineer."
      />
    </React.Fragment>
  );
}

function Card({ title, subTitle, description }: { title: string; description: string; subTitle: string }) {
  return (
    <ClayCard style={{ marginLeft: "10px", maxHeight: "307px" }}>
      <ClayCard.Body>
        <ClayCard.Description
          displayType="title"
          style={{ margin: "16px", fontSize: "1.5rem", fontWeight: "bold", marginTop: "1rem", marginLeft: "1rem" }}
        >
          {title}
        </ClayCard.Description>
        <ClayCard.Description displayType="subtitle" style={{ margin: "16px", fontSize: "20px", lineHeight: "28px", fontWeight: "600" }}>
          {subTitle}
        </ClayCard.Description>
        <ClayCard.Description truncate={false} displayType="text" style={{ margin: "16px" }}>
          {description}
        </ClayCard.Description>
      </ClayCard.Body>
    </ClayCard>
  );
}

function WizardList() {
  return (
    <ClayList className="col-md-6">
      <ClayList.Header>
        <Link to="wizards">Connect your stuff</Link>
      </ClayList.Header>
      <WizardListItem
        icon={<ClayIcon symbol={"globe"} />}
        to="wizards/web-crawler"
        title="Web Crawler"
        description="A web crawler that indexes a web-site"
      />
      <WizardListItem icon={<ClayIcon symbol={"archive"} />} to="wizards/database" title="Database" description="Index a database query" />
      <WizardListItem
        icon={<ClayIcon symbol={"organizations"} />}
        to="wizards/sitemap"
        title="Site Map"
        description="Index a site-map xml file"
      />
      <WizardListItem
        icon={<ClayIcon symbol={"envelope-open"} />}
        to="wizards/server-email"
        title="Email Server"
        description="Index emails"
      />
    </ClayList>
  );
}

type WizardListItemProps = {
  icon: React.ReactNode;
  to: string;
  title: string;
  description: string;
};
function WizardListItem({ icon, to, title, description }: WizardListItemProps) {
  return (
    <ClayList.Item flex>
      <ClayList.ItemField>
        <div className="sticker sticker-secondary">{icon}</div>
      </ClayList.ItemField>
      <ClayList.ItemField expand>
        <ClayList.ItemTitle>
          <Link to={to}>{title}</Link>
        </ClayList.ItemTitle>
        <ClayList.ItemText>{description}</ClayList.ItemText>
      </ClayList.ItemField>
    </ClayList.Item>
  );
}
