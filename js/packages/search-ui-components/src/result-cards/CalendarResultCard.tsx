import React from "react";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import { format } from "date-fns";

import { CalendarResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";
import { ResultCard, Highlight } from "../components";

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
