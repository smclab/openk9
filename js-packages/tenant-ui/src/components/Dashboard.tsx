import ClayCard from "@clayui/card";
import ClayIcon from "@clayui/icon";
import ClayLayout from "@clayui/layout";
import ClayList from "@clayui/list";
import { Link } from "react-router-dom";
import React from "react";
import { getUserProfile } from "./authentication";
import { BrandLogo } from "./BrandLogo";

export function bytesToMegabytes(bytes: number): number {
  const megabytes = bytes / (1024 * 1024);
  return parseFloat(megabytes.toFixed(4));
}

export function DashBoard() {
  const [user, setUser] = React.useState();
  getUserProfile().then((data) => {
    setUser(JSON.parse(JSON.stringify(data))?.name);
  });

  return (
    <ClayLayout.ContainerFluid view>
      <div style={{ display: "flex", gap: "23px", marginTop: "25px" }}>
        <Presentation user={user || ""} />
      </div>
    </ClayLayout.ContainerFluid>
  );
}

function Presentation({ user }: { user: string }) {
  return (
    <React.Fragment>
      <Card
        title={`Welcome ${user || "no name"}`}
        description="OpenK9 is a complete Cognitive Enterprise Search solution that fits all your needs. Powerful, Modern and Flexible, it employs Machine Learning to enrich your data and give the best experience possible."
      />
    </React.Fragment>
  );
}

function Card({ title, description }: { title: string; description: string }) {
  return (
    <ClayCard style={{ marginLeft: "10px", maxHeight: "350px", borderRadius: "10px", maxWidth: "400px" }}>
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
