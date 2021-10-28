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

import clsx from "clsx";
import React from "react";
import { createUseStyles } from "react-jss";
import { ThemeType } from "../theme";
import { ellipseText } from "../utils/ellipseText";

const useStyles = createUseStyles((theme: ThemeType) => ({
  line: {
    whiteSpace: "nowrap",
    overflow: "hidden",
    maxWidth: "100%",
    textOverflow: "ellipsis",
  },
  highlight: {
    color: theme.digitalLakePrimaryD1,
  },
  inline: {
    display: "inline",
  },
}));

/**
 * It performs highlighting on text with the format incoming from ElasticSearch. It also perform text ellipsis!
 * @example
 * <Highlight
 *   text={data.source.user.lastName}
 *   highlight={data.highlight["user.lastName"]}
 * />
 */
export function Highlight({
  text,
  highlight,
  maxRows = 4,
  inline,
}: {
  text: string;
  highlight?: string[];
  maxRows?: number;
  inline?: boolean;
}) {
  const classes = useStyles();

  if (!highlight || highlight.length === 0) {
    return (
      <div className={clsx(classes.line, inline && classes.inline)}>
        {ellipseText(text, 250)}
      </div>
    );
  }

  return (
    <>
      {highlight.slice(0, maxRows).map((h, index) => (
        <div
          key={index}
          className={clsx(classes.line, inline && classes.inline)}
        >
          {h
            .split("<em>")
            .flatMap((s) => s.split("</em>"))
            .map((s, i) =>
              i % 2 === 1 ? (
                <strong className={classes.highlight} key={s + "-" + i}>
                  {s}
                </strong>
              ) : (
                <React.Fragment key={s + "-" + i}>{s}</React.Fragment>
              ),
            )}
        </div>
      ))}
    </>
  );
}
