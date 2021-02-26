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
