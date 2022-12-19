import ClayIcon from "@clayui/icon";
import ClayLayout from "@clayui/layout";
import ClayList from "@clayui/list";
import { Link } from "react-router-dom";

export function DashBoard() {
  return (
    <ClayLayout.ContainerFluid view>
      <ClayLayout.Row>
        <div className="col-md-6"></div>
        <WizardList />
      </ClayLayout.Row>
    </ClayLayout.ContainerFluid>
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
