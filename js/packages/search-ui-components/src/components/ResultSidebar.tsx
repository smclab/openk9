import React from "react";
import { createUseStyles } from "react-jss";

import { GenericResultItem, SidebarRenderersType } from "@openk9/http-api";
import { ThemeType } from "../theme";
import { arrOrEncapsulate } from "../utils";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    position: "sticky",
    top: 48,
    minWidth: 300,
    width: "100%",
    maxWidth: 500,
    height: "calc(100vh - 48px)",
    overflow: "auto",
    padding: theme.spacingUnit * 2,
    backgroundColor: theme.digitalLakeBackgroundL1,
    flexShrink: 0,
  },
  break: {
    overflow: "hidden",
    maxWidth: "100%",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  previews: {
    margin: [theme.spacingUnit * 2, 0],
  },
}));

function SidebarContentDispatch<E>({
  result,
  renderers,
}: {
  result: GenericResultItem<E>;
  renderers: SidebarRenderersType<E>;
}): JSX.Element | null {
  const Renderer = arrOrEncapsulate(result.source.type as any)
    .map((k) => renderers[k])
    .filter(Boolean)[0];
  if (Renderer) {
    return <Renderer result={result} />;
  } else {
    console.warn("No sidebar renderer for", result.source.type);
    return null;
  }
}

export function ResultSidebar<E>({
  result,
  renderers,
}: {
  result: GenericResultItem<E> | null;
  renderers: SidebarRenderersType<E>;
}) {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      {result && (
        <SidebarContentDispatch result={result} renderers={renderers} />
      )}
    </div>
  );
}
