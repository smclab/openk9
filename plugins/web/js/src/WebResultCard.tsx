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
import ClayIcon from "@clayui/icon";
import { createUseStyles } from "react-jss";
import { Highlight, ResultCard, ThemeType } from "@openk9/search-ui-components";

import { WebResultItem } from "./types";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    "&:focus, &:hover h4 :first-child": {
      textDecoration: "underline",
    },
  },
  iconArea: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 2,
    fontSize: 24,
  },
  title: {
    display: "flex",
    alignItems: "center",
    marginBottom: 0,
  },
  path: {
    fontSize: 12,
    marginBottom: "0.3rem",
  },
  textArea: {
    fontSize: 14,
  },
  badge: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: 11,
    fontWeight: 600,
    textTransform: "uppercase",
    color: "white",
    backgroundColor: theme.digitalLakeMainL3,
    borderRadius: theme.borderRadius,
    padding: [2, theme.spacingUnit],
    marginLeft: theme.spacingUnit,
  },
}));

export function WebResultCard({
  data,
  className,
  ...rest
}: {
  data: WebResultItem;
  onSelect?: () => void;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>): JSX.Element {
  const classes = useStyles();

  return (
    <ResultCard
      href={data.source.web.url}
      target="_blank"
      className={classes.root}
      {...rest}
    >
      <div className={classes.iconArea}>
        {data.source.web.favicon ? (
          <img width={32} src={data.source.web.favicon} />
        ) : (
          <ClayIcon symbol="document" />
        )}
      </div>
      <div style={{ minWidth: 0 }}>
        <h4 className={classes.title}>
          <Highlight
            text={data.source.web.title}
            highlight={data.highlight["web.title"]}
          />
        </h4>
        <div className={classes.path}>{data.source.web.url}</div>
        <div className={classes.textArea}>
          <Highlight
            text={data.source.web.content}
            highlight={data.highlight["web.content"]}
          />
        </div>
      </div>
    </ResultCard>
  );
}
