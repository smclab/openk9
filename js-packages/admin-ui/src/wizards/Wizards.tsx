import React, { ReactNode } from "react";
import ClayCard from "@clayui/card";
import ClayToolbar from "@clayui/toolbar";
import ClayLayout from "@clayui/layout";
import { ClayButtonWithIcon } from "@clayui/button";
import ClayIcon from "@clayui/icon";
import { Link } from "react-router-dom";
import ClayEmptyState from "@clayui/empty-state";
import { GitLabLogo } from "./Logo/GitLabLogo";
import { GitHubLogo } from "./Logo/GitHubLogo";
import { GoogleDriveLogo } from "./Logo/GoogleDriveLogo";
import { LiferayLogo } from "./Logo/LiferayLogo";
import { DropBoxLogo } from "./Logo/DropBoxLogo";
import { ClassNameButton } from "../App";

const wizards = [
  {
    title: "Web Crawler",
    description: "This is a test",
    path: "web-crawler",
    icon: <ClayIcon symbol={"globe"} />,
  },
  {
    title: "Sitemap",
    description: "This is a test",
    path: "sitemap",
    icon: <ClayIcon symbol={"organizations"} />,
  },
  {
    title: "Database",
    description: "This is a test",
    path: "database",
    icon: <ClayIcon symbol={"archive"} />,
  },
  {
    title: "Server email",
    description: "This is a test",
    path: "server-email",
    icon: <ClayIcon symbol={"envelope-open"} />,
  },
  {
    title: "GitHub",
    description: "This is a test",
    path: "github",
    icon: <GitHubLogo height={100} weigth={250} />,
  },
  {
    title: "GitLab",
    description: "This is a test",
    path: "gitlab",
    icon: <GitLabLogo height={150} weigth={100} />,
  },
  {
    title: "Liferay",
    description: "This is a test",
    path: "liferay",
    icon: <LiferayLogo height={200} weigth={100} />,
  },
  {
    title: "Google Drive",
    description: "This is a test",
    path: "google-drive",
    icon: <GoogleDriveLogo height={150} weigth={100} />,
  },
  {
    title: "DropBox",
    description: "This is a test",
    path: "dropbox",
    icon: <DropBoxLogo height={150} weigth={100} />,
  },
];

export function Wizards() {
  const [searchText, setSearchText] = React.useState("");

  const fileterdWizards = wizards.filter((element) => {
    return (
      element.title.toLowerCase().includes(searchText.toLowerCase()) || element.description.toLowerCase().includes(searchText.toLowerCase())
    );
  });

  return (
    <React.Fragment>
      <ClayToolbar light>
        <ClayLayout.ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand>
              <ClayToolbar.Input
                placeholder="Search..."
                sizing="sm"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </ClayToolbar.Item>
            <ClayToolbar.Item>
              <ClayButtonWithIcon aria-label="" className={ClassNameButton} symbol="plus" small />
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ClayLayout.ContainerFluid>
      </ClayToolbar>
      <ClayLayout.ContainerFluid view>
        <div className="row">
          {fileterdWizards.map((wizard) => (
            <div key={wizard.path} className="col-sm-6 col-md-4 col-lg-3">
              <WizardCard title={wizard.title} description={wizard.description} path={wizard.path} icon={wizard.icon} />
            </div>
          ))}
        </div>
        {fileterdWizards.length === 0 && (
          <ClayEmptyState
            description="Try with other search term"
            title="Did not found any connectors"
            className="c-empty-state-animation"
          />
        )}
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}

type WizardCardProps = { title: string; description: string; path: string; icon: ReactNode };

function WizardCard({ title, description, path, icon }: WizardCardProps) {
  return (
    <ClayCard>
      <ClayCard.AspectRatio className="card-item-first">
        <div className="aspect-ratio-item aspect-ratio-item-center-middle aspect-ratio-item-fluid card-type-asset-icon">{icon}</div>
      </ClayCard.AspectRatio>
      <ClayCard.Body>
        <ClayCard.Description displayType="title">
          <Link to={path}>{title}</Link>
        </ClayCard.Description>
        <ClayCard.Description truncate={false} displayType="text">
          {description}
        </ClayCard.Description>
      </ClayCard.Body>
    </ClayCard>
  );
}
