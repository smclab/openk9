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
