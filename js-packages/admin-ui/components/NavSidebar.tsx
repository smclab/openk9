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

import { createUseStyles } from "react-jss";
import clsx from "clsx";
import ClayIcon from "@clayui/icon";
import Link from "next/link";
import { useRouter } from "next/router";
import useSWR from "swr";
import { client } from "./client";
import { ThemeType } from "./theme";
import { firstOrString } from "./utils";
import { SettingsIcon } from "./icons/SettingsIcon";
import { DataSourceIcon } from "./icons/DataSourceIcon";
import { HomeIcon } from "./icons/HomeIcon";
import { TenantsIcon } from "./icons/TenantsIcon";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: 0,
    transition: "width 0.5s",
    overflow: "hidden",
    zIndex: 999,
    flexShrink: 0,
  },
  open: {
    width: theme.adminSidebarWidth,
  },
  inner: {
    width: theme.adminSidebarWidth,
  },
  sectionTitle: {
    textTransform: "uppercase",
    color: theme.digitalLakeMainL3,
    fontWeight: "bold",
    fontSize: 14,
    marginTop: theme.spacingUnit * 2,
    marginLeft: theme.spacingUnit * 2,
    marginBottom: theme.spacingUnit * 0.5,
  },
  menuEntry: {
    height: theme.spacingUnit * 4,
    padding: theme.spacingUnit * 2,
    display: "flex",
    alignItems: "center",
    fontWeight: 300,
    textDecoration: "none",
    color: "inherit",
    "&:hover": {
      textDecoration: "none",
      color: "inherit",
      backgroundColor: theme.digitalLakeBackground,
    },
    "& svg": {
      width: 14,
      height: 14,
      fill: theme.digitalLakeMain,
      marginRight: theme.spacingUnit * 2,
    },
  },
  enabledEntry: {
    color: "white",
    backgroundColor: theme.digitalLakePrimary,
    fontWeight: 600,
    "&:hover": {
      color: "white",
      backgroundColor: theme.digitalLakePrimaryL2,
    },
    "& svg": {
      fill: "white",
    },
  },
  subEnabledEntry: {
    color: "white",
    backgroundColor: theme.digitalLakePrimary,
    opacity: 0.75,
    fontWeight: 600,
    "&:hover": {
      color: "white",
      backgroundColor: theme.digitalLakePrimaryL2,
    },
    "& svg": {
      fill: "white",
    },
  },
}));

function MenuEntry({
  icon,
  text,
  path,
  route,
  noSub,
}: {
  icon: JSX.Element;
  text: string;
  path: string;
  route?: string;
  noSub?: boolean;
}) {
  const classes = useStyles();
  const { route: currentRoute } = useRouter();
  const enabled = currentRoute === route;
  const subEnabled =
    route && currentRoute.startsWith(route) && !enabled && !noSub;

  return (
    <Link href={path} passHref>
      <a
        className={clsx(
          classes.menuEntry,
          enabled && classes.enabledEntry,
          subEnabled && classes.subEnabledEntry,
        )}
      >
        {icon}
        {text}
      </a>
    </Link>
  );
}

function TenantSubMenu({ tenantId }: { tenantId: number }) {
  const classes = useStyles();

  const { data: tenant } = useSWR(`/api/v2/tenant/${tenantId}`, () =>
    client.getTenant(tenantId),
  );

  if (!tenant) {
    return null;
  }

  return (
    <div className={classes.inner}>
      <div className={classes.sectionTitle}>{tenant.name}</div>
      <MenuEntry
        text="Tenant Settings"
        path={`/tenants/${tenantId}/settings/`}
        route={`/tenants/[tenantId]/settings`}
        icon={<SettingsIcon />}
      />
      {/* <MenuEntry
        text="Users"
        path={`/tenants/${tenantId}/users/`}
        route={`/tenants/[tenantId]/users`}
        icon={<TenantsIcon />}
      /> */}
      <MenuEntry
        text="Data Sources"
        path={`/tenants/${tenantId}/dataSources/`}
        route={`/tenants/[tenantId]/dataSources`}
        icon={<DataSourceIcon />}
      />
      <MenuEntry
        text="Events"
        path={`/tenants/${tenantId}/events/`}
        route={`/tenants/[tenantId]/events`}
        icon={<SettingsIcon />}
      />
    </div>
  );
}

export function NavSidebar({ visible }: { visible: boolean }) {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  return (
    <div className={clsx(classes.root, visible && classes.open)}>
      <div className={classes.inner}>
        <div className={classes.sectionTitle}>Global</div>
        <MenuEntry
          text="Dashboard"
          path="/"
          route="/"
          noSub
          icon={<HomeIcon />}
        />
        <MenuEntry
          text="Tenants"
          path="/tenants"
          route="/tenants"
          icon={<TenantsIcon />}
        />
        <MenuEntry
          text="Plugins"
          path="/plugins"
          route="/plugins"
          icon={<ClayIcon symbol="plug" />}
        />
        {/* <MenuEntry
          text="Metrics"
          path="/metrics"
          route="/metrics"
          icon={<ClayIcon symbol="analytics" />}
        />
        <MenuEntry
          text="Global Settings"
          path="/settings"
          route="/settings"
          icon={<SettingsIcon />}
        /> */}
        {/* 
        Momentaneamente disabilitato finchè il back-end non lo rimplementa 
        <MenuEntry
          text="Logs"
          path="/logs"
          route="/logs"
          icon={<ClayIcon symbol="forms" />}
        /> */}
      </div>
      {tenantId && <TenantSubMenu tenantId={parseInt(tenantId)} />}
    </div>
  );
}
