import React from "react";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import clsx from "clsx";
import { ThemeType } from "@openk9/search-ui-components";
import Link from "next/link";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    backgroundColor: "white",
    width: "100%",
    height: theme.spacingUnit * 6,
    display: "flex",
    alignItems: "center",
    flexShrink: 0,
  },
  path: {
    marginLeft: theme.spacingUnit * 3,
    flexGrow: 1,
    fontSize: 16,
    display: "flex",
    alignItems: "center",
  },
  link: {
    color: theme.digitalLakePrimary,
  },
  accent: {
    fontWeight: 600,
    color: theme.digitalLakePrimary,
    textTransform: "uppercase",
  },
  separator: {
    margin: theme.spacingUnit,
  },
}));

export function Breadcrumbs({
  path,
  children,
}: React.PropsWithChildren<{
  path: { label: string; path?: string }[];
}>) {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <div className={classes.path}>
        {path.map((e, i, arr) => (
          <React.Fragment key={`${e.label}-${e.path}-${i}`}>
            {i < arr.length - 1 && e.path ? (
              <Link passHref href={e.path}>
                <a className={clsx(classes.link, i === 0 && classes.accent)}>
                  {e.label}
                </a>
              </Link>
            ) : (
              <div className={clsx(i === 0 && classes.accent)}>{e.label}</div>
            )}
            {i < arr.length - 1 && (
              <ClayIcon className={classes.separator} symbol="angle-right" />
            )}
          </React.Fragment>
        ))}
      </div>
      {children}
    </div>
  );
}
