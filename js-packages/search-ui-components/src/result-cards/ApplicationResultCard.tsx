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

import { ApplicationResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";
import { Highlight, ResultCard } from "../components";

const useStyles = createUseStyles((theme: ThemeType) => ({
  card: {
    alignItems: "center",
    "&:focus, &:hover h4": {
      textDecoration: "underline",
    },
  },
  centering: {
    display: "flex",
    width: 64,
    flexShrink: 0,
    justifyContent: "center",
    marginRight: theme.spacingUnit * 2,
    margin: theme.spacingUnit,
  },
  iconArea: {
    display: "flex",
    backgroundColor: theme.digitalLakePrimary,
    borderRadius: theme.borderRadius,
    fontSize: 24,
  },
}));

export function ApplicationResultCard({
  data,
  className,
  ...rest
}: {
  data: ApplicationResultItem;
  onSelect?: () => void;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>): JSX.Element {
  const classes = useStyles();

  return (
    <ResultCard
      href={data.source.application.URL}
      target="_blank"
      className={classes.card}
      {...rest}
    >
      <div className={classes.centering}>
        <div className={classes.iconArea}>
          <img height={32} src={data.source.application.icon} />
        </div>
      </div>
      <div>
        <h4>
          <Highlight
            text={data.source.application.title}
            highlight={data.highlight["application.title"]}
          />
        </h4>
        <Highlight
          text={data.source.application.description}
          highlight={data.highlight["application.description"]}
        />
      </div>
    </ResultCard>
  );
}
