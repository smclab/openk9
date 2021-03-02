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
import { Dockbar, ThemeType } from "@openk9/search-ui-components";
import { NavSidebar } from "../components/NavSidebar";
import { useStore } from "../state";
import { Breadcrumbs } from "./Breadcrumbs";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    display: "flex",
    flexDirection: "column",
    height: "100%",
    color: theme.digitalLakeMain,
    backgroundColor: theme.digitalLakeBackground,
  },
  content: {
    flexGrow: 1,
    display: "flex",
    flexDirection: "row",
    height: "calc(100% - 48px)",
  },
  contentMain: {
    flexGrow: 1,
    height: "100%",
    display: "flex",
    flexDirection: "column",
    minWidth: 0,
    minHeight: 0,
  },
  children: {
    flexGrow: 1,
    minHeight: 0,
    display: "flex",
    flexDirection: "column",
  },
}));

export function Layout({
  breadcrumbsPath,
  breadcrumbsControls,
  children,
}: React.PropsWithChildren<{
  breadcrumbsPath: { label: string; path?: string }[];
  breadcrumbsControls?: JSX.Element;
}>) {
  const classes = useStyles();
  const sidebarOpen = useStore((s) => s.sidebarOpen);
  const toggleSidebar = useStore((s) => s.toggleSidebar);

  return (
    <div className={classes.root}>
      <Dockbar onHamburgerAction={toggleSidebar} />

      <div className={classes.content}>
        <NavSidebar visible={sidebarOpen} />
        <div className={classes.contentMain}>
          <Breadcrumbs path={breadcrumbsPath} children={breadcrumbsControls} />
          <div className={classes.children}>{children}</div>
        </div>
      </div>
    </div>
  );
}
