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
import { WebResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  break: {
    overflow: "hidden",
    maxWidth: "100%",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  avatar: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 3,
    width: 64,
    height: 64,
    fontSize: 64,
    flexShrink: 0,
    display: "flex",
    justifyContent: "center",
    color: theme.digitalLakeMainL2,
  },
}));

export function WebSidebar({ result }: { result: WebResultItem }) {
  const classes = useStyles();
  return (
    <>
      <h3>
        <div className={classes.avatar}>
          {result.source.web.favicon ? (
            <img src={result.source.web.favicon} />
          ) : (
            <ClayIcon symbol="document" />
          )}
        </div>{" "}
        {result.source.web.title}
      </h3>
      <div className={classes.break}>
        <strong>URL:</strong>{" "}
        <a href={result.source.web.url} target="_blank">
          {result.source.web.url}
        </a>
      </div>
      <div>{result.source.web.content}</div>
    </>
  );
}
