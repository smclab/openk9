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
      {highlight.slice(0, maxRows).map((h) => (
        <div key={h} className={clsx(classes.line, inline && classes.inline)}>
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
