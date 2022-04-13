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
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import clsx from "clsx";
import Link from "next/link";
import { ThemeType } from "./theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    backgroundColor: "white",
    width: "100%",
    height: theme.spacingUnit * 6,
    display: "flex",
    alignItems: "center",
    flexShrink: 0,
  },
  path: {
    marginLeft: theme.spacingUnit * 3,
    flexGrow: 1,
    fontSize: 16,
    display: "flex",
    alignItems: "center",
  },
  link: {
    color: theme.digitalLakePrimary,
  },
  accent: {
    fontWeight: 600,
    color: theme.digitalLakePrimary,
    textTransform: "uppercase",
  },
  separator: {
    margin: theme.spacingUnit,
  },
}));

export function Breadcrumbs({
  path,
  children,
}: React.PropsWithChildren<{
  path: { label: string; path?: string }[];
}>) {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <div className={classes.path}>
        {path.map((e, i, arr) => (
          <React.Fragment key={`${e.label}-${e.path}-${i}`}>
            {i < arr.length - 1 && e.path ? (
              <Link passHref href={e.path}>
                <a className={clsx(classes.link, i === 0 && classes.accent)}>
                  {e.label}
                </a>
              </Link>
            ) : (
              <div className={clsx(i === 0 && classes.accent)}>{e.label}</div>
            )}
            {i < arr.length - 1 && (
              <ClayIcon className={classes.separator} symbol="angle-right" />
            )}
          </React.Fragment>
        ))}
      </div>
      {children}
    </div>
  );
}
