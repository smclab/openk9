import ClayIcon from "@clayui/icon";
import { Link } from "react-router-dom";
import React from "react";
import { gql } from "@apollo/client";
import { useDataIndexInformationQuery } from "../graphql-generated";
import { DashBoardTable } from "./Table";
import { DetailGraph } from "./Graph";
import { ContainerFluid } from "./Form";

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
    <ContainerFluid>
      <div className="card">
        <div className="card-body">
          <div style={{ display: "flex", gap: "23px", marginTop: "25px" }}>
            <Presentation />
            <WizardList />
          </div>
        </div>
      </div>
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
      <div className="card">
        <div className="card-body">
          <DashBoardTable />
        </div>
      </div>
    </ContainerFluid>
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
    <div className="card">
      <div className="card-body">
        <h5
          className="card-title"
          style={{ margin: "16px", fontSize: "1.5rem", fontWeight: "bold", marginTop: "1rem", marginLeft: "1rem" }}
        >
          {title}
        </h5>
        <h6
          className="card-subtitle"
          style={{ margin: "16px", fontSize: "1.5rem", fontWeight: "bold", marginTop: "1rem", marginLeft: "1rem" }}
        >
          {subTitle}
        </h6>
        <div className="card-text" style={{ margin: "16px" }}>
          {description}
        </div>
      </div>
    </div>
  );
}

function WizardList() {
  return (
    <div className="list-group col-md-6 show-quick-actions-on-hover">
      <li className="list-group-header">
        <p className="list-group-header-title">
          <Link to="wizards">Connect your stuff</Link>
        </p>
      </li>

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
    </div>
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
    <li className="list-group-item list-group-item-flex">
      <div className="autofit-col">
        <div className="sticker sticker-secondary">{icon}</div>
      </div>
      <div className="autofit-col autofit-col-expand">
        <p className="list-group-title">
          <Link to={to}>{title}</Link>
        </p>
        <p className="list-group-text">{description}</p>
      </div>
    </li>
  );
}
