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
import { format } from "date-fns";
import { ResultCard, Highlight, ThemeType } from "@openk9/search-ui-components";

import { CalendarResultItem } from "./types";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    "&:focus, &:hover h4": {
      textDecoration: "underline",
    },
  },
  iconArea: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 2,
    flexShrink: 0,
    display: "flex",
    justifyContent: "center",
    fontSize: 24,
  },
  nameLine: {
    marginBottom: 0,
  },
}));

export function CalendarResultCard({
  data,
  className,
  ...rest
}: {
  data: CalendarResultItem;
  onSelect?: () => void;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>): JSX.Element {
  const classes = useStyles();

  const start = new Date(parseInt(data.source.calendar.startTime));
  const end = new Date(parseInt(data.source.calendar.endTime));

  return (
    <ResultCard className={classes.root} {...rest}>
      <div className={classes.iconArea}>
        <ClayIcon symbol="calendar" />
      </div>
      <div style={{ minWidth: 0 }}>
        <h4 className={classes.nameLine}>
          <Highlight
            text={data.source.calendar.titleCurrentValue}
            highlight={data.highlight["calendar.titleCurrentValue"]}
            inline
          />
        </h4>
        <h6>
          {format(start, "dd MMMM yyyy, HH:mm")} —{" "}
          {format(end, "dd MMMM yyyy, HH:mm")}
        </h6>
      </div>
    </ResultCard>
  );
}
