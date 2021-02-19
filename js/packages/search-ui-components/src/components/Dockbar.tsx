import React from "react";
import { createUseStyles } from "react-jss";
import { HamburgerIcon } from "../icons/HamburgerIcon";
import { BrandLogo } from "../icons/BrandLogo";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    backgroundColor: theme.digitalLakeMainL2,
    minHeight: 48,
    display: "flex",
    alignItems: "center",
    position: "sticky",
    top: 0,
    zIndex: 999,
  },
  hamburger: {
    padding: [theme.spacingUnit * 1, theme.spacingUnit * 2],
    marginLeft: theme.spacingUnit * 1,
    marginRight: theme.spacingUnit * -1,
    backgroundColor: "transparent",
    border: "none",
  },
  brand: {
    backgroundColor: "white",
    borderRadius: theme.borderRadiusSm,
    height: 38,
    margin: [0, theme.spacingUnit * 2],
    padding: [0, theme.spacingUnit * 2],
    display: "flex",
    alignItems: "center",
    fontSize: 20,
    letterSpacing: "-0.05ch",
    "& span": { fontWeight: "bold" },
  },
  brandLogo: {
    marginRight: 8,
  },
  spacer: { flexGrow: 1 },
  endButtons: {
    display: "flex",
    alignItems: "center",
    margin: [0, theme.spacingUnit * 1],
  },
  notifications: {
    width: 12,
    height: 12,
    borderRadius: "100%",
    backgroundColor: theme.digitalLakePrimary,
    margin: [0, theme.spacingUnit * 1],
  },
  user: {
    width: 26,
    height: 26,
    borderRadius: theme.borderRadius,
    backgroundColor: "white",
    margin: [0, theme.spacingUnit * 1],
  },
}));

export function Dockbar({
  onHamburgerAction,
}: {
  onHamburgerAction?: () => void;
}) {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      {onHamburgerAction && (
        <button className={classes.hamburger} onClick={onHamburgerAction}>
          <HamburgerIcon />
        </button>
      )}

      <div className={classes.brand}>
        <BrandLogo size={28} className={classes.brandLogo} />
        Open<span>K9</span>
      </div>

      <div className={classes.spacer} />

      <div className={classes.endButtons}>
        <div className={classes.notifications} />
        <div className={classes.user} />
      </div>
    </div>
  );
}
