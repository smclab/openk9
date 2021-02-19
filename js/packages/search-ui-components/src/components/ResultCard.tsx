import React, { useRef } from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";

import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    display: "flex",
    marginBottom: theme.spacingUnit * 1,
    marginLeft: theme.spacingUnit * -1,
    marginRight: theme.spacingUnit * -1,
    padding: [theme.spacingUnit * 2, theme.spacingUnit * 1],
    cursor: "pointer",
    borderRadius: theme.borderRadius,
    position: "relative",
    zIndex: 1,
    backgroundColor: "white",
    color: theme.digitalLakeMain,
    textDecoration: "none",
    "&:hover": {
      color: theme.digitalLakeMain,
      textDecoration: "none",
      backgroundColor: `${theme.digitalLakePrimaryL3}33`,
      boxShadow: `0 0 0 2px ${theme.digitalLakePrimary}44`,
    },
    "&:focus": {
      color: theme.digitalLakeMain,
      textDecoration: "none",
      backgroundColor: `${theme.digitalLakePrimaryL3}AA`,
      boxShadow: `0 0 0 2px ${theme.digitalLakePrimary}AA`,
    },
    // "&:hover::before, &:focus::before": {
    //   zIndex: -1,
    //   content: `""`,
    //   position: "absolute",
    //   top: 0,
    //   bottom: 0,
    //   left: 0,
    //   right: 0,
    //   boxShadow: theme.baseBoxShadow,
    // },
  },
}));

export function ResultCard({
  className,
  onSelect,
  ...rest
}: React.AnchorHTMLAttributes<HTMLAnchorElement> & {
  onSelect?: () => void;
}): JSX.Element {
  const classes = useStyles();
  const ref = useRef<HTMLAnchorElement | null>(null);

  function handleSelect() {
    onSelect && onSelect();
  }

  return (
    <a
      ref={ref}
      onMouseOver={handleSelect}
      onFocus={handleSelect}
      href="#"
      className={clsx(className, classes.root, "resultLink")}
      {...rest}
    />
  );
}
