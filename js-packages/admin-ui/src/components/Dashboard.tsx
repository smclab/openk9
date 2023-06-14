import ClayCard from "@clayui/card";
import ClayIcon from "@clayui/icon";
import ClayLayout from "@clayui/layout";
import ClayList from "@clayui/list";
import { Link } from "react-router-dom";
import React from "react";
import { gql } from "@apollo/client";
import { useDataIndexInformationQuery } from "../graphql-generated";
import { DetailGraph } from "./Graph";
import { getUserProfile } from "./authentication";
import { BrandLogo } from "./BrandLogo";

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

export function bytesToMegabytes(bytes: number): number {
  const megabytes = bytes / (1024 * 1024);
  return parseFloat(megabytes.toFixed(4));
}

export function DashBoard() {
  const dashboardQuery = useDataIndexInformationQuery();
  const [user, setUser] = React.useState();
  getUserProfile().then((data) => {
    setUser(JSON.parse(JSON.stringify(data))?.name);
  });

  const recoveryDocsDeleted = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsDeleted))
    .filter((arr) => arr != null && arr.length > 0);

  const documentDeleted = recoveryDocsDeleted?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

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
      <div style={{ display: "flex", gap: "23px", marginTop: "25px" }}>
        <Presentation user={user || ""} />
        <WizardList />
      </div>
      <DetailGraph
        dataGraph={data}
        secondDataGraph={dataTwo}
        firstCardNumber={docCount || 0}
        secondCardNumber={documentDeleted || 0}
        thirdCardNumber={bytesToMegabytes(byteCount) || 0}
        firstCardLabel={"Document counts"}
        secondCardLabel={"Document deleted"}
        thirdCardLabel={"Store size"}
        thirdCardUnity={"Megabyte"}
      />
    </ClayLayout.ContainerFluid>
  );
}

function Presentation({ user }: { user: string }) {
  return (
    <React.Fragment>
      <Card
        title={`Welcome ${user}`}
        description="When working on a Machine learning project flexibility and reusability are very important to make your life easier while developing the solution. Find the best way to structure your project files can be difficult when you are a beginner or when the project becomes big. Sometime you may end up duplicate or rewrite some part of your project which is not professional as a Data Scientist or Machine learning Engineer."
      />
    </React.Fragment>
  );
}

function Card({ title, description }: { title: string; description: string }) {
  return (
    <ClayCard style={{ marginLeft: "10px", maxHeight: "307px", borderRadius: "10px" }}>
      <ClayCard.Body>
        <div style={{ position: "absolute", right: "0", bottom: "0" }}>
          <BrandLogo colorFill={"#bd61612e"} width={270} height={220} viewBox="0 0 75 73" />
        </div>
        <ClayCard.Description
          displayType="title"
          style={{ margin: "16px", fontSize: "1.5rem", fontWeight: "bold", marginTop: "1rem", marginLeft: "1rem" }}
        >
          {title}
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
    <ClayList className="col-md-6" style={{ maxWidth: "420px", paddingTop: "0px" }}>
      <ClayList.Header style={{ backgroundColor: "white", paddingTop: "18px", borderRadius: "10px" }}>
        <Link to="wizards" style={{ color: "inherit", textDecoration: "inherit", cursor: "pointer" }}>
          Connect your stuff
        </Link>
      </ClayList.Header>
      <WizardListItem
        icon={<ClayIcon symbol={"globe"} />}
        to="wizards/web-crawler"
        title="Web Crawler"
        description="A web crawler that indexes a web-site"
        firstElement={true}
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
        lastElement={true}
      />
    </ClayList>
  );
}

type WizardListItemProps = {
  icon: React.ReactNode;
  to: string;
  title: string;
  description: string;
  firstElement?: boolean;
  lastElement?: boolean;
};
function WizardListItem({ icon, to, title, description, firstElement, lastElement }: WizardListItemProps) {
  return (
    <ClayList.Item
      flex
      style={{
        borderTop: firstElement ? "none" : "",
        marginTop: firstElement ? "-8px" : "",
        borderBottomLeftRadius: lastElement ? "10px" : "",
        borderBottomRightRadius: lastElement ? "10px" : "",
      }}
    >
      <ClayList.ItemField>
        <div className="sticker sticker-secondary">
          <div
            style={{
              backgroundColor: "var(--openk9-embeddable-dashboard--secondary-color)",
              padding: "5px",
              display: "flex",
              borderRadius: "100px",
              color: "#9C0E10",
            }}
          >
            {icon}
          </div>
        </div>
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
