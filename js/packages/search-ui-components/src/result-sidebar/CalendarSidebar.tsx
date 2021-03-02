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
import { CalendarResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  row: {
    display: "flex",
    alignItems: "center",
  },
  avatar: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 3,
    width: 32,
    height: 32,
    fontSize: 32,
    flexShrink: 0,
    display: "flex",
    justifyContent: "center",
    color: theme.digitalLakeMainL2,
  },
}));

export function CalendarSidebar({ result }: { result: CalendarResultItem }) {
  const classes = useStyles();
  return (
    <>
      <h3 className={classes.row}>
        <div className={classes.avatar}>
          <ClayIcon symbol="calendar" />
        </div>{" "}
        <div>{result.source.calendar.titleCurrentValue}</div>
      </h3>
      <div>
        <strong>Start</strong>:{" "}
        {format(
          new Date(Number(result.source.calendar.startTime)),
          "dd MMMM yyyy, HH:mm",
        )}
      </div>
      <div>
        <strong>End</strong>:{" "}
        {format(
          new Date(Number(result.source.calendar.endTime)),
          "dd MMMM yyyy, HH:mm",
        )}
      </div>
      <div>
        <strong>Description</strong>: {result.source.calendar.description}
      </div>
      <div>
        <strong>Location</strong>: {result.source.calendar.location}
      </div>
    </>
  );
}
