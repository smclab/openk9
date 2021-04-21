/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
