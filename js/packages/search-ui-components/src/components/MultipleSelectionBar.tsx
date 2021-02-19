import React from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  selectionBar: {
    padding: [0, theme.spacingUnit],
  },
  selectionBarButton: {
    backgroundColor: "transparent",
    border: "none",
    padding: [theme.spacingUnit, theme.spacingUnit * 2],
    textTransform: "uppercase",
    fontSize: 12,
    transition: "color 0.5s",
    color: theme.digitalLakeMainL3,
    "&:hover": {
      color: theme.digitalLakeMain,
    },
  },
  selected: {
    position: "relative",
    color: theme.digitalLakePrimary,
    transition: "none",
    "&:hover": {
      color: theme.digitalLakePrimary,
    },
    "&:after": {
      backgroundColor: theme.digitalLakePrimary,
      content: '""',
      display: "block",
      height: ".125rem",
      position: "absolute",
      left: 4,
      right: 4,
      bottom: 0,
      width: "auto",
    },
  },
}));

interface ItemProps {
  selected: boolean;
  onClick(): void;
}

export function MultipleSelectionBarItem({
  selected,
  onClick,
  children,
}: React.PropsWithChildren<ItemProps>) {
  const classes = useStyles();

  return (
    <button
      className={clsx(classes.selectionBarButton, selected && classes.selected)}
      onClick={onClick}
    >
      {children}
    </button>
  );
}

interface BarProps {
  className?: string;
}

export function MultipleSelectionBar({
  className,
  children,
}: React.PropsWithChildren<BarProps>) {
  const classes = useStyles();

  return (
    <div className={clsx(className, classes.selectionBar)}>{children}</div>
  );
}
