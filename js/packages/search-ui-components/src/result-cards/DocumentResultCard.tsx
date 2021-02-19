import React from "react";
import ClayIcon from "@clayui/icon";
import { createUseStyles } from "react-jss";

import { DocumentResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";
import { Highlight, ResultCard } from "../components";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    "&:focus, &:hover h4 :first-child": {
      textDecoration: "underline",
    },
  },
  iconArea: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 2,
    fontSize: 24,
  },
  title: {
    display: "flex",
    alignItems: "center",
    marginBottom: 0,
  },
  path: {
    fontSize: 12,
    marginBottom: "0.3rem",
  },
  textArea: {
    fontSize: 14,
  },
  badge: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: 11,
    fontWeight: 600,
    textTransform: "uppercase",
    color: "white",
    backgroundColor: theme.digitalLakeMainL3,
    borderRadius: theme.borderRadius,
    padding: [2, theme.spacingUnit],
    marginLeft: theme.spacingUnit,
  },
}));

export function DocumentResultCard({
  data,
  className,
  ...rest
}: {
  data: DocumentResultItem;
  onSelect?: () => void;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>): JSX.Element {
  const classes = useStyles();

  return (
    <ResultCard
      href={data.source.document.URL}
      target="_blank"
      className={classes.root}
      {...rest}
    >
      <div className={classes.iconArea}>
        {data.source.document.previewUrl ? (
          <img width={64} src={data.source.document.previewUrl} />
        ) : (
          <ClayIcon symbol="document-text" />
        )}
      </div>
      <div style={{ minWidth: 0 }}>
        <h4 className={classes.title}>
          <Highlight
            text={data.source.document.title}
            highlight={data.highlight["document.title"]}
          />{" "}
          {data.source.document.documentType && (
            <div className={classes.badge}>
              {data.source.document.documentType}
            </div>
          )}
        </h4>
        <div className={classes.path}>
          <strong>
            {data.source.spaces?.spaceName || "Documents and Media"}
          </strong>
          {data.source.file.path}
        </div>
        <div className={classes.textArea}>
          <Highlight
            text={data.source.document.content}
            highlight={data.highlight["document.content"]}
          />
        </div>
      </div>
    </ResultCard>
  );
}
