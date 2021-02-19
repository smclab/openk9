import React from "react";
import { createUseStyles } from "react-jss";
import { ApplicationResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  row: {
    display: "flex",
    alignItems: "center",
  },
  iconArea: {
    display: "flex",
    backgroundColor: theme.digitalLakePrimary,
    borderRadius: theme.borderRadius,
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 2,
    fontSize: "24px",
  },
}));

export function ApplicationSidebar({
  result,
}: {
  result: ApplicationResultItem;
}) {
  const classes = useStyles();
  return (
    <>
      <h3 className={classes.row}>
        <div className={classes.iconArea}>
          <img height={32} src={result.source.application.icon} />
        </div>{" "}
        {result.source.application.title}
      </h3>
      {result.source.application.description}
    </>
  );
}
