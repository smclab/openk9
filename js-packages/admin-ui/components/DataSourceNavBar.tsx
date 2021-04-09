import React from "react";
import clsx from "clsx";
import Link from "next/link";
import { createUseStyles } from "react-jss";
import ClayNavigationBar from "@clayui/navigation-bar";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { ThemeType } from "@openk9/search-ui-components";

const useStyles = createUseStyles((theme: ThemeType) => ({
  navMenu: {
    backgroundColor: "transparent",
  },
  navActionButton: {
    marginLeft: theme.spacingUnit,
  },
}));

export function DataSourceNavBar({
  onReindex,
  tenantId,
  datasourceId,
}: {
  onReindex(): void;
  tenantId: number;
  datasourceId: number;
}) {
  const classes = useStyles();

  return (
    <ClayNavigationBar triggerLabel="Configuration" className={classes.navMenu}>
      <ClayNavigationBar.Item active>
        <a className="nav-link">Configuration</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <a className="nav-link">Data Browser</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <Link
          href={`/tenants/${tenantId}/dataSources/${datasourceId}/enrich`}
          passHref
        >
          <a className="nav-link">Enrich</a>
        </Link>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <a className="nav-link">Schedule</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <a className="nav-link">ACL</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <ClayTooltipProvider>
          <div>
            <a
              className={clsx("btn btn-primary", classes.navActionButton)}
              data-tooltip-align="bottom"
              title="Reindex Data Source"
              onClick={onReindex}
            >
              <ClayIcon symbol="reload" />
            </a>
          </div>
        </ClayTooltipProvider>
      </ClayNavigationBar.Item>
    </ClayNavigationBar>
  );
}
